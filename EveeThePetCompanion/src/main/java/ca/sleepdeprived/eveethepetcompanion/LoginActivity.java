/*Section: 0NA
  Wei Wen Chai, N01447321
  John Aquino, N01303112
  Jennifer Nguyen, N01435464
  Ubay Abdulaziz, N01437353
*/

package ca.sleepdeprived.eveethepetcompanion;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private TextView signUpTextView;
    private SignInButton googleSignInButton;
    private GoogleSignInClient googleSignInClient;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameEditText = findViewById(R.id.username_edittext);
        passwordEditText = findViewById(R.id.password_edittext);
        loginButton = findViewById(R.id.login_button);
        signUpTextView = findViewById(R.id.sign_up_textview);
        googleSignInButton = findViewById(R.id.google_sign_in_button);

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                // Perform login validation against the Firestore database
                validateLogin(username, password);
            }
        });

        signUpTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToRegisterActivity();
            }
        });

        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInWithGoogle();
            }
        });

        // Check if the user is already signed in with Google
        try {
            googleSignInClient.silentSignIn().addOnCompleteListener(this, new OnCompleteListener<GoogleSignInAccount>() {
                @Override
                public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                    if (task.isSuccessful()) {
                        GoogleSignInAccount account = task.getResult();
                        handleGoogleSignInSuccess(account);
                    } else {
                        Exception exception = task.getException();
                        if (exception instanceof ApiException) {
                            ApiException apiException = (ApiException) exception;
                            int statusCode = apiException.getStatusCode();
                            Toast.makeText(LoginActivity.this, "Google Sign-In failed: " + statusCode, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(LoginActivity.this, "Google Sign-In failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        } catch (Exception e) {
            Toast.makeText(LoginActivity.this, "Google Sign-In failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void validateLogin(final String email, final String password) {
        Snackbar loggingInSnackbar = Snackbar.make(findViewById(android.R.id.content), getString(R.string.signing_in), Snackbar.LENGTH_INDEFINITE);
        loggingInSnackbar.show();
        // Query the "users" collection for the document with the matching email
        db.collection("users")
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        dismissSnackbar(loggingInSnackbar);
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                // User with matching email found
                                DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                                String storedPassword = document.getString("password");
                                if (storedPassword != null && storedPassword.equals(password)) {
                                    // Password matches, perform successful login
                                    performSuccessfulLogin();
                                } else {
                                    // Password doesn't match, show error message
                                    showSnackbar(getString(R.string.invalid_password));
                                }
                            } else {
                                // User not found with the given email, show error message
                                showSnackbar(getString(R.string.account_not_found));
                            }
                        } else {
                            // Error occurred while accessing the database, show error message
                            showSnackbar(getString(R.string.login_failed));
                        }
                    }
                });
    }
    private void dismissSnackbar(Snackbar snackbar) {
        if (snackbar != null && snackbar.isShown()) {
            snackbar.dismiss();
        }
    }

    private void showSnackbar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
    }


    private void performSuccessfulLogin() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(getString(R.string.isloggedin), true);
        editor.apply();

        navigateToMainActivity();
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleGoogleSignInResult(task);
        }
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> task) {
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            handleGoogleSignInSuccess(account);
        } catch (ApiException e) {
            handleGoogleSignInFailure(e);
        }
    }

    private void handleGoogleSignInSuccess(GoogleSignInAccount account) {
        String email = account.getEmail();
        Toast.makeText(this, "Signed in with Google: " + email, Toast.LENGTH_SHORT).show();
        navigateToMainActivity();
    }

    private void handleGoogleSignInFailure(ApiException e) {
        Toast.makeText(this, "Google Sign-In failed: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
    }

    private void navigateToRegisterActivity() {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }
}