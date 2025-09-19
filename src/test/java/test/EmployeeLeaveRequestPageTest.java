package test;

import org.junit.jupiter.api.*;
import ui.PageEmployeeLeave;
import util.SessionManager;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EmployeeLeaveRequestPageTest {

    PageEmployeeLeave page;

    @BeforeAll
    void setupSession() {
        // Simulate employee session
        SessionManager.setSession("U10012", 10012);
    }

    @BeforeEach
    void setupPage() {
        // Always instantiate a new UI for each test
        page = new PageEmployeeLeave();
    }

    @AfterAll
    void clearSession() {
        SessionManager.clearSession();
    }

    @Test
    void testDefaultTableLoaded() {
        // Table should show all leave records for employee 10012 (without filter)
        JTable table = page.leaveTable;
        assertNotNull(table, "Leave table should exist");
        TableModel model = table.getModel();
        assertTrue(model.getRowCount() >= 1, "At least one record should be present by default");

        // Check required columns
        assertEquals("Status",   model.getColumnName(0));
        assertEquals("Type",     model.getColumnName(1));
        assertEquals("Start",    model.getColumnName(2));
        assertEquals("End",      model.getColumnName(3));
        assertEquals("Reason",   model.getColumnName(4));
        assertEquals("Date Filed", model.getColumnName(5));

        // Sample check: verify at least one pending or approved leave
        boolean foundExpected = false;
        for (int i = 0; i < model.getRowCount(); i++) {
            String status = (String) model.getValueAt(i, 0);
            if ("Pending".equalsIgnoreCase(status) || "Approved".equalsIgnoreCase(status)) {
                foundExpected = true;
            }
        }
        assertTrue(foundExpected, "There should be at least one pending/approved leave in default view");
    }

    @Test
    void testDateChooser_FilterByJuly6_2024() throws ParseException {
        // Set JDateChooser to July 6, 2024
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        Date filterDate = fmt.parse("2024-07-06");
        page.JDateChooser.setDate(filterDate);
        page.JDateChooser.getDateEditor().setDate(filterDate);

        JTable table = page.leaveTable;
        TableModel model = table.getModel();
        assertTrue(model.getRowCount() >= 1, "There should be at least one record for filter July 6, 2024");

        // Check that every row shown has either Start, End, or Date Filed matching July 6, 2024
        SimpleDateFormat uiFmt = new SimpleDateFormat("MMM d, yyyy");
        String expectedDateStr = uiFmt.format(filterDate);

        for (int i = 0; i < model.getRowCount(); i++) {
            String start  = (String) model.getValueAt(i, 2);
            String end    = (String) model.getValueAt(i, 3);
            String filed  = (String) model.getValueAt(i, 5);

            boolean matched = start.equals(expectedDateStr)
                           || end.equals(expectedDateStr)
                           || filed.equals(expectedDateStr);
            assertTrue(matched, String.format(
                    "Row %d must match July 6, 2024 in Start/End/Date Filed, got: start=%s end=%s filed=%s",
                    i, start, end, filed
            ));
        }
    }

    @Test
    void testCreateRequestButton() {
        JButton btn = page.leaveRequestButton;
        assertNotNull(btn, "Create Request button should exist");
        assertEquals("Create Request", btn.getText());

        // Simulate click and verify it launches the new request page (not covering the UI nav here)
        // You can add more specific navigation asserts if desired
        btn.doClick();
        // Optional: check visibility change or page transition here
    }

    @Test
    void testBackButton() {
        JButton btn = page.backButton;
        assertNotNull(btn, "Back button should exist");
        assertEquals("Back", btn.getText());

        // Simulate click (actual navigation is handled in UI)
        btn.doClick();
        // Optional: check visibility/state
    }

    @Test
    void testDateChooserComponent() {
        assertNotNull(page.JDateChooser, "JDateChooser should be present");
    }

    @Test
    void testJScrollPanePresent() {
        // Check that the scroll pane is present and holds the table
        JScrollPane sp = page.jScrollPane1;
        assertNotNull(sp, "JScrollPane should exist");
        assertEquals(page.leaveTable, sp.getViewport().getView());
    }
}
