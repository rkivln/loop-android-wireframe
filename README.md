# Loop — Real-Time Community Marketplace

**Loop** is a real-time peer-to-peer neighborhood sharing and rental marketplace built with Kotlin, Jetpack Compose, and Firebase. Community members can publish listings with their own photography, discover nearby items and community posts on a live GPS map, message owners in real time, and manage reservations from a single unified workspace.

---

## Key Features

### 1. Real-Time Community Marketplace
- **Zero Pre-Stored Sample Data**: Every listing, community post, reservation, and conversation starts clean and comes exclusively from authenticated users in real time via Cloud Firestore.
- **User-Uploaded Photography**: Attach real item photos via the zero-permission Android Photo Picker; images are compressed and synchronized directly through Firestore.
- **Multiple Departments**: Publish or filter items across *Electronics, Furniture, Vehicles, Tools, Events, Study & Office, and Home & Living*.
- **AI Rate & Deposit Appraisal**: Optional Gemini AI assistance estimates daily rates and refundable security deposits from your item title and notes.

### 2. Live GPS Discovery Map
- **Real-Time GPS Centering**: Automatically centers on your live coordinates and reverse-geocodes your neighborhood when location access is granted.
- **Interactive Map Modes**: Switch between **MapLibre GL** and **Google Maps** to explore user-published listings and community posts nearby.
- **Category Filters**: Filter active map pins across *Listings, Community Events, Help Requests, and Study Groups*.

### 3. Real-Time Messaging & Reservations
- **Direct Owner Chat**: Start real-time Firestore conversations with listing owners and share meeting coordinates.
- **OTP Handover Confirmation**: Generates a 4-digit handover code when reservations are confirmed.
- **Live Status Updates**: Track active bookings, pickup dates, and completion status in real time.

### 4. Google Sign-In & Clean Member Profiles
- **Credential Manager Authentication**: Gated behind Google Sign-In so every listing, booking, and message is tied to an authenticated account.
- **Fresh Starter Profiles**: New accounts start with zero stats and zero unverified claims until activity accrues naturally.
- **Community Board**: Publish local events, help requests, or study meetups tagged with live GPS or custom locations.

### 5. Photography-First Material 3 UI
- **Material Design 3 & Custom Typography**: Warm editorial palette with Playfair Display and Plus Jakarta Sans typography, edge-to-edge insets, and photography-only visual assets (no vector illustrations).

---

## Technology Stack
- **Language & UI**: Kotlin, Jetpack Compose, Material Design 3
- **Backend & Auth**: Firebase Cloud Firestore, Firebase Authentication (Google Sign-In via Credential Manager), Firebase AI Logic
- **Location & Maps**: Fused Location Provider, Android Geocoder, MapLibre GL, Google Maps Compose
- **Local Cache**: Room Database (`AppDatabase`)
- **Navigation**: Navigation Compose with Type-Safe `@Serializable` Routes

---

## Getting Started
1. Launch **Loop** and view the photographic introduction.
2. Sign in with your Google account via Jetpack Credential Manager.
3. Grant location permission (optional) to auto-detect your neighborhood and center the Discovery Map on your live GPS coordinates.
4. Tap **Publish Piece** to upload a photo and publish the first real-time listing in your community.
