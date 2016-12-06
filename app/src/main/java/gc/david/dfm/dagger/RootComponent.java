package gc.david.dfm.dagger;

import javax.inject.Singleton;

import dagger.Component;
import gc.david.dfm.ui.FeedbackActivity;
import gc.david.dfm.ui.MainActivity;
import gc.david.dfm.ui.ShowInfoActivity;

/**
 * Created by david on 06.12.16.
 */
@Singleton
@Component(modules = RootModule.class)
public interface RootComponent {

    void inject(MainActivity mainActivity);

    void inject(ShowInfoActivity showInfoActivity);

    void inject(FeedbackActivity feedbackActivity);

}