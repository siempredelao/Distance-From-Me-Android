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
import android.os.Parcelable
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import gc.david.dfm.R
import gc.david.dfm.Utils
import gc.david.dfm.databinding.ActivityShowInfoBinding
import gc.david.dfm.showinfo.presentation.SaveDistanceData
import gc.david.dfm.showinfo.presentation.ShareDialogData
import gc.david.dfm.showinfo.presentation.ShowInfoViewModel
import gc.david.dfm.ui.dialog.SaveDistanceDialogFragment
import org.koin.android.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.util.*

class ShowInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShowInfoBinding

    private var refreshMenuItem: MenuItem? = null

    private val viewModel: ShowInfoViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.tag(TAG).d("onCreate savedInstanceState=%s", Utils.dumpBundleToString(savedInstanceState))

        super.onCreate(savedInstanceState)
        binding = ActivityShowInfoBinding.inflate(layoutInflater).apply {
            setContentView(root)
            setSupportActionBar(tbMain.tbMain)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }

        with(viewModel) {
            originAddress.observe(this@ShowInfoActivity, { originAddress ->
                binding.textViewOriginAddress.text = originAddress
            })
            destinationAddress.observe(this@ShowInfoActivity, { destinationAddress ->
                binding.textViewDestinationAddress.text = destinationAddress
            })
            distanceMessage.observe(this@ShowInfoActivity, { distance ->
                binding.textViewDistance.text = distance
            })
            errorMessage.observe(this@ShowInfoActivity, { message ->
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
            })
            progressVisibility.observe(this@ShowInfoActivity, { visible ->
                if (visible) showProgress() else hideProgress()
            })
            showShareDialogEvent.observe(this@ShowInfoActivity, { event ->
                event.getContentIfNotHandled()?.let { showShareDialog(it) }
            })
            saveDistanceEvent.observe(this@ShowInfoActivity, { event ->
                event.getContentIfNotHandled()?.let { storeDataLocally(it) }
            })
        }

        if (savedInstanceState == null) {
            Timber.tag(TAG).d("onCreate savedInstanceState null")
            loadData()
        }
    }

    private fun loadData() {
        Timber.tag(TAG).d("loadData")

        val positionsList =
                intent.getParcelableArrayListExtra<LatLng>(POSITIONS_LIST_EXTRA_KEY)
                        ?: error("No positions available")
        val distance = intent.getStringExtra(DISTANCE_EXTRA_KEY)!!
        viewModel.onStart(positionsList, distance)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.show_info, menu)

        refreshMenuItem = menu.findItem(R.id.refresh)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_social_share -> {
                viewModel.onShare()
                true
            }
            R.id.refresh -> {
                loadData()
                true
            }
            R.id.menu_save -> {
                viewModel.onSave()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showShareDialog(data: ShareDialogData) {
        with(data) {
            val defaultShareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, description)
            }

            startActivity(Intent.createChooser(defaultShareIntent, title))
        }
    }

    private fun storeDataLocally(data: SaveDistanceData) {
        with(data) {
            val saveDistanceDialogFragment =
                    SaveDistanceDialogFragment.newInstance(positionsList, distance)
            saveDistanceDialogFragment.show(supportFragmentManager, null)
        }
    }

    private fun showProgress() {
        refreshMenuItem?.setActionView(R.layout.actionbar_indeterminate_progress)
    }

    private fun hideProgress() {
        refreshMenuItem?.actionView = null
    }

    companion object {

        private const val TAG = "ShowInfoActivity"

        private const val POSITIONS_LIST_EXTRA_KEY = "positionsList"
        private const val DISTANCE_EXTRA_KEY = "distance"

        fun open(activity: Activity, coordinates: List<LatLng>, distanceAsText: String) {
            val openShowInfoActivityIntent = Intent(activity, ShowInfoActivity::class.java)
            openShowInfoActivityIntent.putParcelableArrayListExtra(POSITIONS_LIST_EXTRA_KEY,
                    ArrayList<Parcelable>(coordinates))
            openShowInfoActivityIntent.putExtra(DISTANCE_EXTRA_KEY, distanceAsText)
            activity.startActivity(openShowInfoActivityIntent)
        }
    }
}
