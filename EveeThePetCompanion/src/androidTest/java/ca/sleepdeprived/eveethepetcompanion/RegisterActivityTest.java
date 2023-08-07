/*Section: 0NA
  Wei Wen Chai, N01447321
  John Aquino, N01303112
  Jennifer Nguyen, N01435464
  Ubay Abdulaziz, N01437353
*/
package ca.sleepdeprived.eveethepetcompanion;

<<<<<<< HEAD
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
=======
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
>>>>>>> master

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

<<<<<<< HEAD
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
=======
import androidx.test.rule.ActivityTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
>>>>>>> master

@RunWith(AndroidJUnit4.class)
public class RegisterActivityTest {

    @Rule
<<<<<<< HEAD
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
=======
    public ActivityTestRule<RegisterActivity> activityRule = new ActivityTestRule<>(RegisterActivity.class);

    @Test
    public void testAllInputFieldsAreEmptyInitially() {
        onView(withId(R.id.email_edittext)).check(matches(withText("")));
        onView(withId(R.id.password_edittext)).check(matches(withText("")));
        onView(withId(R.id.confirm_password_edittext)).check(matches(withText("")));
        onView(withId(R.id.phone_edittext)).check(matches(withText("")));
        onView(withId(R.id.firstname_edittext)).check(matches(withText("")));
        onView(withId(R.id.lastname_edittext)).check(matches(withText("")));
    }

    @Test
    public void testSignUpButtonEnabledAfterAllFieldsFilled() {
        onView(withId(R.id.email_edittext)).perform(replaceText("test@example.com"));
        onView(withId(R.id.password_edittext)).perform(replaceText("Password1"));
        onView(withId(R.id.confirm_password_edittext)).perform(replaceText("Password1"));
        onView(withId(R.id.phone_edittext)).perform(replaceText("1234567890"));
        onView(withId(R.id.firstname_edittext)).perform(replaceText("John"));
        onView(withId(R.id.lastname_edittext)).perform(replaceText("Doe"));
        closeSoftKeyboard();
    }

    @Test
    public void testValidPassword() {
        // Test valid passwords
        assertTrue(RegisterActivity.isValidPassword("Passw0rd!"));
        assertTrue(RegisterActivity.isValidPassword("Abcd123!"));
    }

    @Test
    public void testInvalidPassword() {
        // Test invalid passwords
        assertFalse(RegisterActivity.isValidPassword("password")); // No uppercase letter
        assertFalse(RegisterActivity.isValidPassword("P@ssword")); // No digit
        assertFalse(RegisterActivity.isValidPassword("Password123")); // No special character
        assertFalse(RegisterActivity.isValidPassword("Pa1!")); // Too short
        assertFalse(RegisterActivity.isValidPassword("This is a long password with no special characters")); // No special character
    }

    @Test
    public void testInvalidPasswordTooShort() {
        onView(withId(R.id.email_edittext)).perform(replaceText("test@example.com"));
        onView(withId(R.id.password_edittext)).perform(replaceText("Short1")); // Password is too short (less than 6 characters)
        onView(withId(R.id.confirm_password_edittext)).perform(replaceText("Short1"));
        onView(withId(R.id.phone_edittext)).perform(replaceText("1234567890"));
        onView(withId(R.id.firstname_edittext)).perform(replaceText("John"));
        onView(withId(R.id.lastname_edittext)).perform(replaceText("Doe"));
        closeSoftKeyboard();
>>>>>>> master
    }
}
