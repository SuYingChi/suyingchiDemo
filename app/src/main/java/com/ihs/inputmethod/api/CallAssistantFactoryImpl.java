package com.ihs.inputmethod.api;

import com.acb.call.customize.AcbCallFactoryImpl;
import com.acb.call.views.CallIdleAlert;
import com.acb.utils.MessageCenterUtils;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.settings.activities.SettingsActivity;


public class CallAssistantFactoryImpl extends AcbCallFactoryImpl {

    @Override
    public MessageCenterUtils.Config getSMSConfig() {

        return new MessageCenterUtils.Config() {
            @Override
            public String getAdPlacement() {
                return HSApplication.getContext().getResources().getString(R.string.ad_placement_call_assist);
            }

            @Override
            public boolean enable() {
                return true;
            }

            @Override
            public boolean hideNotificationGuide() {
                return true;
            }

            @Override
            public int getAppNameDrawable() {
                return R.drawable.ic_charging_screen_logo;
            }
        };
    }

    @Override
    public boolean isCallAssistantOpenDefault() {
        return HSConfig.optBoolean(false, "Application", "ScreenFlash", "DefaultSwitch");
    }

    @Override
    public boolean isSMSAssistantOpenDefault() {
        return true;
    }

    @Override
    public CallIdleAlert.Config getCallIdleConfig() {
        return new CPCallIdleConfig();
    }

    private static class CPCallIdleConfig extends CallIdleAlert.PlistConfig {
        @Override
        public String getAdPlaceName() {
            return HSApplication.getContext().getResources().getString(R.string.ad_placement_call_assist);
        }

        @Override
        public int getAppNameDrawable() {
            return R.drawable.ic_charging_screen_logo;
        }
    }

    @Override
    public boolean isFeatureRestrict() {
        return  !HSPreferenceHelper.getDefault().getBoolean(SettingsActivity.CALL_ASSISTANT_HAS_SWITCHED_ON, false);
    }
}
