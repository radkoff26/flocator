package com.example.flocator.yartsev_test.fragments

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.example.flocator.R

class ChangePasswordFragment {
    fun close(): SettingsFragment {
        onView(withId(R.id.change_password_close_button))
            .perform(click())
        return SettingsFragment()
    }
}