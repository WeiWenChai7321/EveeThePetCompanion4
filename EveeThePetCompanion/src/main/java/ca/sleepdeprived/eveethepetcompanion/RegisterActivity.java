/*Section: 0NA
Wei Wen Chai, N01447321
John Aquino, N01303112
Jennifer Nguyen, N01435464
Ubay Abdulaziz, N01437353
*/

package ca.sleepdeprived.eveethepetcompanion;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private EditText phoneEditText;
    private EditText firstnameEditText;
    private EditText lastnameEditText;
    private Button signUpButton; //Refactoring: Renamed the SignUpButton to signUpButton to follow Java naming conventions.

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private PetInfoViewModel petInfoViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        petInfoViewModel = new ViewModelProvider(this).get(PetInfoViewModel.class);

        emailEditText = findViewById(R.id.email_edittext);
        passwordEditText = findViewById(R.id.password_edittext);
        confirmPasswordEditText = findViewById(R.id.confirm_password_edittext);
        phoneEditText = findViewById(R.id.phone_edittext);
        signUpButton = findViewById(R.id.sign_up_button);
        firstnameEditText = findViewById(R.id.firstname_edittext);
        lastnameEditText = findViewById(R.id.lastname_edittext);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInput()) {
                    checkEmailAndRegister();
                }
            }
        });
    }

    private boolean validateInput() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();
        String phoneNumber = phoneEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || phoneNumber.isEmpty()) {
            Toast.makeText(this, R.string.please_fill_fields, Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, R.string.passwds_not_match, Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!isValidPassword(password)) {
            Toast.makeText(this, R.string.invalid_password, Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, R.string.invalid_email, Toast.LENGTH_SHORT).show();
            return false;
        }

        if (phoneNumber.length() != 10 || !Patterns.PHONE.matcher(phoneNumber).matches()) {
            Toast.makeText(this, R.string.invalid_phone_number, Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

<<<<<<< HEAD
    private boolean isValidPassword(String password) {
=======
    public static boolean isValidPassword(String password) {
>>>>>>> master
        if (password.length() < 6) return false;
        if (!password.matches(".*[A-Z].*")) return false; // Check for an uppercase letter
        if (!password.matches(".*[0-9].*")) return false; // Check for a digit
        if (!password.matches(".*[^a-zA-Z0-9 ].*")) return false; // Check for a special character
        return true;
    }

    private void checkEmailAndRegister() {
        final String email = emailEditText.getText().toString().trim();
        final String password = passwordEditText.getText().toString().trim();

        firebaseAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
            @Override
            public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                if (task.isSuccessful()) {
                    SignInMethodQueryResult result = task.getResult();
                    if (result != null && result.getSignInMethods() != null && result.getSignInMethods().size() > 0) {
                        Toast.makeText(RegisterActivity.this, R.string.account_already_exists, Toast.LENGTH_SHORT).show();
                    } else {
                        createAccount(email, password);
                    }
                } else {
                    Toast.makeText(RegisterActivity.this, R.string.failed_access_firebase, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void createAccount(final String email, final String password) {
        final String firstName = firstnameEditText.getText().toString().trim();
        final String lastName = lastnameEditText.getText().toString().trim();

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Account creation successful, show success message
                            Toast.makeText(RegisterActivity.this, R.string.account_created_successfully, Toast.LENGTH_SHORT).show();
                            saveUserToFirestore(email, password, firstName, lastName);
                            navigateToLoginActivity();
                        } else {
                            // Error occurred while creating the account, show error message
                            Toast.makeText(RegisterActivity.this, R.string.failed_create_account, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void saveUserToFirestore(String email, String password, String firstName, String lastName) {
        String uid = firebaseAuth.getCurrentUser().getUid();
        User user = new User(email, password, firstName, lastName);

        // Save user information to Firestore
        db.collection("users")
                .document(uid)
                .set(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // User data saved to Firestore
                        } else {
                            // Error occurred while saving user data, show error message
                            Toast.makeText(RegisterActivity.this, R.string.failed_save_user_data, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void navigateToLoginActivity() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
    }
}
