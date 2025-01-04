/*
 * Copyright (c) 2022 David Aguiar Gonzalez
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension

plugins {
	id("com.android.application")
	id("org.jetbrains.kotlin.android")
	id("com.google.devtools.ksp")
	id("org.jetbrains.kotlin.plugin.parcelize")
	id("com.google.gms.google-services")
	id("com.google.firebase.crashlytics")
	id("androidx.room")
}

android {

	namespace = "gc.david.dfm"
	compileSdk = SdkVersions.target

	defaultConfig {
		applicationId = "gc.david.dfm"
		minSdk = SdkVersions.min
		targetSdk = SdkVersions.target
		versionName = AppVersions.name
		versionCode = AppVersions.code
		resourceConfigurations.addAll(listOf("en", "ca", "de", "es", "fr", "it", "pt"))
	}

	room {
		schemaDirectory("$projectDir/schemas")
	}

	buildTypes {
		debug {
			versionNameSuffix = "-dev"

			// Uncomment this to test ProGuard before release
//			isMinifyEnabled = true
//			isShrinkResources = true
//			proguardFiles getDefaultProguardFile('proguard-android.txt')
//			val proguards = fileTree("proguard") {
//				include("*.pro")
//			}
//			proguardFiles(*proguards.toList().toTypedArray())

//			if (System.getenv("TRAVIS")) {
//				buildConfigField("String", "maps_api_key", "guess_it")
//				buildConfigField("String", "maps_geocode_api_key", "guess_it")
//			}

			configure<CrashlyticsExtension> {
				mappingFileUploadEnabled = false
			}
		}

		release {
			isMinifyEnabled = true
			isShrinkResources = true
			val proguards = fileTree("proguard") {
				include("*.pro")
			}
			proguardFiles(*proguards.toList().toTypedArray())
		}

		create("beta") {
			initWith(getByName("release"))

			versionNameSuffix = "-beta"
		}
	}

	buildFeatures {
		viewBinding = true
		buildConfig = true
	}

	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_11
		targetCompatibility = JavaVersion.VERSION_11
	}

	kotlinOptions {
		jvmTarget = "11"
	}
}

dependencies {

	implementation(Dependencies.materialComponents)
	implementation(Dependencies.preference)
	implementation(Dependencies.playServicesMaps)
	implementation(Dependencies.playServicesLocation)
	implementation(Dependencies.graphview)
	implementation(Dependencies.crashlytics)
	implementation(Dependencies.okhttp)
	implementation(Dependencies.gson)
	implementation(Dependencies.constraintLayout)
	debugImplementation(Dependencies.leakCanary)
	implementation(Dependencies.cardView)
	implementation(Dependencies.collectionKtx)
	implementation(Dependencies.coreKtx)
    implementation(Dependencies.roomRuntime)
    implementation(Dependencies.roomKtx)
    ksp(Dependencies.roomCompiler)
	implementation(Dependencies.timber)
	implementation(Dependencies.koin)
	implementation(Dependencies.koinAndroid)
	implementation(Dependencies.lifecycleViewModel)
	implementation(Dependencies.lifecycleRuntime)
	implementation(Dependencies.activityKtx)
	implementation(Dependencies.lottie)
	implementation(Dependencies.playCore)
	implementation(Dependencies.coroutines)

    testImplementation(TestDependencies.junit)
	testImplementation(TestDependencies.mockito)
    testImplementation(TestDependencies.mockitoInline)
    testImplementation(TestDependencies.mockitoKotlin)
	testImplementation(TestDependencies.archCompTesting)
	testImplementation(TestDependencies.coroutinesTest)
}
