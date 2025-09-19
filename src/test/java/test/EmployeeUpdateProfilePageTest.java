package test;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;
import ui.PageEmployeeDataUpdateProfile;
import util.SessionManager;

import javax.swing.*;
import java.awt.event.ActionEvent;

class EmployeeUpdateProfilePageTest {
//
//    private PageEmployeeDataUpdateProfile page;
//
//    // ---- Configure these for any employee you want to test ----
//    private final String expectedEmployeeID     = "10012";
//    private final String expectedFirstName      = "Josie";
//    private final String expectedLastName       = "Lopez";
//    private final String expectedPosition       = "Payroll Team Leader";
//    private final String expectedSupervisor     = "Salcedo, Anthony";
//    private final String expectedBirthday       = "1987-01-14";
//    private final String expectedPhoneNumber    = "478-355-427";
//    private final String expectedHouseNo        = "49 Apt. 266";
//    private final String expectedStreet         = "Springs";
//    private final String expectedBarangay       = "Poblacion";
//    private final String expectedCity           = "Taguig";
//    private final String expectedProvince       = "Occidental Mindoro";
//    private final String expectedZipCode        = "3200";
//
//    @BeforeEach
//    void setUp() {
//        // Set session to the employee you want to test
//        SessionManager.setSession("U10012", 10012); // change as needed
//        page = new PageEmployeeDataUpdateProfile();
//    }
//
//    @AfterEach
//    void tearDown() {
//        if (page != null) {
//            page.dispose();
//        }
//        SessionManager.clearSession();
//    }
//
//    // --- LABEL TESTS ---
//
//    @Test
//    void testEmployeeIDLabel() {
//        JLabel label = page.getEmployeeIDText();
//        assertEquals(expectedEmployeeID, label.getText());
//    }
//
//    @Test
//    void testFirstNameLabel() {
//        JLabel label = page.getFirstNameText();
//        assertEquals(expectedFirstName, label.getText());
//    }
//
//    @Test
//    void testLastNameLabel() {
//        JLabel label = page.getLastNameText();
//        assertEquals(expectedLastName, label.getText());
//    }
//
//    @Test
//    void testPositionLabel() {
//        JLabel label = page.getPositionText();
//        assertEquals(expectedPosition, label.getText());
//    }
//
//    @Test
//    void testSupervisorLabel() {
//        JLabel label = page.getSupervisorText();
//        assertEquals(expectedSupervisor, label.getText());
//    }
//
//    @Test
//    void testBirthdayLabel() {
//        JLabel label = page.getBirthdayText();
//        assertEquals(expectedBirthday, label.getText());
//    }
//
//    // --- TEXT FIELD TESTS ---
//
//    @Test
//    void testPhoneNumberField() {
//        JTextField field = page.getPhoneNumberField();
//        assertEquals(expectedPhoneNumber, field.getText());
//    }
//
//    @Test
//    void testHouseNoField() {
//        JTextField field = page.getHouseNoField();
//        assertEquals(expectedHouseNo, field.getText());
//    }
//
//    @Test
//    void testStreetField() {
//        JTextField field = page.getStreetField();
//        assertEquals(expectedStreet, field.getText());
//    }
//
//    @Test
//    void testBarangayField() {
//        JTextField field = page.getBarangayField();
//        assertEquals(expectedBarangay, field.getText());
//    }
//
//    @Test
//    void testCityField() {
//        JTextField field = page.getCityField();
//        assertEquals(expectedCity, field.getText());
//    }
//
//    @Test
//    void testProvinceField() {
//        JTextField field = page.getProvinceField();
//        assertEquals(expectedProvince, field.getText());
//    }
//
//    @Test
//    void testZipCodeField() {
//        JTextField field = page.getZipCodeField();
//        // Assert initial value
//        assertEquals(expectedZipCode, field.getText());
//    }
//
//    // --- BUTTON TESTS ---
//
//    @Test
//    void testUpdateButtonInitiallyDisabled() {
//        JButton updateButton = getButton(page, "Update");
//        assertNotNull(updateButton);
//        assertFalse(updateButton.isEnabled());
//    }
//
//    @Test
//    void testCancelButtonExistsAndWorks() {
//        JButton cancelButton = getButton(page, "Cancel");
//        assertNotNull(cancelButton);
//        assertEquals("Cancel", cancelButton.getText());
//    }
//
//    @Test
//    void testBackButtonExistsAndWorks() {
//        JButton backButton = getButton(page, "Back");
//        assertNotNull(backButton);
//        assertEquals("Back", backButton.getText());
//    }
//
//    // --- FUNCTIONAL TESTS ---
//
//    @Test
//    void testEditUnlocksUpdateButton() {
//        JButton updateButton = getButton(page, "Update");
//        JTextField field = page.getCityField();
//        assertFalse(updateButton.isEnabled());
//
//        // Simulate user changing the city
//        field.setText("ChangedCity");
//        // Simulate event
//        for (var listener : field.getActionListeners()) {
//            listener.actionPerformed(new ActionEvent(field, ActionEvent.ACTION_PERFORMED, ""));
//        }
//        // updateButton should now be enabled
//        assertTrue(updateButton.isEnabled());
//    }
//
//    @Test
//    void testPhoneNumberFormatter() throws Exception {
//        JTextField field = page.getPhoneNumberField();
//        SwingUtilities.invokeAndWait(() -> {
//            field.setText("123456789");
//        });
//        // Depending on implementation, formatting might be async—wait for EDT
//        SwingUtilities.invokeAndWait(() -> {});
//        assertEquals("123-456-789", field.getText());
//    }
//
//    @Test
//    void testZipCodeFormatter() throws Exception {
//        JTextField field = page.getZipCodeField();
//        // Explicitly set a zip code longer than 4 digits, expect formatter trims to 4 digits
//        SwingUtilities.invokeAndWait(() -> {
//            field.setText("10010");
//        });
//        SwingUtilities.invokeAndWait(() -> {});
//        assertEquals("1001", field.getText());
//    }
//
//    @Test
//    void testFieldValidationHighlightsOnEmpty() {
//        JTextField field = page.getHouseNoField();
//        field.setText("");
//        // Should have non-default background (highlighted for error)
//        assertNotEquals(java.awt.Color.WHITE, field.getBackground());
//    }
//
//    // --- UTILITY ---
//    private JButton getButton(PageEmployeeDataUpdateProfile page, String text) {
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
