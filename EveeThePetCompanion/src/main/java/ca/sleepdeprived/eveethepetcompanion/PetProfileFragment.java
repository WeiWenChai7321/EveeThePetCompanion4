/*Section: 0NA
  Wei Wen Chai, N01447321
  John Aquino, N01303112
  Jennifer Nguyen, N01435464
  Ubay Abdulaziz, N01437353
*/
package ca.sleepdeprived.eveethepetcompanion;

import androidx.lifecycle.ViewModelProvider;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.HashMap;
import java.util.Map;

public class PetProfileFragment extends Fragment {
    private PetInfoViewModel petInfoViewModel;
    private static final String PREFS_NAME = "CellDataPrefs";
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private EditText cellEditText;
    private boolean isEditMode = false;
    private SharedPreferences sharedPreferences;
    private TextView nameTextView;
    private TextView ageTextView;
    private static final int PICK_IMAGE_REQUEST = 1;
    private TextView colorTextView;
    private TextView breedTextView;
    private ImageButton petDisplayPic;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        petInfoViewModel = new ViewModelProvider(requireActivity()).get(PetInfoViewModel.class);
    }

    public void selectImageFromGallery() {
        // Create an intent to open the gallery
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check if the result is from the image selection request
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            // Get the selected image URI
            Uri imageUri = data.getData();

            // Upload the image to Firebase Storage and update the ImageButton
            uploadImageToFirebaseStorage(imageUri);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_petprofile, container, false);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        cellEditText = new EditText(requireContext());

        sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        nameTextView = view.findViewById(R.id.nameTextView);
        ageTextView = view.findViewById(R.id.ageTextView);
        colorTextView = view.findViewById(R.id.colorTextView);
        breedTextView = view.findViewById(R.id.breedTextView);

        // Initialize petDisplayPic ImageButton here
        petDisplayPic = view.findViewById(R.id.pet_display_pic);
        petDisplayPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImageFromGallery();
            }
        });
        // Set properties for cellEditText
        cellEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        cellEditText.setSingleLine();
        cellEditText.setHint(R.string.cal_enter_text_hint);

        Button editButton = view.findViewById(R.id.editButton);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editPetInfo();
            }
        });

        // Read pet information from Firestore
        readPetInfoFromFirestore();

        return view;
    }

    private void editPetInfo() {
        db.collection("pet_info")
                .limit(1) // Limit to one result, you can remove this to get all documents
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        // Check if any documents exist in the query result
                        if (!queryDocumentSnapshots.isEmpty()) {
                            // Get the first document in the result
                            DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);

                            // Retrieve the pet information from the document
                            String name = documentSnapshot.getString("petName");
                            int age = documentSnapshot.getLong("petAge").intValue();
                            String color = documentSnapshot.getString("petColor");
                            String breed = documentSnapshot.getString("petBreed");

                            // Inflate the dialog_pet_info_edit.xml layout
                            View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_pet_info_edit, null);

                            // Find the EditText views
                            EditText nameEditText = dialogView.findViewById(R.id.nameEditText);
                            EditText ageEditText = dialogView.findViewById(R.id.ageEditText);
                            EditText colorEditText = dialogView.findViewById(R.id.colorEditText);
                            EditText breedEditText = dialogView.findViewById(R.id.breedEditText);

                            // Set the initial text for the EditText views with the retrieved data
                            nameEditText.setText(name);
                            ageEditText.setText(String.valueOf(age));
                            colorEditText.setText(color);
                            breedEditText.setText(breed);

                            // Create and show the AlertDialog for editing pet info
                            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                            builder.setTitle(R.string.edit_pet_info)
                                    .setView(dialogView)
                                    .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            // Get the updated text from the EditText views
                                            String name = nameEditText.getText().toString();
                                            int age = Integer.parseInt(ageEditText.getText().toString());
                                            String color = colorEditText.getText().toString();
                                            String breed = breedEditText.getText().toString();

                                            // Update the TextViews with the edited information
                                            nameTextView.setText(name);
                                            ageTextView.setText(String.valueOf(age));
                                            colorTextView.setText(color);
                                            breedTextView.setText(breed);

                                            // Create a Map with the edited data
                                            Map<String, Object> editedData = new HashMap<>();
                                            editedData.put("petName", name);
                                            editedData.put("petAge", age);
                                            editedData.put("petColor", color);
                                            editedData.put("petBreed", breed);

                                            // Update any entry in the "pet_info" collection with the edited data
                                            db.collection("pet_info")
                                                    .document(documentSnapshot.getId())
                                                    .update(editedData)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            // Show a toast message to indicate successful editing
                                                            Toast.makeText(requireContext(), R.string.pet_info_updated, Toast.LENGTH_SHORT).show();
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            // Show a toast message to indicate failure
                                                            Toast.makeText(requireContext(), R.string.pet_info_update_failed, Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        }
                                    })
                                    .setNegativeButton(R.string.cancel, null)
                                    .show();
                        } else {
                            // The collection is empty, handle the case where there are no entries
                            Toast.makeText(requireContext(), "No pet info found in the database", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Show a toast message to indicate failure
                        Toast.makeText(requireContext(), "Failed to retrieve pet info", Toast.LENGTH_SHORT).show();
                    }
                });
    }



    @Override
    public void onResume() {
        super.onResume();
        isEditMode = sharedPreferences.getBoolean(getString(R.string.is_edit_mode_key), false);

        // Retrieve the stored pet information from SharedPreferences
        String name = sharedPreferences.getString(getString(R.string.name_key), "");
        String age = sharedPreferences.getString(getString(R.string.age_key), "");
        String color = sharedPreferences.getString(getString(R.string.color_key), "");
        String breed = sharedPreferences.getString(getString(R.string.breed_key), "");

        // Update the TextViews with the stored pet information if available
        if (!name.isEmpty()) {
            nameTextView.setText(name);
        }
        if (!age.isEmpty()) {
            ageTextView.setText(age);
        }
        if (!color.isEmpty()) {
            colorTextView.setText(color);
        }
        if (!breed.isEmpty()) {
            breedTextView.setText(breed);
        }

        // Fetch the current pet_profile_pic from Firebase Storage and load it into the ImageButton
        fetchAndLoadPetProfilePic();
    }

    private void fetchAndLoadPetProfilePic() {
        // Get the storage reference for the pet_profile_pic image
        String imageName = "pet_profile_pic.jpg";
        StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                .child("pet_profile_pic").child(imageName);

        // Fetch the pet_profile_pic image
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri downloadUri) {
                // Use Glide to load the image into the ImageButton and set the scale type to fit center
                Glide.with(requireContext())
                        .load(downloadUri)
                        .fitCenter() // Set the scale type to fit center
                        .into(petDisplayPic);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Image retrieval failed, show a toast or handle the error
                Toast.makeText(requireContext(), "Failed to retrieve pet profile pic", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getString(R.string.is_edit_mode_key), isEditMode);
        editor.apply();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(getString(R.string.is_edit_mode_key), isEditMode);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            isEditMode = savedInstanceState.getBoolean(getString(R.string.is_edit_mode_key), false);
        }
    }

    private void readPetInfoFromFirestore() {
        db.collection("pet_info")
                .limit(1) // Limit to one result, you can remove this to get all documents
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        // Check if any documents exist in the query result
                        if (!queryDocumentSnapshots.isEmpty()) {
                            // Get the first document in the result
                            DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);

                            String name = documentSnapshot.getString("petName");
                            Long ageLong = documentSnapshot.getLong("petAge");
                            String color = documentSnapshot.getString("petColor");
                            String breed = documentSnapshot.getString("petBreed");

                            int age = (ageLong != null) ? ageLong.intValue() : 0;

                            // Update the TextViews with the retrieved pet information
                            if (name != null && !name.isEmpty()) {
                                nameTextView.setText(name);
                            }
                            // ageTextView expects an int, so no need to check for null here
                            ageTextView.setText(String.valueOf(age));
                            if (color != null && !color.isEmpty()) {
                                colorTextView.setText(color);
                            }
                            if (breed != null && !breed.isEmpty()) {
                                breedTextView.setText(breed);
                            }
                        } else {
                            // The collection is empty, handle the case where there are no entries
                            Toast.makeText(requireContext(), "No pet info found in the database", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Show a toast message to indicate failure
                        Toast.makeText(requireContext(), "Failed to retrieve pet info", Toast.LENGTH_SHORT).show();
                    }
                });
    }



    private void uploadImageToFirebaseStorage(Uri imageUri) {
        // Get the storage reference for the image
        String imageName = "pet_profile_pic.jpg"; // Set a name for the image in Firebase Storage
        StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                .child("pet_profile_pic").child(imageName);

        // Upload the image to Firebase Storage
        storageReference.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Image upload successful, update the ImageButton
                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri downloadUri) {
                                // Use Glide or any other library to load the image into the ImageButton
                                Glide.with(requireContext())
                                        .load(downloadUri)
                                        .into(petDisplayPic);

                                // Show a toast message to indicate successful update
                                Toast.makeText(requireContext(), "Updated profile picture", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Image upload failed, show a toast or handle the error
                        Toast.makeText(requireContext(), "Image upload failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }


}
