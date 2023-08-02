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
import android.widget.Button;
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
    private View view;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        reminderCheckboxes = new ArrayList<>();

        // Retrieve saved reminders
        sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE);
        savedReminders = sharedPreferences.getStringSet(getString(R.string.reminders_key), new HashSet<>());
        for (String reminder : savedReminders) {
            addReminder(reminder);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        this.view = view;
        remindersLayout = view.findViewById(R.id.reminders_card);
        editReminderEditText = view.findViewById(R.id.edit_text_reminder);
        Button newReminderButton = view.findViewById(R.id.button_new_reminder);
        updateNoRemindersVisibility();
        newReminderButton.setOnClickListener(v -> {
            String reminderText = editReminderEditText.getText().toString().trim();
            if (!TextUtils.isEmpty(reminderText)) {
                // Create a new LinearLayout to hold the CheckBox and the EditText
                LinearLayout reminderLayout = new LinearLayout(requireContext());
                reminderLayout.setOrientation(LinearLayout.HORIZONTAL);
                reminderLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                // Create a new CheckBox dynamically and add it to the LinearLayout
                CheckBox newReminderCheckBox = new CheckBox(requireContext());
                newReminderCheckBox.setText(reminderText);
                newReminderCheckBox.setChecked(false);
                newReminderCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        removeReminderDelayed((CheckBox) buttonView);
                    }
                });
                reminderLayout.addView(newReminderCheckBox);

                // Create a new EditText dynamically and add it to the LinearLayout
                EditText newReminderEditText = new EditText(requireContext());
                newReminderEditText.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
                newReminderEditText.setText(reminderText);
                reminderLayout.addView(newReminderEditText);
                newReminderEditText.setFocusableInTouchMode(true);
                newReminderEditText.requestFocus();

                // Add the new LinearLayout (containing CheckBox and EditText) to the layout
                remindersLayout.addView(reminderLayout);

                // Clear the input from the previous EditText
                editReminderEditText.setText("");
                updateNoRemindersVisibility();
            }
        });

        // Add reminder when pressing enter on the keyboard
        editReminderEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                String reminderText = editReminderEditText.getText().toString().trim();
                if (!TextUtils.isEmpty(reminderText)) {
                    addReminder(reminderText);
                    editReminderEditText.setText("");
                    updateNoRemindersVisibility();
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
            ArrayList<String> remindersArrayList = savedInstanceState.getStringArrayList(getString(R.string.saved_reminders_key));
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
        outState.putStringArrayList(getString(R.string.saved_reminders_key), new ArrayList<>(savedReminders));
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
        editor.putStringSet(getString(R.string.reminders_key), savedReminders);
        editor.apply();
    }

    private void updateNoRemindersVisibility() {
        TextView noRemindersTextView = view.findViewById(R.id.text_no_reminders);
        if (reminderCheckboxes.isEmpty()) {
            noRemindersTextView.setVisibility(View.VISIBLE);
        } else {
            noRemindersTextView.setVisibility(View.GONE);
        }
    }
}