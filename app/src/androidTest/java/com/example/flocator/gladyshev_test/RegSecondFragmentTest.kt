import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.example.flocator.R
import com.example.flocator.authentication.registration.RegSecondFragment
import com.example.flocator.common.utils.FragmentNavigationUtils
import com.example.flocator.gladyshev_test.BaseTest
import com.example.flocator.gladyshev_test.pages.RegFragmentPage
import org.junit.Before
import org.junit.Test

class RegSecondFragmentTest : BaseTest() {
    private lateinit var regFragmentPage: RegFragmentPage

    companion object {
        private const val INVALID_EMAIL_ERROR = "Некорректный email"
    }

    @Before
    fun setup() {
        regFragmentPage = RegFragmentPage()

        activityScenario.scenario.onActivity {
            FragmentNavigationUtils.clearAllAndOpenFragment(
                it.supportFragmentManager,
                RegSecondFragment()
            )
        }

        onView(withId(R.id.submit_btn)).check(matches(isDisplayed()))
    }

    @Test
    fun testEmptyLoginAndEmail() {
        regFragmentPage.clickButton(R.id.second_input_edit_field)
        onView(withId(R.id.registration_error_message_text))
            .check(matches(isDisplayed()))
            .check(matches(withText(INVALID_EMAIL_ERROR)))
    }

    @Test
    fun testValidLoginAndEmail() {
        val login = "testlogin"
        val email = "test@gmail.com"

        regFragmentPage
            .enterTextAndCloseKeyboard(R.id.first_input_edit_field, login)
            .enterTextAndCloseKeyboard(R.id.second_input_edit_field, email)
            .clickButton(R.id.submit_btn)

        onView(withId(R.id.third_fragment_root)).check(matches(isDisplayed()))
    }
}
