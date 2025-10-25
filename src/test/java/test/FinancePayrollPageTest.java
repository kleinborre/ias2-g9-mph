package test;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import ui.PageFinancePayroll;

public class FinancePayrollPageTest {

    PageFinancePayroll page;

    @BeforeEach
    void setup() throws Exception {
        util.SessionManager.setSession("U10011", 10011); // Finance login
        SwingUtilities.invokeAndWait(() -> page = new PageFinancePayroll());
    }

    @AfterEach
    void cleanup() throws Exception {
        SwingUtilities.invokeAndWait(() -> page.dispose());
        util.SessionManager.clearSession();
    }

    @Test
    void testAllComponentsPresentAndEnabled() {
        assertNotNull(page.backButton, "Back button exists");
        assertNotNull(page.printPayrollButton, "Print Payroll button exists");
        assertNotNull(page.JDateChooser, "JDateChooser exists");
        assertNotNull(page.payrollTable, "Payroll table exists");
        assertNotNull(page.jScrollPane1, "Table scroll pane exists");
        assertNotNull(page.totalGrossField, "Total Gross field exists");
        assertNotNull(page.totalContributionsField, "Total Contributions field exists");
        assertNotNull(page.totalDeductionsField, "Total Deductions field exists");
        assertNotNull(page.totalNetPayField, "Total Net Pay field exists");

        assertTrue(page.backButton.isEnabled(), "Back button enabled");
        assertTrue(page.printPayrollButton.isEnabled(), "Print Payroll enabled");
        assertTrue(page.JDateChooser.isEnabled(), "JDateChooser enabled");
    }

    @Test
    void testTableAndScrollPaneSetup() throws Exception {
        // Set JDateChooser to June 3, 2024 to ensure relevant data is loaded
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date june3 = sdf.parse("2024-06-03");

        SwingUtilities.invokeAndWait(() -> {
            page.JDateChooser.setDate(june3);
            // The table and fields should auto-refresh via property change
        });

        // Now do assertions
        assertSame(page.payrollTable, page.jScrollPane1.getViewport().getView(), "Table in scroll pane");

        TableModel model = page.payrollTable.getModel();
        assertTrue(model.getColumnCount() >= 8, "Table has correct columns");
        assertTrue(model.getRowCount() > 0, "Table has at least one row");

        // Column names check
        assertEquals("Payslip No", model.getColumnName(0));
        assertEquals("Employee ID", model.getColumnName(1));
        assertEquals("Employee Name", model.getColumnName(2));
        assertEquals("Position/Department", model.getColumnName(3));
        assertEquals("Gross Income", model.getColumnName(4));
        assertEquals("Contributions", model.getColumnName(5));
        assertEquals("Deductions", model.getColumnName(6));
        assertEquals("Net Pay", model.getColumnName(7));
    }


    @Test
    void testDateChooserFilterForJune2024() throws Exception {
        // Set JDateChooser to June 3, 2024
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date june3 = sdf.parse("2024-06-03");

        SwingUtilities.invokeAndWait(() -> {
            page.JDateChooser.setDate(june3);
            // The table and fields should auto-refresh via property change
        });

        TableModel model = page.payrollTable.getModel();
        assertTrue(model.getRowCount() > 0, "Rows present for June 2024");

        // Check summary fields reflect correct format (should be updated by UI logic)
        assertFalse(page.totalGrossField.getText().isEmpty(), "Total Gross populated");
        assertFalse(page.totalContributionsField.getText().isEmpty(), "Total Contributions populated");
        assertFalse(page.totalDeductionsField.getText().isEmpty(), "Total Deductions populated");
        assertFalse(page.totalNetPayField.getText().isEmpty(), "Total Net Pay populated");

        // Validate numeric format: "1,352,876.41" etc.
        assertTrue(page.totalGrossField.getText().matches("[\\d,]+\\.\\d{2}"), "Gross format ok");
        assertTrue(page.totalNetPayField.getText().matches("[\\d,]+\\.\\d{2}"), "Net pay format ok");
    }

    @Test
    void testBackButtonHasActionListener() {
        ActionListener[] listeners = page.backButton.getActionListeners();
        assertTrue(listeners.length > 0, "Back button should have action listeners");
    }

    @Test
    void testPrintPayrollButtonTriggersAction() throws Exception {
        // Ensure at least one payslip is present for June 2024
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date june3 = sdf.parse("2024-06-03");
        SwingUtilities.invokeAndWait(() -> page.JDateChooser.setDate(june3));

        // ActionListener is attached
        ActionListener[] listeners = page.printPayrollButton.getActionListeners();
        assertTrue(listeners.length > 0, "Print Payroll button has action listener");

        // Simulate click: just ensure no exceptions (PDF generation handled in UI class)
        SwingUtilities.invokeAndWait(() -> {
            for (ActionListener l : listeners) {
                l.actionPerformed(new ActionEvent(page.printPayrollButton, ActionEvent.ACTION_PERFORMED, "click"));
            }
        });
    }

    @Test
    void testSummaryFieldsAreNonEditable() {
        assertFalse(page.totalGrossField.isEditable(), "Gross is non-editable");
        assertFalse(page.totalContributionsField.isEditable(), "Contributions non-editable");
        assertFalse(page.totalDeductionsField.isEditable(), "Deductions non-editable");
        assertFalse(page.totalNetPayField.isEditable(), "Net Pay non-editable");
    }
}
