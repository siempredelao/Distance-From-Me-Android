package gc.david.dfm.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import gc.david.dfm.DFMApplication;

public class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((DFMApplication) getApplication()).inject(this);
    }
}
