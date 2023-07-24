/*Section: 0NA
  Wei Wen Chai, N01447321
  John Aquino, N01303112
  Jennifer Nguyen, N01435464
  Ubay Abdulaziz, N01437353
*/
package ca.sleepdeprived.eveethepetcompanion;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            // Check for internet connection
            boolean isConnected = checkInternetConnection(context);

            // Update UI based on the network connectivity status
            updateUIBasedOnConnectivity(context, isConnected);
        }
    }

    private boolean checkInternetConnection(Context context) {
        // Get the ConnectivityManager from the system service
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

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

    private void updateUIBasedOnConnectivity(Context context, boolean isConnected) {
        if (isConnected) {
            // If there is an internet connection, attempt to submit the offline feedback
            FeedbackFragment fragment = (FeedbackFragment) getVisibleFragment(context);
            if (fragment != null) {
                fragment.submitOfflineFeedback();
            }
        } else {
            // If there is no internet connection, show a toast message or snackbar to inform the user
            Toast.makeText(context, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
        }
    }

    // Helper method to get the currently visible fragment
    private Fragment getVisibleFragment(Context context) {
        if (context instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) context;
            return mainActivity.getVisibleFragment();
        }
        return null;
    }
}
