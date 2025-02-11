/* Copyright Airship and Contributors */

package ti.airship;

import android.content.Context;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.urbanairship.AirshipConfigOptions;
import com.urbanairship.Autopilot;
import com.urbanairship.UAirship;
import com.urbanairship.channel.AirshipChannelListener;
import com.urbanairship.push.NotificationActionButtonInfo;
import com.urbanairship.push.NotificationInfo;
import com.urbanairship.push.NotificationListener;
import com.urbanairship.util.UAStringUtil;

import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiProperties;

import ti.airship.events.ChannelRegistrationEvent;
import ti.airship.events.DeepLinkEvent;
import ti.airship.events.EventEmitter;
import ti.airship.events.PushReceivedEvent;

public class TiAutopilot extends Autopilot {

    static final String PRODUCTION_KEY = "com.urbanairship.production_app_key";
    static final String PRODUCTION_SECRET = "com.urbanairship.production_app_secret";
    static final String DEVELOPMENT_KEY = "com.urbanairship.development_app_key";
    static final String DEVELOPMENT_SECRET = "com.urbanairship.development_app_secret";
    static final String IN_PRODUCTION = "com.urbanairship.in_production";
    static final String GCM_SENDER = "com.urbanairship.gcm_sender";
    static final String NOTIFICATION_ICON = "com.urbanairship.notification_icon";
    static final String NOTIFICATION_ACCENT_COLOR = "com.urbanairship.notification_accent_color";
    static final String DATA_COLLECTION_OPT_IN = "com.urbanairship.data_collection_opt_in_enabled";
    static final String CLOUD_SITE = "com.urbanairship.site";

    private static final String TAG = "UrbanAirshipModule";

    @Override
    public void onAirshipReady(UAirship airship) {
        Log.i(TAG, "Airship ready");
        TiAirship.shared().onAirshipReady(airship);
    }

    @Override
    public boolean allowEarlyTakeOff(Context context) {
        return TiApplication.getInstance().getAppProperties() != null;
    }

    @Override
    public AirshipConfigOptions createAirshipConfigOptions(Context context) {
        TiProperties properties = TiApplication.getInstance().getAppProperties();

        AirshipConfigOptions.Builder options = new AirshipConfigOptions.Builder()
                .setDevelopmentAppKey(properties.getString(DEVELOPMENT_KEY, ""))
                .setDevelopmentAppSecret(properties.getString(DEVELOPMENT_SECRET, ""))
                .setProductionAppKey(properties.getString(PRODUCTION_KEY, ""))
                .setProductionAppSecret(properties.getString(PRODUCTION_SECRET, ""))
                .setInProduction(properties.getBool(IN_PRODUCTION, false))
                .setDataCollectionOptInEnabled(properties.getBool(DATA_COLLECTION_OPT_IN, false))
                .setSite(parseCloudSite(properties.getString(CLOUD_SITE, null)))
                .setUrlAllowListScopeOpenUrl(new String[]{"*"});


        // Accent color
        String accentColor = properties.getString(NOTIFICATION_ACCENT_COLOR, null);
        if (!UAStringUtil.isEmpty(accentColor)) {
            try {
                options.setNotificationAccentColor(Color.parseColor(accentColor));
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Unable to parse notification accent color: " + accentColor, e);
            }
        }

        // Notification icon
        String notificationIconName = properties.getString(NOTIFICATION_ICON, null);
        if (!UAStringUtil.isEmpty(notificationIconName)) {
            int id = context.getResources().getIdentifier(notificationIconName, "drawable", context.getPackageName());
            if (id > 0) {
                options.setNotificationIcon(id);
            } else {
                Log.e(TAG, "Unable to find notification icon with name: " + notificationIconName);
            }
        }

        return options.build();
    }

    @NonNull
    @AirshipConfigOptions.Site
    private static String parseCloudSite(@Nullable String value) {
        if (AirshipConfigOptions.SITE_EU.equalsIgnoreCase(value)) {
            return AirshipConfigOptions.SITE_EU;
        }
        return AirshipConfigOptions.SITE_US;
    }
}
