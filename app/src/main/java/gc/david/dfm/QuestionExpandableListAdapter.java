package gc.david.dfm;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by David on 17/10/2014.
 */
public class QuestionExpandableListAdapter extends BaseExpandableListAdapter {

    private final Context      context;
    private       String       header;
    private final List<String> selectableElements;

    public QuestionExpandableListAdapter(final Context context,
                                         final String header,
                                         final List<String> selectableElements) {
        this.context = context;
        this.header = header;
        this.selectableElements = selectableElements;
    }

    @Override
    public int getGroupCount() {
        return 1; // Este adapter solo controlará instancias únicas
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return selectableElements.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return header;
    }

    public void setGroup(String newName) {
        header = newName;
        notifyDataSetChanged();
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        if (childPosition > selectableElements.size() - 1) {
            throw new ArrayIndexOutOfBoundsException("childPosition cannot be bigger than selectableElements size.");
        }
        return selectableElements.get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.simple_textview_list_item, parent, false);
        }

        final TextView tvHeader = (TextView) convertView.findViewById(R.id.simple_textview);
        tvHeader.setText((CharSequence) getGroup(0));

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition,
                             int childPosition,
                             boolean isLastChild,
                             View convertView,
                             ViewGroup parent) {
        if (convertView == null) {
            final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.simple_textview_list_item, parent, false);
        }

        final TextView tvListElement = (TextView) convertView.findViewById(R.id.simple_textview);
        tvListElement.setText((CharSequence) getChild(0, childPosition));

        convertView.setPadding(15, 0, 0, 0);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
