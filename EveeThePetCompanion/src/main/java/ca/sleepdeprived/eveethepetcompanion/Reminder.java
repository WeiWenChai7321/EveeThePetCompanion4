/*Section: 0NA
  Wei Wen Chai, N01447321
  John Aquino, N01303112
  Jennifer Nguyen, N01435464
  Ubay Abdulaziz, N01437353
*/
package ca.sleepdeprived.eveethepetcompanion;

public class Reminder {
    private String reminder;

    public Reminder() {
        // Empty constructor required for Firestore
    }

    public Reminder(String reminder) {
        this.reminder = reminder;
    }

    public String getReminder() {
        return reminder;
    }
}
