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

## Screenshots

In order of appearance: home screen, search screen, add new review/listen screen, community screen, profile page main and lists screens.

<img width="220" height="420" alt="Screenshot_20260825-153024" src="https://github.com/user-attachments/assets/42ca7fe3-dd28-42ea-be88-81c5478203c1" />
<img width="220" height="420" alt="Screenshot_20260825-153101" src="https://github.com/user-attachments/assets/90e17a9b-fb9d-47c0-b040-168b3d88afe0" />
<img width="220" height="420" alt="Screenshot_20260825-153111" src="https://github.com/user-attachments/assets/f1918592-6fef-4ac7-aa63-684ded7b637a" />
<img width="220" height="420" alt="Screenshot_20260825-153115" src="https://github.com/user-attachments/assets/da67291b-9456-4288-ad45-1f9217252b5f" />
<img width="220" height="420" alt="Screenshot_20260825-153211" src="https://github.com/user-attachments/assets/6fae9f3a-0ae1-4fe0-929d-e76236dc1b88" />
<img width="220" height="420" alt="Screenshot_20260825-154206" src="https://github.com/user-attachments/assets/8c8164ff-2166-4f22-9577-0394d36adf8e" />

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
