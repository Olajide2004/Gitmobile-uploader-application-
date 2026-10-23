# GitMobile Uploader

[![Build & Release APK](https://github.com/Olajide2004/Gitmobile-uploader-application-/actions/workflows/build-apk.yml/badge.svg)](https://github.com/Olajide2004/Gitmobile-uploader-application-/actions/workflows/build-apk.yml)

A powerful Android application built with Kotlin and Jetpack Compose for committing and uploading folders directly to GitHub repositories using the GitHub Git Trees & Commits API. Features integrated banner ad monetization and a live RSS news feed with notifications from [Osun Hive](https://www.osunhive.name.ng).

---

## 📥 Direct APK Download

* **Direct Repository APK Download**:
  👉 [**Download GitMobile-OsunHive-v1.0.apk**](https://github.com/Olajide2004/Gitmobile-uploader-application-/raw/main/release/GitMobile-OsunHive-v1.0.apk) (Included in `/release/` folder)

* **Via GitHub Actions CI/CD**:
  1. Go to the [GitHub Actions Tab](https://github.com/Olajide2004/Gitmobile-uploader-application-/actions).
  2. Select the latest **Build & Release Android APK** workflow run.
  3. Scroll down to **Artifacts** and click **GitMobile-Debug-APK**.

* **Via GitHub Releases**:
  Check the [Releases](https://github.com/Olajide2004/Gitmobile-uploader-application-/releases) tab.

---

## 🚀 Key Features

* **Folder Uploading via Git Trees API**:
  * Traverses Android directory trees via Storage Access Framework (`OpenDocumentTree`).
  * Creates Git blobs, trees, commits, and updates branch refs with zero local Git CLI required.
  * Live dual-track workflow progress bar tracking file scanning, blob uploads, and commit status.
* **Osun Hive Monetization**:
  * Integrated banner ads linking to [https://www.osunhive.name.ng](https://www.osunhive.name.ng).
  * Sticky bottom banner & in-feed native sponsored cards.
  * Real-time monetization metrics (impressions, clicks, CTR, and estimated earnings).
* **Osun Hive RSS News Feed**:
  * Live feed parser reading from [https://www.osunhive.name.ng/feed/](https://www.osunhive.name.ng/feed/).
  * System notification engine alerting users when new articles publish.
  * Search, category filtering, bookmarking, and in-app reader modal.

---

## 🛠️ Building Locally

```bash
# Clone the repository
git clone https://github.com/Olajide2004/Gitmobile-uploader-application-.git
cd Gitmobile-uploader-application-

# Build the debug APK
gradle assembleDebug

# Output APK path:
# app/build/outputs/apk/debug/app-debug.apk
```
