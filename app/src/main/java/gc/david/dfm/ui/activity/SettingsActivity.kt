/*
 * Copyright (c) 2021 David Aguiar Gonzalez
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

package gc.david.dfm.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import gc.david.dfm.R
import gc.david.dfm.databinding.ActivitySettingsBinding
import gc.david.dfm.ui.fragment.SettingsFragment

class SettingsActivity : AppCompatActivity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivitySettingsBinding.inflate(layoutInflater).apply {
            setContentView(root)
            setSupportActionBar(tbMain.tbMain)
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        supportFragmentManager.beginTransaction()
                .replace(R.id.settings_activity_container_framelayout, SettingsFragment())
                .commit()
    }

    companion object {

        fun open(activity: Activity) {
            val openSettingsActivityIntent = Intent(activity, SettingsActivity::class.java)
            activity.startActivity(openSettingsActivityIntent)
        }
    }
}
