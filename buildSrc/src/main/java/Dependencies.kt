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

object SdkVersions {

    const val min = 22
    const val target = 31
}

object AppVersions {

    const val code = 30
    const val name = "3.5"
}

object LibraryVersions {

    const val activityKtx = "1.4.0"
    const val archCompTesting = "2.1.0"
    const val cardView = "1.0.0"
    const val collectionKtx = "1.2.0"
    const val constraintLayout = "2.1.2"
    const val coreKtx = "1.7.0"
    const val crashlytics = "18.2.5"
    const val crashlyticsPlugin = "2.7.1"
    const val googleServices = "4.3.10"
    const val gradle = "7.0.3"
    const val graphview = "4.2.2"
    const val gson = "2.8.9"
    const val junit = "4.13.2"
    const val koin = "3.1.4"
    const val kotlin = "1.6.0"
    const val leakCanary = "2.7"
    const val lifecycle = "2.4.0"
    const val lottie = "3.4.0"
    const val materialComponents = "1.4.0"
    const val mockito = "4.0.0"
    const val mockitoKotlin = "4.0.0"
    const val okhttp = "4.9.0"
    const val playCore = "1.10.2"
    const val playServicesLocation = "19.0.0"
    const val playServicesMaps = "18.0.1"
    const val preference = "1.0.0"
    const val room = "2.3.0"
    const val timber = "5.0.1"
}

object Dependencies {

    const val activityKtx = "androidx.activity:activity-ktx:${LibraryVersions.activityKtx}"
    const val cardView = "androidx.cardview:cardview:${LibraryVersions.cardView}"
    const val collectionKtx = "androidx.collection:collection-ktx:${LibraryVersions.collectionKtx}"
    const val constraintLayout = "androidx.constraintlayout:constraintlayout:${LibraryVersions.constraintLayout}"
    const val coreKtx = "androidx.core:core-ktx:${LibraryVersions.coreKtx}"
    const val crashlytics = "com.google.firebase:firebase-crashlytics-ktx:${LibraryVersions.crashlytics}"
    const val crashlyticsPlugin = "com.google.firebase:firebase-crashlytics-gradle:${LibraryVersions.crashlyticsPlugin}"
    const val googleServicesPlugin = "com.google.gms:google-services:${LibraryVersions.googleServices}"
    const val gradlePlugin = "com.android.tools.build:gradle:${LibraryVersions.gradle}"
    const val graphview = "com.jjoe64:graphview:${LibraryVersions.graphview}"
    const val gson = "com.google.code.gson:gson:${LibraryVersions.gson}"
    const val koin = "io.insert-koin:koin-core:${LibraryVersions.koin}"
    const val koinAndroid = "io.insert-koin:koin-android:${LibraryVersions.koin}"
    const val kotlinGradle = "org.jetbrains.kotlin:kotlin-gradle-plugin:${LibraryVersions.kotlin}"
    const val leakCanary = "com.squareup.leakcanary:leakcanary-android:${LibraryVersions.leakCanary}"
    const val lifecycleViewModel = "androidx.lifecycle:lifecycle-viewmodel-ktx:${LibraryVersions.lifecycle}"
    const val lifecycleRuntime = "androidx.lifecycle:lifecycle-runtime-ktx:${LibraryVersions.lifecycle}"
    const val lottie = "com.airbnb.android:lottie:${LibraryVersions.lottie}"
    const val materialComponents = "com.google.android.material:material:${LibraryVersions.materialComponents}"
    const val okhttp = "com.squareup.okhttp3:okhttp:${LibraryVersions.okhttp}"
    const val playCore = "com.google.android.play:core:${LibraryVersions.playCore}"
    const val playServicesLocation = "com.google.android.gms:play-services-location:${LibraryVersions.playServicesLocation}"
    const val playServicesMaps = "com.google.android.gms:play-services-maps:${LibraryVersions.playServicesMaps}"
    const val preference = "androidx.preference:preference:${LibraryVersions.preference}"
    const val roomCompiler = "androidx.room:room-compiler:${LibraryVersions.room}"
    const val roomKtx = "androidx.room:room-ktx:${LibraryVersions.room}"
    const val roomRuntime = "androidx.room:room-runtime:${LibraryVersions.room}"
    const val timber = "com.jakewharton.timber:timber:${LibraryVersions.timber}"
}

object TestDependencies {

    const val archCompTesting = "androidx.arch.core:core-testing:${LibraryVersions.archCompTesting}"
    const val junit = "junit:junit:${LibraryVersions.junit}"
    const val mockito = "org.mockito:mockito-core:${LibraryVersions.mockito}"
    const val mockitoInline = "org.mockito:mockito-inline:${LibraryVersions.mockito}"
    const val mockitoKotlin = "org.mockito.kotlin:mockito-kotlin:${LibraryVersions.mockitoKotlin}"
}
