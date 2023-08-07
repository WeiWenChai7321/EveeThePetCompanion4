package ca.sleepdeprived.eveethepetcompanion;

<<<<<<< HEAD
import android.content.SharedPreferences;
import android.widget.EditText;
import android.widget.Switch;
import androidx.test.core.app.ApplicationProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

=======
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import ca.sleepdeprived.eveethepetcompanion.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowToast;

import static org.junit.Assert.*;

import androidx.fragment.app.FragmentActivity;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = "src/test/AndroidManifest.xml")
>>>>>>> master
public class SettingsFragmentTest {

    private SettingsFragment settingsFragment;

<<<<<<< HEAD
    @Mock
    private FirebaseAuth mockFirebaseAuth;

    @Mock
    private FirebaseFirestore mockFirestore;

    @Mock
    private EditText mockEmailEditText;

    @Mock
    private Switch mockLockOrientationSwitch;

    @Mock
    private Switch mockPushNotificationSwitch;

    @Mock
    private SharedPreferences.Editor mockEditor;

    @Mock
    private SharedPreferences mockSharedPreferences;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        settingsFragment = new SettingsFragment();

        // Set the mocked instances for testing
        settingsFragment.setFirestore(mockFirestore);
        settingsFragment.sharedPreferences = mockSharedPreferences;
        settingsFragment.emailEditText = mockEmailEditText;
        settingsFragment.lockOrientationSwitch = mockLockOrientationSwitch;

        // Set the mocked FirebaseAuth instance
        settingsFragment.setFirebaseAuth(mockFirebaseAuth);
    }

    @After
    public void tearDown() {
        settingsFragment.onStop();
    }

    // Test if emailListener is added when the user is logged in
    @Test
    public void testOnStart_UserLoggedIn_EmailListenerAdded() {
        when(mockFirebaseAuth.getCurrentUser()).thenReturn(mock(FirebaseUser.class));
        settingsFragment.onStart();
        assertNotNull(settingsFragment.getEmailListener());
    }

    // Test if emailListener is not added when the user is not logged in
    @Test
    public void testOnStart_UserNotLoggedIn_NoEmailListenerAdded() {
        when(mockFirebaseAuth.getCurrentUser()).thenReturn(null);
        settingsFragment.onStart();
        assertNull(settingsFragment.getEmailListener());
    }

    // Test if emailListener is removed on onStop
    @Test
    public void testOnStop_EmailListenerRemoved() {
        when(mockFirebaseAuth.getCurrentUser()).thenReturn(mock(FirebaseUser.class));
        settingsFragment.onStart();
        ListenerRegistration emailListener = settingsFragment.getEmailListener();
        settingsFragment.onStop();
        verify(emailListener).remove();
    }

    // Test if logoutUser clears SharedPreferences and signs out from FirebaseAuth
    @Test
    public void testLogoutUser_ClearsSharedPreferencesAndSignsOut() {
        settingsFragment.testLogoutUser();
        verify(mockEditor).putBoolean(anyString(), eq(false));
        verify(mockEditor, times(2)).apply();
        verify(mockFirebaseAuth).signOut();
    }

    // Test if onResume disables emailEditText and sets the LockOrientationSwitch state
    @Test
    public void testOnResume_EmailEditTextDisabledAndLockOrientationSwitchStateSet() {
        when(mockSharedPreferences.getString(any(), anyString())).thenReturn("");
        when(mockSharedPreferences.getBoolean(any(), anyBoolean())).thenReturn(true);
        settingsFragment.onResume();
        assertFalse(settingsFragment.getEmailEditText().isEnabled());
        verify(mockLockOrientationSwitch).setChecked(true);
    }
}
=======
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

>>>>>>> master
