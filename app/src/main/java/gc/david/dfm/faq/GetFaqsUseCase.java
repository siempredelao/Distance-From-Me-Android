package gc.david.dfm.faq;

import java.util.Set;

import gc.david.dfm.faq.model.Faq;

/**
 * Created by david on 17.12.16.
 */
public interface GetFaqsUseCase {

    interface Callback {

        void onFaqsLoaded(final Set<Faq> faqs);

        void onError();

    }

    void execute(Callback callback);
}
