package com.example.data

import android.util.Log
import com.example.data.firebase.HouseFirestoreDataSource
import com.example.data.models.HouseFilterState
import com.example.data.models.HouseListing
import com.example.data.models.HouseSortOption
import com.example.data.models.HouseVisitRequest
import com.example.data.models.TenantPreference
import com.example.data.models.VisitRequestStatus
import com.example.data.models.VisitTimeSlot
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

object HouseRentalRepository {
    private const val TAG = "HouseRentalRepository"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var dataSource: HouseFirestoreDataSource? = null
    private var activeUid: String? = null
    private val syncJobs = mutableListOf<Job>()

    private val _rawHousesFlow = MutableStateFlow<List<HouseListing>>(emptyList())
    private val _visitRequestsFlow = MutableStateFlow<List<HouseVisitRequest>>(emptyList())
    val visitRequestsFlow: StateFlow<List<HouseVisitRequest>> = _visitRequestsFlow.asStateFlow()

    private val _houseSearchQuery = MutableStateFlow("")
    val houseSearchQuery: StateFlow<String> = _houseSearchQuery.asStateFlow()

    private val _houseFilterState = MutableStateFlow(HouseFilterState())
    val houseFilterState: StateFlow<HouseFilterState> = _houseFilterState.asStateFlow()

    private val _isHousesLoadingFlow = MutableStateFlow(false)
    val isHousesLoadingFlow: StateFlow<Boolean> = _isHousesLoadingFlow.asStateFlow()

    private val _isVisitsLoadingFlow = MutableStateFlow(false)
    val isVisitsLoadingFlow: StateFlow<Boolean> = _isVisitsLoadingFlow.asStateFlow()

    val housesFlow: StateFlow<List<HouseListing>> = combine(
        _rawHousesFlow,
        RentalRepository.userProfileFlow
    ) { rawHouses, profile ->
        val favIds = profile.favoriteHouseIds.toSet()
        val blockedIds = profile.settings.blockedUserIds
        rawHouses
            .filter { it.owner.id !in blockedIds }
            .map { house ->
                house.copy(isFavorite = house.id in favIds)
            }
    }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    val filteredHousesFlow: StateFlow<List<HouseListing>> = combine(
        housesFlow,
        _houseSearchQuery,
        _houseFilterState
    ) { allHouses, query, filter ->
        val q = query.trim().lowercase()
        val filtered = allHouses.filter { house ->
            val matchesQuery = q.isEmpty() ||
                house.title.lowercase().contains(q) ||
                house.location.lowercase().contains(q) ||
                house.description.lowercase().contains(q) ||
                house.propertyType.label.lowercase().contains(q) ||
                house.configurationLabel.lowercase().contains(q)
            val matchesType = filter.propertyType == null || house.propertyType == filter.propertyType
            val matchesBhk = filter.minBhk == null || house.bhkCount >= filter.minBhk
            val matchesFurnishing = filter.furnishing == null || house.furnishing == filter.furnishing
            val matchesTenant = filter.tenantPreference == null ||
                house.tenantPreference == TenantPreference.ANY ||
                house.tenantPreference == filter.tenantPreference
            val matchesRent = filter.maxMonthlyRent == null || house.monthlyRent <= filter.maxMonthlyRent
            val matchesPet = !filter.petFriendlyOnly || com.example.data.models.HouseAmenity.PET_FRIENDLY in house.amenities
            val matchesAvailable = !filter.availableOnly || house.isAvailableForRent
            val matchesSaved = !filter.savedOnly || house.isFavorite

            matchesQuery && matchesType && matchesBhk && matchesFurnishing &&
                matchesTenant && matchesRent && matchesPet && matchesAvailable && matchesSaved
        }

        when (filter.sortOption) {
            HouseSortOption.FEATURED -> filtered.sortedWith(
                compareByDescending<HouseListing> { it.isAvailableForRent }
                    .thenByDescending { it.createdAtEpoch }
            )
            HouseSortOption.RENT_LOW_TO_HIGH -> filtered.sortedBy { it.monthlyRent }
            HouseSortOption.RENT_HIGH_TO_LOW -> filtered.sortedByDescending { it.monthlyRent }
            HouseSortOption.AREA_HIGH_TO_LOW -> filtered.sortedByDescending { it.areaSqFt }
            HouseSortOption.TOP_RATED -> filtered.sortedByDescending { it.rating }
        }
    }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    val savedHousesFlow: StateFlow<List<HouseListing>> = housesFlow
        .map { list -> list.filter { it.isFavorite } }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    val myPublishedHousesFlow: StateFlow<List<HouseListing>> = combine(
        housesFlow,
        RentalRepository.userProfileFlow
    ) { allHouses, profile ->
        if (profile.uid.isBlank()) emptyList() else allHouses.filter { it.owner.id == profile.uid }
    }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    val myTenantVisitsFlow: StateFlow<List<HouseVisitRequest>> = combine(
        _visitRequestsFlow,
        RentalRepository.userProfileFlow
    ) { visits, profile ->
        if (profile.uid.isBlank()) emptyList() else visits.filter { it.tenantId == profile.uid }
    }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    val incomingLandlordVisitsFlow: StateFlow<List<HouseVisitRequest>> = combine(
        _visitRequestsFlow,
        RentalRepository.userProfileFlow
    ) { visits, profile ->
        if (profile.uid.isBlank()) emptyList() else visits.filter { it.landlordId == profile.uid }
    }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    fun startSession(
        uid: String,
        source: HouseFirestoreDataSource,
        onError: (String) -> Unit = {}
    ) {
        clearSession()
        activeUid = uid
        dataSource = source
        _isHousesLoadingFlow.value = true
        _isVisitsLoadingFlow.value = true

        syncJobs += scope.launch {
            try {
                source.listenToHouses().collect { list ->
                    _isHousesLoadingFlow.value = false
                    _rawHousesFlow.value = list
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _isHousesLoadingFlow.value = false
                Log.e(TAG, "Houses sync error: ${e.message}", e)
                onError("Houses sync error: ${e.localizedMessage}")
            }
        }

        syncJobs += scope.launch {
            try {
                source.listenToVisitRequests(uid).collect { visits ->
                    _isVisitsLoadingFlow.value = false
                    _visitRequestsFlow.value = visits
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _isVisitsLoadingFlow.value = false
                Log.e(TAG, "Visit requests sync error: ${e.message}", e)
                onError("Visit requests sync error: ${e.localizedMessage}")
            }
        }
    }

    fun clearSession() {
        syncJobs.forEach { it.cancel() }
        syncJobs.clear()
        dataSource = null
        activeUid = null
        _rawHousesFlow.value = emptyList()
        _visitRequestsFlow.value = emptyList()
        _isHousesLoadingFlow.value = false
        _isVisitsLoadingFlow.value = false
    }

    fun setSearchQuery(query: String) {
        _houseSearchQuery.value = query
    }

    fun setFilterState(state: HouseFilterState) {
        _houseFilterState.value = state
    }

    fun resetFilters() {
        _houseFilterState.value = HouseFilterState()
        _houseSearchQuery.value = ""
    }

    fun toggleFavoriteHouse(houseId: String) {
        val currentProfile = RentalRepository.userProfileFlow.value
        if (currentProfile.uid.isBlank()) return
        val updatedFavs = currentProfile.favoriteHouseIds.toMutableList()
        if (houseId in updatedFavs) {
            updatedFavs.remove(houseId)
        } else {
            updatedFavs.add(houseId)
        }
        val optimistic = currentProfile.copy(favoriteHouseIds = updatedFavs)
        RentalRepository.applyLocalProfileSnapshot(optimistic)
        scope.launch {
            dataSource?.updateFavoriteHouses(updatedFavs)?.onFailure {
                RentalRepository.applyLocalProfileSnapshot(currentProfile)
            }
        }
    }

    suspend fun publishHouse(house: HouseListing): Result<HouseListing> {
        val src = dataSource ?: return Result.failure(IllegalStateException("Not connected to cloud session."))
        return src.createHouse(house).onSuccess { saved ->
            _rawHousesFlow.value = listOf(saved) + _rawHousesFlow.value.filterNot { it.id == saved.id }
        }
    }

    suspend fun updateHouse(house: HouseListing): Result<HouseListing> {
        val src = dataSource ?: return Result.failure(IllegalStateException("Not connected to cloud session."))
        return src.updateHouse(house).onSuccess { updated ->
            _rawHousesFlow.value = _rawHousesFlow.value.map { if (it.id == updated.id) updated else it }
        }
    }

    suspend fun setHouseAvailability(houseId: String, available: Boolean): Result<Unit> {
        val src = dataSource ?: return Result.failure(IllegalStateException("Not connected to cloud session."))
        return src.updateHouseAvailability(houseId, available).onSuccess {
            _rawHousesFlow.value = _rawHousesFlow.value.map {
                if (it.id == houseId) it.copy(isAvailableForRent = available) else it
            }
        }
    }

    suspend fun deleteHouse(houseId: String): Result<Unit> {
        val src = dataSource ?: return Result.failure(IllegalStateException("Not connected to cloud session."))
        return src.deleteHouse(houseId).onSuccess {
            _rawHousesFlow.value = _rawHousesFlow.value.filterNot { it.id == houseId }
        }
    }

    suspend fun submitVisitRequest(
        house: HouseListing,
        preferredDate: String,
        timeSlot: VisitTimeSlot,
        occupantsCount: Int,
        householdType: TenantPreference,
        targetMoveInMonth: String,
        tenantNote: String
    ): Result<HouseVisitRequest> {
        val src = dataSource ?: return Result.failure(IllegalStateException("Not connected to cloud session."))
        val uid = activeUid ?: Firebase.auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("Sign in required to schedule a visit."))
        if (house.owner.id == uid) {
            return Result.failure(IllegalArgumentException("You cannot request a visit on your own property."))
        }
        val duplicateActive = _visitRequestsFlow.value.firstOrNull {
            it.houseId == house.id &&
                it.tenantId == uid &&
                (it.status == VisitRequestStatus.REQUESTED || it.status == VisitRequestStatus.CONFIRMED || it.status == VisitRequestStatus.RESCHEDULE_SUGGESTED)
        }
        if (duplicateActive != null) {
            return Result.failure(IllegalStateException("You already have an active visit request for this home."))
        }

        val profile = RentalRepository.userProfileFlow.value
        val requestId = "visit_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val request = HouseVisitRequest(
            id = requestId,
            houseId = house.id,
            houseTitle = house.title,
            houseLocation = house.location,
            monthlyRent = house.monthlyRent,
            tenantId = uid,
            tenantName = profile.displayName.ifBlank { Firebase.auth.currentUser?.displayName ?: "Prospective Tenant" },
            landlordId = house.owner.id,
            landlordName = house.owner.name.ifBlank { "Landlord" },
            preferredDate = preferredDate,
            preferredTimeSlot = timeSlot,
            occupantsCount = occupantsCount.coerceIn(1, 20),
            householdType = householdType,
            targetMoveInMonth = targetMoveInMonth.ifBlank { "Immediate" },
            tenantNote = tenantNote.trim(),
            status = VisitRequestStatus.REQUESTED
        )
        return src.createVisitRequest(request).onSuccess { saved ->
            _visitRequestsFlow.value = listOf(saved) + _visitRequestsFlow.value.filterNot { it.id == saved.id }
        }
    }

    suspend fun confirmVisitByLandlord(
        visitId: String,
        responseNote: String,
        confirmedAddressNote: String
    ): Result<Unit> {
        val src = dataSource ?: return Result.failure(IllegalStateException("Not connected to cloud session."))
        return src.updateVisitByLandlord(
            visitId = visitId,
            status = VisitRequestStatus.CONFIRMED,
            responseNote = responseNote,
            confirmedAddressNote = confirmedAddressNote
        )
    }

    suspend fun suggestRescheduleByLandlord(
        visitId: String,
        responseNote: String
    ): Result<Unit> {
        val src = dataSource ?: return Result.failure(IllegalStateException("Not connected to cloud session."))
        return src.updateVisitByLandlord(
            visitId = visitId,
            status = VisitRequestStatus.RESCHEDULE_SUGGESTED,
            responseNote = responseNote,
            confirmedAddressNote = ""
        )
    }

    suspend fun declineVisitByLandlord(
        visitId: String,
        responseNote: String
    ): Result<Unit> {
        val src = dataSource ?: return Result.failure(IllegalStateException("Not connected to cloud session."))
        return src.updateVisitByLandlord(
            visitId = visitId,
            status = VisitRequestStatus.DECLINED,
            responseNote = responseNote,
            confirmedAddressNote = ""
        )
    }

    suspend fun cancelVisitByTenant(visitId: String): Result<Unit> {
        val src = dataSource ?: return Result.failure(IllegalStateException("Not connected to cloud session."))
        return src.updateVisitByTenant(
            visitId = visitId,
            status = VisitRequestStatus.CANCELLED
        )
    }

    suspend fun markVisitCompleted(visit: HouseVisitRequest): Result<Unit> {
        val src = dataSource ?: return Result.failure(IllegalStateException("Not connected to cloud session."))
        val uid = activeUid ?: Firebase.auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("Sign in required."))
        return if (uid == visit.landlordId) {
            src.updateVisitByLandlord(
                visitId = visit.id,
                status = VisitRequestStatus.COMPLETED,
                responseNote = visit.landlordResponseNote,
                confirmedAddressNote = visit.confirmedAddressNote
            )
        } else {
            src.updateVisitByTenant(
                visitId = visit.id,
                status = VisitRequestStatus.COMPLETED
            )
        }
    }

    fun activeVisitForHouse(houseId: String, uid: String = activeUid.orEmpty()): HouseVisitRequest? {
        if (uid.isBlank()) return null
        return _visitRequestsFlow.value.firstOrNull {
            it.houseId == houseId &&
                it.tenantId == uid &&
                it.status != VisitRequestStatus.CANCELLED &&
                it.status != VisitRequestStatus.DECLINED
        }
    }
}
