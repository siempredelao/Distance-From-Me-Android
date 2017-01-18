/*
 * Copyright (c) 2017 David Aguiar Gonzalez
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

package gc.david.dfm.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.ProgressBar;

import java.util.Set;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import gc.david.dfm.DFMApplication;
import gc.david.dfm.deviceinfo.DeviceInfo;
import gc.david.dfm.deviceinfo.PackageManager;
import gc.david.dfm.R;
import gc.david.dfm.dagger.DaggerFaqComponent;
import gc.david.dfm.dagger.FaqModule;
import gc.david.dfm.dagger.RootModule;
import gc.david.dfm.faq.FAQAdapter;
import gc.david.dfm.faq.model.Faq;
import gc.david.dfm.faq.Faqs;
import gc.david.dfm.faq.FaqsPresenter;
import gc.david.dfm.faq.GetFaqsUseCase;
import gc.david.dfm.feedback.Feedback;
import gc.david.dfm.feedback.FeedbackPresenter;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static gc.david.dfm.Utils.toastIt;

public class HelpAndFeedbackActivity extends AppCompatActivity implements Faqs.View {

    @BindView(R.id.tbMain)
    protected Toolbar      toolbar;
    @BindView(R.id.help_and_feedback_activity_progressbar)
    protected ProgressBar  progressBar;
    @BindView(R.id.help_and_feedback_activity_recyclerview)
    protected RecyclerView recyclerview;

    @Inject
    PackageManager packageManager;
    @Inject
    DeviceInfo     deviceInfo;
    @Inject
    GetFaqsUseCase getFaqsUseCase;

    private Faqs.Presenter faqsPresenter;
    private FAQAdapter     faqAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_and_feedback);
        DaggerFaqComponent.builder()
                          .rootModule(new RootModule((DFMApplication) getApplication()))
                          .faqModule(new FaqModule())
                          .build()
                          .inject(this);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        faqsPresenter = new FaqsPresenter(this, getFaqsUseCase);
        faqsPresenter.start();
    }

    @OnClick(R.id.help_and_feedback_activity_send_feedback_textview)
    public void onSendFeedbackClick() {
        new FeedbackPresenter(new Feedback.View() {
            @Override
            public void showError() {
                toastIt(R.string.toast_send_feedback_error, getApplicationContext());
            }

            @Override
            public void showEmailClient(final Intent intent) {
                startActivity(intent);
            }

            @Override
            public Context context() {
                return getApplicationContext();
            }
        }, packageManager, deviceInfo).start();
    }

    @Override
    public void setPresenter(final Faqs.Presenter presenter) {
        this.faqsPresenter = presenter;
    }

    @Override
    public void showLoading() {
        progressBar.setVisibility(VISIBLE);
        recyclerview.setVisibility(GONE);
    }

    @Override
    public void hideLoading() {
        progressBar.setVisibility(GONE);
        recyclerview.setVisibility(VISIBLE);
    }

    @Override
    public void add(final Set<Faq> faqSet) {
        faqAdapter.addAll(faqSet);
    }

    @Override
    public void showError() {
        Snackbar.make(recyclerview, R.string.faq_error_message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void setupList() {
        recyclerview.setLayoutManager(new LinearLayoutManager(this));
        recyclerview.setItemAnimator(new DefaultItemAnimator());
        recyclerview.getItemAnimator().setAddDuration(1000);
        faqAdapter = new FAQAdapter();
        recyclerview.setAdapter(faqAdapter);
    }

    public static void open(final Activity activity) {
        final Intent showInfoActivityIntent = new Intent(activity, HelpAndFeedbackActivity.class);
        activity.startActivity(showInfoActivityIntent);
    }
}
