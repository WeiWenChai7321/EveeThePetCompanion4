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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class PetProfileFragment extends Fragment {
    private PetInfoViewModel petInfoViewModel;
    private static final int NUM_COLUMNS = 7; // Number of days in a week
    private static final int NUM_ROWS = 24; // Number of hours in a day
    private static final String PREFS_NAME = "CellDataPrefs";

    private GridLayout scheduleGrid;
    private EditText cellEditText;
    private boolean isEditMode = false;
    private SharedPreferences sharedPreferences;
    private Button editScheduleButton;
    private TextView modifyText;
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

        scheduleGrid = view.findViewById(R.id.schedule_grid);
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

        // Create the schedule grid
        createScheduleGrid();

        // Set click listener for edit button
        editScheduleButton = view.findViewById(R.id.edit_schedule_button);
        modifyText = view.findViewById(R.id.modify_text);

        updateEditModeUI();

        Button editButton = view.findViewById(R.id.editButton);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editPetInfo();
            }
        });

        editScheduleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isEditMode = !isEditMode; // Toggle the edit mode flag
                updateCellClickability(); // Update cell clickability based on edit mode
                updateEditModeUI(); // Update the UI based on edit mode
            }
        });

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

        String name = nameEditText.getText().toString();
        String age = ageEditText.getText().toString();
        String color = colorEditText.getText().toString();
        String breed = breedEditText.getText().toString();

        petInfoViewModel.setPetName(name);
        petInfoViewModel.setPetAge(age);
        petInfoViewModel.setPetColor(color);
        petInfoViewModel.setPetBreed(breed);

        // Create and show the AlertDialog for editing pet info
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(R.string.edit_pet_info)
                .setView(dialogView)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Get the updated text from the EditText views
                        String name = nameEditText.getText().toString();
                        String age = ageEditText.getText().toString();
                        String color = colorEditText.getText().toString();
                        String breed = breedEditText.getText().toString();

                        // Update the TextViews with the edited information
                        nameTextView.setText(name);
                        ageTextView.setText(age);
                        colorTextView.setText(color);
                        breedTextView.setText(breed);

                        // Store the edited information in SharedPreferences
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(getString(R.string.name_key), name);
                        editor.putString(getString(R.string.age_key), age);
                        editor.putString(getString(R.string.color_key), color);
                        editor.putString(getString(R.string.breed_key), breed);
                        editor.apply();

                        // Show a toast message to indicate successful editing
                        Toast.makeText(requireContext(), R.string.pet_info_updated, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void updateEditModeUI() {
        if (isEditMode) {
            editScheduleButton.setText(R.string.finish_edits);
            modifyText.setVisibility(View.VISIBLE);
        } else {
            editScheduleButton.setText(R.string.edit);
            modifyText.setVisibility(View.GONE);
        }
    }

    private void createScheduleGrid() {
        scheduleGrid.setColumnCount(NUM_COLUMNS + 1); // Include an extra column for the hour labels
        scheduleGrid.setRowCount(NUM_ROWS + 1); // Include an extra row for the day labels

        // Add day labels
        for (int day = 0; day <= NUM_COLUMNS; day++) {
            TextView dayLabelTextView = new TextView(requireContext());
            GridLayout.LayoutParams dayLayoutParams = new GridLayout.LayoutParams(
                    GridLayout.spec(0, GridLayout.FILL),
                    GridLayout.spec(day, GridLayout.FILL, 1f)
            );
            dayLabelTextView.setLayoutParams(dayLayoutParams);
            dayLabelTextView.setPadding(32, 16, 32, 16); // Adjust padding to make columns wider

            if (day > 0) {
                // Set the day labels (Mon, Tue, Wed, etc.)
                String[] dayLabels = {getString(R.string.mon), getString(R.string.tue), getString(R.string.wed), getString(R.string.thu), getString(R.string.fri), getString(R.string.sat), getString(R.string.sun)};
                dayLabelTextView.setText(dayLabels[day - 1]);
                dayLabelTextView.setBackgroundColor(Color.LTGRAY); // Set the background color to light grey
                dayLabelTextView.setTextColor(Color.BLACK); // Set the text color to black
            }

            scheduleGrid.addView(dayLabelTextView);
        }

        // Add hour labels and cells
        for (int hour = 0; hour < NUM_ROWS; hour++) {
            // Add hour labels
            TextView hourLabelTextView = new TextView(requireContext());
            GridLayout.LayoutParams hourLayoutParams = new GridLayout.LayoutParams(
                    GridLayout.spec(hour + 1, GridLayout.FILL, 1f),
                    GridLayout.spec(0, GridLayout.FILL)
            );
            hourLabelTextView.setLayoutParams(hourLayoutParams);
            hourLabelTextView.setPadding(20, 30, 20, 30); // Adjust padding to make columns wider
            hourLabelTextView.setBackgroundColor(Color.LTGRAY); // Set the background color to light grey
            hourLabelTextView.setTextColor(Color.BLACK); // Set the text color to black
            hourLabelTextView.setText(String.format("%02d:00", hour)); // Set the hour labels (00:00, 01:00, etc.)
            scheduleGrid.addView(hourLabelTextView);

            // Add cells
            for (int day = 0; day < NUM_COLUMNS; day++) {
                final TextView cellTextView = new TextView(requireContext());
                GridLayout.LayoutParams cellLayoutParams = new GridLayout.LayoutParams(
                        GridLayout.spec(hour + 1, GridLayout.FILL, 1f),
                        GridLayout.spec(day + 1, GridLayout.FILL, 1f)
                );
                cellTextView.setLayoutParams(cellLayoutParams);
                cellTextView.setPadding(140, 16, 140, 16); // Adjust padding to make columns wider
                cellTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isEditMode) {
                            showCellEditDialog(cellTextView);
                        }
                    }
                });

                // Get the stored text for the cell from SharedPreferences
                String storedText = getStoredText(hour, day);
                cellTextView.setText(storedText); // Set the stored text in the cell

                scheduleGrid.addView(cellTextView);
            }
        }
    }

    private String getStoredText(int hour, int day) {
        String key = "cell_" + hour + "_" + day;
        return sharedPreferences.getString(key, "");
    }

    private void storeText(int hour, int day, String text) {
        String key = "cell_" + hour + "_" + day;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, text);
        editor.apply();
    }

    private void updateCellClickability() {
        for (int i = 0; i < scheduleGrid.getChildCount(); i++) {
            View child = scheduleGrid.getChildAt(i);
            if (child instanceof TextView) {
                TextView cellTextView = (TextView) child;
                cellTextView.setClickable(isEditMode);
            }
        }
    }

    private void showCellEditDialog(final TextView cellTextView) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(R.string.edit_cell);

        // Inflate the dialog layout
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_pet_info_edit, null);

        // Find the EditText fields in the dialog layout
        final EditText nameEditText = dialogView.findViewById(R.id.nameEditText);
        final EditText ageEditText = dialogView.findViewById(R.id.ageEditText);
        final EditText colorEditText = dialogView.findViewById(R.id.colorEditText);
        final EditText breedEditText = dialogView.findViewById(R.id.breedEditText);

        // Set the initial values of the EditText fields
        nameEditText.setText(nameTextView.getText());
        ageEditText.setText(ageTextView.getText());
        colorEditText.setText(colorTextView.getText());
        breedEditText.setText(breedTextView.getText());

        builder.setView(dialogView)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = nameEditText.getText().toString().trim();
                        String age = ageEditText.getText().toString().trim();
                        String color = colorEditText.getText().toString().trim();
                        String breed = breedEditText.getText().toString().trim();

                        // Store the updated values in SharedPreferences
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(getString(R.string.name_key), name);
                        editor.putString(getString(R.string.age_key), age);
                        editor.putString(getString(R.string.color_key), color);
                        editor.putString(getString(R.string.breed_key), breed);
                        editor.apply();

                        Toast.makeText(requireContext(), R.string.pet_info_saved, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private static class Grid {
        static int getHourFromView(TextView textView) {
            GridLayout.LayoutParams params = (GridLayout.LayoutParams) textView.getLayoutParams();
            GridLayout.Spec rowSpec = params.rowSpec;
            int rowIndex = getRowIndexFromSpec(rowSpec);
            return rowIndex - 1;
        }

        static int getDayFromView(TextView textView) {
            GridLayout.LayoutParams params = (GridLayout.LayoutParams) textView.getLayoutParams();
            GridLayout.Spec columnSpec = params.columnSpec;
            int columnIndex = getColumnIndexFromSpec(columnSpec);
            return columnIndex - 1;
        }

        static int getRowIndexFromSpec(GridLayout.Spec spec) {
            try {
                java.lang.reflect.Field field = spec.getClass().getDeclaredField("startLine");
                field.setAccessible(true);
                return field.getInt(spec);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return 0;
        }

        static int getColumnIndexFromSpec(GridLayout.Spec spec) {
            try {
                java.lang.reflect.Field field = spec.getClass().getDeclaredField("startLine");
                field.setAccessible(true);
                return field.getInt(spec);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return 0;
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
    public void onResume() {
        super.onResume();
        isEditMode = sharedPreferences.getBoolean(getString(R.string.is_edit_mode_key), false);
        updateCellClickability();

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
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(getString(R.string.is_edit_mode_key), isEditMode);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            isEditMode = savedInstanceState.getBoolean(getString(R.string.is_edit_mode_key), false);
            updateEditModeUI();
            updateCellClickability();
        }
    }
}
