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

package gc.david.dfm.adapter;

import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import gc.david.dfm.R;
import gc.david.dfm.opensource.presentation.model.OpenSourceLibraryModel;
import gc.david.dfm.ui.fragment.OpenSourceMasterFragment.OnItemClickListener;

/**
 * Created by david on 24.01.17.
 */
public class OpenSourceLibraryAdapter extends RecyclerView.Adapter<OpenSourceLibraryAdapter.OpenSourceLibraryViewHolder> {

    private final List<OpenSourceLibraryModel> openSourceLibraryModelList;
    private final OnItemClickListener          onItemClickListener;

    public OpenSourceLibraryAdapter(@NonNull final OnItemClickListener listener) {
        this.openSourceLibraryModelList = new ArrayList<>();
        this.onItemClickListener = listener;
    }

    @Override
    public OpenSourceLibraryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext())
                                        .inflate(R.layout.view_opensourcelibrary_item, parent, false);
        return new OpenSourceLibraryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final OpenSourceLibraryViewHolder holder, final int position) {
        holder.onBind(openSourceLibraryModelList.get(position));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onItemClick(openSourceLibraryModelList.get(holder.getAdapterPosition()), holder);
            }
        });

        ViewCompat.setTransitionName(holder.getTvName(), String.valueOf(position) + "_id");
        ViewCompat.setTransitionName(holder.getTvShortLicense(), String.valueOf(position) + "_content");
    }

    public void add(final List<OpenSourceLibraryModel> openSourceLibraryModels) {
        final int previousSize = openSourceLibraryModels.size();
        openSourceLibraryModelList.addAll(openSourceLibraryModels);
        notifyItemRangeInserted(previousSize, openSourceLibraryModels.size());
    }

    @Override
    public int getItemCount() {
        return openSourceLibraryModelList.size();
    }

    public static class OpenSourceLibraryViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.opensourcelibrary_item_view_name_textview)
        protected TextView tvName;
        @BindView(R.id.opensourcelibrary_item_view_version_textview)
        protected TextView tvVersion;
        @BindView(R.id.opensourcelibrary_item_view_description_textview)
        protected TextView tvDescription;
        @BindView(R.id.opensourcelibrary_item_view_short_license_textview)
        protected TextView tvShortLicense;

        OpenSourceLibraryViewHolder(final View view) {
            super(view);
            ButterKnife.bind(this, itemView);
        }

        void onBind(final OpenSourceLibraryModel openSourceLibraryModel) {
            tvName.setText(openSourceLibraryModel.getName());
            tvVersion.setText(String.format("v%s", openSourceLibraryModel.getVersion()));
            tvDescription.setText(openSourceLibraryModel.getDescription());
            tvShortLicense.setText(String.format("%s license", openSourceLibraryModel.getLicense()));
        }

        public TextView getTvName() {
            return tvName;
        }

        public TextView getTvShortLicense() {
            return tvShortLicense;
        }
    }
}
