/*Section: 0NA
  Wei Wen Chai, N01447321
  John Aquino, N01303112
  Jennifer Nguyen, N01435464
  Ubay Abdulaziz, N01437353
*/
package ca.sleepdeprived.eveethepetcompanion;

import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class StreamFragment extends Fragment {

    private ImageButton btnObstacleAvoidance;
    private ImageButton btnLineFollowing;
    private ImageButton btnTreat;
    private ImageButton btnRecord;
    private ImageButton btnPicture;
    private int treatCount = 30; // The initial treat count, change it to any desired value
    private final int MAX_TREATS = 30; // Maximum number of treats
    private boolean obstacleAvoidanceEnabled = false;
    private boolean lineFollowingEnabled = false;
    private static final int LONG_PRESS_DURATION = 3000; // 3 seconds
    private boolean treatButtonLongPressed = false;
    private Handler handler = new Handler();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stream, container, false);

        btnObstacleAvoidance = view.findViewById(R.id.btn_obstacle_avoidance);
        btnLineFollowing = view.findViewById(R.id.btn_line_following);
        btnTreat = view.findViewById(R.id.btn_treat);
        btnRecord = view.findViewById(R.id.btn_record);
        btnPicture = view.findViewById(R.id.btn_picture);

        btnObstacleAvoidance.setColorFilter(ContextCompat.getColor(requireContext(), android.R.color.white));
        btnLineFollowing.setColorFilter(ContextCompat.getColor(requireContext(), android.R.color.white));
        btnTreat.setColorFilter(ContextCompat.getColor(requireContext(), android.R.color.white));
        btnRecord.setColorFilter(ContextCompat.getColor(requireContext(), android.R.color.white));
        btnPicture.setColorFilter(ContextCompat.getColor(requireContext(), android.R.color.white));

        btnTreat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (treatCount > 0) {
                    treatCount--;
                    updateTreatButton();
                    Toast.makeText(getActivity(), getString(R.string.treat_dispensed) + "\n" + getString(R.string.treats_remaining, treatCount), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), getString(R.string.refill_treat_dispenser), Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnObstacleAvoidance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                obstacleAvoidanceEnabled = !obstacleAvoidanceEnabled;
                if (obstacleAvoidanceEnabled) {
                    Toast.makeText(getActivity(), R.string.obstacle_avoidance_enabled, Toast.LENGTH_SHORT).show();
                    btnObstacleAvoidance.setBackgroundResource(R.color.bright_pink); // Change to bright pink when enabled
                } else {
                    Toast.makeText(getActivity(), R.string.obstacle_avoidance_disabled, Toast.LENGTH_SHORT).show();
                    btnObstacleAvoidance.setBackgroundResource(R.color.primary_color); // Revert to default color when disabled
                }
            }
        });

        btnLineFollowing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lineFollowingEnabled = !lineFollowingEnabled;
                if (lineFollowingEnabled) {
                    Toast.makeText(getActivity(), R.string.line_following_enabled, Toast.LENGTH_SHORT).show();
                    btnLineFollowing.setBackgroundResource(R.color.bright_pink); // Change to bright pink when enabled
                } else {
                    Toast.makeText(getActivity(), R.string.line_following_disabled, Toast.LENGTH_SHORT).show();
                    btnLineFollowing.setBackgroundResource(R.color.primary_color); // Revert to default color when disabled
                }
            }
        });

        updateTreatButton();

        btnTreat.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // Start the long-press handler
                handler.postDelayed(longPressRunnable, LONG_PRESS_DURATION);
                treatButtonLongPressed = true;
                return true;
            }
        });

        btnTreat.setOnTouchListener(new View.OnTouchListener() {
            private long touchStartTime;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    touchStartTime = System.currentTimeMillis();
                    handler.postDelayed(longPressRunnable, LONG_PRESS_DURATION);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    long touchDuration = System.currentTimeMillis() - touchStartTime;
                    handler.removeCallbacks(longPressRunnable);
                    if (touchDuration < LONG_PRESS_DURATION) {
                        // Short click
                        if (treatCount > 0) {
                            treatCount--;
                            updateTreatButton();
                            Toast.makeText(getActivity(), getString(R.string.treat_dispensed) + "\n" + getString(R.string.treats_remaining, treatCount), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity(), getString(R.string.refill_treat_dispenser), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Long press
                        resetTreatCounter();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                    handler.removeCallbacks(longPressRunnable);
                }
                return true;
            }
        });

        return view;
    }

    private void updateTreatButton() {
        if (treatCount == 0) {
            btnTreat.setBackgroundResource(R.color.grey); // Change background color to grey when treatCount is 0
        } else {
            btnTreat.setBackgroundResource(R.color.primary_color); // Revert to default color when treats available
        }
    }
    private Runnable longPressRunnable = new Runnable() {
        @Override
        public void run() {
            resetTreatCounter();
        }
    };

    private void resetTreatCounter() {
        // Reset the treat count and update the button background
        treatCount = MAX_TREATS;
        updateTreatButton();
    }
}
