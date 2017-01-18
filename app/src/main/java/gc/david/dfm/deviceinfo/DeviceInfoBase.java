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

package gc.david.dfm.deviceinfo;

import android.content.Context;
import android.os.Build;

import java.util.Locale;

/**
 * Created by david on 06.12.16.
 */
public class DeviceInfoBase implements DeviceInfo {

    private final Context        context;
    private final PackageManager packageManager;

    public DeviceInfoBase(final Context context, final PackageManager packageManager) {
        this.context = context;
        this.packageManager = packageManager;
    }

    @Override
    public String getDeviceInfo() {
        return String.format(Locale.getDefault(),
                             "Important device info for analysis:\n\nVersion:\nNAME=%s\nRELEASE=%s\nSDK_INT=%d\n\nDevice:\nMANUFACTURER=%s\nBRAND=%s\nMODEL=%s\nDEVICE=%s\nPRODUCT=%s\nDENSITY_DPI=%d\n\nOther:\nBOARD=%s\nBOOTLOADER=%s\nDISPLAY=%s\nFINGERPRINT=%s\nHARDWARE=%s\nHOST=%s\nID=%s\nTAGS=%s\nTIME=%d\nTYPE=%s\nUSER=%s",
                             packageManager.getVersionName(),
                             Build.VERSION.RELEASE,
                             Build.VERSION.SDK_INT,
                             Build.MANUFACTURER,
                             Build.BRAND,
                             Build.MODEL,
                             Build.DEVICE,
                             Build.PRODUCT,
                             context.getResources().getDisplayMetrics().densityDpi,
                             Build.BOARD,
                             Build.BOOTLOADER,
                             Build.DISPLAY,
                             Build.FINGERPRINT,
                             Build.HARDWARE,
                             Build.HOST,
                             Build.ID,
                             Build.TAGS,
                             Build.TIME,
                             Build.TYPE,
                             Build.USER);
    }
}
