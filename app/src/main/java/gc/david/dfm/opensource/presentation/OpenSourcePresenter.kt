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

package gc.david.dfm.opensource.presentation

import gc.david.dfm.opensource.data.model.OpenSourceLibraryEntity
import gc.david.dfm.opensource.domain.OpenSourceInteractor
import gc.david.dfm.opensource.presentation.mapper.OpenSourceLibraryMapper

/**
 * Created by david on 25.01.17.
 */
class OpenSourcePresenter(
        private val openSourceView: OpenSource.View,
        private val openSourceUseCase: OpenSourceInteractor,
        private val openSourceLibraryMapper: OpenSourceLibraryMapper
) : OpenSource.Presenter {

    init {
        this.openSourceView.setPresenter(this)
    }

    override fun start() {
        openSourceView.showLoading()

        openSourceUseCase.execute(object : OpenSourceInteractor.Callback {
            override fun onOpenSourceLibrariesLoaded(openSourceLibraryEntityList: List<OpenSourceLibraryEntity>) {
                openSourceView.hideLoading()
                openSourceView.setupList()
                openSourceView.add(openSourceLibraryMapper.transform(openSourceLibraryEntityList))
            }

            override fun onError(errorMessage: String) {
                openSourceView.hideLoading()
                openSourceView.showError(errorMessage)
            }
        })
    }
}
