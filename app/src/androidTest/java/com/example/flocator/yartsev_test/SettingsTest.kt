package com.example.flocator.yartsev_test

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.rule.GrantPermissionRule
import com.example.flocator.MainActivity
import com.example.flocator.authentication.authorization.AuthFragment
import com.example.flocator.common.storage.SharedStorage
import com.example.flocator.common.utils.FragmentNavigationUtils
import com.example.flocator.di.SharedStorageModule
import com.example.flocator.yartsev_test.fragments.AuthorizationFragment
import com.example.flocator.yartsev_test.fragments.MainFragment
import com.example.flocator.yartsev_test.models.Credentials
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.*

class SettingsTest {
    companion object {
        val random = Random()

        fun generateStringInRange(minLength: Int, maxLength: Int): String {
            val alphabet = ('a'..'z') + ('A'..'Z') + ('0'..'9') // список символов
            val length = minLength + random.nextInt(maxLength - minLength + 1) // случайная длина строки в заданном диапазоне
            return List(length) { alphabet.random() }.joinToString("") // создание случайной строки
        }
    }


    @get:Rule
    val activityScenario = ActivityScenarioRule(MainActivity::class.java)
    @get:Rule
    val permissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION)

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

        val authorizationFragment = AuthorizationFragment()

        authorizationFragment.logInWith(Credentials("alexander", "qwertyuiop"))
    }

    @Test
    fun changeNickname() {
        val randomNickname = generateStringInRange(10, 20)

        MainFragment().openSettings()
            .typeNickname(randomNickname)
            .nicknameEquals(randomNickname)
    }

    @Test
    fun openCloseChangePasswordFragment() {
        MainFragment().openSettings()
            .openChangePasswordFragment()
            .close()
            .isChangePasswordFragmentNotOpen()

    }
}