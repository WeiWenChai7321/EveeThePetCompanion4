/*Section: 0NA
  Wei Wen Chai, N01447321
  John Aquino, N01303112
  Jennifer Nguyen, N01435464
  Ubay Abdulaziz, N01437353
*/
package ca.sleepdeprived.eveethepetcompanion;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class StreamFragment extends Fragment {

    private ImageButton btnObstacleAvoidance;
    private ImageButton btnLineFollowing;
    private ImageButton btnTreat;
    private ImageButton btnRecord;
    private ImageButton btnPicture;

    private ImageButton btnArrowUp;
    private ImageButton btnArrowDown;
    private ImageButton btnArrowLeft;
    private ImageButton btnArrowRight;

    private ImageButton btnLookLeft;
    private ImageButton btnLookRight;
    private int treatCount = 30; // The initial treat count, change it to any desired value
    private final int MAX_TREATS = 30; // Maximum number of treats
    private boolean obstacleAvoidanceEnabled = false;
    private boolean lineFollowingEnabled = false;
    private static final int LONG_PRESS_DURATION = 3000; // 3 seconds
    private boolean treatButtonLongPressed = false;
    private Handler handler = new Handler();
    private boolean hasPermission = false;

    private FirebaseFirestore firestore;
    private StorageReference storageReference;
    private boolean turningLeft = false;
    private boolean turningRight = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firestore and Storage
        firestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stream, container, false);

        btnObstacleAvoidance = view.findViewById(R.id.btn_obstacle_avoidance);
        btnLineFollowing = view.findViewById(R.id.btn_line_following);
        btnTreat = view.findViewById(R.id.btn_treat);
        btnRecord = view.findViewById(R.id.btn_record);
        btnPicture = view.findViewById(R.id.btn_picture);
        btnArrowUp = view.findViewById(R.id.btn_arrow_up);
        btnArrowDown = view.findViewById(R.id.btn_arrow_down);
        btnArrowLeft = view.findViewById(R.id.btn_arrow_left);
        btnArrowRight = view.findViewById(R.id.btn_arrow_right);
        btnLookLeft = view.findViewById(R.id.btn_look_left);
        btnLookRight = view.findViewById(R.id.btn_look_right);

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

        btnArrowUp.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    // Start moving forward logic and show the toast
                    Toast.makeText(getActivity(), "Moving forward", Toast.LENGTH_SHORT).show();
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    // Stop moving forward logic and remove the toast (if required)
                }
                return true;
            }
        });

        btnArrowDown.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    // Start moving backward logic and show the toast
                    Toast.makeText(getActivity(), "Moving backward", Toast.LENGTH_SHORT).show();
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    // Stop moving backward logic and remove the toast (if required)
                }
                return true;
            }
        });

        btnArrowLeft.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    // Start moving left logic and show the toast
                    Toast.makeText(getActivity(), "Moving left", Toast.LENGTH_SHORT).show();
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    // Stop moving left logic and remove the toast (if required)
                }
                return true;
            }
        });

        btnArrowRight.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    // Start moving right logic and show the toast
                    Toast.makeText(getActivity(), "Moving right", Toast.LENGTH_SHORT).show();
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    // Stop moving right logic and remove the toast (if required)
                }
                return true;
            }
        });

        btnLookLeft.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    // Start turning left logic and show the toast
                    turningLeft = true;
                    Toast.makeText(getActivity(), "Turning left", Toast.LENGTH_SHORT).show();
                } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    // Stop turning left logic and remove the toast
                    turningLeft = false;
                }
                return true;
            }
        });

        btnLookRight.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    // Start turning right logic and show the toast
                    turningRight = true;
                    Toast.makeText(getActivity(), "Turning right", Toast.LENGTH_SHORT).show();
                } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    // Stop turning right logic and remove the toast
                    turningRight = false;
                }
                return true;
            }
        });

        btnPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takeScreenCapture();
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

    private void takeScreenCapture() {
        View streamView = requireActivity().findViewById(R.id.stream_view);
        streamView.setDrawingCacheEnabled(true);
        streamView.buildDrawingCache();
        Bitmap streamBitmap = Bitmap.createBitmap(streamView.getDrawingCache());
        streamView.setDrawingCacheEnabled(false);

        saveScreenCapture(streamBitmap);
    }


    private void saveScreenCapture(Bitmap bitmap) {
        String imageFileName = getString(R.string.image) + System.currentTimeMillis() + getString(R.string.jpg);
        final StorageReference imageRef = storageReference.child("photos/" + imageFileName);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = imageRef.putBytes(data);
        uploadTask.addOnCompleteListener(requireActivity(), new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    showToast("Screen capture uploaded to Firebase Storage");
                    // You can save the download URL to Firestore here if needed
                    imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String downloadUrl = uri.toString();
                            saveImageUrlToFirestore(downloadUrl);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            showToast("Failed to retrieve download URL");
                        }
                    });
                } else {
                    showToast("Failed to upload screen capture");
                }
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    private void saveImageUrlToFirestore(String imageUrl) {
        // You can save the image URL to Firestore here
        // For example, create a new document in a collection named "highlights" with the image URL
        Map<String, Object> highlightData = new HashMap<>();
        highlightData.put("imageUrl", imageUrl);

        firestore.collection("image_URLs")
                .add(highlightData)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        showToast("Image URL saved to Firestore");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showToast("Failed to save image URL to Firestore");
                    }
                });
    }


}
