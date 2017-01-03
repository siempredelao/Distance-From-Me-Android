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
        questionsAndAnswers.add(new Faq("How can I get a distance?", "Just do a long press in the map :)"));
        questionsAndAnswers.add(new Faq("How can I get a distance from my current position to any position in the map?",
                                        "In the side bar, select \"Current position\" item and then perform a long press in the desired place in the map."));
        questionsAndAnswers.add(new Faq("How can I get a distance from any position to another position in the map?",
                                        "In the side bar, select \"Any position\" item ant then perform a long press in the desired place in the map."));
        questionsAndAnswers.add(new Faq("Do I need to grant \"Location\" permission to get distances from current point?",
                                        "Yes."));
        questionsAndAnswers.add(new Faq("Do I need to grant \"Location\" permission to get distances from any point?",
                                        "No."));
        questionsAndAnswers.add(new Faq("How can I create a multiple points distance?",
                                        "Perform single clicks in the desired points and a long click in the last one."));
        questionsAndAnswers.add(new Faq("How can I reset the status of the map?",
                                        "Select any position mode in the side bar."));
        questionsAndAnswers.add(new Faq("Why my GPS position is not accurate?",
                                        "This issue could be related to your device GPS sensor."));
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
