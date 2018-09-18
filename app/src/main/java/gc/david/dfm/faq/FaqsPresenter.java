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

package gc.david.dfm.faq;

import java.util.Set;

import gc.david.dfm.faq.model.Faq;

/**
 * Created by david on 21.12.16.
 */
public class FaqsPresenter implements Faqs.Presenter {

    private final Faqs.View      faqsView;
    private final GetFaqsUseCase getFaqsUseCase;

    public FaqsPresenter(final Faqs.View faqsView, final GetFaqsUseCase getFaqsUseCase) {
        this.faqsView = faqsView;
        this.getFaqsUseCase = getFaqsUseCase;
        this.faqsView.setPresenter(this);
    }

    @Override
    public void start() {
        faqsView.showLoading();

        getFaqsUseCase.execute(new GetFaqsUseCase.Callback() {
            @Override
            public void onFaqsLoaded(final Set<Faq> faqs) {
                faqsView.hideLoading();
                faqsView.setupList();
                faqsView.add(faqs);
            }

            @Override
            public void onError() {
                faqsView.hideLoading();
                faqsView.showError();
            }
        });
    }
}
