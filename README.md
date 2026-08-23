# YourSoundTrack
A casual music tracking app for rating, sharing with friends, and list making

---

## Features

* **Album Discovery & Search:** browse a catalog of albums and artists. From the Home page you can see upcoming and highly rated albums. and via the Search page, you can look up albums, artists and other users of the app. 
* **Review & Rating System:** Rate albums on a 5-star scale, write personalized text reflections, and mark favorite listens. Each review computes and updates global album ratings and review counts as community members submit entries.
* **Aggregated Community Metrics:** See real-time updates from your friends who you can follow using their profile page. 
* **Social & Profile Tracking:** User profile you can choose your favourite albums and artists and keep track of what you listend and what you plan to listen using the lists section.
* **Modern Android Architecture:** Built using Kotlin Coroutines, Jetpack Navigation, ViewModels, and StateFlow for reactive, lifecycle-aware UI updates.

---

## Tech Stack & Architecture

* **Language:** Kotlin
* **UI Framework:** Android Views, XML Layouts, Material Design Components, ConstraintLayout, RecyclerView, and ViewBinding.
* **Architecture Pattern:** MVVM (Model-View-ViewModel) with Repository pattern.
* **Backend & Database:** Firebase Authentication & Cloud Firestore (utilizing atomic transactions for multi-document consistency).
* **Asynchronous Operations:** Kotlin Coroutines and Flows.
* **Data Sources:** Local JSON assets (`albums.json`, `artists.json`) combined with remote cloud sync.

---

## Project Structure

```text
com.example.yoursoundtrack
│
├── adapters/        # RecyclerView adapters for search results and lists
├── dataModel/       # Data classes (Album, Artist, Review, User)
├── fragments/       # UI Screens (NewListenFragment, Feed, Profile, etc.)
├── managers/        # Repository and ViewModel classes handling business logic & Firebase
└── utils/           # Extension functions and helper utilities (e.g., Image loading)
