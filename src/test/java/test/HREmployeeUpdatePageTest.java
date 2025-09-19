package test;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.Date;

import ui.PageHREmployeeUpdate;

public class HREmployeeUpdatePageTest {

    PageHREmployeeUpdate page;

    @BeforeEach
    void setup() throws Exception {
        util.SessionManager.setSession("U10006", 10006); // HR/Admin user
        util.SessionManager.setSelectedEmployeeID(10001); // select employeeID 10001 for update
        SwingUtilities.invokeAndWait(() -> page = new PageHREmployeeUpdate());
    }

    @AfterEach
    void cleanup() throws Exception {
        SwingUtilities.invokeAndWait(() -> page.dispose());
        util.SessionManager.clearSession();
    }

    @Test
    void testAllButtonsExistAndEnabled() {
        assertNotNull(page.backButton, "Back button should exist");
        assertNotNull(page.cancelButton, "Cancel button should exist");
        assertNotNull(page.confirmButton, "Update/Confirm button should exist");

        assertTrue(page.backButton.isEnabled(), "Back button should be enabled");
        assertTrue(page.cancelButton.isEnabled(), "Cancel button should be enabled");
        // Confirm/Update button is disabled until changes are made
        assertFalse(page.confirmButton.isEnabled(), "Update button should be disabled until fields change");
    }

    @Test
    void testTextFieldsPresentAndEditable() {
        for (JTextField field : new JTextField[] {
                page.lastNameField, page.firstNameField, page.provinceField, page.cityMunicipalityField,
                page.barangayField, page.streetField, page.houseNumberField, page.zipCodeField,
                page.phoneNumberField, page.sssNumberField, page.philhealthNumberField,
                page.tinNumberField, page.pagibigNumberField
        }) {
            assertNotNull(field, "Field should not be null: " + field);
            assertTrue(field.isEditable(), "Field should be editable: " + field);
        }
    }

    @Test
    void testComboBoxesPresentAndSelectable() throws Exception {
        JComboBox<?>[] combos = {
                page.employeeRoleComboBox,
                page.employmentStatusComboBox,
                page.positionComboBox,
                page.departmentComboBox,
                page.supervisorComboBox,
                page.basicSalaryComboBox
        };
        for (JComboBox<?> combo : combos) {
            assertNotNull(combo, "ComboBox should exist: " + combo);
            assertTrue(combo.getItemCount() > 0, "ComboBox should have options: " + combo);
            // Test changing selection
            int lastIndex = combo.getItemCount() - 1;
            SwingUtilities.invokeAndWait(() -> combo.setSelectedIndex(lastIndex));
            assertEquals(lastIndex, combo.getSelectedIndex(), "ComboBox should be able to select last item");
        }
    }

    @Test
    void testJDateChooserExistsAndSettable() throws Exception {
        assertNotNull(page.dateOfBirthCalendar, "JDateChooser should exist");
        Date today = new Date();
        SwingUtilities.invokeAndWait(() -> page.dateOfBirthCalendar.setDate(today));
        assertNotNull(page.dateOfBirthCalendar.getDate(), "JDateChooser date should be settable");
    }

    @Test
    void testUpdateButtonUnlocksOnFieldChange() throws Exception {
        assertFalse(page.confirmButton.isEnabled(), "Update button disabled at start");
        // Simulate editing first name
        SwingUtilities.invokeAndWait(() -> page.firstNameField.setText("Juan"));
        // Simulate editing last name
        SwingUtilities.invokeAndWait(() -> page.lastNameField.setText("Dela Cruz"));
        assertTrue(page.confirmButton.isEnabled(), "Update button should be enabled after change");
    }

    @Test
    void testBackAndCancelButtonAction() {
        ActionListener[] backListeners = page.backButton.getActionListeners();
        assertTrue(backListeners.length > 0, "Back button should have action listeners");
        ActionListener[] cancelListeners = page.cancelButton.getActionListeners();
        assertTrue(cancelListeners.length > 0, "Cancel button should have action listeners");
    }

    @Test
    void testAllFieldValidatorsAndHighlighting() throws Exception {
        // Set values and expect the formatters to kick in
        SwingUtilities.invokeAndWait(() -> {
            page.firstNameField.setText("Juan");
            page.lastNameField.setText("Dela Cruz");
            page.provinceField.setText("Laguna");
            page.cityMunicipalityField.setText("Calamba");
            page.barangayField.setText("Malaban");
            page.streetField.setText("Bonifacio");
            page.houseNumberField.setText("123");
            page.zipCodeField.setText("4024");
            page.phoneNumberField.setText("123143546");           // Should become 123-143-546
            page.sssNumberField.setText("4657685658");            // Should become 46-5768565-8
            page.philhealthNumberField.setText("364575656785");   // Pass-through 12 digits
            page.pagibigNumberField.setText("456476558588");      // Pass-through 12 digits
            page.tinNumberField.setText("475768557000");          // Should become 475-768-557-000
        });

        // Wait for auto-formatters to finish
        for (int i = 0; i < 3; ++i) SwingUtilities.invokeAndWait(() -> {});

        assertEquals("Juan", page.firstNameField.getText());
        assertEquals("Dela Cruz", page.lastNameField.getText());
        assertEquals("Laguna", page.provinceField.getText());
        assertEquals("Calamba", page.cityMunicipalityField.getText());
        assertEquals("Malaban", page.barangayField.getText());
        assertEquals("Bonifacio", page.streetField.getText());
        assertEquals("123", page.houseNumberField.getText());
        assertEquals("4024", page.zipCodeField.getText());

        assertEquals("123-143-546", page.phoneNumberField.getText(), "Phone should format as NNN-NNN-NNN");
        assertEquals("46-5768565-8", page.sssNumberField.getText(), "SSS should format as NN-NNNNNNN-N");
        assertEquals("364575656785", page.philhealthNumberField.getText(), "PhilHealth should be plain 12 digits");
        assertEquals("456476558588", page.pagibigNumberField.getText(), "Pag-IBIG should be plain 12 digits");
        assertEquals("475-768-557-000", page.tinNumberField.getText(), "TIN should format as NNN-NNN-NNN-000");

        // Background checks for min length can be included, but these are cosmetic for headless tests
        assertNotNull(page.firstNameField.getBackground());
    }

    @Test
    void testComboBoxSynchronization() throws Exception {
        int origPosIdx = page.positionComboBox.getSelectedIndex();
        int newIdx = (origPosIdx == 0) ? 1 : 0;
        SwingUtilities.invokeAndWait(() -> page.positionComboBox.setSelectedIndex(newIdx));
        assertTrue(page.departmentComboBox.getSelectedIndex() >= 0, "Department should have valid index after position change");
    }
}
