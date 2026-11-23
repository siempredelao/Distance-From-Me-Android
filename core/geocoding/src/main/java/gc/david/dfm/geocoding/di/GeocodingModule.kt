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

package gc.david.dfm.geocoding.di

import gc.david.dfm.geocoding.data.DefaultGeocodeApiKeyProvider
import gc.david.dfm.geocoding.data.GoogleApiKeyInterceptor
import gc.david.dfm.geocoding.domain.GeocodeApiKeyProvider
import okhttp3.OkHttpClient
import org.koin.core.qualifier.Qualifier
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * The [OkHttpClient] for the Google Maps web services. Its [GoogleApiKeyInterceptor] adds the API key to each request.
 */
val GOOGLE_API_CLIENT: Qualifier = named("GoogleApiClient")

val geocodingModule = module {

    single<GeocodeApiKeyProvider> { DefaultGeocodeApiKeyProvider(get()) }
    single(GOOGLE_API_CLIENT) {
        OkHttpClient.Builder()
            .addInterceptor(GoogleApiKeyInterceptor(get()))
            .build()
    }
}
