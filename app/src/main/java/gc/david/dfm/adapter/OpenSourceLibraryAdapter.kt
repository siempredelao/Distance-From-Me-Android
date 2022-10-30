/*
 * Copyright (c) 2022 David Aguiar Gonzalez
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

package gc.david.dfm.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import gc.david.dfm.databinding.ViewOpensourcelibraryItemBinding
import gc.david.dfm.opensource.presentation.model.OpenSourceLibraryUiModel
import gc.david.dfm.ui.fragment.OpenSourceMasterFragment.OnItemClickListener

/**
 * Created by david on 24.01.17.
 */
class OpenSourceLibraryAdapter(
    private val onItemClickListener: OnItemClickListener
) : RecyclerView.Adapter<OpenSourceLibraryAdapter.OpenSourceLibraryViewHolder>() {

    private val openSourceLibraryModelList = mutableListOf<OpenSourceLibraryUiModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OpenSourceLibraryViewHolder {
        val binding = ViewOpensourcelibraryItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OpenSourceLibraryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OpenSourceLibraryViewHolder, position: Int) {
        holder.onBind(openSourceLibraryModelList[position])

        holder.itemView.setOnClickListener {
            onItemClickListener.onItemClick(
                openSourceLibraryModelList[holder.adapterPosition],
                holder
            )
        }

        holder.binding.textViewName.transitionName = position.toString() + "_id"
        holder.binding.textViewLicense.transitionName = position.toString() + "_content"
    }

    fun add(openSourceLibraryUiModels: List<OpenSourceLibraryUiModel>) {
        val previousSize = openSourceLibraryUiModels.size
        openSourceLibraryModelList.addAll(openSourceLibraryUiModels)
        notifyItemRangeInserted(previousSize, openSourceLibraryUiModels.size)
    }

    override fun getItemCount(): Int {
        return openSourceLibraryModelList.size
    }

    class OpenSourceLibraryViewHolder(
        val binding: ViewOpensourcelibraryItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        internal fun onBind(openSourceLibraryUiModel: OpenSourceLibraryUiModel) = with(binding) {
            textViewName.text = openSourceLibraryUiModel.name
            textViewVersion.text = openSourceLibraryUiModel.version
            textViewDescription.text = openSourceLibraryUiModel.description
            textViewLicense.text = openSourceLibraryUiModel.licenseTitle
        }
    }
}
