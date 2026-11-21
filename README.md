# Screen Assistant Android — prototype 0.2


This is a build-ready Android Studio source project, not a prebuilt APK.


## Implemented in 0.2
- Accessibility overlay floating bubble.
- Command panel and local command interpreter.
- `read screen`: reads visible accessibility text, excluding password nodes and editable field contents.
- `tap: label`: searches visible text/content descriptions and clicks the matching clickable node or clickable parent. User must confirm first.
- `scroll down` / `scroll up`: sends a screen gesture after confirmation.
- `type: text`: writes text into a focused, non-password editable field after confirmation.
- No credit counter, subscription, premium, ad, or billing logic.


## Not implemented
- No generative AI/model connection. Commands are locally recognized patterns only.
- No APK has been built or tested on a physical Android device in this environment.
- Accessibility may not expose all screen content or elements; labels can be ambiguous, and actions may fail in some apps.


## Safety
- Screen Assistant does not automatically operate payment, banking, password, or OTP screens. It excludes password nodes from reading/tapping and typing targets.
- Accessibility permission grants broad control. Enable it only when you trust this source; disable it after testing if not needed.
- Review and confirm each action.


## Build APK using Android Studio
1. Install Android Studio and the Android SDK Platform 35.
2. Extract this ZIP and open the `ScreenAssistantAndroid` folder in Android Studio.
3. Allow Gradle sync to finish.
4. Select **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
5. Debug APK should be generated under `app/build/outputs/apk/debug/app-debug.apk`.
6. Install it on Android 8.0 (API 26) or newer.
7. Open Screen Assistant > Open Accessibility settings > enable Screen Assistant.
8. Return to app; tap the floating `AI` bubble. Try `read screen`, `tap: label`, `scroll down`, `scroll up`, or `type: text`.

The project uses Android Gradle Plugin 8.6.1 and Kotlin plugin 2.0.21.
