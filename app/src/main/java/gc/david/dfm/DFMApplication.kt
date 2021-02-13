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

package gc.david.dfm

import android.app.Application
import gc.david.dfm.dagger.DaggerRootComponent
import gc.david.dfm.dagger.RootModule
import gc.david.dfm.initializers.Initializers
import javax.inject.Inject

/**
 * Created by David on 28/10/2014.
 */
class DFMApplication : Application() {

    @Inject
    lateinit var initializers: Initializers

    override fun onCreate() {
        super.onCreate()

        DaggerRootComponent.builder()
                .rootModule(RootModule(this))
                .build()
                .inject(this)

        initializers.init(this)
    }
}
