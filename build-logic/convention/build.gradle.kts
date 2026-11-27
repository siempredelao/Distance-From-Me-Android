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
    `kotlin-dsl`
}

group = "gc.david.dfm.buildlogic"

dependencies {
    // compileOnly: the root build puts these plugins on the classpath (`apply false`), so the
    // convention plugins only compile against them and never bring a second copy.
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = libs.plugins.dfm.android.application.get().pluginId
            implementationClass = "gc.david.dfm.buildlogic.AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = libs.plugins.dfm.android.library.get().pluginId
            implementationClass = "gc.david.dfm.buildlogic.AndroidLibraryConventionPlugin"
        }
        register("androidCompose") {
            id = libs.plugins.dfm.android.compose.get().pluginId
            implementationClass = "gc.david.dfm.buildlogic.AndroidComposeConventionPlugin"
        }
        register("unitTest") {
            id = libs.plugins.dfm.unit.test.get().pluginId
            implementationClass = "gc.david.dfm.buildlogic.UnitTestConventionPlugin"
        }
        register("jvmLibrary") {
            id = libs.plugins.dfm.jvm.library.get().pluginId
            implementationClass = "gc.david.dfm.buildlogic.JvmLibraryConventionPlugin"
        }
    }
}
