package gc.david.dfm.ui;

import android.support.v7.app.AppCompatActivity;

import gc.david.dfm.DFMApplication;
import gc.david.dfm.dagger.RootComponent;

public class BaseActivity extends AppCompatActivity {

    protected RootComponent getRootComponent() {
        return ((DFMApplication) getApplication()).getRootComponent();
    }
}
