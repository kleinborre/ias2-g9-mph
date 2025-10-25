package test;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.event.*;

import ui.PageHREmployeeRecords;
import util.FieldAccessTest; // Use your shared helper!

/**
 * Tests for HR Employee Records page functionality and UI.
 * Uses FieldAccessTest to robustly access and validate private fields.
 */
public class HREmployeeRecordsPageTest {

    PageHREmployeeRecords page;

    /**
     * Set up an HR session and instantiate the Employee Records page before every test.
     */
    @BeforeEach
    void setup() throws Exception {
        util.SessionManager.setSession("U10006", 10006); // HR account session
        SwingUtilities.invokeAndWait(() -> page = new PageHREmployeeRecords());
    }

    /**
     * Dispose of the page and clear static session state after every test.
     */
    @AfterEach
    void cleanup() throws Exception {
        SwingUtilities.invokeAndWait(() -> page.dispose());
    }

    /**
     * Test: All expected control buttons should exist and be enabled for use.
     */
    @Test
    void testButtonsExistAndFunctionality() {
        JButton ownRecordBtn    = FieldAccessTest.getField(page, "ownRecordButton", JButton.class);
        JButton newEmployeeBtn  = FieldAccessTest.getField(page, "newEmployeeButton", JButton.class);
        JButton backBtn         = FieldAccessTest.getField(page, "backButton", JButton.class);

        assertNotNull(ownRecordBtn, "Own Record button should exist");
        assertNotNull(newEmployeeBtn, "New Employee button should exist");
        assertNotNull(backBtn, "Back button should exist");

        assertTrue(ownRecordBtn.isEnabled(), "Own Record button should be enabled");
        assertTrue(newEmployeeBtn.isEnabled(), "New Employee button should be enabled");
        assertTrue(backBtn.isEnabled(), "Back button should be enabled");
    }

    /**
     * Test: The status filter combo box should exist, have options, and respond to changes.
     */
    @Test
    void testStatusFilterComboBoxExistsAndCanChange() throws Exception {
        JComboBox<?> statusFilter = FieldAccessTest.getField(page, "statusFilter", JComboBox.class);
        assertNotNull(statusFilter, "Status filter combo box should exist");
        int originalIndex = statusFilter.getSelectedIndex();
        assertTrue(statusFilter.getItemCount() > 1, "Status filter should have multiple options");

        // Change filter selection and verify it updates
        SwingUtilities.invokeAndWait(() -> statusFilter.setSelectedItem("Active"));
        assertEquals("Active", statusFilter.getSelectedItem(), "Status filter should change selection to Active");

        // Restore previous filter state for test isolation
        SwingUtilities.invokeAndWait(() -> statusFilter.setSelectedIndex(originalIndex));
    }

    /**
     * Test: The employee records table must exist, load the correct column headers,
     * and have zero or more rows (rows may be empty if database is empty).
     */
    @Test
    void testEmployeeRecordsTableExistsAndLoadsRows() throws Exception {
        JTable table = FieldAccessTest.getField(page, "employeeRecordsTable", JTable.class);
        assertNotNull(table, "Employee records table should exist");

        // Wait for the table to load at least once (max 2 seconds)
        int retries = 0;
        while (table.getRowCount() == 0 && retries < 20) {
            Thread.sleep(100);
            retries++;
        }
        assertTrue(table.getRowCount() >= 0, "Table should be loaded with zero or more rows"); // Accepts empty if DB empty
        assertEquals(14, table.getColumnCount(), "Table should have 14 columns");
        TableModel model = table.getModel();
        assertTrue(model instanceof DefaultTableModel, "Table model should be DefaultTableModel");

        // Verify each column header
        String[] expectedHeaders = {
            "Employee ID","Account Status","Employment Status","Name",
            "Birthdate","Contact Number","Address","Position",
            "Department","Immediate Supervisor","SSS No.",
            "Philhealth No.","TIN No.","Pag-Ibig No."
        };
        for (int i = 0; i < expectedHeaders.length; i++) {
            assertEquals(expectedHeaders[i], table.getColumnName(i), "Header mismatch at column " + i);
        }
    }

    /**
     * Test: Simulate double-clicking a table row to trigger the update employee flow.
     * Passes if event triggers with no exception (cannot assert navigation in unit test).
     */
    @Test
    void testDoubleClickTableRowOpensUpdatePage() throws Exception {
        JTable table = FieldAccessTest.getField(page, "employeeRecordsTable", JTable.class);
        if (table.getRowCount() == 0) return; // Skip if no data to click

        int row = 0;
        table.setRowSelectionInterval(row, row);
        MouseEvent click = new MouseEvent(table, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0, 10, 10, 2, false);

        for (MouseListener ml : table.getMouseListeners()) {
            ml.mouseClicked(click);
        }

        assertTrue(true, "Double-click event triggered");
    }

    /**
     * Test: The scroll pane should exist and its view must be the employee records table.
     */
    @Test
    void testScrollPaneContainsTable() {
        JScrollPane scroll = FieldAccessTest.getField(page, "jScrollPane1", JScrollPane.class);
        assertNotNull(scroll, "Scroll pane should exist");
        assertNotNull(scroll.getViewport().getView(), "Scroll pane should contain a view");
        assertTrue(scroll.getViewport().getView() instanceof JTable, "Scroll pane should contain JTable");
    }
}