/*Section: 0NA
  Wei Wen Chai, N01447321
  John Aquino, N01303112
  Jennifer Nguyen, N01435464
  Ubay Abdulaziz, N01437353
*/
package ca.sleepdeprived.eveethepetcompanion;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PetInfoViewModel extends ViewModel {
    private MutableLiveData<String> petNameLiveData = new MutableLiveData<>();
    private MutableLiveData<String> petAgeLiveData = new MutableLiveData<>();
    private MutableLiveData<String> petColorLiveData = new MutableLiveData<>();
    private MutableLiveData<String> petBreedLiveData = new MutableLiveData<>();

    // Method to set the pet name
    public void setPetName(String petName) {
        petNameLiveData.setValue(petName);
    }

    // Method to observe the pet name
    public LiveData<String> getPetName() {
        return petNameLiveData;
    }

    // Method to set the pet age
    public void setPetAge(String petAge) {
        petAgeLiveData.setValue(petAge);
    }

    // Method to observe the pet age
    public LiveData<String> getPetAge() {
        return petAgeLiveData;
    }

    // Method to set the pet color
    public void setPetColor(String petColor) {
        petColorLiveData.setValue(petColor);
    }

    // Method to observe the pet color
    public LiveData<String> getPetColor() {
        return petColorLiveData;
    }

    // Method to set the pet breed
    public void setPetBreed(String petBreed) {
        petBreedLiveData.setValue(petBreed);
    }

    // Method to observe the pet breed
    public LiveData<String> getPetBreed() {
        return petBreedLiveData;
    }
}
