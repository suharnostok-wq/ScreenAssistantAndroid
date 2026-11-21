# Build APK using GitHub Actions from an Android phone


This project bundle is source code, not an APK. It includes a GitHub Actions workflow to build a debug APK online.


## Steps
1. Sign in to GitHub and create a new repository (Public is easiest for the workflow setup; Private is also supported).
2. Extract this ZIP on your phone.
3. Upload the **contents** of the `ScreenAssistantAndroid` folder to the repository's root (make sure `.github/workflows/build-apk.yml` is included; enable viewing hidden files if uploading from a file manager).
4. Open the repository on GitHub, tap **Actions**. If asked, allow workflows.
5. Choose **Build Screen Assistant APK** and tap **Run workflow**. Or push to the `main` branch to trigger a build.
6. Wait for the workflow to complete successfully, open that run, and download its **ScreenAssistant-debug-apk** artifact.
7. Extract the artifact ZIP, tap `app-debug.apk`, and install. Android may ask permission to install from that browser/file manager; allow only for the app you used to download it.
8. Open Screen Assistant and use its button to enable the Accessibility service. Only grant permissions you understand.


## Project notes
- This prototype has no AI model connection yet. It recognizes a few fixed commands only.
- It can read accessible screen text, search/tap labels, scroll, and type into a focused non-password field, with confirmation prompts.
- Password fields are skipped. Avoid giving it access while handling payment, banking, passwords, or OTPs.
- Android Gradle Plugin is 8.6.1, Java 17, and minimum Android SDK is 26 (Android 8.0).
- Build workflow produces a debug APK; it is not a Play Store release package.
