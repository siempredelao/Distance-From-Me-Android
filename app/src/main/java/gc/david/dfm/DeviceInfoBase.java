package gc.david.dfm;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

/**
 * Created by david on 06.12.16.
 */
public class DeviceInfoBase implements DeviceInfo {

    private final Context context;

    public DeviceInfoBase(final Context context) {
        this.context = context;
    }

    @Override
    public String getDeviceInfo() {
        String appVersionName;
        try {
            appVersionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            appVersionName = "Not found";
        }
        return "Important device info for analysis:" +
               "\n\nVersion:" +
               "\nNAME=" + appVersionName +
               "\nRELEASE=" + Build.VERSION.RELEASE +
               "\nSDK_INT=" + Build.VERSION.SDK_INT +
               "\n\nDevice:" +
               "\nMANUFACTURER=" + Build.MANUFACTURER +
               "\nBRAND=" + Build.BRAND +
               "\nMODEL=" + Build.MODEL +
               "\nDEVICE=" + Build.DEVICE +
               "\nPRODUCT=" + Build.PRODUCT +
               "\nDENSITY_DPI=" + context.getResources().getDisplayMetrics().densityDpi +
               "\n\nOther:" +
               "\nBOARD=" + Build.BOARD +
               "\nBOOTLOADER=" + Build.BOOTLOADER +
               "\nDISPLAY=" + Build.DISPLAY +
               "\nFINGERPRINT=" + Build.FINGERPRINT +
               "\nHARDWARE=" + Build.HARDWARE +
               "\nHOST=" + Build.HOST +
               "\nID=" + Build.ID +
               "\nTAGS=" + Build.TAGS +
               "\nTIME=" + Build.TIME +
               "\nTYPE=" + Build.TYPE +
               "\nUSER=" + Build.USER;

    }
}
