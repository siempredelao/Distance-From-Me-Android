package gc.david.dfm.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import gc.david.dfm.R;

import static butterknife.ButterKnife.bind;

/**
 * Created by David on 01/11/2014.
 */
public class NavigationDrawerItemAdapter extends BaseAdapter {

    private final Activity      activity;
    private final List<String>  itemTitles;
    private final List<Integer> itemIcons;

    public NavigationDrawerItemAdapter(final Activity activity,
                                       final List<String> itemTitles,
                                       final List<Integer> itemIcons) {
        this.activity = activity;
        this.itemTitles = itemTitles;
        this.itemIcons = itemIcons;
    }

    @Override
    public int getCount() {
        return itemTitles.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View item = convertView;
        final ViewHolder holder;

        if (item == null) {
            final LayoutInflater inflater = activity.getLayoutInflater();
            item = inflater.inflate(R.layout.navigation_drawer_item, parent, false);

            holder = new ViewHolder(item);

            item.setTag(holder);
        } else {
            holder = (ViewHolder) item.getTag();
        }
        holder.ivIcon.setImageResource(itemIcons.get(position));
        holder.tvTitle.setText(itemTitles.get(position));

        return item;
    }

    static class ViewHolder {
        @BindView(R.id.navigation_drawer_item_icon)
        protected ImageView ivIcon;
        @BindView(R.id.navigation_drawer_item_title)
        protected TextView  tvTitle;

        public ViewHolder(final View view) {
            bind(this, view);
        }
    }
}
