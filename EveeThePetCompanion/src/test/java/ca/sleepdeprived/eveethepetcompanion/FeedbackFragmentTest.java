package ca.sleepdeprived.eveethepetcompanion;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class FeedbackFragmentTest {

    private FeedbackFragment feedbackFragment;

    @Before
    public void setup() {
        feedbackFragment = new FeedbackFragment();
    }

    @Test
    public void testIsValidPhoneNumber_ValidPhoneNumber() {
        assertTrue(feedbackFragment.isValidPhoneNumber("1234567890"));
    }

    @Test
    public void testIsValidPhoneNumber_InvalidPhoneNumber() {
        assertFalse(feedbackFragment.isValidPhoneNumber("123"));
    }

    @Test
    public void testIsValidPhoneNumber_InvalidPhoneNumber2() {
        assertFalse(feedbackFragment.isValidPhoneNumber("12312312311"));
    }

    @Test
    public void testIsValidEmailFormat_ValidEmail() {
        assertTrue(feedbackFragment.isValidEmailFormat("test@example.com"));
    }

    @Test
    public void testIsValidEmailFormat_InvalidEmail() {
        assertFalse(feedbackFragment.isValidEmailFormat("invalid_email"));
    }
}
