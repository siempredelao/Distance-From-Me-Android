package gc.david.dfm.faq;

import java.util.Set;

import gc.david.dfm.faq.model.Faq;

/**
 * Created by david on 19.12.16.
 */
public interface GetFaqsRepository {

    Set<Faq> getFaqs();

}
