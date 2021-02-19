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

import android.os.Bundle
import android.transition.Fade
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import gc.david.dfm.R
import gc.david.dfm.adapter.OpenSourceLibraryAdapter
import gc.david.dfm.databinding.FragmentOpensourcelibraryMasterBinding
import gc.david.dfm.opensource.presentation.OpenSourceViewModel
import gc.david.dfm.opensource.presentation.model.OpenSourceLibraryModel
import gc.david.dfm.ui.animation.DetailsTransition
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * Created by david on 24.01.17.
 */
class OpenSourceMasterFragment : Fragment() {

    private lateinit var binding: FragmentOpensourcelibraryMasterBinding
    private lateinit var adapter: OpenSourceLibraryAdapter

    private val viewModel: OpenSourceViewModel by inject()

    private val listener = object : OnItemClickListener {
        override fun onItemClick(model: OpenSourceLibraryModel,
                                 viewHolder: OpenSourceLibraryAdapter.OpenSourceLibraryViewHolder) {
            val openSourceDetailFragment = OpenSourceDetailFragment().apply {
                val changeBoundsTransition = DetailsTransition()
                val fadeTransition = Fade()

                this@OpenSourceMasterFragment.exitTransition = fadeTransition

                sharedElementEnterTransition = changeBoundsTransition
                enterTransition = fadeTransition
                sharedElementReturnTransition = changeBoundsTransition
                arguments = bundleOf(OpenSourceDetailFragment.LIBRARY_KEY to model)
            }

            requireActivity().supportFragmentManager
                    .beginTransaction()
                    .addSharedElement(viewHolder.binding.textViewName,
                            getString(R.string.transition_opensourcelibrary_name))
                    .addSharedElement(viewHolder.binding.textViewLicense,
                            getString(R.string.transition_opensourcelibrary_description))
                    .replace(R.id.container, openSourceDetailFragment)
                    .addToBackStack(null)
                    .commit()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Keep adapter initialization here so the transition backwards (detail->master) works
        adapter = OpenSourceLibraryAdapter(listener)

        with(viewModel) {
            progressVisibility.observe(this@OpenSourceMasterFragment, { visible ->
                binding.progressBar.isVisible = visible
                binding.recyclerView.isVisible = !visible
            })
            openSourceList.observe(this@OpenSourceMasterFragment, { list ->
                adapter.add(list)
            })
            errorMessage.observe(this@OpenSourceMasterFragment, { message ->
                Timber.tag(TAG).e(Exception(message))
                Snackbar.make(binding.recyclerView, message, Snackbar.LENGTH_LONG).show()
            })
            onStart()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentOpensourcelibraryMasterBinding.inflate(layoutInflater, container, false).apply {
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = adapter
        }
        return binding.root
    }

    interface OnItemClickListener {

        fun onItemClick(model: OpenSourceLibraryModel, viewHolder: OpenSourceLibraryAdapter.OpenSourceLibraryViewHolder)
    }

    companion object {

        private const val TAG = "OpenSourceMasterFrgmnt"
    }
}
