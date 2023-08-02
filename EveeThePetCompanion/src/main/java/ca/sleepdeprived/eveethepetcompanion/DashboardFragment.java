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
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DashboardFragment extends Fragment {
    private List<CheckBox> reminderCheckboxes;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference remindersCollectionRef = db.collection("reminders");
    private SharedPreferences sharedPreferences;
    private LinearLayout remindersLayout;
    private Context context;
    private EditText editReminderEditText;
    private Set<Reminder> savedReminders;
    private boolean isInitialCreate = true;
    private boolean isEditTextVisible = false;

    private View view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        reminderCheckboxes = new ArrayList<>();

        // Retrieve saved reminders
        sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE);
        savedReminders = new HashSet<>();

        // Read existing reminders from the database on fragment creation
        readExistingReminders();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        this.view = view;

        remindersLayout = view.findViewById(R.id.reminders_card);
        editReminderEditText = view.findViewById(R.id.edit_text_reminder);
        editReminderEditText.setVisibility(View.GONE); // Initially hide the EditText

        Button newReminderButton = view.findViewById(R.id.button_new_reminder);
        updateNoRemindersVisibility();
        newReminderButton.setOnClickListener(v -> {
            String reminderText = editReminderEditText.getText().toString().trim();
            if (isEditTextVisible) {
                // If the EditText is visible, hide it and change the button text to "New Reminder"
                editReminderEditText.setVisibility(View.GONE);
                newReminderButton.setText(R.string.new_reminder_button_text);
            } else {
                // If the EditText is not visible, show it and change the button text to "Cancel"
                editReminderEditText.setVisibility(View.VISIBLE);
                editReminderEditText.requestFocus();
                newReminderButton.setText(R.string.cancel_button_text);
            }
            isEditTextVisible = !isEditTextVisible;
            updateNoRemindersVisibility();
        });

        editReminderEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || event != null && event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                String reminderText = editReminderEditText.getText().toString().trim();
                if (!TextUtils.isEmpty(reminderText)) {
                    addReminder(reminderText);
                    editReminderEditText.setText("");
                    editReminderEditText.setVisibility(View.GONE); // Hide the EditText after creating the reminder
                    newReminderButton.setText(R.string.new_reminder_button_text);
                    isEditTextVisible = false;
                    updateNoRemindersVisibility();
                    hideKeyboard();
                    return true;
                }
            }
            return false;
        });


        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            ArrayList<String> remindersArrayList = savedInstanceState.getStringArrayList(getString(R.string.saved_reminders_key));
            if (remindersArrayList != null) {
                for (String reminderText : remindersArrayList) {
                    savedReminders.add(new Reminder(reminderText));
                }
            }
        }
        // Set the flag to false to indicate that this is not the initial creation of the fragment
        isInitialCreate = false;

        // Update the UI with existing reminders
        updateUIWithExistingReminders();
    }




    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        ArrayList<String> remindersArrayList = new ArrayList<>();
        for (Reminder reminder : savedReminders) {
            remindersArrayList.add(reminder.getReminderText());
        }
        outState.putStringArrayList(getString(R.string.saved_reminders_key), remindersArrayList);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void addReminder(String reminderText) {
        if (remindersLayout == null || context == null) {
            return;
        }

        if (isReminderAdded(reminderText)) {
            return; // Do not add duplicate reminders
        }

        Reminder newReminder = new Reminder(reminderText);

        CheckBox reminderCheckBox = new CheckBox(context);
        reminderCheckBox.setText(reminderText);
        reminderCheckBox.setChecked(false);
        reminderCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                removeReminderDelayed((CheckBox) buttonView);
            }
        });
        remindersLayout.addView(reminderCheckBox);
        reminderCheckboxes.add(reminderCheckBox);
        savedReminders.add(newReminder);

        remindersCollectionRef.add(newReminder)
                .addOnSuccessListener(documentReference -> {
                    // Success
                    String documentId = documentReference.getId();
                    // You can store the documentId in your local savedReminders or in a separate list if needed.
                })
                .addOnFailureListener(e -> {
                    // Error handling
                });
        updateNoRemindersVisibility();
    }


    private void removeReminderDelayed(CheckBox checkBox) {

        new Handler().postDelayed(() -> {
            remindersLayout.removeView(checkBox);
            reminderCheckboxes.remove(checkBox);

            String reminderText = checkBox.getText().toString();

            // Find the specific reminder to delete from the savedReminders set
            Reminder reminderToDelete = null;
            for (Reminder reminder : savedReminders) {
                if (reminder.getReminderText().equals(reminderText)) {
                    reminderToDelete = reminder;
                    break;
                }
            }

            if (reminderToDelete != null) {
                // Remove the reminder from the savedReminders set
                savedReminders.remove(reminderToDelete);

                // Delete the corresponding document from the database using the reminder text
                remindersCollectionRef.whereEqualTo("reminderText", reminderText)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                documentSnapshot.getReference().delete()
                                        .addOnSuccessListener(aVoid -> {
                                            // Success
                                        })
                                        .addOnFailureListener(e -> {
                                            // Error handling
                                            Log.e("DashboardFragment", "Error deleting reminder: " + e.getMessage());
                                        });
                            }
                        })
                        .addOnFailureListener(e -> {
                            // Error handling
                            Log.e("DashboardFragment", "Error retrieving reminders: " + e.getMessage());
                        });
            }
            updateNoRemindersVisibility();
        }, 5000);
    }





    private void updateNoRemindersVisibility() {
        TextView noRemindersTextView = view.findViewById(R.id.text_no_reminders);
        if (noRemindersTextView != null) {
            if (reminderCheckboxes.isEmpty() && !isEditTextVisible) { // Check both conditions
                noRemindersTextView.setVisibility(View.VISIBLE);
            } else {
                noRemindersTextView.setVisibility(View.GONE);
            }
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        // Update the visibility of the "no reminder" text when the fragment is resumed
        updateNoRemindersVisibility();
    }

    private void readExistingReminders() {
        remindersCollectionRef.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    savedReminders.clear(); // Clear the savedReminders set
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String reminderId = documentSnapshot.getId();
                        String reminderText = documentSnapshot.getString("reminderText");
                        if (reminderText != null) {
                            savedReminders.add(new Reminder(reminderText));
                        }
                    }
                    // Update the UI after fetching reminders
                    updateUIWithExistingReminders();
                })
                .addOnFailureListener(e -> {
                    // Error handling
                    Log.e("DashboardFragment", "Error retrieving reminders: " + e.getMessage());
                });
    }



    private boolean isReminderAdded(String reminderText) {
        for (Reminder reminder : savedReminders) {
            // Compare the text of the reminder with the provided reminderText
            if (reminder.getReminderText().equals(reminderText)) {
                return true; // Reminder already exists
            }
        }
        return false; // Reminder is not present
    }

    private void updateUIWithExistingReminders() {
        remindersLayout.removeAllViews(); // Clear the UI first

        for (Reminder reminder : savedReminders) {
            CheckBox reminderCheckBox = new CheckBox(context);
            reminderCheckBox.setText(reminder.getReminderText());
            reminderCheckBox.setChecked(false);
            reminderCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    removeReminderDelayed((CheckBox) buttonView);
                }
            });
            remindersLayout.addView(reminderCheckBox);
            reminderCheckboxes.add(reminderCheckBox);
        }

        // Update the visibility of the "no reminder" text when the fragment is resumed
        updateNoRemindersVisibility();
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editReminderEditText.getWindowToken(), 0);
    }

}