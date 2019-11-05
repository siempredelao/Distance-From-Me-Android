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

package gc.david.dfm.adapter

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.annotation.LayoutRes
import butterknife.BindView
import butterknife.ButterKnife.bind
import gc.david.dfm.R
import gc.david.dfm.model.Distance
import java.text.SimpleDateFormat
import java.util.*

// TODO OH GOD! Use a RecyclerView instead...
class DistanceAdapter(
        private val activity: Activity,
        private val distanceList: List<Distance>
) : ArrayAdapter<Distance>(activity, R.layout.database_list_item, distanceList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var item = convertView
        val holder: ViewHolder

        if (item == null) {
            val inflater = activity.systemService<LayoutInflater>(Context.LAYOUT_INFLATER_SERVICE)
            item = inflater.inflate(R.layout.database_list_item, parent, false)

            holder = ViewHolder(item)

            item!!.tag = holder
        } else {
            holder = item.tag as ViewHolder
        }

        holder.title.text = distanceList[position].name
        holder.distance.text = distanceList[position].distance
        holder.date.text = DATE_FORMAT.format(distanceList[position].date)

        return item
    }

    internal class ViewHolder(view: View) {

        @BindView(R.id.alias)
        lateinit var title: TextView
        @BindView(R.id.distancia)
        lateinit var distance: TextView
        @BindView(R.id.fecha)
        lateinit var date: TextView

        init {
            bind(this, view)
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

fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
}