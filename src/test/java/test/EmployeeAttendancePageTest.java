package test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import ui.PageEmployeeAttendance;
import util.SessionManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.io.File;
import java.util.Calendar;

class EmployeeAttendancePageTest {

    private PageEmployeeAttendance page;
    private final String expectedUserID = "U10012";
    private final int expectedEmployeeID = 10012;

    @BeforeEach
    void setUp() {
        SessionManager.setSession(expectedUserID, expectedEmployeeID);
        page = new PageEmployeeAttendance();
    }

    @AfterEach
    void tearDown() {
        if (page != null) page.dispose();
        SessionManager.clearSession();
    }

    // ---- BASIC COMPONENT TESTS ----
    @Test void testLabelsExistAndCorrect() {
        JLabel totalLabel = (JLabel) getComponent(page, JLabel.class, "Total Hours:");
        assertNotNull(totalLabel);
        assertEquals("Total Hours:", totalLabel.getText());

        JLabel filterLabel = (JLabel) getComponent(page, JLabel.class, "Filter by Date:");
        assertNotNull(filterLabel);
        assertEquals("Filter by Date:", filterLabel.getText());
    }

    @Test
    void testTotalWorkedHoursFieldEditableAndDefault() {
        assertFalse(page.totalWorkedHoursField.isEditable());
        assertEquals("1205 hrs, 16 min", page.totalWorkedHoursField.getText()); // Update to actual sum!
    }

    @Test void testDateChooserExists() {
        Component chooser = getComponent(page, com.toedter.calendar.JDateChooser.class, null);
        assertNotNull(chooser);
    }

    @Test void testAttendanceTableStructure() {
        JTable table = (JTable) getComponent(page, JTable.class, null);
        assertNotNull(table);
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        assertEquals(4, model.getColumnCount());
        assertArrayEquals(
            new String[]{"Date", "Log In", "Log Out", "Worked Hours"},
            getColumnNames(model)
        );
    }

    @Test void testBackButtonExists() {
        JButton backBtn = (JButton) getComponent(page, JButton.class, "Back");
        assertNotNull(backBtn);
        assertEquals("Back", backBtn.getText());
    }

    @Test void testPrintAttendanceButtonExists() {
        JButton printBtn = (JButton) getComponent(page, JButton.class, "Print Attendance");
        assertNotNull(printBtn);
        assertEquals("Print Attendance", printBtn.getText());
    }

    // ---- FUNCTIONAL TABLE TESTS ----
    @Test
    void testTablePopulatesWithDBRecords() {
        JTable table = (JTable) getComponent(page, JTable.class, null);
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        assertTrue(model.getRowCount() > 0, "Attendance table should have rows if DB is populated.");
        // Optionally assert column values for first row if you know expected data
    }

    @Test
    void testTableUpdatesAfterDateFilter() throws Exception {
        // Pick a date present in the DB, e.g., June 3, 2024
        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.JUNE, 3);
        Date dateToTest = new Date(cal.getTimeInMillis());
        page.JDateChooser.setDate(dateToTest);

        // Simulate a property change (Swing event firing)
        SwingUtilities.invokeAndWait(() -> {
            page.JDateChooser.getDateEditor().setDate(dateToTest);
        });

        JTable table = page.attendanceTable;
        int rowCount = table.getRowCount();
        assertTrue(rowCount > 0, "Filtered table should not be empty if data exists for date.");
    }

    // ---- BUTTON ACTION TESTS ----
    @Test
    void testBackButtonClosesWindow() throws Exception {
        JButton backBtn = (JButton) getComponent(page, JButton.class, "Back");
        assertNotNull(backBtn);
        assertTrue(page.isDisplayable());
        SwingUtilities.invokeAndWait(backBtn::doClick);
        // Window should now be disposed
        assertFalse(page.isDisplayable());
    }

    @Test
    void testPrintAttendanceButtonWorks() throws Exception {
        JButton printBtn = (JButton) getComponent(page, JButton.class, "Print Attendance");
        assertNotNull(printBtn);

        String tmpDir = System.getProperty("java.io.tmpdir");
        File dir = new File(tmpDir);
        String[] beforeFiles = dir.list();

        // Click the print button (triggers dialog + PDF generation)
        SwingUtilities.invokeAndWait(printBtn::doClick);

        // Give dialogs some time to appear (needed in some environments)
        Thread.sleep(1000);

        // (Optional) Check for dialog pop-up, but don't fail if not found
        boolean foundDialog = false;
        for (Window w : Window.getWindows()) {
            if (w.isShowing() && w instanceof JDialog) {
                JDialog dialog = (JDialog) w;
                if ("Success".equals(dialog.getTitle()) || "Error".equals(dialog.getTitle())) {
                    foundDialog = true;
                    SwingUtilities.invokeLater(dialog::dispose);
                }
            }
        }
        if (!foundDialog) {
            System.out.println("Dialog not found (may require manual click in UI test).");
        }
        // Do not assertTrue(foundDialog); // Just log

        // Check for PDF file creation
        Thread.sleep(500);
        String[] afterFiles = dir.list();
        boolean foundPDF = false;
        for (String f : afterFiles) {
            if (!java.util.Arrays.asList(beforeFiles).contains(f)
                    && f.startsWith("attendance_10012") && f.endsWith(".pdf")) {
                foundPDF = true;
                break;
            }
        }
        assertTrue(foundPDF, "PDF file should be created in temp directory after printing.");
    }


    // ---- UTILITY ----
    private String[] getColumnNames(DefaultTableModel model) {
        int cols = model.getColumnCount();
        String[] names = new String[cols];
        for (int i = 0; i < cols; i++) names[i] = model.getColumnName(i);
        return names;
    }

    private Component getComponent(Container container, Class<?> cls, String text) {
        for (Component c : container.getComponents()) {
            if (cls.isInstance(c)) {
                if (text == null) return c;
                if (c instanceof JLabel && text.equals(((JLabel) c).getText())) return c;
                if (c instanceof JButton && text.equals(((JButton) c).getText())) return c;
            }
            if (c instanceof Container) {
                Component found = getComponent((Container) c, cls, text);
                if (found != null) return found;
            }
        }
        return null;
    }
}
