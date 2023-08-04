/*Section: 0NA
  Wei Wen Chai, N01447321
  John Aquino, N01303112
  Jennifer Nguyen, N01435464
  Ubay Abdulaziz, N01437353
*/
package ca.sleepdeprived.eveethepetcompanion;

import android.os.Bundle;
import android.view.LayoutInflater;
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
                Toast.makeText(getActivity(), getString(R.string.treat_dispensed), Toast.LENGTH_SHORT).show();
            }
        });

        btnObstacleAvoidance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), R.string.obstacle_avoidance_enabled, Toast.LENGTH_SHORT).show();
            }
        });

        btnLineFollowing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), R.string.line_following_enabled, Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}


