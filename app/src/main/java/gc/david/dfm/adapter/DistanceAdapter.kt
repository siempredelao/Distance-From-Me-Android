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

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import gc.david.dfm.R
import gc.david.dfm.database.Distance
import gc.david.dfm.databinding.DatabaseListItemBinding
import java.text.SimpleDateFormat
import java.util.*

class DistanceAdapter(
        activity: Activity,
        private val distanceList: List<Distance>
) : ArrayAdapter<Distance>(activity, R.layout.database_list_item, distanceList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var item = convertView
        val holder: ViewHolder

        if (item == null) {
            val binding = DatabaseListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            item = binding.root

            holder = ViewHolder(binding)

            item.tag = holder
        } else {
            holder = item.tag as ViewHolder
        }

        holder.bind(distanceList[position])

        return item
    }

    internal class ViewHolder(private val binding: DatabaseListItemBinding) {

        fun bind(distance: Distance) {
            binding.textViewAlias.text = distance.name
            binding.textViewDistance.text = distance.distance
            binding.textViewDate.text = DATE_FORMAT.format(distance.date)
        }
    }

    companion object {

        private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    }
}

// TODO move to utils or some better place
inline fun <reified T> Context.systemService(name: String): T {
    return getSystemService(name) as T
}
