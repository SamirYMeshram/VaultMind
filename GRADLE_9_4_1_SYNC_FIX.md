# Gradle 9.4.1 Sync Fix

The previous sync reached the next compatibility check:

- Android Gradle Plugin: 9.2.0
- Current Gradle runtime used by Android Studio: 9.0.0
- Required Gradle runtime: 9.4.1

This project now includes:

`gradle/wrapper/gradle-wrapper.properties`

with:

`distributionUrl=https\://services.gradle.org/distributions/gradle-9.4.1-bin.zip`

## In Android Studio

1. Close the old broken project window.
2. Extract this ZIP into a new folder.
3. Open the new `VaultMind` folder.
4. Let Android Studio download Gradle 9.4.1 from the wrapper.
5. Sync again.

If Android Studio still shows Gradle 9.0.0, open:

`Settings > Build, Execution, Deployment > Build Tools > Gradle`

and set Gradle to use the project wrapper / wrapper task configuration.
