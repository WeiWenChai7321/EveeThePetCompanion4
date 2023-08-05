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
import android.widget.ImageView;
import android.widget.Toast;
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
        ImageView videoImage1 = view.findViewById(R.id.video_image1);
        ImageView videoImage2 = view.findViewById(R.id.video_image2);
        ImageView photoImage1 = view.findViewById(R.id.photo_image1);
        ImageView photoImage2 = view.findViewById(R.id.photo_image2);
        ImageView photoImage3 = view.findViewById(R.id.photo_image3);
        ImageView photoImage4 = view.findViewById(R.id.photo_image4);
        ImageView photoImage5 = view.findViewById(R.id.photo_image5);
        ImageView photoImage6 = view.findViewById(R.id.photo_image6);
        imageViews.add(videoImage1);
        imageViews.add(videoImage2);
        imageViews.add(photoImage1);
        imageViews.add(photoImage2);
        imageViews.add(photoImage3);
        imageViews.add(photoImage4);
        imageViews.add(photoImage5);
        imageViews.add(photoImage6);

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
}