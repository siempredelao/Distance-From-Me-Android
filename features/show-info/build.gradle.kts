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

plugins {
    alias(libs.plugins.dfm.android.library)
    alias(libs.plugins.dfm.android.compose)
    alias(libs.plugins.dfm.unit.test)
}

android {
    namespace = "gc.david.dfm.showinfo"
}

dependencies {

    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.material.icons)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui)
    implementation(libs.coroutines)
    implementation(libs.koin)
    implementation(libs.koin.android)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.timber)
    implementation(project(":core:address"))
    implementation(project(":core:coordinates"))
    implementation(project(":core:design-system"))
    implementation(project(":core:distances"))
    implementation(project(":core:settings"))
    implementation(project(":core:units"))
    implementation(project(":support:android"))
    implementation(project(":support:coroutines"))
}
