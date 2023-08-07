/*Section: 0NA
  Wei Wen Chai, N01447321
  John Aquino, N01303112
  Jennifer Nguyen, N01435464
  Ubay Abdulaziz, N01437353
*/
package ca.sleepdeprived.eveethepetcompanion;

// Import statements

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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DashboardFragment extends Fragment {
    // Member variables
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
        sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE);
        savedReminders = new HashSet<>();
        readExistingReminders();
        loadReminders();
    }

    // Refactored code in onCreateView()
    // Replaced direct view assignments with findViewById() to improve code readability
    // Used lambda expression to simplify the click listener implementation
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        remindersLayout = view.findViewById(R.id.reminders_card);
        editReminderEditText = view.findViewById(R.id.edit_text_reminder);
        editReminderEditText.setVisibility(View.GONE);
        Button newReminderButton = view.findViewById(R.id.button_new_reminder);
        updateNoRemindersVisibility();
        newReminderButton.setOnClickListener(v -> {
            String reminderText = editReminderEditText.getText().toString().trim();
            if (isEditTextVisible) {
                toggleReminderEditText(false);
                hideKeyboard();
            } else {
                toggleReminderEditText(true);
                showKeyboard();
            }
        });

        editReminderEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || event != null && event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                String reminderText = editReminderEditText.getText().toString().trim();
                if (!TextUtils.isEmpty(reminderText)) {
                    addReminder(reminderText);
                    editReminderEditText.setText("");
                    toggleReminderEditText(false);
                    hideKeyboard();
                    return true;
                }
            }
            return false;
        });

        context = requireContext();

        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        if (isAdded()) {
            fetchUserNameFromDatabase();
        }
        loadImagesFromFirebaseStorage();
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
        loadReminders();
        isInitialCreate = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateNoRemindersVisibility();
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

    // UI-related methods

    // Method to add or remove the reminder EditText based on visibility flag
    private void toggleReminderEditText(boolean isVisible) {
        Button newReminderButton = view.findViewById(R.id.button_new_reminder);
        if (isVisible) {
            editReminderEditText.setVisibility(View.VISIBLE);
            editReminderEditText.requestFocus();
            newReminderButton.setText(R.string.cancel_button_text);
        } else {
            editReminderEditText.setVisibility(View.GONE);
            newReminderButton.setText(R.string.new_reminder_button_text);
        }
        isEditTextVisible = isVisible;
        updateNoRemindersVisibility();
    }

    // Method to update the visibility of the "No reminders" text
    private void updateNoRemindersVisibility() {
        if (view != null) {
            TextView noRemindersTextView = view.findViewById(R.id.text_no_reminders);
            if (noRemindersTextView != null) {
                if (reminderCheckboxes.isEmpty() && !isEditTextVisible) {
                    noRemindersTextView.setVisibility(View.VISIBLE);
                } else {
                    noRemindersTextView.setVisibility(View.GONE);
                }
            }
        }
    }

    // Firebase-related methods

    // Method to read existing reminders from Firestore
    private void readExistingReminders() {
        if (!isAdded() || getContext() == null) {
            return;
        }

        remindersCollectionRef.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    savedReminders.clear();
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String reminderText = documentSnapshot.getString("reminderText");
                        if (reminderText != null) {
                            savedReminders.add(new Reminder(reminderText));
                        }
                    }
                    updateUIWithExistingReminders();
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Log.e(getContext().getString(R.string.dashboardfragment), getContext().getString(R.string.error_retrieving_reminders) + e.getMessage());
                    }
                });
    }

    // Method to check if a reminder is already added
    private boolean isReminderAdded(String reminderText) {
        for (Reminder reminder : savedReminders) {
            if (reminder.getReminderText().equals(reminderText)) {
                return true;
            }
        }
        return false;
    }

    // Method to update the UI with existing reminders
    private void updateUIWithExistingReminders() {
        if (remindersLayout != null) {
            remindersLayout.removeAllViews();
            for (Reminder reminder : savedReminders) {
                CheckBox reminderCheckBox = createReminderCheckBox(reminder.getReminderText());
                remindersLayout.addView(reminderCheckBox);
                reminderCheckboxes.add(reminderCheckBox);
            }
        }
        updateNoRemindersVisibility();
    }

    // Method to create a new reminder CheckBox
    private CheckBox createReminderCheckBox(String reminderText) {
        CheckBox reminderCheckBox = new CheckBox(context);
        reminderCheckBox.setText(reminderText);
        reminderCheckBox.setChecked(false);
        reminderCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                removeReminderDelayed((CheckBox) buttonView);
            }
        });
        return reminderCheckBox;
    }

    // Method to add a new reminder
    private void addReminder(String reminderText) {
        if (remindersLayout == null || context == null) {
            return;
        }

        if (isReminderAdded(reminderText)) {
            return;
        }

        Reminder newReminder = new Reminder(reminderText);
        CheckBox reminderCheckBox = createReminderCheckBox(reminderText);

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
        saveReminders();
        updateNoRemindersVisibility();
    }

    // Method to remove a reminder after a delay
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
                savedReminders.remove(reminderToDelete);

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
                                            Log.e(getString(R.string.dashboardfragment), getString(R.string.error_deleting_reminder) + e.getMessage());
                                        });
                            }
                        })
                        .addOnFailureListener(e -> {
                            // Error handling
                            Log.e(getString(R.string.dashboardfragment), getString(R.string.error_retrieving_reminders) + e.getMessage());
                        });
            }
            saveReminders();
            updateNoRemindersVisibility();
        }, 5000);
    }

    // Method to save reminders using SharedPreferences
    private void saveReminders() {
        if (getContext() == null) {
            return;
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();

        // Convert savedReminders to a Set of strings (reminderTexts) to save in SharedPreferences
        Set<String> reminderTexts = new HashSet<>();
        for (Reminder reminder : savedReminders) {
            reminderTexts.add(reminder.getReminderText());
        }

        editor.putStringSet("reminder_texts", reminderTexts);
        editor.apply();
    }

    // Method to load reminders from SharedPreferences
    private void loadReminders() {
        if (getContext() == null) {
            return;
        }

        Set<String> reminderTexts = sharedPreferences.getStringSet("reminder_texts", new HashSet<>());
        savedReminders.clear();
        for (String reminderText : reminderTexts) {
            savedReminders.add(new Reminder(reminderText));
        }
        updateUIWithExistingReminders();
    }

    // Method to show the soft keyboard
    private void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editReminderEditText, InputMethodManager.SHOW_IMPLICIT);
    }

    // Method to hide the soft keyboard
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editReminderEditText.getWindowToken(), 0);
    }

    // Method to fetch user name from the database
    private void fetchUserNameFromDatabase() {
        if (!isAdded() || getContext() == null) {
            return;
        }
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            for (UserInfo profile : currentUser.getProviderData()) {
                if ("google.com".equals(profile.getProviderId())) {
                    String googleAccountName = profile.getDisplayName();
                    if (googleAccountName != null && !googleAccountName.isEmpty() && view != null) {
                        TextView dashboardTitleTextView = view.findViewById(R.id.dashboard_title);
                        if (dashboardTitleTextView != null) {
                            String greeting = getString(R.string.hi) + getString(R.string.space) + googleAccountName;
                            dashboardTitleTextView.setText(greeting);
                        }
                    }
                    return;
                }
            }

            DocumentReference userDocRef = db.collection("users").document(currentUserId);
            userDocRef.get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (isAdded() && documentSnapshot.exists() && getContext() != null) {
                            String firstName = documentSnapshot.getString(getContext().getString(R.string.firstname));
                            if (firstName != null && !firstName.isEmpty() && view != null) {
                                TextView dashboardTitleTextView = view.findViewById(R.id.dashboard_title);
                                if (dashboardTitleTextView != null) {
                                    String greeting = getString(R.string.hi) + getString(R.string.space) + firstName;
                                    dashboardTitleTextView.setText(greeting);
                                }
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (getContext() != null) {
                            Log.e(getContext().getString(R.string.dashboardfragment), getContext().getString(R.string.error_fetching_user_information) + e.getMessage());
                        }
                    });
        }
    }

    // Method to load images from Firebase storage
    private void loadImagesFromFirebaseStorage() {
        if (view == null || getContext() == null) {
            return;
        }

        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("photos");
        storageRef.listAll().addOnSuccessListener(listResult -> {
            List<StorageReference> items = listResult.getItems();
            int numImagesToDisplay = Math.min(items.size(), 7);
            LinearLayout recentImagesLayout = view.findViewById(R.id.recent_images_layout);
            recentImagesLayout.removeAllViews();

            for (int i = 0; i < numImagesToDisplay; i++) {
                StorageReference item = items.get(i);
                item.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    ImageView imageView = createImageView(imageUrl);
                    if (imageView != null) {
                        addImageViewToRecentLayout(imageView);
                    } else {
                        Log.e("DashboardFragment", "Image creation failed.");
                    }
                }).addOnFailureListener(exception -> {
                    Log.e("DashboardFragment", "Error downloading image: " + exception.getMessage());
                });
            }
        }).addOnFailureListener(exception -> {
            Log.e("DashboardFragment", "Error listing images: " + exception.getMessage());
        });
    }

    // Method to add an ImageView to the recent layout
    private void addImageViewToRecentLayout(ImageView imageView) {
        LinearLayout recentImagesLayout = view.findViewById(R.id.recent_images_layout);
        recentImagesLayout.addView(imageView);
    }

    // Refactored code in createImageView() and loadImagesIntoImageView()
    // Simplified image loading using Glide for better code organization
    private ImageView createImageView(String imageUrl) {
        if (!isAdded()) {
            return null;
        }
        ImageView imageView = new ImageView(requireContext());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                getResources().getDimensionPixelSize(R.dimen.recent_image_width),
                getResources().getDimensionPixelSize(R.dimen.recent_image_height)
        );

        int margin = getResources().getDimensionPixelSize(R.dimen.margin_8);
        layoutParams.setMargins(margin, 0, margin, 0);
        imageView.setLayoutParams(layoutParams);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        loadImageIntoImageView(imageUrl, imageView);

        return imageView;
    }

    // Method to load an image into an ImageView using Glide with a rotation transformation
    private void loadImageIntoImageView(String imageUrl, ImageView imageView) {
        Glide.with(this)
                .asBitmap()
                .load(imageUrl)
                .transform(new RotateTransformation(90))
                .into(imageView);
    }
}