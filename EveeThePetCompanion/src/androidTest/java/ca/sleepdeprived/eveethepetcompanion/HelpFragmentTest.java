package ca.sleepdeprived.eveethepetcompanion;

import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class HelpFragmentTest {

    private FirebaseFirestore firestore;
    @Before
    public void setUp() {
        // Initialize Firestore for testing
        FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext());
        firestore = FirebaseFirestore.getInstance();
    }
    @Rule
    public ActivityTestRule<MainActivity> activityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Before
    public void navigateToHelpFragment() {
        // Assuming the HelpFragment is accessible from the MainActivity, navigate to the HelpFragment
        // You may need to adapt this part depending on your app's navigation flow
        // For example, you can use a button click or navigation drawer to open the HelpFragment
    }

    @Test
    public void testContactUsButton_Click() {
        // Click on the "Contact Us" button and check if the email chooser dialog is displayed
        Espresso.onView(ViewMatchers.withId(R.id.contact_us_button))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withText(R.string.send_email))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void testFloatingActionButtonVisibility() {
        // Check if the FloatingActionButton is initially hidden
        Espresso.onView(ViewMatchers.withId(R.id.fabScrollToTop))
                .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.INVISIBLE)));

        // Scroll to the bottom of the NestedScrollView
        // Note: You may need to customize this if your NestedScrollView has a different content height
        Espresso.onView(ViewMatchers.withId(R.id.nestedScrollView))
                .perform(ViewActions.swipeUp());

        // Check if the FloatingActionButton is visible after scrolling
        Espresso.onView(ViewMatchers.withId(R.id.fabScrollToTop))
                .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
    }
    @Test
    public void testSendEmail() {
        // Since the sendEmail() method is called internally in the onClick of the "Contact Us" button,
        // we can't directly test the intent. However, we can at least verify that the method does not throw any exceptions.

        HelpFragment helpFragment = new HelpFragment();
        helpFragment.sendEmail();
    }

    @Test
    public void testScrollToTop_FAB_Click() {
        // Scroll to the bottom of the NestedScrollView
        Espresso.onView(ViewMatchers.withId(R.id.nestedScrollView))
                .perform(ViewActions.swipeUp());

        // Check if the FloatingActionButton is visible after scrolling
        Espresso.onView(ViewMatchers.withId(R.id.fabScrollToTop))
                .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));

        // Click on the FloatingActionButton to scroll back to the top
        Espresso.onView(ViewMatchers.withId(R.id.fabScrollToTop))
                .perform(ViewActions.click());

        // Check if the FloatingActionButton is hidden again after scrolling back to the top
        Espresso.onView(ViewMatchers.withId(R.id.fabScrollToTop))
                .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.INVISIBLE)));
    }

}
