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
