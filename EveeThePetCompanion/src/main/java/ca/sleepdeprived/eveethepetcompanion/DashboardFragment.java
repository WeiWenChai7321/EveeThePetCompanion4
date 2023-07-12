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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.HashSet;
import java.util.Set;

public class DashboardFragment extends Fragment {
    private Handler handler;
    private Runnable removeCrossedOutRemindersRunnable;
    private SharedPreferences sharedPreferences;
    private Set<String> crossedOutReminders;

    private CheckBox vetVisitsReminderCheckBox;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        handler = new Handler();
        removeCrossedOutRemindersRunnable = this::removeCrossedOutReminders;
        sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE);
        crossedOutReminders = sharedPreferences.getStringSet("crossed_out_reminders", new HashSet<>());

        vetVisitsReminderCheckBox = view.findViewById(R.id.vet_visits_reminder);
        vetVisitsReminderCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                crossedOutReminders.add(buttonView.getTag().toString());
            } else {
                crossedOutReminders.remove(buttonView.getTag().toString());
            }
        });

        // Hide crossed-out reminders
        if (crossedOutReminders.contains(vetVisitsReminderCheckBox.getTag().toString())) {
            vetVisitsReminderCheckBox.setVisibility(View.GONE);
        }

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
        ViewGroup remindersLayout = getView().findViewById(R.id.reminders_card);
        int childCount = remindersLayout.getChildCount();
        for (int i = childCount - 1; i >= 0; i--) {
            View childView = remindersLayout.getChildAt(i);
            if (childView instanceof CheckBox) {
                CheckBox checkBox = (CheckBox) childView;
                if (checkBox.isChecked()) {
                    remindersLayout.removeViewAt(i);
                    crossedOutReminders.add(checkBox.getTag().toString());
                }
            }
        }
    }
}