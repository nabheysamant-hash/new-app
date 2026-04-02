# 🧪 OSMOS SDK Test App

A minimal Android app to test and understand OSMOS banner ad integration.

---

## 🎯 What This App Does

This app has **two built-in test scenarios** you can switch between:

| Scenario | Config | Expected Result |
|---|---|---|
| ✅ WORKING | `test-page` + `test-inventory` | Real ad appears on screen |
| ❌ FAILING | `demo_page` + `banner_ads` | Empty response → crash (replicates client bug) |

---

## 🚀 How to Run

### Option A — Android Studio (Easiest)
1. Install Android Studio from https://developer.android.com/studio
2. Open this folder as a project
3. Wait for Gradle sync to finish
4. Connect your Android phone via USB
5. Click the ▶️ Run button

### Option B — GitHub Codespaces (No install needed)
1. Upload this folder to a GitHub repo
2. Open in Codespaces
3. Run: `./gradlew assembleDebug`
4. Download the APK from `app/build/outputs/apk/debug/`
5. Install on your Android phone

---

## 🔧 Switching Between Scenarios

Open `MainActivity.kt` and change this one line:

```kotlin
// true  = Working ad (server returns real ad)
// false = Failing scenario (replicates client's bug)
private val USE_WORKING_CONFIG = true   // ← change to false to see the failure
```

---

## 📱 What You'll See On Screen

The app shows a **live status log** so you can follow every step:

```
🟢 Running: WORKING config
   page_type = test-page
   ad_unit   = test-inventory
⏳ Initializing OSMOS SDK...
✅ SDK initialized with client_id: 10131833
✅ AdRenderer and BannerAdViewManager ready
📡 Making ad request...
✅ Ad data received!
📐 Creating BannerAdSettings (300x250)...
🎨 Rendering banner ad...
✅ Ad view loaded successfully!
✅ Banner ad added to screen!
```

---

## ⚙️ Config Values Used

| Parameter | Working Value | Failing Value |
|---|---|---|
| `client_id` | `10131833` | `10131833` |
| `cli_ubid` | `user_123` | `user_123` |
| `page_type` | `test-page` ✅ | `demo_page` ❌ |
| `ad_unit` | `test-inventory` ✅ | `banner_ads` ❌ |

---

## 🔍 Key Files

- `MainActivity.kt` — All the SDK integration code with comments
- `activity_main.xml` — Layout with the banner ad container
- `app/build.gradle` — SDK and ExoPlayer dependencies

---

## 📦 Dependencies Added (as per OSMOS docs)

```gradle
// OSMOS SDK
implementation 'com.ai.osmos:osmos-android-sdk:latest.release'

// ExoPlayer (required for video banner ads)
implementation("androidx.media3:media3-exoplayer:1.6.0")
implementation("androidx.media3:media3-ui:1.6.0")
implementation("androidx.media3:media3-exoplayer-hls:1.6.0")
implementation("androidx.media3:media3-exoplayer-dash:1.6.0")
```
