import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.example.flocator.R

class RegFirstFragmentPage {

    fun typeFirstName(firstName: String) {
        onView(withId(R.id.first_input_edit_field)).perform(
            click(),
            typeText(firstName),
            closeSoftKeyboard()
        )
    }

    fun typeLastName(lastName: String) {
        onView(withId(R.id.second_input_edit_field)).perform(
            click(),
            typeText(lastName),
            closeSoftKeyboard()
        )
    }

    fun clickSubmitButton() {
        onView(withId(R.id.submit_btn)).perform(click())
    }
}
