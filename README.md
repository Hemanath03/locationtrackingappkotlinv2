# 📍 Location Tracking App – Kotlin (Foreground GPS Tracking)

A production-ready **Android ForeGROUND Location Tracking Application** built using **Kotlin**, **Hilt (DI)**, **MVVM Architecture**, **Coroutines**, and **StateFlow**.  
This app continuously tracks the user's location in the background and updates the UI in real-time.

---

## 🚀 Features

- ✅ Foreground Location Tracking
- ✅ Real-time Location Updates
- ✅ MVVM Architecture
- ✅ Hilt Dependency Injection
- ✅ Kotlin Coroutines + StateFlow
- ✅ Runtime Permission Handling
- ✅ Background Service Support
- ✅ Toll Detection Logic (Optional Domain Logic)
- ✅ Git Version Control Ready
- ✅ Clean & Scalable Architecture

---

## 🧱 Architecture Used


- **UI Layer** → `MainActivity`
- **ViewModel Layer** → `MainViewModel`
- **Data Layer** → `LocationRepository`
- **Service Layer** → `LocationService`
- **Utils Layer** → `PermissionManager`, `ServiceController`, `Constants`
- **DI Layer** → `Hilt Modules`

---

## 🛠️ Tech Stack

- **Language:** Kotlin
- **Architecture:** MVVM
- **Dependency Injection:** Hilt
- **Asynchronous:** Coroutines + StateFlow
- **Location Provider:** FusedLocationProviderClient
- **Background Processing:** Foreground Service
- **Version Control:** Git + Bitbucket/GitHub

---

## 🔐 Required Permissions

```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
<uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION"/>
<uses-permission android:name="android.permission.FOREGROUND_SERVICE"/>
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_LOCATION"/>

com.example.locationtrackingapp
│
├── data
│   ├── LocationRepository
│   ├── TollRepository
│   └── LocationData
│
├── services
│   └── LocationService
│
├── ui
│   ├── MainActivity
│   └── MainViewModel
│
├── utils
│   ├── PermissionManager
│   ├── ServiceController
│   └── Constants
│
└── MyApplication (Hilt Entry)
