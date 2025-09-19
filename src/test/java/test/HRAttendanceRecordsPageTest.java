package test;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import ui.PageHRAttendanceRecords;
import util.SessionManager;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HRAttendanceRecordsPageTest {

    private PageHRAttendanceRecords page;

    @BeforeEach
    void setup() throws Exception {
        // Set session as HR (U10006)
        SessionManager.setSession("U10006", 10006);
        SwingUtilities.invokeAndWait(() -> page = new PageHRAttendanceRecords());
    }

    @AfterEach
    void cleanup() throws Exception {
        SwingUtilities.invokeAndWait(() -> page.dispose());
        SessionManager.clearSession();
    }

    @Test
    void testAllButtonsAndComponentsPresent() {
        assertNotNull(page.backButton, "Back button exists");
        assertNotNull(page.viewOwnRecordButton, "View Own Record button exists");
        assertNotNull(page.printAttendanceButton, "Print Attendance button exists");
        assertNotNull(page.JDateChooser, "JDateChooser exists");
        assertNotNull(page.employeeIDComboBox, "EmployeeID ComboBox exists");
        assertNotNull(page.attendanceTable, "Attendance table exists");
        assertNotNull(page.jScrollPane1, "Table scroll pane exists");

        assertTrue(page.backButton.isEnabled(), "Back button enabled");
        assertTrue(page.viewOwnRecordButton.isEnabled(), "View Own Record enabled");
        assertTrue(page.printAttendanceButton.isEnabled(), "Print Attendance enabled");
        assertTrue(page.employeeIDComboBox.isEnabled(), "ComboBox enabled");
        assertTrue(page.attendanceTable.isEnabled(), "Table enabled");
        assertTrue(page.jScrollPane1.isEnabled(), "ScrollPane enabled");
    }

    @Test
    void testTableAndScrollPaneSetup_Default_AllRecords() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            // Default: no date and blank combo box -> all records
            TableModel model = page.attendanceTable.getModel();
            assertTrue(model.getColumnCount() >= 5, "Table has at least 5 columns");
            assertTrue(model.getRowCount() > 0, "Table has at least one row (all records)");
            assertEquals("Employee ID", model.getColumnName(0));
            assertEquals("Date", model.getColumnName(1));
            assertEquals("Log In", model.getColumnName(2));
            assertEquals("Log Out", model.getColumnName(3));
            assertEquals("Worked Hours", model.getColumnName(4));
        });
    }

    @Test
    void testComboBox_FilterByID_10001() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            // Select "10001" in combo box
            JComboBox<String> combo = page.employeeIDComboBox;
            boolean found = false;
            for (int i = 0; i < combo.getItemCount(); ++i) {
                if ("10001".equals(combo.getItemAt(i))) {
                    combo.setSelectedIndex(i);
                    found = true;
                    break;
                }
            }
            assertTrue(found, "ComboBox contains 10001");
            TableModel model = page.attendanceTable.getModel();
            assertTrue(model.getRowCount() > 0, "Table filtered by 10001 should have rows");
            for (int i = 0; i < model.getRowCount(); ++i) {
                assertEquals("10001", model.getValueAt(i, 0).toString(), "Each row has employeeID 10001");
            }
        });
    }

    @Test
    void testDateChooser_FilterByJuly3_2024() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
                Date target = fmt.parse("2024-07-03");
                page.JDateChooser.setDate(target);
                // Fire property change manually if needed
                page.JDateChooser.getDateEditor().setDate(target);
                TableModel model = page.attendanceTable.getModel();
                assertTrue(model.getRowCount() > 0, "Table filtered by July 2024 should have rows");
                for (int i = 0; i < model.getRowCount(); ++i) {
                    String dateStr = model.getValueAt(i, 1).toString();
                    // Accept any date in July 2024
                    assertTrue(dateStr.startsWith("2024-07-"), "Each row is in July 2024: got " + dateStr);
                }
            } catch (Exception e) {
                fail("Date parse or filter failed: " + e.getMessage());
            }
        });
    }

    @Test
    void testPrintAttendancePDFGeneration() throws Exception {
        // This will trigger PDF export (file will be created and should not error out)
        SwingUtilities.invokeAndWait(() -> {
            page.printAttendanceButton.doClick();
        });
        // No assertion on file, but no exception = pass
    }

    @Test
    void testViewOwnRecordButtonActionListenerPresent() {
        ActionListener[] listeners = page.viewOwnRecordButton.getActionListeners();
        assertTrue(listeners.length > 0, "View Own Record button has action listener");
    }

    @Test
    void testBackButtonActionListenerPresent() {
        ActionListener[] listeners = page.backButton.getActionListeners();
        assertTrue(listeners.length > 0, "Back button has action listener");
    }
}
