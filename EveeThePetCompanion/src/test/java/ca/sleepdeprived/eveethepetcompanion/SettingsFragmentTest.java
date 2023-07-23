package ca.sleepdeprived.eveethepetcompanion;

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

public class SettingsFragmentTest {

    private SettingsFragment settingsFragment;

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
        settingsFragment.pushNotificationSwitch = mockPushNotificationSwitch;

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
