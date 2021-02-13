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

package gc.david.dfm.ui.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.*
import androidx.fragment.app.Fragment
import gc.david.dfm.R
import gc.david.dfm.databinding.FragmentOpensourcelibraryDetailBinding
import gc.david.dfm.opensource.presentation.LicensePrinter
import gc.david.dfm.opensource.presentation.model.OpenSourceLibraryModel

/**
 * Created by david on 24.01.17.
 */
class OpenSourceDetailFragment : Fragment() {

    private lateinit var binding: FragmentOpensourcelibraryDetailBinding
    private lateinit var openSourceLibraryModel: OpenSourceLibraryModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        val arguments = checkNotNull(arguments)
        openSourceLibraryModel = arguments.getParcelable(LIBRARY_KEY) ?: error("No model available")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentOpensourcelibraryDetailBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_opensource, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_open_license_browser -> {
                val openBrowserIntent =
                        Intent(Intent.ACTION_VIEW, Uri.parse(openSourceLibraryModel.link))
                startActivity(openBrowserIntent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.textViewTitle.text = openSourceLibraryModel.name
        binding.textViewDescription.text = LicensePrinter.print(openSourceLibraryModel, requireContext())
        binding.textViewDescription.movementMethod = ScrollingMovementMethod.getInstance()
    }

    companion object {

        const val LIBRARY_KEY = "opensourcedetailfragment.library.key"
    }
}
