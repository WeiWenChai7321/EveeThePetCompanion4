/*Section: 0NA
  Wei Wen Chai, N01447321
  John Aquino, N01303112
  Jennifer Nguyen, N01435464
  Ubay Abdulaziz, N01437353
*/
package ca.sleepdeprived.eveethepetcompanion;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;


import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class HelpFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_help, container, false);
        Button contactUsButton = view.findViewById(R.id.contact_us_button);
        contactUsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmail();
            }
        });

        // Find the FAB button by its ID
        FloatingActionButton fabScrollToTop = view.findViewById(R.id.fabScrollToTop);

        ScrollView scrollView = view.findViewById(R.id.nestedScrollView);

        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                boolean canScrollUp = scrollView.canScrollVertically(-1); // -1 for up direction

                // Show or hide the FAB button based on scroll position
                fabScrollToTop.setVisibility(canScrollUp ? View.VISIBLE : View.INVISIBLE);
            }
        });

        // Set an OnClickListener for the FAB button
        fabScrollToTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Scroll the NestedScrollView back to the top
                scrollView.smoothScrollTo(0, 0);
            }
        });
        return view;
    }

    public void sendEmail() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse(getString(R.string.mailto_chloeissleeping_gmail_com)));
        startActivity(Intent.createChooser(intent, getString(R.string.send_email)));
    }

}
