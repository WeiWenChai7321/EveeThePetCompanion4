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
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import java.util.Locale;

public class SettingsFragment extends Fragment {

    private Switch lockOrientationSwitch;
    private Switch pushNotificationSwitch;
    private SharedPreferences sharedPreferences;

    public SettingsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        lockOrientationSwitch = view.findViewById(R.id.switch_lock_orientation);
        pushNotificationSwitch = view.findViewById(R.id.switch_push_notifications);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        EditText emailEditText = view.findViewById(R.id.et_email);
        Button updateButton = view.findViewById(R.id.btn_update);
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
                    Toast.makeText(getActivity(), getString(R.string.email_saved) + updatedEmail, Toast.LENGTH_SHORT).show();
                    emailEditText.setEnabled(false);
                    updateButton.setText(R.string.update);
                    // Save the updated email in SharedPreferences
                    sharedPreferences.edit().putString(getString(R.string.saved_email), updatedEmail).apply();
                } else {
                    emailEditText.setEnabled(true);
                    updateButton.setText(R.string.save);
                }
            }
        });
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

        lockOrientationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
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
                if (isChecked) {
                    Toast.makeText(getActivity(), R.string.push_notifications_enabled, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), R.string.push_notifications_disabled, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
