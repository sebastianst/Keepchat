package com.sturmen.xposed.keepchat;

/**
 * Copyright (C) 2013 Nick Tinsley (00sturm@gmail.com), Sebastian Stammler (stammler@cantab.net)
 * Created on 8/9/13.
 * <p/>
 * This file is part of Keepchat.
 * <p/>
 * Keepchat is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * Keepchat is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * a gazillion times.  If not, see <http://www.gnu.org/licenses/>.
 */

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

public class SettingsActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null)
            getFragmentManager().beginTransaction().replace(android.R.id.content, new PrefFragment()).commit();
    }

    public static class PrefFragment extends PreferenceFragment
            implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);

            getPreferenceManager().setSharedPreferencesMode(MODE_WORLD_READABLE);
            addPreferencesFromResource(R.xml.preferences);
            populateSaveLocation();
            updateListSummary("pref_saveLocation");
            updateListSummary("pref_imageSaving");
            updateListSummary("pref_videoSaving");
            updateListSummary("pref_toast");
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }

        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals("pref_saveLocation") || key.equals("pref_imageSaving") || key.equals("pref_videoSaving") || key.equals("pref_toast")) {
                updateListSummary(key);
            }
        }

        private void updateListSummary(String key) {
            ListPreference savePref = (ListPreference) findPreference(key);
            // Set summary to be the user-description for the selected value
            savePref.setSummary(savePref.getEntry());
        }

        private void populateSaveLocation() {
            ListPreference pref = (ListPreference) findPreference("pref_saveLocation");


            List<String> entries = new ArrayList<String>();
            List<String> entryValues = new ArrayList<String>();

            entries.add("Default (" + Environment.getExternalStorageDirectory().toString() + ")");
            entryValues.add("");

            String rootDir = Environment.getExternalStorageDirectory().toString();
            String[] files = getStorageDirectories();
            for (String file : files) {
                if (!file.equals(rootDir)) {
                    entries.add(file);
                    entryValues.add(file);
                }
            }
            CharSequence[] xEntries = entries.toArray(new CharSequence[entries.size()]);
            CharSequence[] xEntryList = entryValues.toArray(new CharSequence[entryValues.size()]);
            pref.setEntries(xEntries);
            pref.setEntryValues(xEntryList);
        }

        private static String[] getStorageDirectories()
        {
            String[] dirs = null;
            BufferedReader bufReader = null;
            try {
                bufReader = new BufferedReader(new FileReader("/proc/mounts"));
                ArrayList list = new ArrayList();
                String line;
                while ((line = bufReader.readLine()) != null) {
                    if (line.contains("vfat") || line.contains("/mnt")) {
                        StringTokenizer tokens = new StringTokenizer(line, " ");
                        String s = tokens.nextToken();
                        s = tokens.nextToken(); // Take the second token, i.e. mount point

                        if (s.equals(Environment.getExternalStorageDirectory().getPath())) {
                            list.add(s);
                        }
                        else if (line.contains("/dev/block/vold")) {
                            if (!line.contains("/mnt/secure") && !line.contains("/mnt/asec") && !line.contains("/mnt/obb") && !line.contains("/dev/mapper") && !line.contains("tmpfs")) {
                                list.add(s);
                            }
                        }
                    }
                }

                dirs = new String[list.size()];
                for (int i = 0; i < list.size(); i++) {
                    dirs[i] = (list.get(i).toString());
                }
            }
            catch (FileNotFoundException e) {}
            catch (IOException e) {}
            finally {
                if (bufReader != null) {
                    try {
                        bufReader.close();
                    }
                    catch (IOException e) {}
                }

                return dirs;
            }
        }
    }
}