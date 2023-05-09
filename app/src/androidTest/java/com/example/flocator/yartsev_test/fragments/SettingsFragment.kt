package com.example.flocator.yartsev_test.fragments

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.example.flocator.R

class SettingsFragment {
    fun typeNickname(nickname: String): SettingsFragment {
        onView(withId(R.id.name_field)).perform(
            typeText(nickname),
            closeSoftKeyboard()
        )
        return this
    }

    fun nicknameEquals(nickname: String) {
        onView(withId(R.id.name_field))
            .check(matches(withText(nickname)))
    }

    fun openChangePasswordFragment(): ChangePasswordFragment {
        onView(withId(R.id.change_password_line))
            .perform(click())
        return ChangePasswordFragment()
    }

    fun isChangePasswordFragmentNotOpen() {
        onView(withId(R.id.change_password_close_button))
            .check(doesNotExist())
    }
}