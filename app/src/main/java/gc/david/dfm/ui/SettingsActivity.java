package gc.david.dfm.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import javax.inject.Inject;

import butterknife.BindView;
import gc.david.dfm.R;

import static butterknife.ButterKnife.bind;

public class SettingsActivity extends BaseActivity {

    @Inject
    protected Context appContext; // TODO: 13.11.16 remove this, it's stupid!

    @BindView(R.id.tbMain)
    protected Toolbar tbMain;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        bind(this);

        setSupportActionBar(tbMain);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportFragmentManager().beginTransaction()
                                   .replace(R.id.settings_activity_container_framelayout, new SettingsFragment())
                                   .commit();
    }
}
