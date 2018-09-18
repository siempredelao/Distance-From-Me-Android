/*
 * Copyright (c) 2018 David Aguiar Gonzalez
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

import gc.david.dfm.opensource.presentation.model.OpenSourceLibraryModel;

/**
 * Created by david on 25.01.17.
 */
public interface OpenSource {

    interface View {
        void setPresenter(Presenter presenter);

        void showLoading();

        void hideLoading();

        void add(List<OpenSourceLibraryModel> openSourceLibraryModelList);

        void showError(String errorMessage);

        void setupList();
    }

    interface Presenter {
        void start();
    }

}
