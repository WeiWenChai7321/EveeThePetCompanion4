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

    @Override
    public void onReceive(Context context, Intent intent) {
        // Get the notification ID and request code from the intent
        int notificationId = intent.getIntExtra("notificationId", -1);
        int requestCode = intent.getIntExtra("requestCode", -1);

        // Check if the request code is the one for "No thanks"
        if (notificationId != -1 && requestCode == 1) { // Ensure the requestCode matches the one used when creating the PendingIntent
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

