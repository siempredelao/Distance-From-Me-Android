/*
 * Copyright (c) 2026 David Aguiar Gonzalez
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
import java.io.FileInputStream
import java.util.Properties

plugins {
	alias(libs.plugins.dfm.android.application)
	alias(libs.plugins.dfm.android.compose)
	alias(libs.plugins.dfm.unit.test)
	id("kotlin-parcelize")
	alias(libs.plugins.google.services)
	alias(libs.plugins.crashlytics)
}

android {

	namespace = "gc.david.dfm"

	// Load signing properties from keystore.properties (keep this file out of VCS)
	val keystoreProperties = Properties()
	val keystorePropertiesFile = rootProject.file("keystore.properties")
	if (keystorePropertiesFile.exists()) {
		keystoreProperties.load(FileInputStream(keystorePropertiesFile))
	}

	signingConfigs {
		create("release") {
			keyAlias = keystoreProperties["keyAlias"] as String
			keyPassword = keystoreProperties["keyPassword"] as String
			storeFile = file(keystoreProperties["storeFile"] as String)
			storePassword = keystoreProperties["storePassword"] as String
		}
	}

	defaultConfig {
		applicationId = "gc.david.dfm"
		versionName = libs.versions.appVersionName.get()
		versionCode = libs.versions.appVersionCode.get().toInt()
		androidResources.localeFilters.addAll(listOf("en", "ca", "de", "es", "fr", "it", "pt"))
	}

	buildTypes {
		debug {
			versionNameSuffix = "-dev"

			configure<CrashlyticsExtension> {
				mappingFileUploadEnabled = false
			}
		}

		release {
			isMinifyEnabled = true
			isShrinkResources = true
			// Use the signing config loaded from keystore.properties
			signingConfig = signingConfigs.getByName("release")
			val proguards = fileTree("proguard") {
				include("*.pro")
			}
			proguardFiles(*proguards.toList().toTypedArray())
		}
	}

	buildFeatures {
		buildConfig = true
	}
}

dependencies {

	implementation(libs.androidx.activity.compose)
	implementation(libs.androidx.activity.ktx)
	implementation(libs.androidx.cardview)
	implementation(libs.androidx.collection.ktx)
	implementation(libs.androidx.constraintlayout)
	implementation(libs.androidx.core.ktx)
	implementation(libs.androidx.preference)
	implementation(libs.splashscreen)
	implementation(libs.coroutines)
	implementation(libs.crashlytics)
	implementation(libs.koin)
	implementation(libs.koin.android)
	debugImplementation(libs.leakcanary)
	implementation(libs.lifecycle.runtime)
	implementation(libs.lifecycle.viewmodel)
	implementation(libs.lottie.compose)
	implementation(libs.maps.compose)
	implementation(libs.material.components)
	implementation(libs.okhttp)
	implementation(libs.okhttp.coroutines)
	implementation(libs.playservices.location)
	implementation(libs.playservices.maps)
	implementation(libs.timber)
	implementation(libs.compose.material.icons)
	implementation(libs.compose.material3)
	implementation(libs.compose.material3.adaptive)
	implementation(libs.compose.ui)
    implementation(project(":core:address"))
    implementation(project(":core:coordinates"))
    implementation(project(":core:design-system"))
    implementation(project(":core:distances"))
    implementation(project(":core:geocoding"))
    implementation(project(":core:settings"))
    implementation(project(":core:units"))
    implementation(project(":features:about"))
    implementation(project(":features:address"))
    implementation(project(":features:connectivity"))
    implementation(project(":features:elevation"))
    implementation(project(":features:faq"))
    implementation(project(":features:feedback"))
    implementation(project(":features:opensource"))
    implementation(project(":features:settings"))
    implementation(project(":features:show-info"))
    implementation(project(":support:android"))
    implementation(project(":support:coroutines"))
    implementation(project(":support:navigation"))

	testImplementation(libs.androidx.arch.core.testing)
}
