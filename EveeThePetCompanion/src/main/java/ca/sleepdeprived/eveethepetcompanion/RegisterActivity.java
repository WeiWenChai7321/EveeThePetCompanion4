/*Section: 0NA
  Wei Wen Chai, N01447321
  John Aquino, N01303112
  Jennifer Nguyen, N01435464
  Ubay Abdulaziz, N01437353
*/

package ca.sleepdeprived.eveethepetcompanion;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private Button signUpButton;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = FirebaseFirestore.getInstance();

        emailEditText = findViewById(R.id.email_edittext);
        passwordEditText = findViewById(R.id.password_edittext);
        confirmPasswordEditText = findViewById(R.id.confirm_password_edittext);
        signUpButton = findViewById(R.id.sign_up_button);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }

    private void registerUser() {
        final String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(RegisterActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
        } else if (!password.equals(confirmPassword)) {
            Toast.makeText(RegisterActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
        } else {
            // Check if the email already exists in the database
            db.collection("users")
                    .document(email)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document != null && document.exists()) {
                                    // Email doesn't exist, proceed with account creation
                                    createAccount(email);
                                } else {
                                    // Email already exists in the database, show error message
                                    Toast.makeText(RegisterActivity.this, "Account already exists with this email", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                // Error occurred while accessing the database, show error message
                                Toast.makeText(RegisterActivity.this, "Failed to access the database. Please try again.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void createAccount(String email) {
        String password = passwordEditText.getText().toString().trim();

        // Create a new document in the "users" collection with the email as the document ID
        db.collection("users")
                .document(email)
                .set(new User(email, password))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Account creation successful, show success message
                            Toast.makeText(RegisterActivity.this, "Account created successfully", Toast.LENGTH_SHORT).show();

                            // Navigate to LoginActivity
                            navigateToLoginActivity();
                        } else {
                            // Error occurred while creating the account, show error message
                            Toast.makeText(RegisterActivity.this, "Failed to create account. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    private void navigateToLoginActivity() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
    }
}