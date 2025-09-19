package test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import ui.PageEmployeeDataUpdateCredential;
import util.SessionManager;
import java.lang.reflect.Method;

import javax.swing.*;

class EmployeeUpdateCredentialPageTest {
//
//    private PageEmployeeDataUpdateCredential page;
//
//    private final String expectedUserID      = "U10012";
//    private final int    expectedEmployeeID  = 10012;
//    private final String expectedEmail       = "jlopez@motor.ph";
//    private final String expectedUsername    = "Josie Lopez";
//    private final String expectedPosition    = "Payroll Team Leader";
//    private final String expectedSupervisor  = "Salcedo, Anthony";
//    private static final String CORRECT_PASSWORD = "Lopez@10012"; // <- Make sure this matches the user's real password
//
//    @BeforeEach
//    void setUp() {
//        SessionManager.setSession(expectedUserID, expectedEmployeeID);
//        page = new PageEmployeeDataUpdateCredential();
//    }
//
//    @AfterEach
//    void tearDown() {
//        if (page != null) page.dispose();
//        SessionManager.clearSession();
//    }
//
//    // --- LABEL TESTS ---
//    @Test void testUsernameLabel()      { assertEquals(expectedUsername,  page.getUsernameText().getText()); }
//    @Test void testEmailLabel()         { assertEquals(expectedEmail,     page.getEmailText().getText()); }
//    @Test void testEmployeeIDLabel()    { assertEquals(String.valueOf(expectedEmployeeID), page.getEmployeeIDText().getText()); }
//    @Test void testPositionLabel()      { assertEquals(expectedPosition,  page.getPositionText().getText()); }
//    @Test void testSupervisorLabel()    { assertEquals(expectedSupervisor,page.getSupervisorText().getText()); }
//
//    // --- PASSWORD FIELD TESTS ---
//    @Test void testPasswordFieldsAreEmptyOnInit() {
//        assertArrayEquals(new char[0], page.passwordCurrentField.getPassword());
//        assertArrayEquals(new char[0], page.passwordNewField.getPassword());
//        assertArrayEquals(new char[0], page.passwordReEnterField.getPassword());
//    }
//
//    // --- BUTTON TESTS ---
//    @Test void testUpdateButtonInitiallyDisabled() {
//        JButton updateButton = getButton(page, "Update");
//        assertNotNull(updateButton);
//        assertFalse(updateButton.isEnabled());
//    }
//    @Test void testCancelButtonExists() {
//        JButton cancelButton = getButton(page, "Cancel");
//        assertNotNull(cancelButton);
//        assertEquals("Cancel", cancelButton.getText());
//    }
//    @Test void testBackButtonExists() {
//        JButton backButton = getButton(page, "Back");
//        assertNotNull(backButton);
//        assertEquals("Back", backButton.getText());
//    }
//
//    // --- FUNCTIONAL/VALIDATION TESTS ---
//
//    @Test void testEditEnablesUpdateButton() throws Exception {
//        JButton updateButton = getButton(page, "Update");
//        assertFalse(updateButton.isEnabled());
//        page.passwordCurrentField.setText(CORRECT_PASSWORD);
//        page.passwordNewField.setText("aA1!aaaa");
//        SwingUtilities.invokeAndWait(() -> {});
//        assertTrue(updateButton.isEnabled());
//    }
//
//    @Test void testPasswordRequirement_TooShort() throws Exception {
//        page.passwordCurrentField.setText(CORRECT_PASSWORD);
//        page.passwordNewField.setText("aA1!");
//        page.passwordReEnterField.setText("aA1!");
//        SwingUtilities.invokeAndWait(() -> {});
//        boolean valid = invokeValidateFieldsAndShowErrors();
//        assertFalse(valid);
//        assertEquals("New password too weak.", page.errorMessageLabel.getText());
//    }
//
//    @Test void testPasswordRequirement_NoUppercase() throws Exception {
//        page.passwordCurrentField.setText(CORRECT_PASSWORD);
//        page.passwordNewField.setText("aa1!aaaa");
//        page.passwordReEnterField.setText("aa1!aaaa");
//        SwingUtilities.invokeAndWait(() -> {});
//        boolean valid = invokeValidateFieldsAndShowErrors();
//        assertFalse(valid);
//        assertEquals("New password too weak.", page.errorMessageLabel.getText());
//    }
//
//    @Test void testPasswordRequirement_NoLowercase() throws Exception {
//        page.passwordCurrentField.setText(CORRECT_PASSWORD);
//        page.passwordNewField.setText("AA1!AAAA");
//        page.passwordReEnterField.setText("AA1!AAAA");
//        SwingUtilities.invokeAndWait(() -> {});
//        boolean valid = invokeValidateFieldsAndShowErrors();
//        assertFalse(valid);
//        assertEquals("New password too weak.", page.errorMessageLabel.getText());
//    }
//
//    @Test void testPasswordRequirement_NoDigit() throws Exception {
//        page.passwordCurrentField.setText(CORRECT_PASSWORD);
//        page.passwordNewField.setText("Aa!aaaaa");
//        page.passwordReEnterField.setText("Aa!aaaaa");
//        SwingUtilities.invokeAndWait(() -> {});
//        boolean valid = invokeValidateFieldsAndShowErrors();
//        assertFalse(valid);
//        assertEquals("New password too weak.", page.errorMessageLabel.getText());
//    }
//
//    @Test void testPasswordRequirement_NoSpecialChar() throws Exception {
//        page.passwordCurrentField.setText(CORRECT_PASSWORD);
//        page.passwordNewField.setText("Aa1aaaaa");
//        page.passwordReEnterField.setText("Aa1aaaaa");
//        SwingUtilities.invokeAndWait(() -> {});
//        boolean valid = invokeValidateFieldsAndShowErrors();
//        assertFalse(valid);
//        assertEquals("New password too weak.", page.errorMessageLabel.getText());
//    }
//
//    @Test void testPasswordRequirement_AllValid() throws Exception {
//        page.passwordCurrentField.setText(CORRECT_PASSWORD);
//        page.passwordNewField.setText("NewValid1!");
//        page.passwordReEnterField.setText("NewValid1!");
//        SwingUtilities.invokeAndWait(() -> {});
//        boolean valid = invokeValidateFieldsAndShowErrors();
//        assertTrue(valid);
//    }
//
//    @Test void testPasswordMismatch() throws Exception {
//        page.passwordCurrentField.setText(CORRECT_PASSWORD);
//        page.passwordNewField.setText("NewValid1!");
//        page.passwordReEnterField.setText("Mismatch1!");
//        SwingUtilities.invokeAndWait(() -> {});
//        boolean valid = invokeValidateFieldsAndShowErrors();
//        assertFalse(valid);
//        assertEquals("Passwords do not match.", page.errorMessageLabel.getText());
//    }
//
//    @Test void testReuseOldPassword() throws Exception {
//        page.passwordCurrentField.setText(CORRECT_PASSWORD);
//        page.passwordNewField.setText(CORRECT_PASSWORD);
//        page.passwordReEnterField.setText(CORRECT_PASSWORD);
//        SwingUtilities.invokeAndWait(() -> {});
//        boolean valid = invokeValidateFieldsAndShowErrors();
//        assertFalse(valid);
//        assertEquals("New password must differ from current.", page.errorMessageLabel.getText());
//    }
//
//    @Test void testNewPasswordContainsCurrentPassword() throws Exception {
//        page.passwordCurrentField.setText(CORRECT_PASSWORD);
//        page.passwordNewField.setText(CORRECT_PASSWORD + "Aa1!");
//        page.passwordReEnterField.setText(CORRECT_PASSWORD + "Aa1!");
//        SwingUtilities.invokeAndWait(() -> {});
//        boolean valid = invokeValidateFieldsAndShowErrors();
//        assertFalse(valid);
//        assertEquals("New password must not contain your current password.", page.errorMessageLabel.getText());
//    }
//
//    @Test void testEmptyFields() throws Exception {
//        // Clear the fields
//        page.passwordCurrentField.setText("");
//        page.passwordNewField.setText("");
//        page.passwordReEnterField.setText("");
//        // Simulate an edit to set isDirty=true
//        SwingUtilities.invokeAndWait(() -> page.passwordCurrentField.setText(" ")); // Type something
//        SwingUtilities.invokeAndWait(() -> page.passwordCurrentField.setText(""));  // Delete again
//        boolean valid = invokeValidateFieldsAndShowErrors();
//        assertFalse(valid);
//        assertEquals("Current password is required.", page.errorMessageLabel.getText());
//    }
//
//
//    // --- Reflection Helper for Protected Method ---
//    private boolean invokeValidateFieldsAndShowErrors() throws Exception {
//        Method m = page.getClass().getSuperclass().getDeclaredMethod("validateFieldsAndShowErrors");
//        m.setAccessible(true);
//        return (Boolean) m.invoke(page);
//    }
//
//    // --- UTILITY ---
//    private JButton getButton(PageEmployeeDataUpdateCredential page, String text) {
//        for (java.awt.Component c : page.getContentPane().getComponents()) {
//            if (c instanceof JButton) {
//                JButton btn = (JButton) c;
//                if (text.equals(btn.getText())) {
//                    return btn;
//                }
//            }
//        }
//        return null;
//    }
}
