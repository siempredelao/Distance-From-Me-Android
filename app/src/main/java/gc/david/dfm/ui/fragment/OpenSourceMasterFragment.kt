/*
 * Copyright (c) 2018 David Aguiar Gonzalez
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
import android.widget.ProgressBar
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.google.android.material.snackbar.Snackbar
import gc.david.dfm.DFMApplication
import gc.david.dfm.R
import gc.david.dfm.adapter.OpenSourceLibraryAdapter
import gc.david.dfm.dagger.DaggerOpenSourceComponent
import gc.david.dfm.dagger.OpenSourceModule
import gc.david.dfm.dagger.RootModule
import gc.david.dfm.logger.DFMLogger
import gc.david.dfm.opensource.domain.OpenSourceUseCase
import gc.david.dfm.opensource.presentation.OpenSource
import gc.david.dfm.opensource.presentation.OpenSourcePresenter
import gc.david.dfm.opensource.presentation.mapper.OpenSourceLibraryMapper
import gc.david.dfm.opensource.presentation.model.OpenSourceLibraryModel
import gc.david.dfm.ui.animation.DetailsTransition
import javax.inject.Inject

/**
 * Created by david on 24.01.17.
 */
class OpenSourceMasterFragment : Fragment(), OpenSource.View {

    @BindView(R.id.opensourcelibrary_fragment_recyclerview)
    lateinit var recyclerView: RecyclerView
    @BindView(R.id.opensourcelibrary_fragment_progressbar)
    lateinit var progressbar: ProgressBar

    @Inject
    lateinit var openSourceUseCase: OpenSourceUseCase
    @Inject
    lateinit var openSourceLibraryMapper: OpenSourceLibraryMapper

    private lateinit var presenter: OpenSource.Presenter
    private lateinit var adapter: OpenSourceLibraryAdapter
    
    private val listener = object : OnItemClickListener {
        override fun onItemClick(openSourceLibraryModel: OpenSourceLibraryModel,
                                 viewHolder: OpenSourceLibraryAdapter.OpenSourceLibraryViewHolder) {
            val openSourceDetailFragment = OpenSourceDetailFragment().apply {
                val changeBoundsTransition = DetailsTransition()
                val fadeTransition = Fade()

                this@OpenSourceMasterFragment.exitTransition = fadeTransition

                sharedElementEnterTransition = changeBoundsTransition
                enterTransition = fadeTransition
                sharedElementReturnTransition = changeBoundsTransition
                arguments = bundleOf(OpenSourceDetailFragment.LIBRARY_KEY to openSourceLibraryModel)
            }

            requireActivity().supportFragmentManager
                    .beginTransaction()
                    .addSharedElement(viewHolder.tvName,
                            getString(R.string.transition_opensourcelibrary_name))
                    .addSharedElement(viewHolder.tvShortLicense,
                            getString(R.string.transition_opensourcelibrary_description))
                    .replace(R.id.about_activity_container_framelayout, openSourceDetailFragment)
                    .addToBackStack(null)
                    .commit()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DaggerOpenSourceComponent.builder()
                .rootModule(RootModule(requireActivity().application as DFMApplication))
                .openSourceModule(OpenSourceModule())
                .build()
                .inject(this)

        adapter = OpenSourceLibraryAdapter(listener)

        presenter = OpenSourcePresenter(this, openSourceUseCase, openSourceLibraryMapper)
        presenter.start()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_opensourcelibrary_master, container, false)
        ButterKnife.bind(this, view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (adapter.itemCount != 0) {
            hideLoading()
            setupList()
        }
    }

    override fun setPresenter(presenter: OpenSource.Presenter) {
        this.presenter = presenter
    }

    override fun showLoading() {
        if (view != null) { // Workaround: at this point, onCreateView could not have been executed
            progressbar.isVisible = true
            recyclerView.isVisible = false
        }
    }

    override fun hideLoading() {
        progressbar.isVisible = false
        recyclerView.isVisible = true
    }

    override fun add(openSourceLibraryModelList: List<OpenSourceLibraryModel>) {
        adapter.add(openSourceLibraryModelList)
    }

    override fun showError(errorMessage: String) {
        DFMLogger.logException(Exception(errorMessage))

        Snackbar.make(recyclerView, R.string.opensourcelibrary_error_message, Snackbar.LENGTH_LONG).show()
    }

    override fun setupList() {
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
    }

    interface OnItemClickListener {

        fun onItemClick(openSourceLibraryModel: OpenSourceLibraryModel, item: OpenSourceLibraryAdapter.OpenSourceLibraryViewHolder)
    }
}
