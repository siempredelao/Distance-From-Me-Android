package gc.david.dfm.faq;

import java.util.Set;

import gc.david.dfm.faq.model.Faq;

/**
 * Created by david on 21.12.16.
 */

public interface Faqs {

    interface View {
        void setPresenter(Presenter presenter);

        void showLoading();

        void hideLoading();

        void add(Set<Faq> faq);

        void showError();

        void setupList();
    }

    interface Presenter {
        void start();
    }
}
