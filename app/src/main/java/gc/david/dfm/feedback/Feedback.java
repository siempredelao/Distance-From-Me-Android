package gc.david.dfm.feedback;

import android.content.Context;
import android.content.Intent;

/**
 * Created by david on 07.12.16.
 */
public interface Feedback {

    interface View {
        void showError();

        void showEmailClient(Intent intent);

        Context context();
    }

    interface Presenter {
        void start();
    }

}
