package com.example.flocator.radko_test

import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import com.example.flocator.MainActivity
import com.example.flocator.R
import com.example.flocator.authentication.authorization.AuthFragment
import com.example.flocator.common.storage.SharedStorage
import com.example.flocator.common.utils.FragmentNavigationUtils
import com.example.flocator.di.SharedStorageModule
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class AuthFragmentTest {
    @get:Rule
    val activityScenario = ActivityScenarioRule(MainActivity::class.java)

    @get:Rule
    var permissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION)

    companion object {
        const val LOGIN = "radkoff"
        const val PASSWORD = "qwertyuiop"
    }

    @Before
    fun adjust() {
        val storage = SharedStorage(
            ApplicationProvider.getApplicationContext(),
            SharedStorageModule.encryptedSharedPreferences(ApplicationProvider.getApplicationContext())
        )
        storage.clearUserData()
        activityScenario.scenario.onActivity {
            FragmentNavigationUtils.clearAllAndOpenFragment(
                it.supportFragmentManager,
                AuthFragment()
            )
        }
    }

    @Test
    fun testLogin() {
        onView(withId(R.id.email_login_field_edit)).perform(click(), typeText(LOGIN))
        onView(withId(R.id.password_login_field_edit)).perform(
            click(),
            typeText(PASSWORD),
            closeSoftKeyboard()
        )
        onView(withId(R.id.entrance_btn)).perform(click())
        onView(withId(R.id.open_add_mark_fragment)).check(matches(isDisplayed()))
    }
}
