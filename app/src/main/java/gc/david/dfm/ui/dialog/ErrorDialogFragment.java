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

package gc.david.dfm.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import gc.david.dfm.logger.DFMLogger;

/**
 * Created by David on 13/10/2014.
 */
public class ErrorDialogFragment extends DialogFragment {

    private static final String TAG = ErrorDialogFragment.class.getSimpleName();

    private Dialog dialog;

    public ErrorDialogFragment() {
        super();

        dialog = null;
    }

    public void setDialog(final Dialog dialog) {
        DFMLogger.logMessage(TAG, "setDialog");

        this.dialog = dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        DFMLogger.logMessage(TAG, "onCreateDialog");

        return dialog;
    }
}
