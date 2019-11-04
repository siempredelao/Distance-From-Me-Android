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

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import gc.david.dfm.R;
import gc.david.dfm.model.Distance;

import static butterknife.ButterKnife.bind;

public class DistanceAdapter extends ArrayAdapter<Distance> {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private final Activity       activity;
    private final List<Distance> distanceList;

    public DistanceAdapter(final Activity activity, final List<Distance> distanceList) {
        super(activity, R.layout.database_list_item, distanceList);
        this.activity = activity;
        this.distanceList = distanceList;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View item = convertView;
        final ViewHolder holder;

        if (item == null) {
            final LayoutInflater inflater = activity.getLayoutInflater();
            item = inflater.inflate(R.layout.database_list_item, parent, false);

            holder = new ViewHolder(item);

            item.setTag(holder);
        } else {
            holder = (ViewHolder) item.getTag();
        }

        holder.title.setText(distanceList.get(position).getName());
        holder.distance.setText(distanceList.get(position).getDistance());
        holder.date.setText(DATE_FORMAT.format(distanceList.get(position).getDate()));

        return item;
    }

    static class ViewHolder {
        @BindView(R.id.alias)
        protected TextView title;
        @BindView(R.id.distancia)
        protected TextView distance;
        @BindView(R.id.fecha)
        protected TextView date;

        ViewHolder(final View view) {
            bind(this, view);
        }
    }
}