/*Section: 0NA
  Wei Wen Chai, N01447321
  John Aquino, N01303112
  Jennifer Nguyen, N01435464
  Ubay Abdulaziz, N01437353
*/

package ca.sleepdeprived.eveethepetcompanion;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private SignInButton googleSignInButton;
    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameEditText = findViewById(R.id.username_edittext);
        passwordEditText = findViewById(R.id.password_edittext);
        loginButton = findViewById(R.id.login_button);
        googleSignInButton = findViewById(R.id.google_sign_in_button); // Add this line

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

                // Replace with your login logic
                boolean isLoggedIn = performLogin(username, password);

                if (isLoggedIn) {
                    navigateToMainActivity();
                }
            }
        });

        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInWithGoogle();
            }
        });

        try {
            googleSignInClient.silentSignIn().addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    // Silent sign-in successful, handle the signed-in account
                    GoogleSignInAccount account = task.getResult();
                    handleGoogleSignInSuccess(account);
                } else {
                    // Silent sign-in failed, handle the error
                    Exception exception = task.getException();
                    if (exception instanceof ApiException) {
                        ApiException apiException = (ApiException) exception;
                        int statusCode = apiException.getStatusCode();
                        // Handle the specific error status code
                        Toast.makeText(this, "Google Sign-In failed: " + statusCode, Toast.LENGTH_SHORT).show();
                    } else {
                        // Handle other exceptions
                        Toast.makeText(this, "Google Sign-In failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (Exception e) {
            // Handle any exceptions
            Toast.makeText(this, "Google Sign-In failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    private boolean performLogin(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            // Display a Toast message for empty fields
            Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!username.equals(getString(R.string.admin)) || !password.equals(getString(R.string.loginpassword))) {
            // Display a Toast message for incorrect username/password
            Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Set the isLoggedIn flag in SharedPreferences
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(getString(R.string.isloggedin), true);
        editor.apply();

        return true;
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish(); // Finish LoginActivity to prevent going back to it
    }

    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleGoogleSignInResult(task);
        }
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> task) {
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            // Signed in successfully, navigate to MainActivity or perform additional actions
            // with the account information
            handleGoogleSignInSuccess(account);
        } catch (ApiException e) {
            // Google Sign-In failed, update UI accordingly
            handleGoogleSignInFailure(e);
        }
    }

    private void handleGoogleSignInSuccess(GoogleSignInAccount account) {
        // Perform any necessary actions after a successful Google Sign-In
        // For example, you could authenticate the user on your backend server
        String email = account.getEmail();
        Toast.makeText(this, "Signed in with Google: " + email, Toast.LENGTH_SHORT).show();

        navigateToMainActivity();
    }

    private void handleGoogleSignInFailure(ApiException e) {
        // Google Sign-In failed, display an error message or handle the failure
        // You can use the exception's status code to determine the specific error
        Toast.makeText(this, "Google Sign-In failed: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
    }
}
