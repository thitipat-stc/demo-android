package com.stc.scanprint.settings

import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import com.stc.scanprint.R

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        val pref: ListPreference? = findPreference(getString(R.string.txt_key_interface))
        pref?.setOnPreferenceChangeListener { preference, newValue ->
            /*val index = prefMaster.findIndexOfValue(newValue.toString())
            if (index != -1) {
                println("MMMMM: ${prefMaster.entries[index]}")
            }*/
            true
        }

    }
}