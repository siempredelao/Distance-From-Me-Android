package gc.david.dfm.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import butterknife.BindView;
import gc.david.dfm.R;

import static butterknife.ButterKnife.bind;

public class SettingsActivity extends AppCompatActivity {

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

    public static void open(final Activity activity) {
        final Intent showInfoActivityIntent = new Intent(activity, SettingsActivity.class);
        activity.startActivity(showInfoActivityIntent);
    }
}
