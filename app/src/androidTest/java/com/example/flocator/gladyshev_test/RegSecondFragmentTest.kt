import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.example.flocator.R
import com.example.flocator.authentication.registration.RegSecondFragment
import com.example.flocator.common.utils.FragmentNavigationUtils
import com.example.flocator.gladyshev_test.BaseTest
import org.junit.Before
import org.junit.Test

class RegSecondFragmentTest : BaseTest() {

    private lateinit var regSecondFragmentPage: RegSecondFragmentPage

    @Before
    fun setup() {
        regSecondFragmentPage = RegSecondFragmentPage()

        activityScenario.scenario.onActivity {
            FragmentNavigationUtils.clearAllAndOpenFragment(
                it.supportFragmentManager,
                RegSecondFragment()
            )
        }
    }

    @Test
    fun testEmptyLoginAndEmail() {
        regSecondFragmentPage.clickSubmitButton()
        onView(withId(R.id.registration_error_message_text)).check(matches(isDisplayed()))
        onView(withId(R.id.registration_error_message_text)).check(matches(withText("Некорректный email")))
    }

    @Test
    fun testValidLoginAndEmail() {
        val login = "testlogin"
        val email = "test@gmail.com"

        regSecondFragmentPage.typeLogin(login)
        regSecondFragmentPage.typeEmail(email)
        regSecondFragmentPage.clickSubmitButton()
        
        onView(withId(R.id.third_fragment_root)).check(matches(isDisplayed()))
    }
}
