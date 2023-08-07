/*Section: 0NA
  Wei Wen Chai, N01447321
  John Aquino, N01303112
  Jennifer Nguyen, N01435464
  Ubay Abdulaziz, N01437353
*/
package ca.sleepdeprived.eveethepetcompanion;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = "src/test/AndroidManifest.xml")
public class SettingsFragmentTest {

    private SettingsFragment settingsFragment;

    @Before
    public void setUp() {
        settingsFragment = new SettingsFragment();
    }

    @Test
    public void testEmailEditTextInitialized() {
        // Inflate the fragment's layout
        View view = LayoutInflater.from(RuntimeEnvironment.application).inflate(R.layout.fragment_settings, null);

        // Attach the view to the fragment
        settingsFragment.onViewCreated(view, null);

        // Check if emailEditText is not null
        EditText emailEditText = view.findViewById(R.id.et_email);
        assertNotNull(emailEditText);
    }

    @Test
    public void testOnViewCreated_EmailEditTextDisabled() {
        View view = LayoutInflater.from(RuntimeEnvironment.application).inflate(R.layout.fragment_settings, null);
        EditText emailEditText = view.findViewById(R.id.et_email);
        emailEditText.setEnabled(false);

        settingsFragment.onViewCreated(view, null);

        Button updateButton = view.findViewById(R.id.btn_update);
        assertEquals(RuntimeEnvironment.application.getString(R.string.update), updateButton.getText().toString());
    }

    @Test
    public void testOnViewCreated_EmailEditTextEnabled() {
        View view = LayoutInflater.from(RuntimeEnvironment.application).inflate(R.layout.fragment_settings, null);
        EditText emailEditText = view.findViewById(R.id.et_email);
        emailEditText.setEnabled(true);

        settingsFragment.onViewCreated(view, null);

        Button updateButton = view.findViewById(R.id.btn_update);
        assertEquals(RuntimeEnvironment.application.getString(R.string.update), updateButton.getText().toString());
    }

    @Test
    public void testLockOrientationSwitchInitialized() {
        // Inflate the fragment's layout
        View view = LayoutInflater.from(RuntimeEnvironment.application).inflate(R.layout.fragment_settings, null);

        // Attach the view to the fragment
        settingsFragment.onViewCreated(view, null);

        // Check if lockOrientationSwitch is not null
        Switch lockOrientationSwitch = view.findViewById(R.id.switch_lock_orientation);
        assertNotNull(lockOrientationSwitch);
    }

    @Test
    public void testLogoutButtonNotNull() {
        View view = LayoutInflater.from(RuntimeEnvironment.application).inflate(R.layout.fragment_settings, null);

        // Attach the view to the fragment
        settingsFragment.onViewCreated(view, null);

        Button logoutButton = view.findViewById(R.id.btn_logout);
        assertNotNull(logoutButton);
    }

}
