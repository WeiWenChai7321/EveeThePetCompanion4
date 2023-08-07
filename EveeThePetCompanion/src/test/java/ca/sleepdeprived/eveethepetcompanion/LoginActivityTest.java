/*Section: 0NA
  Wei Wen Chai, N01447321
  John Aquino, N01303112
  Jennifer Nguyen, N01435464
  Ubay Abdulaziz, N01437353
*/
package ca.sleepdeprived.eveethepetcompanion;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LoginActivityTest {

    @Test
<<<<<<< HEAD
    public void isValidEmail_CorrectFormat_ReturnsTrue() {
        assertTrue(LoginActivity.isValidEmail("example@gmail.com"));
    }

    @Test
    public void isValidEmail_IncorrectFormat_ReturnsFalse() {
        assertFalse(LoginActivity.isValidEmail("examplegmail.com"));
=======
    public void isValidPassword_OnlyUpperCaseAndSpecialChar_ReturnsTrue() {
        assertTrue(LoginActivity.isValidPassword("ABC@1234"));
    }

    @Test
    public void isValidPassword_Empty_ReturnsFalse() {
        assertFalse(LoginActivity.isValidPassword(""));
>>>>>>> master
    }

    @Test
    public void isValidPassword_CorrectFormat_ReturnsTrue() {
        assertTrue(LoginActivity.isValidPassword("Abc@1234"));
    }

    @Test
    public void isValidPassword_NoUpperCase_ReturnsFalse() {
        assertFalse(LoginActivity.isValidPassword("abc@1234"));
    }

    @Test
    public void isValidPassword_NoDigit_ReturnsFalse() {
        assertFalse(LoginActivity.isValidPassword("Abc@abcd"));
    }

    @Test
    public void isValidPassword_NoSpecialChar_ReturnsFalse() {
        assertFalse(LoginActivity.isValidPassword("Abc12345"));
    }

    @Test
    public void isValidPassword_LessThanSixChars_ReturnsFalse() {
        assertFalse(LoginActivity.isValidPassword("Ab@1"));
    }
<<<<<<< HEAD
=======

>>>>>>> master
}
