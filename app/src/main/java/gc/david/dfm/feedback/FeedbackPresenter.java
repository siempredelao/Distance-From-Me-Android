package gc.david.dfm.feedback;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;

import gc.david.dfm.DeviceInfo;
import gc.david.dfm.PackageManager;
import gc.david.dfm.R;
import gc.david.dfm.logger.DFMLogger;

/**
 * Created by david on 07.12.16.
 */
public class FeedbackPresenter implements Feedback.Presenter {

    private final Feedback.View  feedbackView;
    private final PackageManager packageManager;
    private final DeviceInfo     deviceInfo;

    public FeedbackPresenter(final Feedback.View feedbackView,
                             final PackageManager packageManager,
                             final DeviceInfo deviceInfo) {
        this.feedbackView = feedbackView;
        this.packageManager = packageManager;
        this.deviceInfo = deviceInfo;
    }

    @Override
    public void start() {
        final Intent mailtoIntent = new Intent(Intent.ACTION_SENDTO);

        final String emailAddress = "davidaguiargonzalez@gmail.com";
        final String emailSubject = feedbackView.context().getString(R.string.feedback_email_subject);
        final String emailBody = feedbackView.context()
                                             .getString(R.string.feedback_device_info_comments,
                                                        deviceInfo.getDeviceInfo());

        final Uri uri = Uri.parse(String.format("mailto:%s?subject=%s&body=%s",
                                                emailAddress,
                                                Uri.encode(emailSubject),
                                                Uri.encode(emailBody)));
        mailtoIntent.setData(uri);

        final boolean existsAnyActivityForIntent = packageManager.isThereAnyActivityForIntent(mailtoIntent);

        // Emulators may not like this check...
        if (existsAnyActivityForIntent) {
            feedbackView.showEmailClient(mailtoIntent);
        } else {
            // Nothing resolves mailto, so fallback to send...
            final Intent sendtoIntent = new Intent(Intent.ACTION_SENDTO);
            sendtoIntent.setType("text/plain");
            sendtoIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{emailAddress});
            sendtoIntent.putExtra(Intent.EXTRA_SUBJECT, emailSubject);
            sendtoIntent.putExtra(Intent.EXTRA_TEXT, emailBody);
            try {
                feedbackView.showEmailClient(sendtoIntent);
            } catch (ActivityNotFoundException e) {
                DFMLogger.logException(e);
                feedbackView.showError();
            }
        }
    }
}
