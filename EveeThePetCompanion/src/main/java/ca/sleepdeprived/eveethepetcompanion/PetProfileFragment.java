/*Section: 0NA
  Wei Wen Chai, N01447321
  John Aquino, N01303112
  Jennifer Nguyen, N01435464
  Ubay Abdulaziz, N01437353
*/
package ca.sleepdeprived.eveethepetcompanion;

import androidx.lifecycle.ViewModelProvider;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

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
    private TextView colorTextView;
    private TextView breedTextView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        petInfoViewModel = new ViewModelProvider(requireActivity()).get(PetInfoViewModel.class);
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
        // Inflate the dialog_pet_info_edit.xml layout
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_pet_info_edit, null);

        // Find the EditText views
        EditText nameEditText = dialogView.findViewById(R.id.nameEditText);
        EditText ageEditText = dialogView.findViewById(R.id.ageEditText);
        EditText colorEditText = dialogView.findViewById(R.id.colorEditText);
        EditText breedEditText = dialogView.findViewById(R.id.breedEditText);

        // Set the initial text for the EditText views
        nameEditText.setText(nameTextView.getText());
        ageEditText.setText(ageTextView.getText());
        colorEditText.setText(colorTextView.getText());
        breedEditText.setText(breedTextView.getText());

// Create and show the AlertDialog for editing pet info
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(R.string.edit_pet_info)
                .setView(dialogView)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Get the updated text from the EditText views
                        String name = nameEditText.getText().toString();
                        String ageString = ageEditText.getText().toString();
                        String color = colorEditText.getText().toString();
                        String breed = breedEditText.getText().toString();

                        // Validate age as a number
                        int age = 0;
                        try {
                            age = Integer.parseInt(ageString);
                        } catch (NumberFormatException e) {
                            // Handle the case where age is not a valid number
                            Toast.makeText(requireContext(), R.string.invalid_age, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Update the TextViews with the edited information
                        nameTextView.setText(name);
                        ageTextView.setText(String.valueOf(age)); // Set the parsed age as an integer
                        colorTextView.setText(color);
                        breedTextView.setText(breed);

                        // Update the Firestore database with the edited information
                        String userId = auth.getCurrentUser().getUid();
                        DocumentReference docRef = db.collection("users").document(userId).collection("pet_info").document();

                        // Create a Map with the edited data
                        Map<String, Object> editedData = new HashMap<>();
                        editedData.put("petName", name);
                        editedData.put("petAge", age);
                        editedData.put("petColor", color);
                        editedData.put("petBreed", breed);

                        docRef.set(editedData)
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
        // Query to retrieve any document from the "pet_info" collection
        db.collection("pet_info")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot querySnapshot) {
                        if (!querySnapshot.isEmpty()) {
                            // Get the first document from the query result
                            DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);

                            String name = documentSnapshot.getString("petName");
                            String age = documentSnapshot.getString("petAge");
                            String color = documentSnapshot.getString("petColor");
                            String breed = documentSnapshot.getString("petBreed");

                            // Update the TextViews with the retrieved pet information
                            if (name != null && !name.isEmpty()) {
                                nameTextView.setText(name);
                            }
                            if (age != null && !age.isEmpty()) {
                                ageTextView.setText(age);
                            }
                            if (color != null && !color.isEmpty()) {
                                colorTextView.setText(color);
                            }
                            if (breed != null && !breed.isEmpty()) {
                                breedTextView.setText(breed);
                            }
                        } else {
                            // The "pet_info" collection is empty, show a message or handle accordingly
                            Toast.makeText(requireContext(), R.string.pet_info_retrieval_failed, Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Show a toast message to indicate failure
                        Toast.makeText(requireContext(), R.string.pet_info_retrieval_failed, Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
