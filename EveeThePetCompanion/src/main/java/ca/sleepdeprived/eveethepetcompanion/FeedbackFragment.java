/*Section: 0NA
  Wei Wen Chai, N01447321
  John Aquino, N01303112
  Jennifer Nguyen, N01435464
  Ubay Abdulaziz, N01437353
*/

package ca.sleepdeprived.eveethepetcompanion;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class FeedbackFragment extends Fragment {

    private EditText editName;

    private ProgressBar progressBar;
    private EditText editPhone;
    private EditText editEmail;
    private EditText editComment;
    private RatingBar ratingBar;
    private Button btnSubmitFeedback;
    private FirebaseFirestore db;
    private List<Feedback> offlineFeedbackList;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize the list to store offline feedback data
        offlineFeedbackList = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feedback, container, false);

        editName = view.findViewById(R.id.edit_name);
        editPhone = view.findViewById(R.id.edit_phone);
        editEmail = view.findViewById(R.id.edit_email);
        editComment = view.findViewById(R.id.edit_comment);
        ratingBar = view.findViewById(R.id.rating_bar);
        db = FirebaseFirestore.getInstance();
        progressBar = view.findViewById(R.id.progress_bar);
        btnSubmitFeedback = view.findViewById(R.id.btn_submit_feedback);

        btnSubmitFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the user input
                String name = editName.getText().toString();
                String phone = editPhone.getText().toString();
                String email = editEmail.getText().toString();
                String comment = editComment.getText().toString();
                float rating = ratingBar.getRating();

                // Validate phone number
                if (!isValidPhoneNumber(phone)) {
                    editPhone.setError(getString(R.string.invalid_phone));
                    return;
                }

                // Validate email
                if (!isValidEmail(email)) {
                    editEmail.setError(getString(R.string.invalid_email));
                    return;
                }

                // Get the device model programmatically
                String deviceModel = android.os.Build.MODEL;

                // Create a new feedback document with the captured data
                Feedback feedback = new Feedback(name, phone, email, comment, rating, deviceModel);

                // Display the progress bar and disable the submit button
                progressBar.setVisibility(View.VISIBLE);
                btnSubmitFeedback.setEnabled(false);

                // Delay the submission for a few seconds
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Proceed with saving the feedback
                        saveFeedbackToFirestore(feedback);
                    }
                }, 2000);  // 2000 milliseconds = 2 seconds
            }
        });

        return view;
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        String phoneNumberPattern = "\\d{3}-\\d{3}-\\d{4}";
        return phoneNumber.matches(phoneNumberPattern);
    }

    private boolean isValidEmail(String email) {
        // Use the built-in Patterns class to validate the email format
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    @Override
    public void onResume() {
        super.onResume();

        // Check if there is an internet connection
        boolean isConnected = checkInternetConnection();

        // If there is an internet connection, attempt to submit the offline feedback
        if (isConnected) {
            submitOfflineFeedback();
        }
    }

    private void saveFeedbackToFirestore(Feedback feedback) {
        // Check for internet connection
        boolean isConnected = checkInternetConnection();

        if (isConnected) {
            // If there is an internet connection, proceed with saving the feedback to Firestore
            db.collection(getString(R.string.feedback_collection))
                    .add(feedback)
                    .addOnSuccessListener(documentReference -> {
                        // Feedback saved successfully
                        showSnackbar(getString(R.string.feedback_submitted));

                        // Reset the form
                        editName.setText("");
                        editPhone.setText("");
                        editEmail.setText("");
                        editComment.setText("");
                        ratingBar.setRating(0);

                        // Once feedback is saved, hide the progress bar and enable the submit button
                        progressBar.setVisibility(View.GONE);
                        btnSubmitFeedback.setEnabled(true);

                        // Attempt to submit any offline feedback data
                        submitOfflineFeedback();
                    })
                    .addOnFailureListener(e -> {
                        // Failed to save feedback
                        showSnackbar(getString(R.string.failed_to_submit_feedback));

                        // Once feedback submission has failed, hide the progress bar and enable the submit button
                        progressBar.setVisibility(View.GONE);
                        btnSubmitFeedback.setEnabled(true);

                        // Save the feedback data to the offline list
                        offlineFeedbackList.add(feedback);

                        // Show a toast or snackbar to inform the user that the feedback is saved offline
                        Toast.makeText(getActivity(), R.string.feedback_saved_offline, Toast.LENGTH_SHORT).show();
                    });
        } else {
            // If there is no internet connection, save the feedback data to the offline list
            offlineFeedbackList.add(feedback);

            // Show a toast or snackbar to inform the user that the feedback is saved offline
            Toast.makeText(getActivity(), R.string.feedback_saved_offline, Toast.LENGTH_SHORT).show();
        }
    }


    private void showSnackbar(String message) {
        Snackbar.make(getView(), message, Snackbar.LENGTH_SHORT).show();
    }

    // Method to submit offline feedback data
    private void submitOfflineFeedback() {
        if (!offlineFeedbackList.isEmpty()) {
            // Iterate through the offlineFeedbackList and attempt to submit each feedback
            for (Feedback feedback : offlineFeedbackList) {
                // Attempt to save feedback to Firestore
                saveFeedbackToFirestore(feedback);
            }
            // Clear the list after submission
            offlineFeedbackList.clear();
        }
    }

    private boolean checkInternetConnection() {
        // Get the ConnectivityManager from the system service
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        // Check if the ConnectivityManager is not null
        if (connectivityManager != null) {
            // Get the active network information
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

            // Check if the network is connected and available
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }

        // Return false if the ConnectivityManager is null (unable to check connectivity)
        return false;
    }
}