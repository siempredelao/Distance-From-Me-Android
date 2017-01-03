package gc.david.dfm.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import gc.david.dfm.R;
import gc.david.dfm.model.Distance;

import static butterknife.ButterKnife.bind;
import static org.apache.http.impl.cookie.DateUtils.formatDate;

public class DistanceAdapter extends ArrayAdapter<Distance> {

    private final Activity       activity;
    private final List<Distance> distanceList;

    public DistanceAdapter(final Activity activity, final List<Distance> distanceList) {
        super(activity, R.layout.database_list_item, distanceList);
        this.activity = activity;
        this.distanceList = distanceList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
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
        holder.date.setText(formatDate(distanceList.get(position).getDate(), "yyyy-MM-dd"));

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