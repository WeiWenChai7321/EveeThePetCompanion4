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
    private Set<String> savedReminders;
    private boolean isInitialCreate = true;
    private boolean isEditTextVisible = false;

    private View view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        reminderCheckboxes = new ArrayList<>();

        // Retrieve saved reminders
        sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE);
        savedReminders = sharedPreferences.getStringSet(getString(R.string.reminders_key), new HashSet<>());

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

        // Add reminder when pressing enter on the keyboard
        editReminderEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                String reminderText = editReminderEditText.getText().toString().trim();
                if (!TextUtils.isEmpty(reminderText)) {
                    addReminder(reminderText);
                    editReminderEditText.setText("");
                    editReminderEditText.setVisibility(View.GONE); // Hide the EditText after creating the reminder
                    newReminderButton.setText(R.string.new_reminder_button_text);
                    isEditTextVisible = false;
                    updateNoRemindersVisibility();
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
                savedReminders = new HashSet<>(remindersArrayList);
            }
        }
        // Set the flag to false to indicate that this is not the initial creation of the fragment
        isInitialCreate = false;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList(getString(R.string.saved_reminders_key), new ArrayList<>(savedReminders));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void addReminder(String reminderText) {
        if (remindersLayout == null || context == null) {
            return;
        }

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
        savedReminders.add(reminderText);

        // Update the field name to "reminder" when adding the reminder to the database
        remindersCollectionRef.add(new Reminder(reminderText))
                .addOnSuccessListener(documentReference -> {
                    // Success
                    String documentId = documentReference.getId();
                    // You can store the documentId in your local savedReminders or in a separate list if needed.
                })
                .addOnFailureListener(e -> {
                    // Error handling
                });
    }

    private void removeReminderDelayed(CheckBox checkBox) {
        new Handler().postDelayed(() -> {
            remindersLayout.removeView(checkBox);
            reminderCheckboxes.remove(checkBox);
            savedReminders.remove(checkBox.getText().toString());

            String reminderText = checkBox.getText().toString();

            // Find the specific reminder document to delete
            remindersCollectionRef
                    .whereEqualTo("reminder", reminderText)
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
                        Log.e("DashboardFragment", "Error retrieving reminder document: " + e.getMessage());
                    });
        }, 5000);
    }

    private void updateNoRemindersVisibility() {
        TextView noRemindersTextView = view.findViewById(R.id.text_no_reminders);
        if (reminderCheckboxes.isEmpty() && !isEditTextVisible) { // Check both conditions
            noRemindersTextView.setVisibility(View.VISIBLE);
        } else {
            noRemindersTextView.setVisibility(View.GONE);
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
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String reminderText = documentSnapshot.getString("reminder");
                        addReminder(reminderText);
                    }
                })
                .addOnFailureListener(e -> {
                    // Error handling
                    Log.e("DashboardFragment", "Error retrieving reminders: " + e.getMessage());
                });
    }

    private boolean isReminderAdded(String reminderText) {
        for (CheckBox checkBox : reminderCheckboxes) {
            if (checkBox.getText().toString().equals(reminderText)) {
                return true;
            }
        }
        return false;
    }
}
