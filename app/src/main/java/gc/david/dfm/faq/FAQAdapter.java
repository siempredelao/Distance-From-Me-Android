package gc.david.dfm.faq;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import gc.david.dfm.R;
import gc.david.dfm.faq.model.Faq;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * Created by david on 14.12.16.
 */
public class FAQAdapter extends RecyclerView.Adapter<FAQAdapter.FAQViewHolder> {

    private List<Faq> faqList;

    public FAQAdapter() {
        this.faqList = Collections.emptyList();
    }

    @Override
    public FAQViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View itemView = LayoutInflater.from(parent.getContext())
                                            .inflate(R.layout.view_feedback_card_item, parent, false);
        return new FAQViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(FAQViewHolder holder, int position) {
        holder.onBind(faqList.get(position));
    }

    @Override
    public int getItemCount() {
        return faqList == null ? 0 : faqList.size();
    }

    public void addAll(final Set<Faq> faqSet) {
        faqList.addAll(faqSet);
        notifyItemRangeInserted(0, faqList.size());
    }

    protected static class FAQViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.feedback_card_item_view_title_textview)
        TextView     tvTitle;
        @BindView(R.id.feedback_card_item_view_hidden_content_linearlayout)
        LinearLayout llHiddenContent;
        @BindView(R.id.feedback_card_item_view_content_textview)
        TextView     tvContent;

        FAQViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    llHiddenContent.setVisibility(llHiddenContent.isShown() ? GONE : VISIBLE);
                }
            });
        }

        void onBind(final Faq faq) {
            tvTitle.setText(faq.getQuestion());
            tvContent.setText(faq.getAnswer());
        }
    }
}
