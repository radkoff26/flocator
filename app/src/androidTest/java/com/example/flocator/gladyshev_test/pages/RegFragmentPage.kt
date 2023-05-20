package com.example.flocator.gladyshev_test.pages

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.matcher.ViewMatchers.withId

class RegFragmentPage {
    fun enterTextAndCloseKeyboard(fieldId: Int, text: String): RegFragmentPage {
        onView(withId(fieldId)).perform(
            click(),
            typeText(text),
            closeSoftKeyboard()
        )
        return this
    }

    fun clickButton(buttonId: Int): RegFragmentPage {
        onView(withId(buttonId)).perform(click())
        return this
    }
}
