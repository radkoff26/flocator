package com.example.flocator.radko_test.fragments

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import com.example.flocator.R
import com.example.flocator.radko_test.data.TestUserCredentials

class TestAuthFragment {
    // Starts new MainFragment
    fun login(): TestMainFragment {
        Espresso.onView(ViewMatchers.withId(R.id.email_login_field_edit))
            .perform(ViewActions.click(), ViewActions.typeText(TestUserCredentials.login))
        Espresso.onView(ViewMatchers.withId(R.id.password_login_field_edit)).perform(
            ViewActions.click(),
            ViewActions.typeText(TestUserCredentials.password),
            ViewActions.closeSoftKeyboard()
        )
        Espresso.onView(ViewMatchers.withId(R.id.entrance_btn)).perform(ViewActions.click())
        return TestMainFragment()
    }
}