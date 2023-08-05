/* Section: 0NA
   Wei Wen Chai, N01447321
   John Aquino, N01303112
   Jennifer Nguyen, N01435464
   Ubay Abdulaziz, N01437353
*/
package ca.sleepdeprived.eveethepetcompanion;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.core.app.NotificationManagerCompat;

public class DisableNotificationReceiver extends BroadcastReceiver {
    private static final String EXTRA_NOTIFICATION_ID = "notificationid";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Get the notification ID from the intent
        int notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1);

        // Cancel the notification
        if (notificationId != -1) {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.cancel(notificationId);

            // Disable the notification from appearing in the future
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(context.getString(R.string.show_review_notification), false);
            editor.apply();
        }
    }
}

