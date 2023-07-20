package com.stc.onecheck.modules

import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import com.stc.onecheck.R

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        val prefKeyboard: ListPreference? = findPreference(getString(R.string.txt_key_keyboard))
        prefKeyboard?.setOnPreferenceChangeListener { preference, newValue ->
            val index = prefKeyboard.findIndexOfValue(newValue.toString())
            if (index != -1) {
                println("KKKKK: ${prefKeyboard.entries[index]}")
            }
            true
        }

        val prefEffect: ListPreference? = findPreference(getString(R.string.txt_key_effect))
        prefEffect?.setOnPreferenceChangeListener { preference, newValue ->
            val index = prefEffect.findIndexOfValue(newValue.toString())
            if (index != -1) {
                println("EEEEE: ${prefEffect.entries[index]}")
            }
            true
        }

    }

    //return the password in asterisks
    private fun setAsterisks(length: Int): String {
        val sb = StringBuilder()
        for (s in 0 until length) {
            sb.append("*")
        }
        return sb.toString()
    }
}