package com.example.dermamindapp.data

import android.content.Context
import android.content.SharedPreferences

class PreferencesHelper(context: Context) {

    private val PREFS_NAME = "dermamind_prefs"
    private val sharedPref: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        const val KEY_USER_NAME = "key_user_name"
        const val KEY_USER_ID = "key_user_id"
        const val KEY_USER_AGE = "key_user_age"
        const val KEY_SKIN_TYPE = "key_skin_type"
        const val KEY_PREFERENCES = "key_preferences"
        const val KEY_ONBOARDING_COMPLETED = "key_onboarding_completed"
        const val KEY_ROUTINES = "key_routines"

    }

    fun saveString(key: String, value: String) {
        val editor = sharedPref.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getString(key: String): String? {
        return sharedPref.getString(key, null)
    }

    fun saveBoolean(key: String, value: Boolean) {
        val editor = sharedPref.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return sharedPref.getBoolean(key, defaultValue)
    }

    fun clear() {
        val editor = sharedPref.edit()
        editor.clear()
        editor.apply()
    }
}