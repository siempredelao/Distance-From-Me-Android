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

package gc.david.dfm.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.project

/**
 * JUnit 5 unit tests for an Android module, with Mockito, coroutines-test and the shared
 * `CoroutineExtension`. Apply it after dfm.android.application or dfm.android.library.
 */
class UnitTestConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        android.testOptions.unitTests.all { it.useJUnitPlatform() }

        dependencies {
            "testImplementation"(libs.library("coroutines-test"))
            "testImplementation"(libs.library("junit-jupiter-api"))
            "testImplementation"(libs.library("junit-jupiter-params"))
            "testImplementation"(libs.library("mockito"))
            "testImplementation"(libs.library("mockito-kotlin"))
            "testImplementation"(project(TEST_SUPPORT_PATH))

            "testRuntimeOnly"(libs.library("junit-jupiter-engine"))
        }
    }

    private companion object {
        const val TEST_SUPPORT_PATH = ":support:test"
    }
}
