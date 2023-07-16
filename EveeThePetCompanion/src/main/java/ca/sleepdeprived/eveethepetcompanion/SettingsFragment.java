/*Section: 0NA
  Wei Wen Chai, N01447321
  John Aquino, N01303112
  Jennifer Nguyen, N01435464
  Ubay Abdulaziz, N01437353
*/
package ca.sleepdeprived.eveethepetcompanion;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

public class SettingsFragment extends Fragment {
    private EditText emailEditText;

    private Switch lockOrientationSwitch;
    private Switch pushNotificationSwitch;
    private SharedPreferences sharedPreferences;
    private FirebaseFirestore firestore;
    private ListenerRegistration emailListener;
    private Button updateButton;

    public SettingsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        lockOrientationSwitch = view.findViewById(R.id.switch_lock_orientation);
        pushNotificationSwitch = view.findViewById(R.id.switch_push_notifications);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        firestore = FirebaseFirestore.getInstance();
        emailEditText = view.findViewById(R.id.et_email);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        updateButton = view.findViewById(R.id.btn_update);

        Button logoutButton = view.findViewById(R.id.btn_logout);

        super.onViewCreated(view, savedInstanceState);

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (emailEditText.isEnabled()) {
                    String updatedEmail = emailEditText.getText().toString();
                    // Update the email in Firestore

                    // Check if the user is signed in with Google
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null && user.getProviderData().size() > 1) {
                        // User signed in with Google, show a toast message
                        Toast.makeText(getActivity(), "You cannot edit a Google email.", Toast.LENGTH_SHORT).show();
                    } else {
                        // User not signed in with Google, proceed with email update
                        updateEmailInFirestore(updatedEmail);
                    }
                } else {
                    emailEditText.setEnabled(true);
                    updateButton.setText(R.string.save);
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            emailListener = firestore.collection("users")
                    .document(user.getUid())
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                // Handle error
                                return;
                            }

                            if (documentSnapshot != null && documentSnapshot.exists()) {
                                String email = documentSnapshot.getString("email");
                                if (email != null) {
                                    emailEditText.setText(email);
                                }

                                // Disable editing
                                emailEditText.setEnabled(false);
                            }
                        }
                    });
        }
    }


    @Override
    public void onStop() {
        super.onStop();

        // Unsubscribe from Firestore listener
        if (emailListener != null) {
            emailListener.remove();
        }
    }




    private void logoutUser() {
        // Clear login status
        sharedPreferences.edit().putBoolean(getString(R.string.isloggedin), false).apply();

        // Start login activity
        startLoginActivity();
    }

    private void startLoginActivity() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        String storedEmail = sharedPreferences.getString(getString(R.string.saved_email), "");

        // Read the stored preferences and update the switches accordingly
        boolean isOrientationLocked = sharedPreferences.getBoolean(getString(R.string.lock_orientation), false);
        boolean isPushNotificationsEnabled = sharedPreferences.getBoolean(getString(R.string.push_notifications), false);

        lockOrientationSwitch.setChecked(isOrientationLocked);
        pushNotificationSwitch.setChecked(isPushNotificationsEnabled);

        lockOrientationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Save the orientation preference in SharedPreferences
                sharedPreferences.edit().putBoolean(getString(R.string.lock_orientation), isChecked).apply();

                if (isChecked) {
                    // Lock screen orientation to portrait
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    Toast.makeText(getActivity(), R.string.screen_orientation_locked_to_portrait, Toast.LENGTH_SHORT).show();
                } else {
                    // Reset screen orientation to sensor
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                    Toast.makeText(getActivity(), R.string.screen_orientation_unlocked, Toast.LENGTH_SHORT).show();
                }
            }
        });

        pushNotificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Save the push notifications preference in SharedPreferences
                sharedPreferences.edit().putBoolean(getString(R.string.push_notifications), isChecked).apply();

                if (isChecked) {
                    Toast.makeText(getActivity(), R.string.push_notifications_enabled, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), R.string.push_notifications_disabled, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void updateEmailInFirestore(String updatedEmail) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            firestore.collection("users")
                    .document(uid)
                    .update("email", updatedEmail)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getActivity(), getString(R.string.email_saved) + updatedEmail, Toast.LENGTH_SHORT).show();
                            emailEditText.setEnabled(false);
                            updateButton.setText(R.string.update);
                            // Save the updated email in SharedPreferences
                            sharedPreferences.edit().putString(getString(R.string.saved_email), updatedEmail).apply();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getActivity(), "Failed to update email. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

}
