package com.example.flocator.authentication.pages

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import com.example.flocator.R

class AuthFragmentPage {

    fun inputEmailAndPassword(email: String, password: String) {
        onView(withId(R.id.email_login_field_edit)).perform(clearText(), typeText(email), closeSoftKeyboard())
        onView(withId(R.id.password_login_field_edit)).perform(clearText(), typeText(password), closeSoftKeyboard())
    }

    fun clickEntranceButton() {
        onView(withId(R.id.entrance_btn)).perform(click())
    }

    fun checkErrorMessageDisplayed(errorMessage: String) {
        onView(withId(R.id.login_error_message_text))
            .check(matches(isDisplayed()))
            .check(matches(withText(errorMessage)))
    }
}
