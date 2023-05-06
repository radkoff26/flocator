package com.example.flocator.radko_test.fragments

import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import com.example.flocator.R

class TestMainFragment {

    fun checkTextOnButton() {
        Espresso.onView(ViewMatchers.withId(R.id.open_add_mark_fragment))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            .check(ViewAssertions.matches(ViewMatchers.withText("Добавить метку")))
    }
}