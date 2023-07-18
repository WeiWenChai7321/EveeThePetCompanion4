/*Section: 0NA
  Wei Wen Chai, N01447321
  John Aquino, N01303112
  Jennifer Nguyen, N01435464
  Ubay Abdulaziz, N01437353
*/
package ca.sleepdeprived.eveethepetcompanion;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DashboardFragment extends Fragment {
    private List<CheckBox> reminderCheckboxes;
    private SharedPreferences sharedPreferences;
    private LinearLayout remindersLayout;
    private EditText editReminderEditText;
    private Set<String> savedReminders;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        reminderCheckboxes = new ArrayList<>();

        // Retrieve saved reminders
        sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE);
        savedReminders = sharedPreferences.getStringSet("reminders", new HashSet<>());
        for (String reminder : savedReminders) {
            addReminder(reminder);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        remindersLayout = view.findViewById(R.id.reminders_card);
        editReminderEditText = view.findViewById(R.id.edit_text_reminder);

        // Add reminder when pressing enter on the keyboard
        editReminderEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                String reminderText = editReminderEditText.getText().toString().trim();
                if (!TextUtils.isEmpty(reminderText)) {
                    addReminder(reminderText);
                    editReminderEditText.setText("");
                    return true;
                }
            }
            return false;
        });

        // Add saved reminders
        addSavedReminders();

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            ArrayList<String> remindersArrayList = savedInstanceState.getStringArrayList("savedReminders");
            if (remindersArrayList != null) {
                savedReminders = new HashSet<>(remindersArrayList);
                // Add saved reminders
                addSavedReminders();
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList("savedReminders", new ArrayList<>(savedReminders));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        saveReminders();
    }

    private void addReminder(String reminderText) {
        if (remindersLayout == null) {
            return;
        }

        CheckBox reminderCheckBox = new CheckBox(requireContext());
        reminderCheckBox.setText(reminderText);
        reminderCheckBox.setChecked(false);
        reminderCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                removeReminderDelayed((CheckBox) buttonView);
            }
        });
        remindersLayout.addView(reminderCheckBox);
        reminderCheckboxes.add(reminderCheckBox);
        savedReminders.add(reminderText);
    }

    private void removeReminderDelayed(CheckBox checkBox) {
        new Handler().postDelayed(() -> {
            remindersLayout.removeView(checkBox);
            reminderCheckboxes.remove(checkBox);
            savedReminders.remove(checkBox.getText().toString());
        }, 5000);
    }

    private void addSavedReminders() {
        for (String reminder : savedReminders) {
            addReminder(reminder);
        }
    }

    private void saveReminders() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet("reminders", savedReminders);
        editor.apply();
    }
}
