package test;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;

import ui.PageManagerLeaveAdmin;
import javax.swing.*;
import javax.swing.table.TableModel;
import java.text.SimpleDateFormat;
import java.util.Date;

class ManagerLeaveAdminPageTest {

    private PageManagerLeaveAdmin page;

    @BeforeEach
    void setUp() {
        // Simulate logging in as manager (U10001, employeeID 10001) if needed
        page = new PageManagerLeaveAdmin();
    }

    @AfterEach
    void tearDown() {
        if (page != null) page.dispose();
    }

    @Test
    void testAllUIComponentsExistAndInitialState() {
        assertNotNull(page.employeeIDComboBox, "EmployeeID ComboBox exists");
        assertNotNull(page.JDateChooser, "DateChooser exists");
        assertNotNull(page.leaveTable, "Leave JTable exists");
        assertNotNull(page.jScrollPane1, "JScrollPane exists");
        assertNotNull(page.backButton, "Back Button exists");
        assertNotNull(page.viewOwnRecordButton, "View Own Record Button exists");
        assertNotNull(page.refreshButton, "Refresh Table Button exists");

        // Table should have correct columns
        TableModel model = page.leaveTable.getModel();
        String[] expectedCols = {
            "Leave ID", "Employee ID", "Approval Status", "Leave Type",
            "Leave Start", "Leave End", "Leave Reason"
        };
        for (int i = 0; i < expectedCols.length; ++i) {
            assertEquals(expectedCols[i], model.getColumnName(i));
        }
        // Should show some rows on startup (from DB/dummy)
        assertTrue(model.getRowCount() > 0, "Leave table should not be empty");
    }

    @Test
    void testEmployeeIDComboBox_Filter10002() throws Exception {
        JComboBox<String> combo = page.employeeIDComboBox;
        boolean found = false;
        for (int i = 0; i < combo.getItemCount(); ++i)
            if ("10002".equals(combo.getItemAt(i))) found = true;
        assertTrue(found, "Combo box contains '10002'");

        // Select 10002, should filter table to only that employee
        combo.setSelectedItem("10002");
        // Table updates on selection
        TableModel model = page.leaveTable.getModel();
        assertTrue(model.getRowCount() > 0, "Table shows leave(s) for employee 10002");
        for (int i = 0; i < model.getRowCount(); ++i) {
            String empID = model.getValueAt(i, 1).toString();
            assertEquals("10002", empID, "Each row is for employee 10002");
        }
    }

    @Test
    void testDateChooser_FilterJuly2024() throws Exception {
        // Set JDateChooser to July 15, 2024. Should show all records in July 2024
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        Date july15 = fmt.parse("2024-07-15");
        page.JDateChooser.setDate(july15);
        page.JDateChooser.getDateEditor().setDate(july15);

        TableModel model = page.leaveTable.getModel();
        assertTrue(model.getRowCount() > 0, "Table filtered by July 2024 should have rows");

        for (int i = 0; i < model.getRowCount(); ++i) {
            String leaveStart = model.getValueAt(i, 4).toString(); // "Leave Start"
            String leaveEnd = model.getValueAt(i, 5).toString();   // "Leave End"
            // Optionally: Add Date Created if available in table

            boolean isJuly = leaveStart.startsWith("2024-07-") || leaveEnd.startsWith("2024-07-");
            assertTrue(isJuly, 
                String.format("Leave Start or End is in July 2024: %s - %s", leaveStart, leaveEnd));
        }
    }

    @Test
    void testEmployeeIDComboBoxAndDateChooserTogether() throws Exception {
        // Select 10002 and filter for July 2024. Should only see July 2024 leaves for employee 10002
        page.employeeIDComboBox.setSelectedItem("10002");

        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        Date july15 = fmt.parse("2024-07-15");
        page.JDateChooser.setDate(july15);
        page.JDateChooser.getDateEditor().setDate(july15);

        TableModel model = page.leaveTable.getModel();
        assertTrue(model.getRowCount() > 0, "Filtered table should not be empty");
        for (int i = 0; i < model.getRowCount(); ++i) {
            String empID = model.getValueAt(i, 1).toString();
            String leaveStart = model.getValueAt(i, 4).toString();
            String leaveEnd = model.getValueAt(i, 5).toString();

            assertEquals("10002", empID, "Row is for employee 10002");
            boolean isJuly = leaveStart.startsWith("2024-07-") || leaveEnd.startsWith("2024-07-");
            assertTrue(isJuly, 
                String.format("Leave Start or End is in July 2024: %s - %s", leaveStart, leaveEnd));
        }
    }

    @Test
    void testRefreshButtonResetsFilters() throws Exception {
        // Apply filters first
        page.employeeIDComboBox.setSelectedItem("10003");
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        Date july15 = fmt.parse("2024-07-15");
        page.JDateChooser.setDate(july15);

        // Now click refresh
        page.refreshButton.doClick();

        // Employee Combo resets to 'All' and date chooser resets
        assertEquals("All", page.employeeIDComboBox.getSelectedItem());
        assertNull(page.JDateChooser.getDate());

        // Table should show all leaves again, not just for 10003 or July
        TableModel model = page.leaveTable.getModel();
        assertTrue(model.getRowCount() > 0, "All leaves should show after refresh");
        boolean foundNot10003 = false;
        for (int i = 0; i < model.getRowCount(); ++i) {
            String empID = model.getValueAt(i, 1).toString();
            if (!"10003".equals(empID)) foundNot10003 = true;
        }
        assertTrue(foundNot10003, "At least one row not for 10003 after refresh");
    }

    @Test
    void testBackAndViewOwnRecordButtonsExist() {
        JButton backBtn = page.backButton;
        JButton viewOwnBtn = page.viewOwnRecordButton;
        assertNotNull(backBtn);
        assertNotNull(viewOwnBtn);

        // Labels
        assertEquals("Back", backBtn.getText());
        assertEquals("View Own Record", viewOwnBtn.getText());
    }

}
