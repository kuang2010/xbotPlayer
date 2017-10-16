package cn.iscas.xlab.xbotplayer;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by lisongting on 2017/10/16.
 */

public class SettingFragment extends PreferenceFragment {

    private static final String TAG = "SettingFragment";

    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                Log.i(TAG, "key:" + key + ",getString" + sharedPreferences.getString(key, "default"));
            }
        };
        addPreferencesFromResource(R.xml.pref_settings);
    }
}
