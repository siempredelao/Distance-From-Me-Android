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

package gc.david.dfm.ui.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Fade;
import android.transition.Transition;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import gc.david.dfm.DFMApplication;
import gc.david.dfm.R;
import gc.david.dfm.adapter.OpenSourceLibraryAdapter;
import gc.david.dfm.dagger.DaggerOpenSourceComponent;
import gc.david.dfm.dagger.OpenSourceModule;
import gc.david.dfm.dagger.RootModule;
import gc.david.dfm.logger.DFMLogger;
import gc.david.dfm.opensource.domain.OpenSourceUseCase;
import gc.david.dfm.opensource.presentation.OpenSource;
import gc.david.dfm.opensource.presentation.OpenSourcePresenter;
import gc.david.dfm.opensource.presentation.mapper.OpenSourceLibraryMapper;
import gc.david.dfm.opensource.presentation.model.OpenSourceLibraryModel;
import gc.david.dfm.ui.animation.DetailsTransition;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;

/**
 * Created by david on 24.01.17.
 */
public class OpenSourceMasterFragment extends Fragment implements OpenSource.View {

    @BindView(R.id.opensourcelibrary_fragment_recyclerview)
    protected RecyclerView recyclerView;
    @BindView(R.id.opensourcelibrary_fragment_progressbar)
    protected ProgressBar  progressbar;

    @Inject
    OpenSourceUseCase       openSourceUseCase;
    @Inject
    OpenSourceLibraryMapper openSourceLibraryMapper;

    private OpenSource.Presenter     presenter;
    private OpenSourceLibraryAdapter adapter;
    private final OnItemClickListener listener = new OnItemClickListener() {
        @Override
        public void onItemClick(final OpenSourceLibraryModel openSourceLibraryModel,
                                final OpenSourceLibraryAdapter.OpenSourceLibraryViewHolder viewHolder) {
            final OpenSourceDetailFragment openSourceDetailFragment = new OpenSourceDetailFragment();
            if (SDK_INT >= LOLLIPOP) {
                final Transition changeBoundsTransition = new DetailsTransition();
                final Transition fadeTransition = new Fade();

                OpenSourceMasterFragment.this.setExitTransition(fadeTransition);

                openSourceDetailFragment.setSharedElementEnterTransition(changeBoundsTransition);
                openSourceDetailFragment.setEnterTransition(fadeTransition);
                openSourceDetailFragment.setSharedElementReturnTransition(changeBoundsTransition);
            }

            final Bundle bundle = new Bundle();
            bundle.putParcelable(OpenSourceDetailFragment.LIBRARY_KEY, openSourceLibraryModel);
            openSourceDetailFragment.setArguments(bundle);

            requireActivity().getSupportFragmentManager()
                         .beginTransaction()
                         .addSharedElement(viewHolder.getTvName(),
                                           getString(R.string.transition_opensourcelibrary_name))
                         .addSharedElement(viewHolder.getTvShortLicense(),
                                           getString(R.string.transition_opensourcelibrary_description))
                         .replace(R.id.about_activity_container_framelayout, openSourceDetailFragment)
                         .addToBackStack(null)
                         .commit();
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DaggerOpenSourceComponent.builder()
                                 .rootModule(new RootModule((DFMApplication) requireActivity().getApplication()))
                                 .openSourceModule(new OpenSourceModule())
                                 .build()
                                 .inject(this);

        adapter = new OpenSourceLibraryAdapter(listener);

        presenter = new OpenSourcePresenter(this, openSourceUseCase, openSourceLibraryMapper);
        presenter.start();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_opensourcelibrary_master, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (adapter.getItemCount() != 0) {
            hideLoading();
            setupList();
        }
    }

    @Override
    public void setPresenter(final OpenSource.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void showLoading() {
        if (getView() != null) { // Workaround: at this point, onCreateView could not have been executed
            progressbar.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }

    @Override
    public void hideLoading() {
        progressbar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void add(final List<OpenSourceLibraryModel> openSourceLibraryModelList) {
        adapter.add(openSourceLibraryModelList);
    }

    @Override
    public void showError(final String errorMessage) {
        DFMLogger.logException(new Exception(errorMessage));

        Snackbar.make(recyclerView, R.string.opensourcelibrary_error_message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void setupList() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    public interface OnItemClickListener {
        void onItemClick(OpenSourceLibraryModel openSourceLibraryModel, OpenSourceLibraryAdapter.OpenSourceLibraryViewHolder item);
    }
}
