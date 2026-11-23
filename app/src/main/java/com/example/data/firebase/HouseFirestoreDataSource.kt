package com.example.data.firebase

import com.example.data.models.FurnishingStatus
import com.example.data.models.HouseAmenity
import com.example.data.models.HouseListing
import com.example.data.models.HousePropertyType
import com.example.data.models.HouseVisitRequest
import com.example.data.models.RentalOwner
import com.example.data.models.TenantPreference
import com.example.data.models.VisitRequestStatus
import com.example.data.models.VisitTimeSlot
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class HouseFirestoreDataSource(
    private val firestore: FirebaseFirestore
) {
    private val auth get() = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing house rentals.")
    }

    private fun computeInitials(name: String): String {
        val parts = name.trim().split(" ").filter { it.isNotBlank() }
        return when {
            parts.isEmpty() -> "U"
            parts.size == 1 -> parts[0].take(2).uppercase()
            else -> "${parts[0].take(1)}${parts[1].take(1)}".uppercase()
        }
    }

    private fun parseHouseSnapshot(doc: DocumentSnapshot): HouseListing? {
        return try {
            val rawGallery = (doc.get("imageUrls") as? List<*>)
                ?.mapNotNull { it?.toString()?.takeIf { url -> url.isNotBlank() } }
                ?.take(4)
                ?: emptyList()
            val coverUrl = doc.getString("imageUrl")?.takeIf { it.isNotBlank() } ?: rawGallery.firstOrNull()
            val resolvedUrls = if (rawGallery.isNotEmpty()) rawGallery else listOfNotNull(coverUrl)
            val labels = (doc.get("photoLabels") as? List<*>)
                ?.mapNotNull { it?.toString()?.trim() }
                ?.take(4)
                ?: emptyList()
            val amenities = (doc.get("amenities") as? List<*>)
                ?.mapNotNull { HouseAmenity.fromNameOrNull(it?.toString()) }
                ?: emptyList()
            val ownerName = doc.getString("ownerName")?.takeIf { it.isNotBlank() } ?: "Landlord"
            val ownerInitials = doc.getString("ownerInitials")?.takeIf { it.isNotBlank() }
                ?: computeInitials(ownerName)
            val createdEpoch = doc.getTimestamp(
                "createdAt",
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            )?.toDate()?.time ?: 0L

            HouseListing(
                id = doc.id,
                title = doc.getString("title").orEmpty(),
                propertyType = HousePropertyType.fromNameOrDefault(doc.getString("propertyType")),
                bhkCount = doc.getLong("bhkCount")?.toInt() ?: 2,
                bathrooms = doc.getLong("bathrooms")?.toInt() ?: 1,
                areaSqFt = doc.getLong("areaSqFt")?.toInt() ?: 950,
                floorNumber = doc.getLong("floorNumber")?.toInt() ?: 1,
                totalFloors = doc.getLong("totalFloors")?.toInt() ?: 2,
                furnishing = FurnishingStatus.fromNameOrDefault(doc.getString("furnishing")),
                tenantPreference = TenantPreference.fromNameOrDefault(doc.getString("tenantPreference")),
                monthlyRent = doc.getLong("monthlyRent")?.toInt() ?: 0,
                securityDeposit = doc.getLong("securityDeposit")?.toInt() ?: 0,
                maintenanceMonthly = doc.getLong("maintenanceMonthly")?.toInt() ?: 0,
                minLeaseMonths = doc.getLong("minLeaseMonths")?.toInt() ?: 11,
                availableFromLabel = doc.getString("availableFromLabel")?.takeIf { it.isNotBlank() } ?: "Immediate",
                isAvailableForRent = doc.getBoolean("isAvailableForRent") ?: true,
                location = doc.getString("location").orEmpty(),
                fullAddressNote = doc.getString("fullAddressNote").orEmpty(),
                description = doc.getString("description").orEmpty(),
                amenities = amenities,
                imageUrl = coverUrl,
                imageUrls = resolvedUrls,
                photoLabels = labels,
                imageCount = doc.getLong("imageCount")?.toInt() ?: resolvedUrls.size,
                latitude = doc.getDouble("latitude"),
                longitude = doc.getDouble("longitude"),
                owner = RentalOwner(
                    id = doc.getString("ownerId").orEmpty(),
                    name = ownerName,
                    badge = doc.getString("ownerBadge")?.takeIf { it.isNotBlank() } ?: "Verified Host",
                    initials = ownerInitials
                ),
                rating = (doc.getDouble("rating") ?: 0.0).toFloat(),
                reviewCount = doc.getLong("reviewCount")?.toInt() ?: 0,
                createdAtEpoch = createdEpoch
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun parseVisitSnapshot(doc: DocumentSnapshot): HouseVisitRequest? {
        return try {
            val createdEpoch = doc.getTimestamp(
                "createdAt",
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            )?.toDate()?.time ?: 0L
            val updatedEpoch = doc.getTimestamp(
                "updatedAt",
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            )?.toDate()?.time ?: createdEpoch

            HouseVisitRequest(
                id = doc.id,
                houseId = doc.getString("houseId").orEmpty(),
                houseTitle = doc.getString("houseTitle").orEmpty(),
                houseLocation = doc.getString("houseLocation").orEmpty(),
                monthlyRent = doc.getLong("monthlyRent")?.toInt() ?: 0,
                tenantId = doc.getString("tenantId").orEmpty(),
                tenantName = doc.getString("tenantName")?.takeIf { it.isNotBlank() } ?: "Prospective Tenant",
                landlordId = doc.getString("landlordId").orEmpty(),
                landlordName = doc.getString("landlordName")?.takeIf { it.isNotBlank() } ?: "Landlord",
                preferredDate = doc.getString("preferredDate").orEmpty(),
                preferredTimeSlot = VisitTimeSlot.fromNameOrDefault(doc.getString("preferredTimeSlot")),
                occupantsCount = doc.getLong("occupantsCount")?.toInt() ?: 1,
                householdType = TenantPreference.fromNameOrDefault(doc.getString("householdType")),
                targetMoveInMonth = doc.getString("targetMoveInMonth")?.takeIf { it.isNotBlank() } ?: "Immediate",
                tenantNote = doc.getString("tenantNote").orEmpty(),
                landlordResponseNote = doc.getString("landlordResponseNote").orEmpty(),
                confirmedAddressNote = doc.getString("confirmedAddressNote").orEmpty(),
                status = VisitRequestStatus.fromNameOrDefault(doc.getString("status")),
                conversationId = doc.getString("conversationId")?.takeIf { it.isNotBlank() },
                createdAtEpoch = createdEpoch,
                updatedAtEpoch = updatedEpoch
            )
        } catch (_: Exception) {
            null
        }
    }

    fun listenToHouses(): Flow<List<HouseListing>> {
        val collectionRef = firestore.collection("houses")
        return collectionRef.snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { parseHouseSnapshot(it) }
                    .sortedByDescending { it.createdAtEpoch }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, collectionRef.path)
                throw error
            }
    }

    suspend fun createHouse(house: HouseListing): Result<HouseListing> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val docRef = firestore.collection("houses").document(house.id)
        return try {
            val ownerName = house.owner.name.ifBlank { auth.currentUser?.displayName ?: "Loop Host" }
            val initials = house.owner.initials.ifBlank { computeInitials(ownerName) }
            val normalizedGallery = house.imageUrls.map { it.trim() }.filter { it.isNotBlank() }.take(4)
                .ifEmpty { listOfNotNull(house.imageUrl?.takeIf { it.isNotBlank() }) }
            val coverUrl = normalizedGallery.firstOrNull()
            val normalizedLabels = house.photoLabels.map { it.trim().take(40) }.take(4)

            val payload = mutableMapOf<String, Any?>(
                "id" to house.id,
                "title" to house.title.trim().take(150),
                "propertyType" to house.propertyType.name,
                "bhkCount" to house.bhkCount.coerceIn(0, 12),
                "bathrooms" to house.bathrooms.coerceIn(1, 12),
                "areaSqFt" to house.areaSqFt.coerceIn(80, 50000),
                "floorNumber" to house.floorNumber.coerceIn(0, 100),
                "totalFloors" to house.totalFloors.coerceIn(1, 100),
                "furnishing" to house.furnishing.name,
                "tenantPreference" to house.tenantPreference.name,
                "monthlyRent" to house.monthlyRent.coerceIn(1000, 1000000),
                "securityDeposit" to house.securityDeposit.coerceIn(0, 5000000),
                "maintenanceMonthly" to house.maintenanceMonthly.coerceIn(0, 100000),
                "minLeaseMonths" to house.minLeaseMonths.coerceIn(1, 60),
                "availableFromLabel" to house.availableFromLabel.trim().take(60),
                "isAvailableForRent" to house.isAvailableForRent,
                "location" to house.location.trim().take(120),
                "fullAddressNote" to house.fullAddressNote.trim().take(300).takeIf { it.isNotBlank() },
                "description" to house.description.trim().take(2500),
                "amenities" to house.amenities.map { it.name }.take(20),
                "imageUrl" to coverUrl,
                "imageUrls" to normalizedGallery,
                "photoLabels" to normalizedLabels,
                "imageCount" to normalizedGallery.size,
                "latitude" to house.latitude,
                "longitude" to house.longitude,
                "ownerId" to uid,
                "ownerName" to ownerName.take(100),
                "ownerInitials" to initials.take(10),
                "ownerBadge" to house.owner.badge.ifBlank { "Verified Host" }.take(60),
                "rating" to house.rating.toDouble(),
                "reviewCount" to house.reviewCount,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )

            @Suppress("UNCHECKED_CAST")
            docRef.set(payload.filterValues { it != null } as Map<String, Any>).await()
            Result.success(
                house.copy(
                    imageUrl = coverUrl,
                    imageUrls = normalizedGallery,
                    photoLabels = normalizedLabels,
                    imageCount = normalizedGallery.size,
                    owner = house.owner.copy(id = uid, name = ownerName, initials = initials)
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateHouse(house: HouseListing): Result<HouseListing> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val docRef = firestore.collection("houses").document(house.id)
        return try {
            val ownerName = house.owner.name.ifBlank { auth.currentUser?.displayName ?: "Loop Host" }
            val initials = house.owner.initials.ifBlank { computeInitials(ownerName) }
            val normalizedGallery = house.imageUrls.map { it.trim() }.filter { it.isNotBlank() }.take(4)
                .ifEmpty { listOfNotNull(house.imageUrl?.takeIf { it.isNotBlank() }) }
            val coverUrl = normalizedGallery.firstOrNull()
            val normalizedLabels = house.photoLabels.map { it.trim().take(40) }.take(4)

            val updates = mutableMapOf<String, Any?>(
                "title" to house.title.trim().take(150),
                "propertyType" to house.propertyType.name,
                "bhkCount" to house.bhkCount.coerceIn(0, 12),
                "bathrooms" to house.bathrooms.coerceIn(1, 12),
                "areaSqFt" to house.areaSqFt.coerceIn(80, 50000),
                "floorNumber" to house.floorNumber.coerceIn(0, 100),
                "totalFloors" to house.totalFloors.coerceIn(1, 100),
                "furnishing" to house.furnishing.name,
                "tenantPreference" to house.tenantPreference.name,
                "monthlyRent" to house.monthlyRent.coerceIn(1000, 1000000),
                "securityDeposit" to house.securityDeposit.coerceIn(0, 5000000),
                "maintenanceMonthly" to house.maintenanceMonthly.coerceIn(0, 100000),
                "minLeaseMonths" to house.minLeaseMonths.coerceIn(1, 60),
                "availableFromLabel" to house.availableFromLabel.trim().take(60),
                "isAvailableForRent" to house.isAvailableForRent,
                "location" to house.location.trim().take(120),
                "fullAddressNote" to house.fullAddressNote.trim().take(300).ifBlank { null },
                "description" to house.description.trim().take(2500),
                "amenities" to house.amenities.map { it.name }.take(20),
                "imageUrl" to coverUrl,
                "imageUrls" to normalizedGallery,
                "photoLabels" to normalizedLabels,
                "imageCount" to normalizedGallery.size,
                "latitude" to house.latitude,
                "longitude" to house.longitude,
                "ownerName" to ownerName.take(100),
                "ownerInitials" to initials.take(10),
                "ownerBadge" to house.owner.badge.ifBlank { "Verified Host" }.take(60),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.update(updates).await()
            Result.success(
                house.copy(
                    imageUrl = coverUrl,
                    imageUrls = normalizedGallery,
                    photoLabels = normalizedLabels,
                    imageCount = normalizedGallery.size,
                    owner = house.owner.copy(id = uid, name = ownerName, initials = initials)
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateHouseAvailability(houseId: String, availableForRent: Boolean): Result<Unit> {
        val docRef = firestore.collection("houses").document(houseId)
        return try {
            requireUserId()
            docRef.update(
                mapOf(
                    "isAvailableForRent" to availableForRent,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteHouse(houseId: String): Result<Unit> {
        val docRef = firestore.collection("houses").document(houseId)
        return try {
            requireUserId()
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateFavoriteHouses(favoriteHouseIds: List<String>): Result<Unit> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val docRef = firestore.collection("users").document(uid)
        return try {
            docRef.update(
                mapOf(
                    "favoriteHouseIds" to favoriteHouseIds.take(200),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    fun listenToVisitRequests(uid: String): Flow<List<HouseVisitRequest>> {
        val colRef = firestore.collection("visit_requests")
        val asTenantFlow = colRef.whereEqualTo("tenantId", uid).snapshots()
            .map { snap -> snap.documents.mapNotNull { parseVisitSnapshot(it) } }
        val asLandlordFlow = colRef.whereEqualTo("landlordId", uid).snapshots()
            .map { snap -> snap.documents.mapNotNull { parseVisitSnapshot(it) } }

        return combine(asTenantFlow, asLandlordFlow) { tenantList, landlordList ->
            (tenantList + landlordList)
                .distinctBy { it.id }
                .sortedByDescending { it.updatedAtEpoch.coerceAtLeast(it.createdAtEpoch) }
        }.catch { error ->
            if (error is Exception) handleFirestoreError(error, OperationType.LIST, colRef.path)
            throw error
        }
    }

    suspend fun createVisitRequest(request: HouseVisitRequest): Result<HouseVisitRequest> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        if (request.landlordId.isBlank() || request.landlordId == uid) {
            return Result.failure(IllegalArgumentException("You cannot request a visit for your own property."))
        }
        val docRef = firestore.collection("visit_requests").document(request.id)
        return try {
            val tenantName = request.tenantName.ifBlank { auth.currentUser?.displayName ?: "Prospective Tenant" }
            val payload = mutableMapOf<String, Any?>(
                "id" to request.id,
                "houseId" to request.houseId,
                "houseTitle" to request.houseTitle.trim().take(150),
                "houseLocation" to request.houseLocation.trim().take(120),
                "monthlyRent" to request.monthlyRent.coerceIn(0, 1000000),
                "tenantId" to uid,
                "tenantName" to tenantName.take(100),
                "landlordId" to request.landlordId,
                "landlordName" to request.landlordName.trim().take(100),
                "preferredDate" to request.preferredDate.trim().take(30),
                "preferredTimeSlot" to request.preferredTimeSlot.name,
                "occupantsCount" to request.occupantsCount.coerceIn(1, 20),
                "householdType" to request.householdType.name,
                "targetMoveInMonth" to request.targetMoveInMonth.trim().take(40),
                "tenantNote" to request.tenantNote.trim().take(1000),
                "landlordResponseNote" to "",
                "confirmedAddressNote" to "",
                "status" to VisitRequestStatus.REQUESTED.name,
                "conversationId" to request.conversationId,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            @Suppress("UNCHECKED_CAST")
            docRef.set(payload.filterValues { it != null } as Map<String, Any>).await()
            Result.success(request.copy(tenantId = uid, tenantName = tenantName, status = VisitRequestStatus.REQUESTED))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateVisitByTenant(
        visitId: String,
        status: VisitRequestStatus,
        preferredDate: String? = null,
        preferredTimeSlot: VisitTimeSlot? = null,
        tenantNote: String? = null,
        conversationId: String? = null
    ): Result<Unit> {
        val docRef = firestore.collection("visit_requests").document(visitId)
        return try {
            requireUserId()
            val updates = mutableMapOf<String, Any>(
                "status" to status.name,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (preferredDate != null) updates["preferredDate"] = preferredDate.trim().take(30)
            if (preferredTimeSlot != null) updates["preferredTimeSlot"] = preferredTimeSlot.name
            if (tenantNote != null) updates["tenantNote"] = tenantNote.trim().take(1000)
            if (!conversationId.isNullOrBlank()) updates["conversationId"] = conversationId
            docRef.update(updates).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateVisitByLandlord(
        visitId: String,
        status: VisitRequestStatus,
        responseNote: String = "",
        confirmedAddressNote: String = "",
        conversationId: String? = null
    ): Result<Unit> {
        val docRef = firestore.collection("visit_requests").document(visitId)
        return try {
            requireUserId()
            val updates = mutableMapOf<String, Any>(
                "status" to status.name,
                "landlordResponseNote" to responseNote.trim().take(1000),
                "confirmedAddressNote" to confirmedAddressNote.trim().take(300),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (!conversationId.isNullOrBlank()) updates["conversationId"] = conversationId
            docRef.update(updates).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }
}
