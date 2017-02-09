/*
 * Copyright (c) 2017 David Aguiar Gonzalez
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gc.david.dfm.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import butterknife.BindView;
import gc.david.dfm.R;
import gc.david.dfm.ui.fragment.SettingsFragment;

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
        final Intent openSettingsActivityIntent = new Intent(activity, SettingsActivity.class);
        activity.startActivity(openSettingsActivityIntent);
    }
}
