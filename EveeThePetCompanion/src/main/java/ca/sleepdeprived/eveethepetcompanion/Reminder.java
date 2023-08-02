/*Section: 0NA
  Wei Wen Chai, N01447321
  John Aquino, N01303112
  Jennifer Nguyen, N01435464
  Ubay Abdulaziz, N01437353
*/
package ca.sleepdeprived.eveethepetcompanion;

public class Reminder {
    private String text;

    // Required empty constructor for Firestore
    public Reminder() {
    }

    public Reminder(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}