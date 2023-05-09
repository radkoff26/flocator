package com.example.flocator.yartsev_test.fragments

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.example.flocator.R
import com.example.flocator.yartsev_test.models.Credentials

class AuthorizationFragment {
    fun logInWith(credentials: Credentials): MainFragment {
        onView(withId(R.id.email_login_field_edit))
            .perform(click(), typeText(credentials.login))
        onView(withId(R.id.password_login_field_edit)).perform(
            click(),
            typeText(credentials.password),
            closeSoftKeyboard()
        )
        onView(withId(R.id.entrance_btn)).perform(click())
        return MainFragment()
    }
}
