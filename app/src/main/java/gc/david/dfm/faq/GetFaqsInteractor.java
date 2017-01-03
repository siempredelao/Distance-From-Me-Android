package gc.david.dfm.faq;

import java.util.Set;

import gc.david.dfm.executor.Executor;
import gc.david.dfm.executor.Interactor;
import gc.david.dfm.executor.MainThread;
import gc.david.dfm.faq.model.Faq;

/**
 * Created by david on 17.12.16.
 */
public class GetFaqsInteractor implements Interactor, GetFaqsUseCase {

    private final Executor          executor;
    private final MainThread        mainThread;
    private final GetFaqsRepository repository;

    private Callback callback;

    public GetFaqsInteractor(final Executor executor, final MainThread mainThread, final GetFaqsRepository repository) {
        this.executor = executor;
        this.mainThread = mainThread;
        this.repository = repository;
    }

    @Override
    public void execute(final Callback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback can't be null, the client of this interactor needs to get the response in the callback");
        }
        this.callback = callback;
        this.executor.run(this);
    }

    @Override
    public void run() {
        try {
            final Set<Faq> faqs = repository.getFaqs();

            mainThread.post(new Runnable() {
                @Override
                public void run() {
                    callback.onFaqsLoaded(faqs);
                }
            });
        } catch (Exception exception) {
            mainThread.post(new Runnable() {
                @Override
                public void run() {
                    callback.onError();
                }
            });
        }
    }
}
