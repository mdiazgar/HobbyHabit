package com.example.hobbyhabit.data

import android.content.Context

class PreferencesManager(context: Context) {

    private val prefs = context.getSharedPreferences("hobbyhabit_prefs", Context.MODE_PRIVATE)

    var onboardingComplete: Boolean
        get()      = prefs.getBoolean("onboarding_complete", false)
        set(value) = prefs.edit().putBoolean("onboarding_complete", value).apply()
}