package gc.david.dfm;

import android.content.Context;

/**
 * Created by david on 06.12.16.
 */
public abstract class DeviceInfoDecorator implements DeviceInfo {

    protected final Context    context;
    protected final DeviceInfo deviceInfo;

    public DeviceInfoDecorator(final Context context, final DeviceInfo deviceInfo) {
        this.deviceInfo = deviceInfo;
        this.context = context;
    }
}
