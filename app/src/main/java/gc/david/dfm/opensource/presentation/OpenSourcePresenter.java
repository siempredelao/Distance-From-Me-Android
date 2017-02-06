/*
 * Copyright (c) 2017 David Aguiar Gonzalez
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

package gc.david.dfm.opensource.presentation;

import java.util.List;

import gc.david.dfm.opensource.data.model.OpenSourceLibraryEntity;
import gc.david.dfm.opensource.domain.OpenSourceUseCase;
import gc.david.dfm.opensource.presentation.mapper.OpenSourceLibraryMapper;

/**
 * Created by david on 25.01.17.
 */
public class OpenSourcePresenter implements OpenSource.Presenter {

    private final OpenSource.View openSourceView;
    private final OpenSourceUseCase openSourceUseCase;
    private final OpenSourceLibraryMapper openSourceLibraryMapper;

    public OpenSourcePresenter(final OpenSource.View openSourceView,
                               final OpenSourceUseCase openSourceUseCase,
                               final OpenSourceLibraryMapper openSourceLibraryMapper) {
        this.openSourceView = openSourceView;
        this.openSourceUseCase = openSourceUseCase;
        this.openSourceLibraryMapper = openSourceLibraryMapper;
        this.openSourceView.setPresenter(this);
    }

    @Override
    public void start() {
        openSourceView.showLoading();

        openSourceUseCase.execute(new OpenSourceUseCase.Callback() {
            @Override
            public void onOpenSourceLibrariesLoaded(final List<OpenSourceLibraryEntity> openSourceLibraryEntityList) {
                openSourceView.hideLoading();
                openSourceView.setupList();
                openSourceView.add(openSourceLibraryMapper.transform(openSourceLibraryEntityList));
            }

            @Override
            public void onError(final String errorMessage) {
                openSourceView.hideLoading();
                openSourceView.showError(errorMessage);
            }
        });
    }
}
