/*Section: 0NA
  Wei Wen Chai, N01447321
  John Aquino, N01303112
  Jennifer Nguyen, N01435464
  Ubay Abdulaziz, N01437353
*/
package ca.sleepdeprived.eveethepetcompanion;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class RegisterActivityTest {

    @Rule
    public ActivityScenarioRule<RegisterActivity> activityScenarioRule = new ActivityScenarioRule<>(RegisterActivity.class);

    @Test
    public void checkEmptyFieldsShowToast() {
        Espresso.onView(ViewMatchers.withId(R.id.sign_up_button)).perform(ViewActions.click());
        Espresso.onView(withText(R.string.please_fill_fields)).inRoot(new ToastMatcher()).check(ViewAssertions.matches(withText(R.string.please_fill_fields)));
    }

    @Test
    public void checkInvalidEmailFormatShowsToast() {
        Espresso.onView(ViewMatchers.withId(R.id.email_edittext)).perform(ViewActions.typeText("invalidEmail"));
        Espresso.onView(ViewMatchers.withId(R.id.sign_up_button)).perform(ViewActions.click());
        Espresso.onView(withText(R.string.invalid_email)).inRoot(new ToastMatcher()).check(ViewAssertions.matches(withText(R.string.invalid_email)));
    }

    @Test
    public void checkPasswordsMismatchShowsToast() {
        Espresso.onView(ViewMatchers.withId(R.id.password_edittext)).perform(ViewActions.typeText("ValidPass1!"));
        Espresso.onView(ViewMatchers.withId(R.id.confirm_password_edittext)).perform(ViewActions.typeText("ValidPass2!"));
        Espresso.onView(ViewMatchers.withId(R.id.sign_up_button)).perform(ViewActions.click());
        Espresso.onView(withText(R.string.passwds_not_match)).inRoot(new ToastMatcher()).check(ViewAssertions.matches(withText(R.string.passwds_not_match)));
    }

    @Test
    public void checkInvalidPhoneShowsToast() {
        Espresso.onView(ViewMatchers.withId(R.id.phone_edittext)).perform(ViewActions.typeText("12345"));
        Espresso.onView(ViewMatchers.withId(R.id.sign_up_button)).perform(ViewActions.click());
        Espresso.onView(withText(R.string.invalid_phone_number)).inRoot(new ToastMatcher()).check(ViewAssertions.matches(withText(R.string.invalid_phone_number)));
    }

    @Test
    public void checkInvalidPasswordShowsToast() {
        Espresso.onView(ViewMatchers.withId(R.id.password_edittext)).perform(ViewActions.typeText("pass"));
        Espresso.onView(ViewMatchers.withId(R.id.confirm_password_edittext)).perform(ViewActions.typeText("pass"));
        Espresso.onView(ViewMatchers.withId(R.id.sign_up_button)).perform(ViewActions.click());
        Espresso.onView(withText(R.string.invalid_password)).inRoot(new ToastMatcher()).check(ViewAssertions.matches(withText(R.string.invalid_password)));
    }
}
