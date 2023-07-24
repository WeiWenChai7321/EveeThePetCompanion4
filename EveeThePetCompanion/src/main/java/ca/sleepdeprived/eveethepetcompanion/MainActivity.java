/*Section: 0NA
  Wei Wen Chai, N01447321
  John Aquino, N01303112
  Jennifer Nguyen, N01435464
  Ubay Abdulaziz, N01437353
*/
package ca.sleepdeprived.eveethepetcompanion;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private boolean isLoggedIn = false;
    private BottomNavigationView bottomNavigationView;
    private PetInfoViewModel petInfoViewModel; // Added

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        if (menu instanceof MenuBuilder) {
            ((MenuBuilder) menu).setOptionalIconsVisible(true);
        }

        MenuItem actionPetProfileItem = menu.findItem(R.id.action_petprofile);
        View actionPetProfileView = actionPetProfileItem.getActionView();
        actionPetProfileView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOptionsItemSelected(actionPetProfileItem);
            }
        });

        return true;
    }

    private void uncheckAllItems() {
        bottomNavigationView.getMenu().setGroupCheckable(0, true, false);
        for (int i = 0; i < bottomNavigationView.getMenu().size(); i++) {
            MenuItem item = bottomNavigationView.getMenu().getItem(i);
            item.setChecked(false);
        }
        bottomNavigationView.getMenu().setGroupCheckable(0, true, true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                uncheckAllItems();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new SettingsFragment()).commit();
                return true;

            case R.id.action_petprofile:
                uncheckAllItems();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new PetProfileFragment()).commit();
                return true;

            case R.id.action_help:
                uncheckAllItems();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new HelpFragment()).commit();
                return true;

            case R.id.action_feedback:
                uncheckAllItems();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new FeedbackFragment()).commit();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onPetProfileClicked(View view) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new PetProfileFragment()).commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);

        // Set the initial selected item (e.g., Dashboard)
        bottomNavigationView.setSelectedItemId(R.id.nav_dashboard);
        isLoggedIn = checkLoginStatus(); // Add this line to check login status

        if (isLoggedIn) {
            showMainActivity();
        } else {
            startLoginActivity();
        }

        petInfoViewModel = new ViewModelProvider(this).get(PetInfoViewModel.class); // Added
        showReviewNotification(); // Added
    }

    private boolean checkLoginStatus() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        return preferences.getBoolean(getString(R.string.isloggedin), false);
    }

    private void showMainActivity() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new DashboardFragment()).commit();
    }

    private void startLoginActivity() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;
                    switch (item.getItemId()) {
                        case R.id.nav_dashboard:
                            selectedFragment = new DashboardFragment();
                            break;
                        case R.id.nav_stream:
                            selectedFragment = new StreamFragment();
                            break;
                        case R.id.nav_highlights:
                            selectedFragment = new HighlightsFragment();
                            break;
                    }
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            selectedFragment).commit();
                    return true;
                }
            };

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(getString(R.string.exit_app))
                .setMessage(getString(R.string.are_you_sure_you_want_to_exit))
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton(getString(R.string.no), null)
                .show();
    }

    // Add this method inside MainActivity class
    public PetInfoViewModel getPetInfoViewModel() {
        return petInfoViewModel;
    }

    private void showReviewNotification() {
        long appStartTimeInMillis = SystemClock.elapsedRealtime();
        long oneHourInMillis = 60 * 60 * 1000;

        if (appStartTimeInMillis >= oneHourInMillis) {
            String CHANNEL_ID = "app_review_channel";
            int notificationId = 1;

            // Create an Intent for the "Okay" action (leads to FeedbackFragment)
            Intent okayIntent = new Intent(this, FeedbackFragment.class);
            PendingIntent okayPendingIntent = PendingIntent.getActivity(this, 0,
                    okayIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            // Create an Intent for the "No thanks" action (disable the notification)
            Intent noThanksIntent = new Intent(this, DisableNotificationReceiver.class);
            PendingIntent noThanksPendingIntent = PendingIntent.getBroadcast(this, 0,
                    noThanksIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            // Build the notification
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.notification_icon)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(getString(R.string.leave_us_a_review))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(okayPendingIntent)
                    .addAction(0, getString(R.string.no_thanks), noThanksPendingIntent)
                    .addAction(0, getString(R.string.okay), okayPendingIntent)
                    .setAutoCancel(true);

            // Create the notification channel
            createNotificationChannel();

            // Create the notification manager and display the notification
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(notificationId, notificationBuilder.build());
        }
    }

    // Create the notification channel
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(getString(R.string.app_review_channel), name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}