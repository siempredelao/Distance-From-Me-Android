/*
 * Copyright (c) 2021 David Aguiar Gonzalez
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
    const val target = 28
}

object AppVersions {

    const val code = 30
    const val name = "3.5"
}

object LibraryVersions {

    const val cardView = "1.0.0"
    const val collectionKtx = "1.1.0"
    const val constraintLayout = "2.0.4"
    const val coreKtx = "1.3.2"
    const val crashlytics = "17.3.0"
    const val crashlyticsPlugin = "2.5.0"
    const val dagger = "2.32"
    const val dexcount = "2.0.0"
    const val googleServices = "4.3.5"
    const val gradle = "4.1.2"
    const val graphview = "4.2.2"
    const val gson = "2.8.6"
    const val junit = "4.12"
    const val kotlin = "1.4.30"
    const val leakCanary = "2.6"
    const val materialComponents = "1.3.0"
    const val mockito = "3.7.7"
    const val mockitoKotlin = "2.2.0"
    const val okhttp = "4.9.0"
    const val playServices = "17.0.0"
    const val preference = "1.0.0"
    const val room = "2.2.6"
    const val timber = "4.7.1"
}

object Dependencies {

    const val cardView = "androidx.cardview:cardview:${LibraryVersions.cardView}"
    const val collectionKtx = "androidx.collection:collection-ktx:${LibraryVersions.collectionKtx}"
    const val constraintLayout = "androidx.constraintlayout:constraintlayout:${LibraryVersions.constraintLayout}"
    const val coreKtx = "androidx.core:core-ktx:${LibraryVersions.coreKtx}"
    const val crashlytics = "com.google.firebase:firebase-crashlytics-ktx:${LibraryVersions.crashlytics}"
    const val crashlyticsPlugin = "com.google.firebase:firebase-crashlytics-gradle:${LibraryVersions.crashlyticsPlugin}"
    const val dagger = "com.google.dagger:dagger:${LibraryVersions.dagger}"
    const val daggerAndroid = "com.google.dagger:dagger-android:${LibraryVersions.dagger}"
    const val daggerAndroidSupport = "com.google.dagger:dagger-android-support:${LibraryVersions.dagger}"
    const val daggerCompiler = "com.google.dagger:dagger-compiler:${LibraryVersions.dagger}"
    const val daggerProcessor = "com.google.dagger:dagger-android-processor:${LibraryVersions.dagger}"
    const val dexcountPlugin = "com.getkeepsafe.dexcount:dexcount-gradle-plugin:${LibraryVersions.dexcount}"
    const val googleServicesPlugin = "com.google.gms:google-services:${LibraryVersions.googleServices}"
    const val gradlePlugin = "com.android.tools.build:gradle:${LibraryVersions.gradle}"
    const val graphview = "com.jjoe64:graphview:${LibraryVersions.graphview}"
    const val gson = "com.google.code.gson:gson:${LibraryVersions.gson}"
    const val kotlinGradle = "org.jetbrains.kotlin:kotlin-gradle-plugin:${LibraryVersions.kotlin}"
    const val leakCanary = "com.squareup.leakcanary:leakcanary-android:${LibraryVersions.leakCanary}"
    const val materialComponents = "com.google.android.material:material:${LibraryVersions.materialComponents}"
    const val okhttp = "com.squareup.okhttp3:okhttp:${LibraryVersions.okhttp}"
    const val playServicesLocation = "com.google.android.gms:play-services-location:${LibraryVersions.playServices}"
    const val playServicesMaps = "com.google.android.gms:play-services-maps:${LibraryVersions.playServices}"
    const val preference = "androidx.preference:preference:${LibraryVersions.preference}"
    const val roomCompiler = "androidx.room:room-compiler:${LibraryVersions.room}"
    const val roomKtx = "androidx.room:room-ktx:${LibraryVersions.room}"
    const val roomRuntime = "androidx.room:room-runtime:${LibraryVersions.room}"
    const val timber = "com.jakewharton.timber:timber:${LibraryVersions.timber}"
}

object TestDependencies {

    const val junit = "junit:junit:${LibraryVersions.junit}"
    const val mockito = "org.mockito:mockito-core:${LibraryVersions.mockito}"
    const val mockitoInline = "org.mockito:mockito-inline:${LibraryVersions.mockito}"
    const val mockitoKotlin = "com.nhaarman.mockitokotlin2:mockito-kotlin:${LibraryVersions.mockitoKotlin}"
}
