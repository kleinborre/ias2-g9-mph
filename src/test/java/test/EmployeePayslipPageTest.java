package test;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import javax.swing.*;
import java.lang.reflect.*;
import java.util.*;
import com.toedter.calendar.JDateChooser;
import ui.PageEmployeePayslip;

public class EmployeePayslipPageTest {

    PageEmployeePayslip page;

    @BeforeEach
    void setup() throws Exception {
        // Set session to use correct employee
        util.SessionManager.setSession("U10012", 10012);

        // Create page instance on the EDT
        SwingUtilities.invokeAndWait(() -> page = new PageEmployeePayslip());
    }

    @AfterEach
    void teardown() throws Exception {
        SwingUtilities.invokeAndWait(() -> page.dispose());
    }

    @Test
    void testSelectSpecificPayslipPeriodAndFieldsPopulate() throws Exception {
        // Find the correct payslip period in comboBox
        JComboBox<String> comboBox = page.payslipPeriodComboBox;
        assertNotNull(comboBox, "Payslip period combo box should exist");
        boolean found = false;
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            String item = comboBox.getItemAt(i);
            if ("12-2024-06-28".equals(item)) {
                comboBox.setSelectedIndex(i);
                found = true;
                break;
            }
        }
        assertTrue(found, "Payslip period '12-2024-06-28' should exist in the combo box");

        // Simulate the selection event (if needed, may be auto-fired)
        SwingUtilities.invokeAndWait(() -> comboBox.setSelectedItem("12-2024-06-28"));

        // Assert fields have expected values (adapt values to actual expected from DB)
        assertEquals("12-2024-06-28", comboBox.getSelectedItem(), "ComboBox selection should be correct");
        assertFalse(page.monthlyRateField.getText().isEmpty(), "Monthly rate should be filled");
        assertFalse(page.takeHomePayField.getText().isEmpty(), "Take-home pay should be filled");
        assertFalse(page.totalDeductionsField.getText().isEmpty(), "Total deductions should be filled");
        // ...add more asserts for the rest of the fields as needed

        // Test the Print Payslip button
        JButton printBtn = page.printPayslipButton;
        assertNotNull(printBtn, "Print Payslip button should exist");
        SwingUtilities.invokeAndWait(printBtn::doClick);

        // Optionally: Verify that the success dialog appears
        // For headless or automated testing, you'd need a DialogFixture or similar
        // Here, just check that fields remain populated after print
        assertFalse(page.takeHomePayField.getText().isEmpty(), "Take-home pay should remain filled after print");
    }

    // Optionally, test JDateChooser interaction too
    @Test
    void testDateChooserSelectionUpdatesFields() throws Exception {
        JDateChooser chooser = page.JDateChooser;
        assertNotNull(chooser, "Date chooser should exist");
        java.util.Date testDate = new java.text.SimpleDateFormat("yyyy-MM-dd").parse("2024-06-28");
        SwingUtilities.invokeAndWait(() -> chooser.setDate(testDate));
        // After selecting a valid date, fields should be populated if a payslip exists for that period
        assertFalse(page.monthlyRateField.getText().isEmpty() || page.takeHomePayField.getText().isEmpty(),
                "After selecting valid date, fields should be filled if payslip exists");
    }
}
