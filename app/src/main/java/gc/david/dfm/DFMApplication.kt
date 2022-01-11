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

package gc.david.dfm

import android.app.Application
import gc.david.dfm.di.*
import gc.david.dfm.initializers.*
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

/**
 * Created by David on 28/10/2014.
 */
class DFMApplication : Application() {

    val initializers: Initializers by inject()

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@DFMApplication)
            modules(appModule,
                    viewModelModule,
                    useCaseModule,
                    repositoryModule,
                    storageModule)
        }

        initializers.init(this)
    }
}
