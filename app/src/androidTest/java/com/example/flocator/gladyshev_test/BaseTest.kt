package com.example.flocator.gladyshev_test

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.flocator.MainActivity
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
abstract class BaseTest {

    @get:Rule
    val activityScenario = ActivityScenarioRule(MainActivity::class.java)
}
