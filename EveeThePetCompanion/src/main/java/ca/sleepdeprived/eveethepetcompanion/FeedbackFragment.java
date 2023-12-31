/*Section: 0NA
  Wei Wen Chai, N01447321
  John Aquino, N01303112
  Jennifer Nguyen, N01435464
  Ubay Abdulaziz, N01437353
*/

package ca.sleepdeprived.eveethepetcompanion;

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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;

public class FeedbackFragment extends Fragment {

    private EditText editName;
    private EditText editPhone;
    private EditText editEmail;
    private EditText editComment;
    private RatingBar ratingBar;
    private Button btnSubmitFeedback;
    private FirebaseFirestore db;
    private ProgressBar progressBar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        btnSubmitFeedback.setOnClickListener(v -> onSubmitFeedback());

        return view;
    }

    //Moved the logic of submitting feedback to a separate method onSubmitFeedback() for better readability and code organization.
    //Was previously under btnSubmitFeedback.setonClickListener
    public void onSubmitFeedback() {
        // Get the user input
        String name = editName.getText().toString();
        String phone = editPhone.getText().toString();
        String email = editEmail.getText().toString();
        String comment = editComment.getText().toString();
        float rating = ratingBar.getRating();

        // Check if any field is empty and display an error message if necessary
        if (name.isEmpty()) {
            editName.setError(getString(R.string.empty_field));
            return;
        }

        if (phone.isEmpty()) {
            editPhone.setError(getString(R.string.empty_field));
            return;
        }

        if (email.isEmpty()) {
            editEmail.setError(getString(R.string.empty_field));
            return;
        }

        if (comment.isEmpty()) {
            editComment.setError(getString(R.string.empty_field));
            return;
        }

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
        new Handler().postDelayed(() -> {
            // Proceed with saving the feedback
            saveFeedbackToFirestore(feedback);
        }, 2000);  // 2000 milliseconds = 2 seconds
    }

    public boolean isValidPhoneNumber(String phoneNumber) {
        String phoneNumberPattern = "\\d{3}\\d{3}\\d{4}";
        return phoneNumber.matches(phoneNumberPattern);
    }

    public boolean isValidEmail(String email) {
        return isValidEmailFormat(email);
    }

    protected boolean isValidEmailFormat(String email) {
        // Use a regular expression pattern for email validation
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        return email.matches(emailPattern);
    }

    private void saveFeedbackToFirestore(Feedback feedback) {
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
                })
                .addOnFailureListener(e -> {
                    // Failed to save feedback
                    showSnackbar(getString(R.string.failed_to_submit_feedback));

                    // Once feedback submission has failed, hide the progress bar and enable the submit button
                    progressBar.setVisibility(View.GONE);
                    btnSubmitFeedback.setEnabled(true);
                });
    }

    private void showSnackbar(String message) {
        Snackbar.make(getView(), message, Snackbar.LENGTH_SHORT).show();
    }

}
