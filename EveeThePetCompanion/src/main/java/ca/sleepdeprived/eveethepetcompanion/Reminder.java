/*Section: 0NA
  Wei Wen Chai, N01447321
  John Aquino, N01303112
  Jennifer Nguyen, N01435464
  Ubay Abdulaziz, N01437353
*/
package ca.sleepdeprived.eveethepetcompanion;

public class Reminder {
    private String reminder;
    private String text;

    public Reminder() {
        // Empty constructor required for Firestore
    }

    public Reminder(String reminder, String text) {
        this.reminder = reminder;
        this.text = text;
    }

    public String getReminder() {
        return reminder;
    }

    public String getText() {
        return text;
    }
}
