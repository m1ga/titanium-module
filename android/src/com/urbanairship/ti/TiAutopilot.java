/* Copyright Urban Airship and Contributors */

package com.urbanairship.ti;

import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiProperties;

import android.content.Context;
import android.graphics.Color;

import com.urbanairship.AirshipConfigOptions;
import com.urbanairship.Autopilot;
import com.urbanairship.UAirship;
import com.urbanairship.push.notifications.DefaultNotificationFactory;
import com.urbanairship.util.UAStringUtil;
import com.urbanairship.actions.DeepLinkAction;
import com.urbanairship.actions.ActionResult;
import com.urbanairship.actions.ActionArguments;

public class TiAutopilot extends Autopilot {

    static final String PRODUCTION_KEY = "com.urbanairship.production_app_key";
    static final String PRODUCTION_SECRET = "com.urbanairship.production_app_secret";
    static final String DEVELOPMENT_KEY = "com.urbanairship.development_app_key";
    static final String DEVELOPMENT_SECRET = "com.urbanairship.development_app_secret";
    static final String IN_PRODUCTION = "com.urbanairship.in_production";
    static final String GCM_SENDER = "com.urbanairship.gcm_sender";
    static final String NOTIFICATION_ICON = "com.urbanairship.notification_icon";
    static final String NOTIFICATION_ACCENT_COLOR = "com.urbanairship.notification_accent_color";

    private static final String TAG = "UrbanAirshipModule";

    @Override
    public void onAirshipReady(UAirship airship) {
        Log.i(TAG, "Airship ready");
        airship.getActionRegistry().getEntry(DeepLinkAction.DEFAULT_REGISTRY_NAME).setDefaultAction(new DeepLinkAction() {
            @Override
            public ActionResult perform(ActionArguments arguments) {
                String deepLink = arguments.getValue().getString();
                if (deepLink != null) {
                	UrbanAirshipModule.deepLinkReceived(deepLink);
                }
                return ActionResult.newResult(arguments.getValue());
            }
        });

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
                .setGcmSender(properties.getString(GCM_SENDER, ""));


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
            int id  = context.getResources().getIdentifier(notificationIconName, "drawable", context.getPackageName());
            if (id > 0) {
                options.setNotificationIcon(id);
            } else {
                Log.e(TAG, "Unable to find notification icon with name: " + notificationIconName);
            }
        }

        return options.build();
    }
}
