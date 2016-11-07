package gc.david.dfm.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by david on 07.11.16.
 */
public class OnboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent openMainActivityIntent = new Intent(this, MainActivity.class);
        startActivity(openMainActivityIntent);
        finish();
    }

}
