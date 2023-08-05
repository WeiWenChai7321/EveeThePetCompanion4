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
import android.view.inputmethod.InputMethodManager;

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
                hideKeyboard(); // Hide the keyboard when canceling
            } else {
                // If the EditText is not visible, show it and change the button text to "Cancel"
                editReminderEditText.setVisibility(View.VISIBLE);
                editReminderEditText.requestFocus();
                newReminderButton.setText(R.string.cancel_button_text);
                showKeyboard(); // Show the keyboard when creating a new reminder
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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (isAdded()) {
            fetchUserNameFromDatabase();
        }
        updateUIWithExistingReminders();
        loadImagesFromFirebaseStorage();
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
                                            Log.e(getString(R.string.dashboardfragment), getString(R.string.error_deleting_reminder) + e.getMessage());
                                        });
                            }
                        })
                        .addOnFailureListener(e -> {
                            // Error handling
                            Log.e(getString(R.string.dashboardfragment), getString(R.string.error_retrieving_reminders) + e.getMessage());
                        });
            }
            updateNoRemindersVisibility();
        }, 5000);
    }

    private void updateNoRemindersVisibility() {
        if (view != null) {
            TextView noRemindersTextView = view.findViewById(R.id.text_no_reminders);
            if (noRemindersTextView != null) {
                if (reminderCheckboxes.isEmpty() && !isEditTextVisible) { // Check both conditions
                    noRemindersTextView.setVisibility(View.VISIBLE);
                } else {
                    noRemindersTextView.setVisibility(View.GONE);
                }
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
        if (!isAdded() || getContext() == null) {
            // Fragment is not attached or context is null, do not proceed
            return;
        }

        remindersCollectionRef.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    savedReminders.clear(); // Clear the savedReminders set
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
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
                    if (getContext() != null) {
                        Log.e(getContext().getString(R.string.dashboardfragment), getContext().getString(R.string.error_retrieving_reminders) + e.getMessage());
                    }
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

        if (remindersLayout != null) {
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
        }

        // Update the visibility of the "no reminder" text when the fragment is resumed
        updateNoRemindersVisibility();
    }

    private void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editReminderEditText, InputMethodManager.SHOW_IMPLICIT);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editReminderEditText.getWindowToken(), 0);
    }

    private void fetchUserNameFromDatabase() {
        if (!isAdded() || getContext() == null) {
            // Fragment is not attached or context is null, do not proceed
            return;
        }
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            // Check if the user is signed in with Google
            for (UserInfo profile : currentUser.getProviderData()) {
                if ("google.com".equals(profile.getProviderId())) {
                    // User signed in with Google, get the Google account name
                    String googleAccountName = profile.getDisplayName();
                    if (googleAccountName != null && !googleAccountName.isEmpty() && view != null) { // Check if the view is not null
                        TextView dashboardTitleTextView = view.findViewById(R.id.dashboard_title);
                        if (dashboardTitleTextView != null) {
                            String greeting = getString(R.string.hi) + " " + googleAccountName;
                            dashboardTitleTextView.setText(greeting);
                        }
                    }
                    return; // Return here to prevent further processing for non-Google sign-in users
                }
            }

            // If not signed in with Google, proceed with fetching the user information from the Firestore database
            DocumentReference userDocRef = db.collection("users").document(currentUserId);
            userDocRef.get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (isAdded() && documentSnapshot.exists() && getContext() != null) { // Check if fragment is attached and context is not null before proceeding
                            String firstName = documentSnapshot.getString(getContext().getString(R.string.firstname));
                            if (firstName != null && !firstName.isEmpty() && view != null) { // Check if the view is not null
                                TextView dashboardTitleTextView = view.findViewById(R.id.dashboard_title);
                                if (dashboardTitleTextView != null) {
                                    String greeting = getString(R.string.hi) + " " + firstName;
                                    dashboardTitleTextView.setText(greeting);
                                }
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Error handling
                        if (getContext() != null) {
                            Log.e(getContext().getString(R.string.dashboardfragment), getContext().getString(R.string.error_fetching_user_information) + e.getMessage());
                        }
                    });
        }
    }

    private void loadImagesFromFirebaseStorage() {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("recent_images");

        storageRef.listAll().addOnSuccessListener(listResult -> {
            List<StorageReference> items = listResult.getItems();
            int numImagesToDisplay = Math.min(items.size(), 7); // Get the minimum of 7 and the number of items in the list

            LinearLayout recentImagesLayout = view.findViewById(R.id.recent_images_layout);
            recentImagesLayout.removeAllViews(); // Clear existing images if any

            for (int i = 0; i < numImagesToDisplay; i++) {
                StorageReference item = items.get(i);
                item.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    ImageView imageView = createImageView(imageUrl);
                    addImageViewToRecentLayout(imageView); // Add the ImageView to the recent layout
                }).addOnFailureListener(exception -> {
                    // Handle the failure, if any.
                    Log.e("DashboardFragment", "Error downloading image: " + exception.getMessage());
                });
            }
        }).addOnFailureListener(exception -> {
            // Handle the failure, if any.
            Log.e("DashboardFragment", "Error listing images: " + exception.getMessage());
        });
    }


    private void addImageViewToRecentLayout(ImageView imageView) {
        LinearLayout recentImagesLayout = requireView().findViewById(R.id.recent_images_layout);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // Add margins to the image views
        int margin = getResources().getDimensionPixelSize(R.dimen.margin_8);
        layoutParams.setMargins(margin, 0, margin, 0);

        imageView.setLayoutParams(layoutParams);
        recentImagesLayout.addView(imageView);
    }

    private ImageView createImageView(String imageUrl) {
        ImageView imageView = new ImageView(requireContext());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                getResources().getDimensionPixelSize(R.dimen.recent_image_width),
                getResources().getDimensionPixelSize(R.dimen.recent_image_height)
        );

        int margin = getResources().getDimensionPixelSize(R.dimen.margin_8);
        layoutParams.setMargins(margin, 0, margin, 0);
        imageView.setLayoutParams(layoutParams);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        // You can use any image loading library or method here to load the image into the ImageView.
        // For example, you can use Glide or Picasso.
        // For simplicity, I'll assume you have a method to load the image into the ImageView directly.
        // Replace 'loadImageIntoImageView' with the actual method to load the image.
        loadImageIntoImageView(imageUrl, imageView);

        return imageView;
    }

    private void loadImageIntoImageView(String imageUrl, ImageView imageView) {
        // Use any image loading library or method here to load the image into the ImageView.
        // For example, you can use Glide or Picasso.
        // Replace 'Glide' with the actual library you're using.
        Glide.with(this).load(imageUrl).into(imageView);
    }


}