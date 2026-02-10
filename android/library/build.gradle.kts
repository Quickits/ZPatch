plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.quickits.zpatch"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        minSdk = 23
        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86_64", "x86")
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        // Pass Android project root directory to CMake
        externalNativeBuild {
            cmake {
                arguments += listOf(
                    "-DANDROID_PROJECT_ROOT=${rootProject.projectDir.absolutePath}",
                    "-DANDROID_SUPPORT_FLEXIBLE_PAGE_SIZES=ON"
                )
            }
        }
    }

    // Specify NDK version (supports 16KB page size)
    ndkVersion = "27.0.12077973"

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
