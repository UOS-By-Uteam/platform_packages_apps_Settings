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
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.MetricsProto.MetricsEvent;

public class PreviewData extends SettingsPreferenceFragment {
    private static final String UNIQUE_ID = "preview_id";
    private static final String DEVICE = "preview_device";
    private static final String VERSION = "preview_version";
    private static final String COUNTRY = "preview_country";
    private static final String CARRIER = "preview_carrier";
    private static final String ROM_NAME = "preview_rom_name";
    private static final String ROM_VERSION = "preview_rom_version";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preview_data);

        final PreferenceScreen prefSet = getPreferenceScreen();
        final Context context = getActivity();

        prefSet.findPreference(UNIQUE_ID).setSummary(Utilities.getUniqueID(context));
        prefSet.findPreference(DEVICE).setSummary(Utilities.getDevice());
        prefSet.findPreference(VERSION).setSummary(Utilities.getModVersion());
        prefSet.findPreference(COUNTRY).setSummary(Utilities.getCountryCode(context));
        prefSet.findPreference(CARRIER).setSummary(Utilities.getCarrier(context));
        prefSet.findPreference(ROM_NAME).setSummary(Utilities.getROM());
        prefSet.findPreference(ROM_VERSION).setSummary(Utilities.getUOSVersion());
    }


    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.UOS_SETTINGS;
    }
}
