package gc.david.dfm.dagger;

import javax.inject.Singleton;

import dagger.Component;
import gc.david.dfm.ui.MainActivity;

/**
 * Created by david on 27.12.16.
 */
@Singleton
@Component(modules = {RootModule.class, StorageModule.class, MainModule.class})
public interface MainComponent {

    void inject(MainActivity mainActivity);
}
