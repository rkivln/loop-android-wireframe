package com.example.data.repository

import com.example.data.models.ChatMessage
import com.example.data.models.CommunityPost
import com.example.data.models.EventItem
import com.example.data.models.LoopCategory
import com.example.data.models.MapPinItem
import com.example.data.models.MessageType
import com.example.data.models.NearbyPerson
import com.example.data.models.StudyGroup
import com.example.data.models.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LoopRepository {

    private val _currentUser = MutableStateFlow(
        UserProfile(
            id = "user_gokulan",
            name = "Gokulan",
            handle = "@gokulan",
            bio = "Student · Designer · Learner\nExploring ideas, meeting new people and growing together.",
            postsCount = 42,
            connectionsCount = 128,
            eventsCount = 12,
            isOnline = true
        )
    )
    val currentUser: StateFlow<UserProfile> = _currentUser.asStateFlow()

    private val _nearbyPeople = MutableStateFlow(
        listOf(
            NearbyPerson("p1", "Arjun K", "CS Student & Dev", "300 m", "AK", 0, 0.28f, 0.42f),
            NearbyPerson("p2", "Sneha R", "UI/UX Designer", "1 km", "SR", 1, 0.72f, 0.35f),
            NearbyPerson("p3", "Rahul M", "AI Enthusiast", "450 m", "RM", 2, 0.18f, 0.68f),
            NearbyPerson("p4", "Priya S", "Product Architect", "800 m", "PS", 3, 0.82f, 0.58f),
            NearbyPerson("p5", "Vikram D", "Fullstack Engineer", "1.2 km", "VD", 4, 0.45f, 0.22f)
        )
    )
    val nearbyPeople: StateFlow<List<NearbyPerson>> = _nearbyPeople.asStateFlow()

    private val _events = MutableStateFlow(
        listOf(
            EventItem(
                id = "e1",
                title = "Design Workshop",
                category = LoopCategory.EVENTS,
                time = "Today, 5:00 PM – 7:00 PM",
                distance = "500 m away",
                location = "Pondicherry Creative Space",
                attendeeCount = 12,
                description = "A hands-on workshop to learn UI/UX design basics. All are welcome!",
                isGoing = false,
                gradientType = 0
            ),
            EventItem(
                id = "e2",
                title = "Tech Founders Meetup",
                category = LoopCategory.EVENTS,
                time = "Tomorrow, 6:30 PM",
                distance = "1.1 km away",
                location = "The Promenade Hub",
                attendeeCount = 28,
                description = "Casual networking for indie hackers, designers, and students building next-gen products.",
                isGoing = true,
                gradientType = 1
            ),
            EventItem(
                id = "e3",
                title = "Sunset Sketch Session",
                category = LoopCategory.EVENTS,
                time = "Sat, 4:30 PM",
                distance = "850 m away",
                location = "Rock Beach Promenade",
                attendeeCount = 19,
                description = "Bring your iPad or sketchbook for open air creative flow as the sun dips.",
                isGoing = false,
                gradientType = 2
            )
        )
    )
    val events: StateFlow<List<EventItem>> = _events.asStateFlow()

    private val _studyGroups = MutableStateFlow(
        listOf(
            StudyGroup(
                id = "s1",
                title = "Data Structures Study Group",
                topic = "DSA & Algorithms",
                membersCount = 8,
                distance = "300 m away",
                schedule = "Weekly study session for DSA. All levels are welcome!",
                description = "Focusing on graph algorithms, trees, and dynamic programming with collaborative whiteboard problem solving.",
                isJoined = false
            ),
            StudyGroup(
                id = "s2",
                title = "Mobile UI/UX Masterclass",
                topic = "Figma & Jetpack Compose",
                membersCount = 14,
                distance = "600 m away",
                schedule = "Every Tue & Thu · 6:00 PM",
                description = "Dissecting Apple Human Interface Guidelines and translating Figma mocks to pixel-perfect Compose layouts.",
                isJoined = true
            )
        )
    )
    val studyGroups: StateFlow<List<StudyGroup>> = _studyGroups.asStateFlow()

    private val _posts = MutableStateFlow(
        listOf(
            CommunityPost(
                id = "post_1",
                authorName = "Arjun K",
                authorHandle = "@arjun_k",
                timeAgo = "2h ago",
                distance = "300 m away",
                content = "Looking for study group for Data Structures. Anyone interested?",
                category = LoopCategory.STUDY,
                likesCount = 12,
                commentsCount = 4,
                isLiked = false,
                isSaved = false,
                visualType = 2
            ),
            CommunityPost(
                id = "post_2",
                authorName = "Sneha R",
                authorHandle = "@snehar",
                timeAgo = "5h ago",
                distance = "1 km away",
                content = "Beautiful sunset at Pondy beach 🌅 Anyone up for a walk this weekend?",
                category = LoopCategory.ALL,
                likesCount = 24,
                commentsCount = 6,
                isLiked = true,
                isSaved = true,
                visualType = 1
            ),
            CommunityPost(
                id = "post_3",
                authorName = "Rahul M",
                authorHandle = "@rahul_ai",
                timeAgo = "1d ago",
                distance = "450 m away",
                content = "Hosting a quick brainstorm on edge AI on mobile devices. Drop a comment if you want in!",
                category = LoopCategory.EVENTS,
                likesCount = 19,
                commentsCount = 8,
                isLiked = false,
                isSaved = false,
                visualType = 3
            )
        )
    )
    val posts: StateFlow<List<CommunityPost>> = _posts.asStateFlow()

    private val _messages = MutableStateFlow(
        listOf(
            ChatMessage(
                id = "m1",
                senderName = "Arjun K",
                text = "Hey! Are you joining the study group today?",
                timestamp = "9:40 AM",
                isFromMe = false,
                type = MessageType.TEXT
            ),
            ChatMessage(
                id = "m2",
                senderName = "Gokulan",
                text = "Yes! I'll be there.\nWhat time are you coming?",
                timestamp = "9:41 AM",
                isFromMe = true,
                type = MessageType.TEXT
            ),
            ChatMessage(
                id = "m3",
                senderName = "Arjun K",
                text = "Around 5. See you there! 🤝",
                timestamp = "9:42 AM",
                isFromMe = false,
                type = MessageType.TEXT
            ),
            ChatMessage(
                id = "m4",
                senderName = "Arjun K",
                timestamp = "9:43 AM",
                isFromMe = false,
                type = MessageType.AUDIO,
                audioDuration = "0:12",
                isPlaying = false
            ),
            ChatMessage(
                id = "m5",
                senderName = "Arjun K",
                timestamp = "9:44 AM",
                isFromMe = false,
                type = MessageType.FILE,
                fileName = "Notes_DSA.pdf",
                fileSize = "2.4 MB"
            )
        )
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _mapPins = MutableStateFlow(
        listOf(
            MapPinItem(
                id = "pin_study",
                title = "Data Structures Study Group",
                category = LoopCategory.STUDY,
                distance = "300 m",
                xRatio = 0.50f,
                yRatio = 0.48f,
                snippet = "Weekly study session for DSA. All levels are welcome!",
                memberCount = 8,
                initials = "DSA",
                latitude = 11.9338,
                longitude = 79.8297
            ),
            MapPinItem(
                id = "pin_event",
                title = "Design Workshop",
                category = LoopCategory.EVENTS,
                distance = "500 m",
                xRatio = 0.65f,
                yRatio = 0.28f,
                snippet = "Learn UI/UX design basics in a collaborative hands-on session.",
                memberCount = 12,
                initials = "DW",
                latitude = 11.9365,
                longitude = 79.8335
            ),
            MapPinItem(
                id = "pin_beach",
                title = "Pondicherry Beach",
                category = LoopCategory.PEOPLE,
                distance = "850 m",
                xRatio = 0.78f,
                yRatio = 0.55f,
                snippet = "Social gathering spot with 45+ Loop members active nearby.",
                memberCount = 45,
                initials = "PB",
                latitude = 11.9310,
                longitude = 79.8360
            ),
            MapPinItem(
                id = "pin_arjun",
                title = "Arjun K",
                category = LoopCategory.PEOPLE,
                distance = "300 m",
                xRatio = 0.32f,
                yRatio = 0.36f,
                snippet = "CS Student working on graph algorithms & Android Compose.",
                memberCount = 1,
                initials = "AK",
                latitude = 11.9350,
                longitude = 79.8260
            ),
            MapPinItem(
                id = "pin_sneha",
                title = "Sneha R",
                category = LoopCategory.PEOPLE,
                distance = "1 km",
                xRatio = 0.75f,
                yRatio = 0.34f,
                snippet = "UI/UX Designer working on Apple HIG & motion prototypes.",
                memberCount = 1,
                initials = "SR",
                latitude = 11.9380,
                longitude = 79.8340
            ),
            MapPinItem(
                id = "pin_help",
                title = "Math & Logic Help",
                category = LoopCategory.HELP,
                distance = "400 m",
                xRatio = 0.38f,
                yRatio = 0.65f,
                snippet = "Looking for buddy to solve discrete math problem sets.",
                memberCount = 3,
                initials = "ML",
                latitude = 11.9295,
                longitude = 79.8275
            )
        )
    )
    val mapPins: StateFlow<List<MapPinItem>> = _mapPins.asStateFlow()

    private val _selectedMapPin = MutableStateFlow<MapPinItem?>(_mapPins.value.first())
    val selectedMapPin: StateFlow<MapPinItem?> = _selectedMapPin.asStateFlow()

    fun selectMapPin(pin: MapPinItem?) {
        _selectedMapPin.value = pin
    }

    fun togglePostLike(postId: String) {
        _posts.update { currentList ->
            currentList.map { post ->
                if (post.id == postId) {
                    val newLiked = !post.isLiked
                    val newCount = if (newLiked) post.likesCount + 1 else maxOf(0, post.likesCount - 1)
                    post.copy(isLiked = newLiked, likesCount = newCount)
                } else post
            }
        }
    }

    fun togglePostSaved(postId: String) {
        _posts.update { currentList ->
            currentList.map { post ->
                if (post.id == postId) {
                    post.copy(isSaved = !post.isSaved)
                } else post
            }
        }
    }

    fun addPost(content: String, category: LoopCategory, visualType: Int = 0) {
        val newPost = CommunityPost(
            id = "post_${System.currentTimeMillis()}",
            authorName = _currentUser.value.name,
            authorHandle = _currentUser.value.handle,
            timeAgo = "Just now",
            distance = "Nearby",
            content = content,
            category = category,
            likesCount = 0,
            commentsCount = 0,
            isLiked = false,
            isSaved = false,
            visualType = visualType
        )
        _posts.update { listOf(newPost) + it }
        _currentUser.update { it.copy(postsCount = it.postsCount + 1) }
    }

    fun toggleEventAttendance(eventId: String) {
        _events.update { currentList ->
            currentList.map { event ->
                if (event.id == eventId) {
                    val newGoing = !event.isGoing
                    val newCount = if (newGoing) event.attendeeCount + 1 else maxOf(0, event.attendeeCount - 1)
                    event.copy(isGoing = newGoing, attendeeCount = newCount)
                } else event
            }
        }
    }

    fun toggleStudyGroupJoin(groupId: String) {
        _studyGroups.update { currentList ->
            currentList.map { group ->
                if (group.id == groupId) {
                    val newJoined = !group.isJoined
                    val newCount = if (newJoined) group.membersCount + 1 else maxOf(0, group.membersCount - 1)
                    group.copy(isJoined = newJoined, membersCount = newCount)
                } else group
            }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        val newMsg = ChatMessage(
            id = "msg_${System.currentTimeMillis()}",
            senderName = "Gokulan",
            text = text.trim(),
            timestamp = "Just now",
            isFromMe = true,
            type = MessageType.TEXT
        )
        _messages.update { it + newMsg }
    }

    fun toggleAudioPlayback(msgId: String) {
        _messages.update { currentList ->
            currentList.map { msg ->
                if (msg.id == msgId) {
                    msg.copy(isPlaying = !msg.isPlaying)
                } else {
                    if (msg.type == MessageType.AUDIO) msg.copy(isPlaying = false) else msg
                }
            }
        }
    }
}
