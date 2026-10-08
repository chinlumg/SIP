# SIP

Android SIP App(Kotlin + Jetpack Compose + Liblinphone)。

## 建置需求
- Android Studio(或 Android SDK,compileSdk 35)
- JDK 17(AGP 8.7 不支援 JDK 25)
- 首次建置需下載 Liblinphone SDK(約 150 MB+,來源 https://download.linphone.org/maven_repository)

## 建置
1. 建立 `local.properties`,內容 `sdk.dir=<你的 Android SDK 路徑>`
2. 在 `gradle.properties` 設定 `org.gradle.java.home=<JDK 17 路徑>`(或用 Android Studio 的 Gradle JDK 設定)
3. 執行 `./gradlew assembleDebug`,APK 在 `app/build/outputs/apk/debug/`

## 結構
- `SipManager.kt`:封裝 Liblinphone Core,以 StateFlow 提供註冊與通話狀態
- `ui/Screens.kt`:登入頁、撥號頁、通話頁
- `MainActivity.kt`:權限請求與畫面切換

## 目前限制
- 前景服務僅宣告權限,尚未實作;App 退到背景後不保證收到來電
- 登入固定使用 UDP
- 尚未整合 TelecomManager / FCM 推播
