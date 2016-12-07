package gc.david.dfm;

import android.app.ActivityManager;
import android.content.Context;
import android.support.annotation.RequiresApi;

import java.util.Locale;

import static android.content.Context.ACTIVITY_SERVICE;
import static android.os.Build.VERSION_CODES.JELLY_BEAN;

/**
 * Created by david on 07.12.16.
 */
public class DeviceInfoApi16Decorator extends DeviceInfoDecorator {

    public DeviceInfoApi16Decorator(final Context context, final DeviceInfo deviceInfo) {
        super(context, deviceInfo);
    }

    @RequiresApi(api = JELLY_BEAN)
    @Override
    public String getDeviceInfo() {
        return deviceInfo.getDeviceInfo() + "\n" + getMemoryParameters();
    }

    @RequiresApi(api = JELLY_BEAN)
    private String getMemoryParameters() {
        final ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        final ActivityManager activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(memoryInfo);
        final long freeMemoryMBs = memoryInfo.availMem / 1048576L; // 1MB
        final long totalMemoryMBs = memoryInfo.totalMem / 1048576L; // 1MB

        return String.format(Locale.getDefault(),
                             "TOTALMEMORYSIZE=%dMB\nFREEMEMORYSIZE=%dMB",
                             totalMemoryMBs,
                             freeMemoryMBs);
    }
}
