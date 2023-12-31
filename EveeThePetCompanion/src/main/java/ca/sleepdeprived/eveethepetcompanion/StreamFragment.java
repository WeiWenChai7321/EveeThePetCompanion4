/*Section: 0NA
  Wei Wen Chai, N01447321
  John Aquino, N01303112
  Jennifer Nguyen, N01435464
  Ubay Abdulaziz, N01437353
*/
package ca.sleepdeprived.eveethepetcompanion;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.hardware.Camera;
import com.google.android.material.snackbar.Snackbar;
import java.io.ByteArrayOutputStream;
import java.util.List;

public class StreamFragment extends Fragment implements SurfaceHolder.Callback {

    private TextView arrowUpText;
    private TextView arrowDownText;
    private TextView arrowLeftText;
    private TextView arrowRightText;
    private TextView turnLeftText;
    private TextView turnRightText;
    private SurfaceView streamView;
    private Camera camera;
    private ImageButton btnObstacleAvoidance;
    private ImageButton btnLineFollowing;
    private ImageButton btnTreat;
    private ImageButton btnPicture;
    private Snackbar uploadingSnackbar;
    private ImageButton btnArrowUp;
    private ImageButton btnArrowDown;
    private ImageButton btnArrowLeft;
    private ImageButton btnArrowRight;

    private ImageButton btnLookLeft;
    private ImageButton btnLookRight;
    private int treatCount = 30; // The initial treat count, change it to any desired value
    private final int MAX_TREATS = 30; // Maximum number of treats

    private static final int YOUR_PERMISSION_REQUEST_CODE = 123;
    private boolean obstacleAvoidanceEnabled = false;
    private boolean lineFollowingEnabled = false;
    private static final int LONG_PRESS_DURATION = 3000; // 3 seconds
    private boolean treatButtonLongPressed = false;
    private Handler handler = new Handler();
    private boolean hasPermission = false;

    private FirebaseFirestore firestore;
    private Toast currentToast;
    private StorageReference storageReference;
    private boolean isCameraPreviewing = false;
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

        streamView = view.findViewById(R.id.stream_view);
        btnObstacleAvoidance = view.findViewById(R.id.btn_obstacle_avoidance);
        btnLineFollowing = view.findViewById(R.id.btn_line_following);
        btnTreat = view.findViewById(R.id.btn_treat);
        btnPicture = view.findViewById(R.id.btn_picture);
        btnArrowUp = view.findViewById(R.id.btn_arrow_up);
        btnArrowDown = view.findViewById(R.id.btn_arrow_down);
        btnArrowLeft = view.findViewById(R.id.btn_arrow_left);
        btnArrowRight = view.findViewById(R.id.btn_arrow_right);
        btnLookLeft = view.findViewById(R.id.btn_look_left);
        btnLookRight = view.findViewById(R.id.btn_look_right);
        arrowUpText = view.findViewById(R.id.arrowUpText);
        arrowDownText = view.findViewById(R.id.arrowDownText);
        arrowLeftText = view.findViewById(R.id.arrowLeftText);
        arrowRightText = view.findViewById(R.id.arrowRightText);
        turnLeftText = view.findViewById(R.id.turnLeftText);
        turnRightText = view.findViewById(R.id.turnRightText);
        arrowUpText.setVisibility(View.INVISIBLE);
        arrowDownText.setVisibility(View.INVISIBLE);
        arrowLeftText.setVisibility(View.INVISIBLE);
        arrowRightText.setVisibility(View.INVISIBLE);
        turnLeftText.setVisibility(View.INVISIBLE);
        turnRightText.setVisibility(View.INVISIBLE);
        btnObstacleAvoidance.setColorFilter(ContextCompat.getColor(requireContext(), android.R.color.white));
        btnLineFollowing.setColorFilter(ContextCompat.getColor(requireContext(), android.R.color.white));
        btnTreat.setColorFilter(ContextCompat.getColor(requireContext(), android.R.color.white));
        btnPicture.setColorFilter(ContextCompat.getColor(requireContext(), android.R.color.white));

        // Check if the CAMERA permission is granted, if not request it
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // You can show a rationale for needing the permission if desired
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.CAMERA)) {
                // Show a dialog or explanation to the user why you need the permission
            }

            // Request the permission
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CAMERA}, YOUR_PERMISSION_REQUEST_CODE);
        } else {
            // The permission is already granted, start the camera preview
            startCameraPreview();
        }

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
                    arrowUpText.setVisibility(View.VISIBLE);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    arrowUpText.setVisibility(View.INVISIBLE);
                }
                return true;
            }
        });

        btnArrowDown.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    arrowDownText.setVisibility(View.VISIBLE);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    arrowDownText.setVisibility(View.INVISIBLE);
                }
                return true;
            }
        });

        btnArrowLeft.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    arrowLeftText.setVisibility(View.VISIBLE);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    arrowLeftText.setVisibility(View.INVISIBLE);
                }
                return true;
            }
        });

        btnArrowRight.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    arrowRightText.setVisibility(View.VISIBLE);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    arrowRightText.setVisibility(View.INVISIBLE);
                }
                return true;
            }
        });

        btnLookLeft.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    turnLeftText.setVisibility(View.VISIBLE);
                } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    turnLeftText.setVisibility(View.INVISIBLE);
                }
                return true;
            }
        });

        btnLookRight.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    turnRightText.setVisibility(View.VISIBLE);
                } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    turnRightText.setVisibility(View.INVISIBLE);
                }
                return true;
            }
        });

        btnPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });

        SurfaceHolder holder = streamView.getHolder();
        holder.addCallback(this);

        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == YOUR_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // The CAMERA permission is granted, start the camera preview
                startCameraPreview();
            } else {
                // The user denied the permission, handle this situation (e.g., show a message)
            }
        }
    }

    private Runnable longPressRunnable = new Runnable() {
        @Override
        public void run() {
            resetTreatCounter();
        }
    };

    private void startCameraPreview() {
        try {
            camera = Camera.open();
            Camera.Parameters parameters = camera.getParameters();
            List<Camera.Size> supportedSizes = parameters.getSupportedPreviewSizes();
            int width = streamView.getWidth();
            int height = streamView.getHeight();
            Camera.Size optimalSize = getOptimalPreviewSize(supportedSizes, width, height);
            parameters.setPreviewSize(optimalSize.width, optimalSize.height);
            camera.setParameters(parameters);
            camera.setPreviewDisplay(streamView.getHolder());
            setCameraOrientation(); // Set camera orientation here
            camera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            camera = Camera.open();
            camera.setPreviewDisplay(holder);
            setCameraOrientation(); // Set camera orientation here
            camera.startPreview();
            isCameraPreviewing = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (camera != null) {
            try {
                camera.stopPreview();
                Camera.Parameters parameters = camera.getParameters();
                List<Camera.Size> supportedSizes = parameters.getSupportedPreviewSizes();
                Camera.Size optimalSize = getOptimalPreviewSize(supportedSizes, width, height);
                parameters.setPreviewSize(optimalSize.width, optimalSize.height);
                camera.setParameters(parameters);
                camera.setPreviewDisplay(holder);
                setCameraOrientation(); // Set camera orientation here as well
                camera.startPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (camera != null) {
            if (isCameraPreviewing) {
                camera.stopPreview();
                isCameraPreviewing = false;
            }
            camera.release();
            camera = null;
        }
    }
    private void resetTreatCounter() {
        // Reset the treat count and update the button background
        treatCount = MAX_TREATS;
        updateTreatButton();
    }

    private void updateTreatButton() {
        if (treatCount == 0) {
            btnTreat.setBackgroundResource(R.color.grey); // Change background color to grey when treatCount is 0
        } else {
            btnTreat.setBackgroundResource(R.color.primary_color); // Revert to default color when treats available
        }
    }

    private void showToast(String message) {
        if (currentToast != null) {
            currentToast.cancel();
        }
        currentToast = Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT);
        currentToast.show();
    }

    private Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            // Convert the byte array to a Bitmap
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

            // Show Snackbar to indicate uploading is in progress
            uploadingSnackbar = Snackbar.make(streamView, R.string.uploading_photo, Snackbar.LENGTH_INDEFINITE);
            uploadingSnackbar.show();

            // Save the captured image to Firebase Storage
            savePictureToStorage(bitmap);

            // Delay the restart of the camera preview by a few milliseconds
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Restart the preview after a delay
                    camera.startPreview();
                }
            }, 1000); // 1 second delay
        }
    };


    private void takePicture() {
        if (camera != null) {
            camera.takePicture(null, null, pictureCallback);
        }
    }

    private void savePictureToStorage(Bitmap bitmap) {
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
                    uploadingSnackbar.dismiss();
                    Toast.makeText(getActivity(), R.string.pic_captured_uploaded, Toast.LENGTH_LONG).show();
                } else {
                    showToast(getString(R.string.failed_to_upload_picture));
                }
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (camera != null && isCameraPreviewing) {
            camera.stopPreview();
            isCameraPreviewing = false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (camera == null) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.CAMERA)) {
                }
                ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CAMERA}, YOUR_PERMISSION_REQUEST_CODE);
            } else {
                startCameraPreview();
            }
        } else if (!isCameraPreviewing) {
            camera.startPreview();
            isCameraPreviewing = true;
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (camera != null) {
            if (isCameraPreviewing) {
                camera.stopPreview();
                isCameraPreviewing = false;
            }
            camera.release();
            camera = null;
        }
    }

    private void setCameraOrientation() {
        if (camera == null) {
            return;
        }
        int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(0, info);
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int width, int height) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) width / height;

        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = height;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }

        return optimalSize;
    }
}