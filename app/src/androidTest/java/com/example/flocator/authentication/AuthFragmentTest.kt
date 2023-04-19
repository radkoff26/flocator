package com.example.flocator.authentication

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.core.app.ActivityScenario
import com.example.flocator.BaseTest
import com.example.flocator.MainActivity
import com.example.flocator.R
import com.example.flocator.authentication.authorization.AuthFragment
import com.example.flocator.authentication.getlocation.LocationRequestFragment
import com.example.flocator.authentication.pages.AuthFragmentPage
import com.example.flocator.main.ui.main.MainFragment
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthFragmentTest : BaseTest<AuthFragment>() {

    private val authFragmentPage = AuthFragmentPage()

    override fun launchFragment(): FragmentScenario<AuthFragment> {
        return launchFragmentInContainer()
    }

    @Test
    fun loginWithInvalidCredentials() {
        val invalidEmail = "invalid@mail.ru"
        val invalidPassword = "invalidpassword"

        authFragmentPage.inputEmailAndPassword(invalidEmail, invalidPassword)
        authFragmentPage.clickEntranceButton()

        authFragmentPage.checkErrorMessageDisplayed("Неверный логин или пароль")
    }

    @Test
    fun loginWithValidCredentials() {
        val validEmail = "alexander"
        val validPassword = "qwertyuiop"

        authFragmentPage.inputEmailAndPassword(validEmail, validPassword)
        authFragmentPage.clickEntranceButton()

        runBlocking {
            ActivityScenario.launch(MainActivity::class.java).onActivity {
                assertTrue(
                    it.supportFragmentManager.findFragmentById(R.id.fragment_container) is LocationRequestFragment ||
                            it.supportFragmentManager.findFragmentById(R.id.fragment_container) is MainFragment
                )
            }
        }
    }
}
