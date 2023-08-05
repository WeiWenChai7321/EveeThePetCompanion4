/*Section: 0NA
  Wei Wen Chai, N01447321
  John Aquino, N01303112
  Jennifer Nguyen, N01435464
  Ubay Abdulaziz, N01437353
*/
package ca.sleepdeprived.eveethepetcompanion;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import android.provider.MediaStore;

public class HighlightsFragment extends Fragment {

    private static final int REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE = 1;

    private boolean hasPermission = false;
    private Button downloadAllButton;
    private List<ImageView> imageViews;
    private static final String FIREBASE_COLLECTION_NAME = "highlights";

    private FirebaseFirestore firestore;
    private StorageReference storageReference;

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
        View view = inflater.inflate(R.layout.fragment_highlights, container, false);

        imageViews = new ArrayList<>();

        downloadAllButton = view.findViewById(R.id.btn_download_all);
        downloadAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestWriteExternalStoragePermission();
            }
        });

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE);
        }

        // Load images from Firebase Storage
        loadImagesFromFirebaseStorage();

        return view;
    }


    private void requestWriteExternalStoragePermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            // Permission already granted
            hasPermission = true;
            showToast(getString(R.string.permissiongranted));
            downloadAllImages();
        } else {
            // Permission not granted
            hasPermission = false;
            showPermissionAlertDialog();
        }
    }

    private void showPermissionAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(getString(R.string.permission_dialog_title))
                .setMessage(getString(R.string.permission_dialog_message))
                .setPositiveButton(getString(R.string.permission_dialog_positive_button), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE);
                    }
                })
                .setNegativeButton(getString(R.string.permission_dialog_negative_button), null)
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                hasPermission = true;
                showToast(getString(R.string.permissiongranted));
                downloadAllImages();
            } else {
                // Permission denied
                hasPermission = false;
                showToast(getString(R.string.permissiondenied));

            }
        }
    }

    private void showToast(String imagePath) {
        int lastSlashIndex = imagePath.lastIndexOf(File.separator);
        if (lastSlashIndex != -1) {
            String imageName = imagePath.substring(lastSlashIndex + 1);
            String toastMessage = getString(R.string.image_saved) + " " + imageName;
            Toast.makeText(requireContext(), toastMessage, Toast.LENGTH_SHORT).show();
        }
    }


    private void downloadAllImages() {
        for (ImageView imageView : imageViews) {
            downloadImage(imageView);
        }
    }

    private void downloadImage(final ImageView imageView) {
        Drawable drawable = imageView.getDrawable();
        if (drawable instanceof BitmapDrawable) {
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            saveImage(bitmap);
        }
    }

    private void saveImage(Bitmap bitmap) {
        if (hasPermission) {
            String imageFileName = getString(R.string.image) + System.currentTimeMillis() + getString(R.string.jpg);
            File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File imageFile = new File(storageDir, imageFileName);

            try {
                FileOutputStream outputStream = new FileOutputStream(imageFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                outputStream.flush();
                outputStream.close();
                showToast(imageFile.getAbsolutePath());

                // Add the image to the Gallery
                addImageToGallery(imageFile);
            } catch (IOException e) {
                e.printStackTrace();
                showToast(getString(R.string.failed_to_save_image));
            }
        } else {
            Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", requireContext().getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
        }
    }

    private void addImageToGallery(File imageFile) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "EveeImage");
        values.put(MediaStore.Images.Media.DISPLAY_NAME, imageFile.getName());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.DATA, imageFile.getAbsolutePath());

        requireContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    private void loadImagesFromFirebaseStorage() {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("photos");

        storageRef.listAll().addOnSuccessListener(listResult -> {
            for (StorageReference item : listResult.getItems()) {
                item.getDownloadUrl().addOnSuccessListener(uri -> {
                    ImageView imageView = createImageView(uri.toString());
                    imageViews.add(imageView);
                    updateGridLayout();
                }).addOnFailureListener(exception -> {
                    // Handle the failure, if any.
                });
            }
        }).addOnFailureListener(exception -> {
            // Handle the failure, if any.
        });
    }

    private ImageView createImageView(String imageUrl) {
        ImageView imageView = new ImageView(requireContext());

        // Load the image using Glide with a transformation to rotate it once to the right.
        Glide.with(requireContext())
                .load(imageUrl)
                .transform(new RotateTransformation(90f)) // 90 degrees for right rotation
                .into(imageView);

        // Set scaling options to maintain the aspect ratio and fit within the column width.
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        imageView.setAdjustViewBounds(true);

        return imageView;
    }
    private void updateGridLayout() {
        GridLayout photosGrid = requireView().findViewById(R.id.photos_grid);
        photosGrid.removeAllViews();

        // Calculate the number of columns based on the screen width.
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int columnCount = Math.min(2, imageViews.size());
        int horizontalSpacing = 1; // Adjust this value as needed for closer spacing

        // Calculate the total horizontal space available for the images and spacing.
        int totalHorizontalSpace = screenWidth - (horizontalSpacing * (columnCount - 1)); // No spacing between columns
        int imageSize = totalHorizontalSpace / columnCount;

        // Add padding between the rows and columns to create spacing.
        int verticalSpacing = 1; // Adjust this value as needed
        int leftPadding = 1;
        int rightPadding = 1;

        photosGrid.setPadding(leftPadding, verticalSpacing, rightPadding, verticalSpacing);

        // Calculate the number of rows based on the number of images and the number of columns.
        int rowCount = (int) Math.ceil((double) imageViews.size() / columnCount);

        // Set the row count for the GridLayout.
        photosGrid.setRowCount(rowCount);

        // Calculate the number of images to add to the GridLayout.
        int imageCountToAdd = imageViews.size();

        // Add the image views to the GridLayout with proper layout parameters.
        for (int i = 0; i < imageCountToAdd; i++) {
            ImageView imageView = imageViews.get(i);
            GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
            layoutParams.width = imageSize;
            layoutParams.height = imageSize; // Set height equal to width to maintain aspect ratio

            int row = i / columnCount; // Row index
            int col = i % columnCount; // Column index

            layoutParams.rowSpec = GridLayout.spec(row);
            layoutParams.columnSpec = GridLayout.spec(col);

            layoutParams.setMargins(horizontalSpacing, verticalSpacing, horizontalSpacing, verticalSpacing); // Add both vertical and horizontal spacing
            imageView.setLayoutParams(layoutParams);
            photosGrid.addView(imageView);
        }
    }

}