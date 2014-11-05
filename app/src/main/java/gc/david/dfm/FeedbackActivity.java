package gc.david.dfm;

import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

import butterknife.InjectView;
import butterknife.OnClick;

import static butterknife.ButterKnife.inject;
import static gc.david.dfm.Utils.showAlertDialog;
import static gc.david.dfm.Utils.toastIt;

/**
 * Created by David on 17/10/2014.
 */
public class FeedbackActivity extends ActionBarActivity {

    @InjectView(R.id.problem_type_expandablelistview)
    protected ExpandableListView expandableListView;
    @InjectView(R.id.problem_description_edittext)
    protected EditText           etDetails;
    @InjectView(R.id.problem_type_header_textview)
    protected TextView           tvQuestionTypeHeader;
    @InjectView(R.id.problem_description_header_textview)
    protected TextView           tvQuestionDescriptionHeader;
    @InjectView(R.id.send_feedback_button)
    protected Button             tvSendFeedback;

    private QuestionExpandableListAdapter questionExpandableListAdapter;
    private List<String>                  feedbackTypes;

    private final TextWatcher nonEmptyTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {
            // nothing
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
            // nothing
        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (etDetails.getText().length() != 0) {
                tvQuestionDescriptionHeader.setTextColor(getResources().getColor(R.color.item_background));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        inject(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        feedbackTypes = Arrays.asList(getResources().getStringArray(R.array.feedback_type_list));
        questionExpandableListAdapter = new QuestionExpandableListAdapter(getApplicationContext(),
                                                                          getString(R.string.problem_type_listview_title),
                                                                          feedbackTypes);
        expandableListView.setAdapter(questionExpandableListAdapter);
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView,
                                        View view,
                                        int groupCount,
                                        int childCount,
                                        long l) {
                expandableListView.collapseGroup(0);
                questionExpandableListAdapter.setGroup((String) questionExpandableListAdapter.getChild(0, childCount));
                tvQuestionTypeHeader.setTextColor(getResources().getColor(R.color.item_background));
                return true;
            }
        });

        etDetails.addTextChangedListener(nonEmptyTextWatcher);
    }

    @OnClick(R.id.send_feedback_button)
    protected void submit_query() {
        if (!validateFields()) {
            return;
        }

        final Intent sendToIntent = new Intent(Intent.ACTION_SENDTO);

        final String emailAddress = "davidaguiargonzalez@gmail.com";
        final String emailSubject = "[Distance From Me] " + getGroupName() + " feedback";
        final String emailBody = etDetails.getText().toString() + getDeviceInfo();

        final Uri uri = Uri.parse("mailto:" + emailAddress +
                                  "?subject=" + Uri.encode(emailSubject) +
                                  "&body=" + Uri.encode(emailBody));
        sendToIntent.setData(uri);

        final List<ResolveInfo> appsAbleToSendEmails = getPackageManager().queryIntentActivities(sendToIntent, 0);

        // Emulators may not like this check...
        if (!appsAbleToSendEmails.isEmpty()) {
            hideKeyboard();
            startActivity(sendToIntent);
        } else {
            // Nothing resolves send to, so fallback to send...
            final Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{emailAddress});
            intent.putExtra(Intent.EXTRA_SUBJECT, emailSubject);
            intent.putExtra(Intent.EXTRA_TEXT, emailBody);
            try {
                hideKeyboard();
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                toastIt(getText(R.string.complain_problem), getApplicationContext());
            }
        }
    }

    private boolean validateFields() {
        final boolean validateQuestionType = isOneFeedbackTypeSelected();
        final boolean validateDescription = !etDetails.getText().toString().equals("");

        focusFieldsRegardingValidation(validateQuestionType, validateDescription);

        if (!validateQuestionType) {
            showAlertDialog(null,
                            getString(R.string.no_feedback_type_dialog_title),
                            getString(R.string.no_feedback_type_dialog_message),
                            null,
                            getText(android.R.string.ok), FeedbackActivity.this);
            return false;
        } else if (!validateDescription) {
            showAlertDialog(null,
                            getString(R.string.no_feedback_description_dialog_title),
                            getString(R.string.no_feedback_description_dialog_message),
                            null,
                            getText(android.R.string.ok), FeedbackActivity.this);
            return false;
        }
        return true;
    }

    private void hideKeyboard() {
        final InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(etDetails.getWindowToken(), 0);
    }

    private void focusFieldsRegardingValidation(final boolean validateQuestionType, final boolean validateDescription) {
        if (!validateQuestionType) {
            tvQuestionTypeHeader.setTextColor(Color.RED);
        }
        if (!validateDescription) {
            tvQuestionDescriptionHeader.setTextColor(Color.RED);
        }
    }

    private String getDeviceInfo() {
        return "\n\nImportant device info for analysis:" +
               "\n\nVersion:" +
               "\nCODENAME=" + Build.VERSION.CODENAME +
               "\nINCREMENTAL=" + Build.VERSION.INCREMENTAL +
               "\nRELEASE=" + Build.VERSION.RELEASE +
               "\nSDK_INT=" + Build.VERSION.SDK_INT +
               "\n\nBuild:" +
               "\nBOARD=" + Build.BOARD +
               "\nBOOTLOADER=" + Build.BOOTLOADER +
               "\nBRAND=" + Build.BRAND +
               "\nDEVICE=" + Build.DEVICE +
               "\nDISPLAY=" + Build.DISPLAY +
               "\nFINGERPRINT=" + Build.FINGERPRINT +
               "\nHARDWARE=" + Build.HARDWARE +
               "\nHOST=" + Build.HOST +
               "\nID=" + Build.ID +
               "\nMANUFACTURER=" + Build.MANUFACTURER +
               "\nMODEL=" + Build.MODEL +
               "\nPRODUCT=" + Build.PRODUCT +
               "\nSERIAL=" + Build.SERIAL +
               "\nTAGS=" + Build.TAGS +
               "\nTIME=" + Build.TIME +
               "\nTYPE=" + Build.TYPE +
               "\nUSER=" + Build.USER +
               "\n\nDevice:" +
               "\nDENSITY=" + getResources().getDisplayMetrics().density +
               "\nDENSITY_DPI=" + getResources().getDisplayMetrics().densityDpi +
               getMemoryParameters();
    }

    private String getMemoryParameters() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            final ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
            final ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
            activityManager.getMemoryInfo(mi);
            long freeMemoryMBs = mi.availMem / 1048576L; // 1MB
            long totalMemoryMBs = mi.totalMem;
            totalMemoryMBs /= 1048576L;
            return "\nTOTALMEMORYSIZE=" + totalMemoryMBs + "MB" +
                   "\nFREEMEMORYSIZE=" + freeMemoryMBs + "MB";
        }
        return "";
    }

    private boolean isOneFeedbackTypeSelected() {
        final String groupHeader = getGroupName();
        for (final String type : feedbackTypes) {
            if (type.contains(groupHeader)) {
                return true;
            }
        }
        return false;
    }

    private String getGroupName() {
        return (String) questionExpandableListAdapter.getGroup(0);
    }
}
