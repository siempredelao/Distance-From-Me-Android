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

package gc.david.dfm;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;

import java.util.List;

/**
 * Created by david on 07.12.16.
 */
public class DefaultPackageManager implements PackageManager {

    private final android.content.pm.PackageManager packageManager;
    private final Context                           context;

    public DefaultPackageManager(final Context context) {
        this.packageManager = context.getPackageManager();
        this.context = context;
    }

    @Override
    public String getVersionName() {
        try {
            return packageManager.getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (android.content.pm.PackageManager.NameNotFoundException e) {
            return "Version name not found";
        }
    }

    @Override
    public boolean isThereAnyActivityForIntent(final Intent intent) {
        final List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(intent, 0);
        return !resolveInfos.isEmpty();
    }
}
