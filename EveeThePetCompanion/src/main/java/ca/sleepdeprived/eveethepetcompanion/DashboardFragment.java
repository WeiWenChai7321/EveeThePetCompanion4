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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DashboardFragment extends Fragment {
    private PetInfoViewModel petInfoViewModel;
    private List<CheckBox> reminderCheckboxes;
    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        petInfoViewModel = new ViewModelProvider(requireActivity()).get(PetInfoViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        LinearLayout remindersLayout = view.findViewById(R.id.reminders_card);
        EditText editReminderEditText = view.findViewById(R.id.edit_text_reminder);
        sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE);
        reminderCheckboxes = new ArrayList<>();

        petInfoViewModel.getPetName().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String petName) {
                // Update the pet name if necessary
            }
        });

        // Retrieve saved reminders
        Set<String> savedReminders = sharedPreferences.getStringSet("reminders", new HashSet<>());
        for (String reminder : savedReminders) {
            addReminder(remindersLayout, reminder);
        }

        // Add reminder when pressing enter on the keyboard
        editReminderEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                String reminderText = editReminderEditText.getText().toString().trim();
                if (!TextUtils.isEmpty(reminderText)) {
                    addReminder(remindersLayout, reminderText);
                    editReminderEditText.setText("");
                    return true;
                }
            }
            return false;
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Save the reminders in SharedPreferences
        Set<String> reminders = new HashSet<>();
        for (CheckBox checkBox : reminderCheckboxes) {
            if (checkBox.isChecked()) {
                reminders.add(checkBox.getText().toString());
            }
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet("reminders", reminders);
        editor.apply();
    }

    private void addReminder(ViewGroup remindersLayout, String reminderText) {
        CheckBox reminderCheckBox = new CheckBox(requireContext());
        reminderCheckBox.setText(reminderText);
        reminderCheckBox.setChecked(false);
        reminderCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                removeReminderDelayed((CheckBox) buttonView, remindersLayout);
            }
        });
        remindersLayout.addView(reminderCheckBox);
        reminderCheckboxes.add(reminderCheckBox);
    }

    private void removeReminderDelayed(CheckBox checkBox, ViewGroup remindersLayout) {
        new Handler().postDelayed(() -> {
            remindersLayout.removeView(checkBox);
            reminderCheckboxes.remove(checkBox);
        }, 5000);
    }
}
