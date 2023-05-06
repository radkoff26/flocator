import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.example.flocator.R

class RegSecondFragmentPage {

    fun typeLogin(login: String) {
        onView(withId(R.id.first_input_edit_field)).perform(
            click(),
            typeText(login),
            closeSoftKeyboard()
        )
    }

    fun typeEmail(email: String) {
        onView(withId(R.id.second_input_edit_field)).perform(
            click(),
            typeText(email),
            closeSoftKeyboard()
        )
    }

    fun clickSubmitButton() {
        onView(withId(R.id.submit_btn)).perform(click())
    }
}