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

package gc.david.dfm.support.navigation

/**
 * A screen that one feature opens in another, with any arguments that screen needs.
 *
 * The routes are sealed so that `:app`'s navigator has to handle every one of them: a new route
 * without a branch there doesn't compile. The cost is that this module lists every cross-feature
 * destination by name. That is fine for a handful of routes. If they grow, reconsider this setup,
 * for example by giving each destination feature a small API module that declares its own routes.
 */
sealed interface Route {

    data object About : Route

    data object Faq : Route

    data object OpenSourceLicenses : Route

    data object Settings : Route

    data object ShowInfo : Route
}
