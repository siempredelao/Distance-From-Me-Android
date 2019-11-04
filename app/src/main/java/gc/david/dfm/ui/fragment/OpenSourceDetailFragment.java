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

package gc.david.dfm.ui.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import gc.david.dfm.R;
import gc.david.dfm.opensource.presentation.LicensePrinter;
import gc.david.dfm.opensource.presentation.model.OpenSourceLibraryModel;

/**
 * Created by david on 24.01.17.
 */
public class OpenSourceDetailFragment extends Fragment {

    public static final String LIBRARY_KEY = "opensourcedetailfragment.library.key";

    @BindView(R.id.opensourcelibrary_detail_fragment_name_textview)
    protected TextView tvName;
    @BindView(R.id.opensourcelibrary_detail_fragment_long_license_textview)
    protected TextView tvLongLicense;

    private OpenSourceLibraryModel openSourceLibraryModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (getArguments() != null) {
            openSourceLibraryModel = getArguments().getParcelable(LIBRARY_KEY);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_opensourcelibrary_detail, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_opensource, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_open_license_browser:
                final Intent openBrowserIntent = new Intent(Intent.ACTION_VIEW,
                                                            Uri.parse(openSourceLibraryModel.getLink()));
                startActivity(openBrowserIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        tvName.setText(openSourceLibraryModel.getName());
        tvLongLicense.setText(LicensePrinter.print(openSourceLibraryModel, getContext()));
        tvLongLicense.setMovementMethod(ScrollingMovementMethod.getInstance());
    }
}
