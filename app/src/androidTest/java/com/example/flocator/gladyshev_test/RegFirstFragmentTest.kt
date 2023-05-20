package com.example.flocator.gladyshev_test

import com.example.flocator.gladyshev_test.pages.RegFragmentPage
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
    private lateinit var regFirstFragmentPage: RegFragmentPage

    companion object {
        private const val EMPTY_FIELDS_ERROR = "Поля не должны быть пустыми!"
    }

    @Before
    fun setup() {
        regFirstFragmentPage = RegFragmentPage()

        activityScenario.scenario.onActivity {
            FragmentNavigationUtils.clearAllAndOpenFragment(
                it.supportFragmentManager,
                RegFirstFragment()
            )
        }

        onView(withId(R.id.submit_btn)).check(matches(isDisplayed()))
    }

    @Test
    fun testEmptyFirstNameAndLastName() {
        regFirstFragmentPage.clickButton(R.id.submit_btn)
        onView(withId(R.id.registration_error_message_text))
            .check(matches(isDisplayed()))
            .check(matches(withText(EMPTY_FIELDS_ERROR)))
    }

    @Test
    fun testValidFirstNameAndLastName() {
        val firstName = "ivan"
        val lastName = "ivanov"

        regFirstFragmentPage
            .enterTextAndCloseKeyboard(R.id.first_input_edit_field, firstName)
            .enterTextAndCloseKeyboard(R.id.second_input_edit_field, lastName)
            .clickButton(R.id.submit_btn)

        onView(withId(R.id.second_fragment_root)).check(matches(isDisplayed()))
    }
}