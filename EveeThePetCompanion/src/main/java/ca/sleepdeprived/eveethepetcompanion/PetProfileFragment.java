/*Section: 0NA
  Wei Wen Chai, N01447321
  John Aquino, N01303112
  Jennifer Nguyen, N01435464
  Ubay Abdulaziz, N01437353
*/
package ca.sleepdeprived.eveethepetcompanion;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.HashSet;
import java.util.Set;

public class PetProfileFragment extends Fragment {
    private TextView vetVisitsReminderText;
    private CheckBox vetShotsReminderCheckBox;
    private CheckBox vetVisitsReminderCheckBox;
    private SharedPreferences sharedPreferences;
    private Set<String> crossedOutReminders;
    private Handler handler;
    private Runnable removeCrossedOutRemindersRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_petprofile, container, false);
        vetVisitsReminderText = view.findViewById(R.id.vet_visits_reminder);
        vetShotsReminderCheckBox = view.findViewById(R.id.vet_shots_reminder);
        vetVisitsReminderCheckBox = view.findViewById(R.id.vet_visits_reminder);
        sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE);
        crossedOutReminders = sharedPreferences.getStringSet("crossed_out_reminders", new HashSet<>());

        // Hide crossed-out reminders
        if (crossedOutReminders.contains(vetShotsReminderCheckBox.getTag().toString())) {
            vetShotsReminderCheckBox.setVisibility(View.GONE);
        }
        if (crossedOutReminders.contains(vetVisitsReminderCheckBox.getTag().toString())) {
            vetVisitsReminderCheckBox.setVisibility(View.GONE);
        }

        handler = new Handler();
        removeCrossedOutRemindersRunnable = this::removeCrossedOutReminders;

        vetShotsReminderCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                crossedOutReminders.add(buttonView.getTag().toString());
                buttonView.setVisibility(View.GONE); // Hide the checkbox when checked
                handler.postDelayed(removeCrossedOutRemindersRunnable, 5000); // Start the handler to remove the reminder after 5 seconds
            } else {
                crossedOutReminders.remove(buttonView.getTag().toString());
            }
        });

        vetVisitsReminderCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                crossedOutReminders.add(buttonView.getTag().toString());
                buttonView.setVisibility(View.GONE); // Hide the checkbox when checked
                handler.postDelayed(removeCrossedOutRemindersRunnable, 5000); // Start the handler to remove the reminder after 5 seconds
            } else {
                crossedOutReminders.remove(buttonView.getTag().toString());
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Start the handler to remove crossed-out reminders after 5 seconds
        handler.postDelayed(removeCrossedOutRemindersRunnable, 5000);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Stop the handler when the fragment is paused or destroyed
        handler.removeCallbacks(removeCrossedOutRemindersRunnable);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Save the crossed-out reminders in SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet("crossed_out_reminders", crossedOutReminders);
        editor.apply();
    }

    private void removeCrossedOutReminders() {
        if (vetShotsReminderCheckBox.getVisibility() == View.GONE) {
            crossedOutReminders.remove(vetShotsReminderCheckBox.getTag().toString());
        }
        if (vetVisitsReminderCheckBox.getVisibility() == View.GONE) {
            crossedOutReminders.remove(vetVisitsReminderCheckBox.getTag().toString());
        }
    }
}