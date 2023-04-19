package com.example.flocator

import android.app.Activity
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.FragmentScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
abstract class BaseTest<T : Fragment> {

    private lateinit var scenario: FragmentScenario<T>
    private lateinit var activity: Activity

    @Before
    fun setUp() {
        scenario = launchFragment()
        scenario.onFragment { fragment ->
            activity = fragment.requireActivity()
        }
    }

    abstract fun launchFragment(): FragmentScenario<T>

    protected fun onViewId(@IdRes id: Int): ViewInteraction = onView(withId(id))
}

