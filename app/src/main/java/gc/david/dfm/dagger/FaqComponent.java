package gc.david.dfm.dagger;

import javax.inject.Singleton;

import dagger.Component;
import gc.david.dfm.ui.HelpAndFeedbackActivity;

/**
 * Created by david on 27.12.16.
 */
@Singleton
@Component(modules = {RootModule.class, FaqModule.class})
public interface FaqComponent {

    void inject(HelpAndFeedbackActivity helpAndFeedbackActivity);
}
