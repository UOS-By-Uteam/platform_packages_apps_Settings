/**
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.notification;

import android.app.admin.DevicePolicyManager;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.TwoStatePreference;
import android.util.Log;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.R;
import com.android.settings.RestrictedListPreference.RestrictedItem;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settingslib.RestrictedLockUtils;

import java.util.ArrayList;

import static android.app.admin.DevicePolicyManager.KEYGUARD_DISABLE_SECURE_NOTIFICATIONS;
import static android.app.admin.DevicePolicyManager.KEYGUARD_DISABLE_UNREDACTED_NOTIFICATIONS;
import static com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;

public class ConfigureNotificationSettings extends SettingsPreferenceFragment {
    private static final String TAG = "ConfigNotiSettings";

    private static final String KEY_NOTIFICATION_PULSE = "notification_pulse";

    private final SettingsObserver mSettingsObserver = new SettingsObserver();

    private Context mContext;

    private TwoStatePreference mNotificationPulse;

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.CONFIGURE_NOTIFICATION;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        addPreferencesFromResource(R.xml.configure_notification_settings);
        initPulse();
    }

    @Override
    public void onResume() {
        super.onResume();
        mSettingsObserver.register(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        mSettingsObserver.register(false);
    }

    // === Pulse notification light ===

    private void initPulse() {
        final NotificationManager nm = mContext.getSystemService(NotificationManager.class);
        final PreferenceCategory category = (PreferenceCategory) findPreference("lights");
        mNotificationPulse =
                (TwoStatePreference) category.findPreference(KEY_NOTIFICATION_PULSE);
        if (mNotificationPulse == null) {
            Log.i(TAG, "Preference not found: " + KEY_NOTIFICATION_PULSE);
            return;
        }
        if (!getResources()
                .getBoolean(com.android.internal.R.bool.config_intrusiveNotificationLed) ||
                !nm.doLightsSupport(NotificationManager.LIGHTS_PULSATING_LED)) {
            category.removePreference(mNotificationPulse);
        } else {
            updatePulse();
            mNotificationPulse.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    final boolean val = (Boolean)newValue;
                    return Settings.System.putInt(getContentResolver(),
                            Settings.System.NOTIFICATION_LIGHT_PULSE,
                            val ? 1 : 0);
                }
            });
        }
    }

    private void updatePulse() {
        if (mNotificationPulse == null) {
            return;
        }
        try {
            mNotificationPulse.setChecked(Settings.System.getInt(getContentResolver(),
                    Settings.System.NOTIFICATION_LIGHT_PULSE) == 1);
        } catch (Settings.SettingNotFoundException snfe) {
            Log.e(TAG, Settings.System.NOTIFICATION_LIGHT_PULSE + " not found");
        }
    }

    // === Callbacks ===

    private final class SettingsObserver extends ContentObserver {
        private final Uri NOTIFICATION_LIGHT_PULSE_URI =
                Settings.System.getUriFor(Settings.System.NOTIFICATION_LIGHT_PULSE);

        public SettingsObserver() {
            super(new Handler());
        }

        public void register(boolean register) {
            final ContentResolver cr = getContentResolver();
            if (register) {
                cr.registerContentObserver(NOTIFICATION_LIGHT_PULSE_URI, false, this);
            } else {
                cr.unregisterContentObserver(this);
            }
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            if (NOTIFICATION_LIGHT_PULSE_URI.equals(uri)) {
                updatePulse();
            }
        }
    }
}
