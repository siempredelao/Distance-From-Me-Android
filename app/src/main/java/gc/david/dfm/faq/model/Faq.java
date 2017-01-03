package gc.david.dfm.faq.model;

/**
 * Created by david on 14.12.16.
 */
public class Faq {

    private final String question;
    private final String answer;

    public Faq(final String question, final String answer) {
        this.question = question;
        this.answer = answer;
    }

    public String getQuestion() {
        return question;
    }

    public String getAnswer() {
        return answer;
    }
}
