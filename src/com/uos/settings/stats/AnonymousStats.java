/*
 * Copyright (C) 2017 The UOS Open Source Project
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

package com.uos.settings.stats;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.os.UserHandle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v14.preference.SwitchPreference;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import android.provider.Settings;
import com.android.internal.logging.MetricsProto.MetricsEvent;

public class AnonymousStats extends SettingsPreferenceFragment {

    private static final String PREF_FILE_NAME = "UOSStats";
    /* package */ static final String ANONYMOUS_OPT_IN = "pref_anonymous_opt_in";
    /* package */ static final String ANONYMOUS_LAST_CHECKED = "pref_anonymous_checked_in";

    /* package */ static final String KEY_LAST_JOB_ID = "last_job_id";

    public static final String KEY_STATS = "stats_collection";

    SwitchPreference mStatsSwitch;

    public static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(PREF_FILE_NAME, 0);
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.UOS_SETTINGS;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.anonymous_stats);
        mStatsSwitch = (SwitchPreference) findPreference(KEY_STATS);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mStatsSwitch) {
            boolean checked = mStatsSwitch.isChecked();
            if (checked) {
                // clear opt out flags
                Settings.Secure.putIntForUser(getContentResolver(),
                        Settings.Secure.UOS_STATS_RECORDED, 0, UserHandle.USER_OWNER);
            }
            // will initiate opt out sequence if necessary
            ReportingServiceManager.setAlarm(getActivity());
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

    public static void updateLastSynced(Context context) {
        getPreferences(context)
                .edit()
                .putLong(ANONYMOUS_LAST_CHECKED,System.currentTimeMillis())
                .commit();
    }

    private static int getLastJobId(Context context) {
        return 1;
    }

    private static void setLastJobId(Context context, int id) {
        getPreferences(context)
                .edit()
                .putInt(KEY_LAST_JOB_ID, id)
                .commit();
    }

    public static int getNextJobId(Context context) {
        return 1;
    }
}
