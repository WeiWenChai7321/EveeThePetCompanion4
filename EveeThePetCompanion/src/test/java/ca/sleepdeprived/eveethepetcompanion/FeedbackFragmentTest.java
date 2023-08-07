/*Section: 0NA
  Wei Wen Chai, N01447321
  John Aquino, N01303112
  Jennifer Nguyen, N01435464
  Ubay Abdulaziz, N01437353
*/
package ca.sleepdeprived.eveethepetcompanion;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.testing.FragmentScenario;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
<<<<<<< HEAD
import org.robolectric.shadows.ShadowToast;
=======
>>>>>>> master

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class FeedbackFragmentTest {

    private FragmentScenario<FeedbackFragment> scenario;

    @Before
    public void setUp() throws Exception {
        scenario = FragmentScenario.launchInContainer(FeedbackFragment.class);
    }

    @Test
    public void testFragmentInView() {
        scenario.onFragment(fragment -> assertNotNull(fragment.getView()));
    }

    @Test
    public void testInvalidEmailShowsError() {
        scenario.onFragment(fragment -> {
            EditText editEmail = fragment.getView().findViewById(R.id.edit_email);
            Button btnSubmitFeedback = fragment.getView().findViewById(R.id.btn_submit_feedback);
            editEmail.setText("invalidEmail");
            btnSubmitFeedback.performClick();
            assertEquals(fragment.getString(R.string.invalid_email), editEmail.getError().toString());
        });
    }

    @Test
    public void testInvalidPhoneNumberShowsError() {
        scenario.onFragment(fragment -> {
            EditText editPhone = fragment.getView().findViewById(R.id.edit_phone);
            Button btnSubmitFeedback = fragment.getView().findViewById(R.id.btn_submit_feedback);
            editPhone.setText("12345");
            btnSubmitFeedback.performClick();
            assertEquals(fragment.getString(R.string.invalid_phone), editPhone.getError().toString());
        });
    }

    @Test
    public void testValidEmailDoesNotShowError() {
        scenario.onFragment(fragment -> {
            EditText editEmail = fragment.getView().findViewById(R.id.edit_email);
            Button btnSubmitFeedback = fragment.getView().findViewById(R.id.btn_submit_feedback);
            editEmail.setText("validemail@example.com");
            btnSubmitFeedback.performClick();
            assertTrue(editEmail.getError() == null || editEmail.getError().toString().isEmpty());
        });
    }

    @Test
    public void testValidPhoneNumberDoesNotShowError() {
        scenario.onFragment(fragment -> {
            EditText editPhone = fragment.getView().findViewById(R.id.edit_phone);
            Button btnSubmitFeedback = fragment.getView().findViewById(R.id.btn_submit_feedback);
            editPhone.setText("123-456-7890");
            btnSubmitFeedback.performClick();
            assertTrue(editPhone.getError() == null || editPhone.getError().toString().isEmpty());
        });
    }
}
