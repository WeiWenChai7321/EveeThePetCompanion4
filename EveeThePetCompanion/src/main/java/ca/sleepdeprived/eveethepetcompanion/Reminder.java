/*Section: 0NA
  Wei Wen Chai, N01447321
  John Aquino, N01303112
  Jennifer Nguyen, N01435464
  Ubay Abdulaziz, N01437353
*/
package ca.sleepdeprived.eveethepetcompanion;

public class Reminder {
    private String id;
    private String reminderText;

    public Reminder(String id, String reminderText) {
        this.id = id;
        this.reminderText = reminderText;
    }

    public String getId() {
        return id;
    }

    public String getReminderText() {
        return reminderText;
    }
}







