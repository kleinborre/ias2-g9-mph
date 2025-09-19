package test;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import ui.PageHRPayroll;
import util.FieldAccessTest;

/**
 * Automated GUI tests for PageHRPayroll.
 * Uses FieldAccessTest for private field reflection.
 * Each test is self-contained and verifies the critical UI and logic expected in HR payroll processing.
 */
public class HRPayrollPageTest {

    PageHRPayroll page;

    /**
     * Prepare fresh test session and load HR Payroll page before each test.
     * - Sets a valid HR session.
     * - Loads the page using SwingUtilities to ensure thread safety.
     */
    @BeforeEach
    void setup() throws Exception {
        util.SessionManager.setSession("U10006", 10006); // HR login
        SwingUtilities.invokeAndWait(() -> page = new PageHRPayroll());
    }

    /**
     * Clean up page and clear session after each test.
     * - Disposes the UI frame.
     * - Clears the static session to avoid side effects across tests.
     */
    @AfterEach
    void cleanup() throws Exception {
        SwingUtilities.invokeAndWait(() -> page.dispose());
        util.SessionManager.clearSession();
    }

    /**
     * Test: All critical UI components should be present and enabled.
     * - Verifies presence and enabled state of: Back, Print buttons, Date chooser, Table, Summary fields.
     */
    @Test
    void testAllComponentsPresentAndEnabled() {
        JButton backButton           = FieldAccessTest.getField(page, "backButton", JButton.class);
        JButton printPayrollButton   = FieldAccessTest.getField(page, "printPayrollButton", JButton.class);
        com.toedter.calendar.JDateChooser JDateChooser = FieldAccessTest.getField(page, "JDateChooser", com.toedter.calendar.JDateChooser.class);
        JTable payrollTable          = FieldAccessTest.getField(page, "payrollTable", JTable.class);
        JScrollPane jScrollPane1     = FieldAccessTest.getField(page, "jScrollPane1", JScrollPane.class);
        JTextField totalGrossField   = FieldAccessTest.getField(page, "totalGrossField", JTextField.class);
        JTextField totalContributionsField = FieldAccessTest.getField(page, "totalContributionsField", JTextField.class);
        JTextField totalDeductionsField    = FieldAccessTest.getField(page, "totalDeductionsField", JTextField.class);
        JTextField totalNetPayField        = FieldAccessTest.getField(page, "totalNetPayField", JTextField.class);

        assertNotNull(backButton, "Back button exists");
        assertNotNull(printPayrollButton, "Print Payroll button exists");
        assertNotNull(JDateChooser, "JDateChooser exists");
        assertNotNull(payrollTable, "Payroll table exists");
        assertNotNull(jScrollPane1, "Table scroll pane exists");
        assertNotNull(totalGrossField, "Total Gross field exists");
        assertNotNull(totalContributionsField, "Total Contributions field exists");
        assertNotNull(totalDeductionsField, "Total Deductions field exists");
        assertNotNull(totalNetPayField, "Total Net Pay field exists");

        assertTrue(backButton.isEnabled(), "Back button enabled");
        assertTrue(printPayrollButton.isEnabled(), "Print Payroll enabled");
        assertTrue(JDateChooser.isEnabled(), "JDateChooser enabled");
    }

    /**
     * Test: The payroll table and scroll pane must be set up correctly.
     * - Date chooser is set to a real pay period.
     * - Ensures correct headers, row count, and that the table is inside the scroll pane.
     */
    @Test
    void testTableAndScrollPaneSetup() throws Exception {
        JTable payrollTable         = FieldAccessTest.getField(page, "payrollTable", JTable.class);
        JScrollPane jScrollPane1    = FieldAccessTest.getField(page, "jScrollPane1", JScrollPane.class);

        com.toedter.calendar.JDateChooser JDateChooser = FieldAccessTest.getField(page, "JDateChooser", com.toedter.calendar.JDateChooser.class);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date june3 = sdf.parse("2024-06-03");

        // Select the June 2024 pay period to ensure data is loaded
        SwingUtilities.invokeAndWait(() -> JDateChooser.setDate(june3));

        // Wait for payroll data to load (max 2 seconds)
        int retries = 0;
        while (payrollTable.getRowCount() == 0 && retries < 20) {
            Thread.sleep(100);
            retries++;
        }

        // The payroll table should be displayed inside the scroll pane
        assertSame(payrollTable, jScrollPane1.getViewport().getView(), "Table in scroll pane");

        // Table structure: at least 8 columns, at least 1 row of data
        TableModel model = payrollTable.getModel();
        assertTrue(model.getColumnCount() >= 8, "Table has correct columns");
        assertTrue(model.getRowCount() > 0, "Table has at least one row");

        // Column headers must match payslip schema
        assertEquals("Payslip No", model.getColumnName(0));
        assertEquals("Employee ID", model.getColumnName(1));
        assertEquals("Employee Name", model.getColumnName(2));
        assertEquals("Position/Department", model.getColumnName(3));
        assertEquals("Gross Income", model.getColumnName(4));
        assertEquals("Contributions", model.getColumnName(5));
        assertEquals("Deductions", model.getColumnName(6));
        assertEquals("Net Pay", model.getColumnName(7));
    }

    /**
     * Test: Setting the date chooser must reload data and summary fields for the selected period.
     * - Ensures that summary fields are correctly updated and non-empty for a real pay period.
     * - Checks correct format (e.g., "1,234.00").
     */
    @Test
    void testDateChooserFilterForJune2024() throws Exception {
        JTable payrollTable         = FieldAccessTest.getField(page, "payrollTable", JTable.class);
        com.toedter.calendar.JDateChooser JDateChooser = FieldAccessTest.getField(page, "JDateChooser", com.toedter.calendar.JDateChooser.class);
        JTextField totalGrossField  = FieldAccessTest.getField(page, "totalGrossField", JTextField.class);
        JTextField totalContributionsField = FieldAccessTest.getField(page, "totalContributionsField", JTextField.class);
        JTextField totalDeductionsField    = FieldAccessTest.getField(page, "totalDeductionsField", JTextField.class);
        JTextField totalNetPayField        = FieldAccessTest.getField(page, "totalNetPayField", JTextField.class);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date june3 = sdf.parse("2024-06-03");

        // Change date to a known period (June 2024)
        SwingUtilities.invokeAndWait(() -> JDateChooser.setDate(june3));

        // Wait for table data to refresh (max 2 seconds)
        int retries = 0;
        while (payrollTable.getRowCount() == 0 && retries < 20) {
            Thread.sleep(100);
            retries++;
        }

        TableModel model = payrollTable.getModel();
        assertTrue(model.getRowCount() > 0, "Rows present for June 2024");

        // Each summary field must be populated (not empty string)
        assertFalse(totalGrossField.getText().isEmpty(), "Total Gross populated");
        assertFalse(totalContributionsField.getText().isEmpty(), "Total Contributions populated");
        assertFalse(totalDeductionsField.getText().isEmpty(), "Total Deductions populated");
        assertFalse(totalNetPayField.getText().isEmpty(), "Total Net Pay populated");

        // Summary fields should have valid numeric/currency format
        assertTrue(totalGrossField.getText().matches("[\\d,]+\\.\\d{2}"), "Gross format ok");
        assertTrue(totalNetPayField.getText().matches("[\\d,]+\\.\\d{2}"), "Net pay format ok");
    }

    /**
     * Test: Back button must have an action listener (clickable).
     * - Ensures navigation is hooked.
     */
    @Test
    void testBackButtonHasActionListener() {
        JButton backButton = FieldAccessTest.getField(page, "backButton", JButton.class);
        ActionListener[] listeners = backButton.getActionListeners();
        assertTrue(listeners.length > 0, "Back button should have action listeners");
    }

    /**
     * Test: Print Payroll button should have action listener and trigger logic on click.
     * - Simulates click (does not validate PDF).
     */
    @Test
    void testPrintPayrollButtonTriggersAction() throws Exception {
        JButton printPayrollButton = FieldAccessTest.getField(page, "printPayrollButton", JButton.class);
        com.toedter.calendar.JDateChooser JDateChooser = FieldAccessTest.getField(page, "JDateChooser", com.toedter.calendar.JDateChooser.class);

        // Set the date to ensure there's payroll data to print
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date june3 = sdf.parse("2024-06-03");
        SwingUtilities.invokeAndWait(() -> JDateChooser.setDate(june3));

        ActionListener[] listeners = printPayrollButton.getActionListeners();
        assertTrue(listeners.length > 0, "Print Payroll button has action listener");

        // Simulate button click; PDF generation is handled inside the app
        SwingUtilities.invokeAndWait(() -> {
            for (ActionListener l : listeners) {
                l.actionPerformed(new ActionEvent(printPayrollButton, ActionEvent.ACTION_PERFORMED, "click"));
            }
        });
    }

    /**
     * Test: Payroll summary fields should be non-editable to prevent tampering.
     * - Ensures end user cannot change summary values manually.
     */
    @Test
    void testSummaryFieldsAreNonEditable() {
        JTextField totalGrossField = FieldAccessTest.getField(page, "totalGrossField", JTextField.class);
        JTextField totalContributionsField = FieldAccessTest.getField(page, "totalContributionsField", JTextField.class);
        JTextField totalDeductionsField = FieldAccessTest.getField(page, "totalDeductionsField", JTextField.class);
        JTextField totalNetPayField = FieldAccessTest.getField(page, "totalNetPayField", JTextField.class);

        assertFalse(totalGrossField.isEditable(), "Gross is non-editable");
        assertFalse(totalContributionsField.isEditable(), "Contributions non-editable");
        assertFalse(totalDeductionsField.isEditable(), "Deductions non-editable");
        assertFalse(totalNetPayField.isEditable(), "Net Pay non-editable");
    }
}