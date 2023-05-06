package com.example.flocator.gladyshev_test

import RegFirstFragmentPage
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.filters.LargeTest
import com.example.flocator.R
import com.example.flocator.authentication.registration.RegFirstFragment
import com.example.flocator.common.utils.FragmentNavigationUtils
import org.junit.Before
import org.junit.Test

@LargeTest
class RegFirstFragmentTest : BaseTest() {
    private lateinit var regFirstFragmentPage: RegFirstFragmentPage

    @Before
    fun setup() {
        regFirstFragmentPage = RegFirstFragmentPage()

        activityScenario.scenario.onActivity {
            FragmentNavigationUtils.clearAllAndOpenFragment(
                it.supportFragmentManager,
                RegFirstFragment()
            )
        }
    }

    @Test
    fun testEmptyFirstNameAndLastName() {
        regFirstFragmentPage.clickSubmitButton()
        onView(withId(R.id.registration_error_message_text)).check(matches(isDisplayed()))
        onView(withId(R.id.registration_error_message_text)).check(matches(withText("Поля не должны быть пустыми!")))
    }

    @Test
    fun testValidFirstNameAndLastName() {
        val firstName = "ivan"
        val lastName = "ivanov"

        regFirstFragmentPage.typeFirstName(firstName)
        regFirstFragmentPage.typeLastName(lastName)
        regFirstFragmentPage.clickSubmitButton()

        onView(withId(R.id.second_fragment_root)).check(matches(isDisplayed()))
    }

}