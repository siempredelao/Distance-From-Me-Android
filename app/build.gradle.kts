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
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	alias(libs.plugins.android.application)
	alias(libs.plugins.jetbrains.kotlin.android)
	alias(libs.plugins.ksp)
	id("kotlin-parcelize")
	alias(libs.plugins.google.services)
	alias(libs.plugins.crashlytics)
	alias(libs.plugins.room)
}

android {

	namespace = "gc.david.dfm"
	compileSdk = libs.versions.compileSdk.get().toInt()

	defaultConfig {
		applicationId = "gc.david.dfm"
		minSdk = libs.versions.minSdk.get().toInt()
		targetSdk = libs.versions.targetSdk.get().toInt()
		versionName = libs.versions.appVersionName.get()
		versionCode = libs.versions.appVersionCode.get().toInt()
		androidResources.localeFilters.addAll(listOf("en", "ca", "de", "es", "fr", "it", "pt"))
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

//			if (isCi()) {
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

}

kotlin {
	compilerOptions {
		jvmTarget.set(JvmTarget.JVM_11)
	}
}

dependencies {

	implementation(libs.androidx.activity.ktx)
	implementation(libs.androidx.cardview)
	implementation(libs.androidx.collection.ktx)
	implementation(libs.androidx.constraintlayout)
	implementation(libs.androidx.core.ktx)
	implementation(libs.androidx.preference)
	implementation(libs.coroutines)
	implementation(libs.crashlytics)
	implementation(libs.graphview)
	implementation(libs.gson)
	implementation(libs.koin)
	implementation(libs.koin.android)
	debugImplementation(libs.leakcanary)
	implementation(libs.lifecycle.runtime)
	implementation(libs.lifecycle.viewmodel)
	implementation(libs.lottie)
	implementation(libs.material.components)
	implementation(libs.okhttp)
	implementation(libs.playservices.location)
	implementation(libs.playservices.maps)
	ksp(libs.room.compiler)
	implementation(libs.room.ktx)
	implementation(libs.room.runtime)
	implementation(libs.timber)
    implementation(project(":common"))
    implementation(project(":faq"))
    implementation(project(":feedback"))
    implementation(project(":opensource"))

	testImplementation(libs.androidx.arch.core.testing)
	testImplementation(libs.coroutines.test)
	testImplementation(libs.junit)
	testImplementation(libs.mockito)
	testImplementation(libs.mockito.kotlin)
	testImplementation(project(":test-support"))
}
