package gc.david.dfm.faq;

import java.util.HashSet;
import java.util.Set;

import gc.david.dfm.faq.model.Faq;

/**
 * Created by david on 19.12.16.
 */
public class GetFaqsDiskDataSource implements GetFaqsRepository {

    private final Set<Faq> questionsAndAnswers;

    public GetFaqsDiskDataSource() {
        questionsAndAnswers = new HashSet<>();
        // TODO: 21.12.16 get questions from Firebase and cache them
        // TODO: 21.12.16 add provisional questions here!
        questionsAndAnswers.add(new Faq("This is a question1", "This is a short answer"));
        questionsAndAnswers.add(new Faq("This is a question2", "This is a super hiper mega eeeeeeeeeeeeeeeeeeeextra long answer"));
    }

    @Override
    public Set<Faq> getFaqs() {
        waitToMakeThisFeatureMoreInteresting();
        return questionsAndAnswers;
    }

    private void waitToMakeThisFeatureMoreInteresting() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            // nothing
        }
    }
}
