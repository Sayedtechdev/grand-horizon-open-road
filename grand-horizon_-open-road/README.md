# Grand Horizon Open Road

## Android Application

Grand Horizon Open Road is an Android application developed using Android Studio. This repository contains the source code and project configuration required to build and run the application locally.

## Run Locally

**Prerequisites:** [Android Studio](https://developer.android.com/studio)

### Setup Instructions

1. Open Android Studio.
2. Select **Open** and choose the directory containing this project.
3. Allow Android Studio to complete the Gradle synchronization and resolve any required dependencies.
4. Create a `.env` file in the project directory and configure the required `GEMINI_API_KEY` if the application uses Gemini API services.
5. Check the app's `build.gradle.kts` configuration and ensure the signing configuration is appropriate for your local build.
6. Run the application on an Android emulator or a physical Android device.

## Project Structure

- `app/` – Contains the Android application source code and resources.
- `gradle/` – Contains Gradle wrapper configuration.
- `build.gradle.kts` – Root-level build configuration.
- `settings.gradle.kts` – Project settings and module configuration.
- `gradle.properties` – Gradle properties.

## Development

The project can be opened, modified, built, and tested using Android Studio.

## License

Add the appropriate license information if applicable.
