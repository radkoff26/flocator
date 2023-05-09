package com.example.flocator.yartsev_test.fragments

import com.example.flocator.R

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId

class MainFragment {
    fun openSettings(): SettingsFragment {
        onView(withId(R.id.settings_btn)).perform(click())
        return SettingsFragment()
    }
}