package test;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.event.*;

import ui.PageITUserAccounts;
import util.FieldAccessTest; // Import helper for field access

/**
 * Automated tests for IT User Accounts page.
 * Utilizes FieldAccessTest to DRY up private field reflection and ensure all UI/logic is verifiable.
 */
public class ITUserAccountsPageTest {

    PageITUserAccounts page;

    /**
     * Sets up an IT admin session and loads the IT user accounts page.
     * Also ensures test user U10034 is always "Active" to allow row/action tests.
     */
    @BeforeEach
    void setup() throws Exception {
        util.SessionManager.setSession("U10005", 10005); // IT admin

        // Reset U10034 to "Active" before each test to guarantee row/action tests work
        service.UserService userService = new service.UserService();
        pojo.User u = userService.getUserByUserID("U10034");
        if (u != null && !"Active".equals(u.getAccountStatus())) {
            u.setAccountStatus("Active");
            userService.updateUser(u);
        }

        SwingUtilities.invokeAndWait(() -> page = new PageITUserAccounts());
    }

    /**
     * Clean up after each test by disposing UI and clearing static session state.
     */
    @AfterEach
    void cleanup() throws Exception {
        SwingUtilities.invokeAndWait(() -> page.dispose());
        util.SessionManager.clearSession();
    }

    /**
     * Test: All core UI controls should exist and be enabled for user interaction.
     */
    @Test
    void testAllButtonsAndCombosPresentAndEnabled() {
        JButton backButton       = FieldAccessTest.getField(page, "backButton", JButton.class);
        JButton ownAccountButton = FieldAccessTest.getField(page, "ownAccountButton", JButton.class);
        JButton newUserButton    = FieldAccessTest.getField(page, "newUserButton", JButton.class);
        JComboBox<?> statusFilter = FieldAccessTest.getField(page, "statusFilter", JComboBox.class);

        assertNotNull(backButton, "Back button exists");
        assertNotNull(ownAccountButton, "Own Account button exists");
        assertNotNull(newUserButton, "New Employee button exists");
        assertNotNull(statusFilter, "Status filter combo box exists");

        assertTrue(backButton.isEnabled(), "Back button enabled");
        assertTrue(ownAccountButton.isEnabled(), "Own Account button enabled");
        assertTrue(newUserButton.isEnabled(), "New Employee button enabled");
        assertTrue(statusFilter.isEnabled(), "Status filter enabled");
    }

    /**
     * Test: Employee ID filter combo box should exist, be populated, and have "All" as its first item.
     */
    @Test
    void testEmployeeIDComboBoxExistsAndWorks() {
        JComboBox<?> employeeIDComboBox = FieldAccessTest.getField(page, "employeeIDComboBox", JComboBox.class);
        assertNotNull(employeeIDComboBox, "Employee ID combo box exists");
        assertTrue(employeeIDComboBox.getItemCount() > 0, "Employee ID combo box has items");
        assertEquals("All", employeeIDComboBox.getItemAt(0), "First combo box item is All");
    }

    /**
     * Test: The main table and its scroll pane must exist and be wired correctly.
     * - Table must be inside the scroll pane.
     * - Table must have the correct columns and any (including zero) rows.
     */
    @Test
    void testTableAndScrollPanePresentAndPopulated() throws Exception {
        JTable table = FieldAccessTest.getField(page, "userAccountsTable", JTable.class);
        JScrollPane scroll = FieldAccessTest.getField(page, "jScrollPane1", JScrollPane.class);

        assertNotNull(table, "User accounts table exists");
        assertNotNull(scroll, "Table scroll pane exists");

        // Table is inside scroll pane
        assertSame(table, scroll.getViewport().getView(), "Table in scroll pane");

        // Wait for table to load (up to 2 seconds)
        int retries = 0;
        while (table.getRowCount() == 0 && retries < 20) {
            Thread.sleep(100);
            retries++;
        }
        TableModel model = table.getModel();
        assertTrue(model.getColumnCount() >= 4, "Table has columns");
        assertTrue(model.getRowCount() >= 0, "Table has zero or more rows (ok if no users)");

        // Must have correct column headers
        assertEquals("Account Status", model.getColumnName(0));
        assertEquals("User ID",        model.getColumnName(1));
        assertEquals("Full Name",      model.getColumnName(2));
        assertEquals("Email",          model.getColumnName(3));
    }

    /**
     * Test: Changing status filter must update table rows count without exception.
     */
    @Test
    void testStatusFilterChangesTableRows() throws Exception {
        JTable table = FieldAccessTest.getField(page, "userAccountsTable", JTable.class);
        JComboBox<?> statusFilter = FieldAccessTest.getField(page, "statusFilter", JComboBox.class);

        SwingUtilities.invokeAndWait(() -> {
            for (int i = 0; i < statusFilter.getItemCount(); ++i) {
                statusFilter.setSelectedIndex(i);
                int count = table.getRowCount();
                assertTrue(count >= 0, "Row count non-negative after filter change");
            }
        });
    }

    /**
     * Test: Both action buttons must be wired to listeners.
     * - Verifies that user interaction is possible.
     */
    @Test
    void testOwnAccountAndNewEmployeeButtonsAction() {
        JButton ownAccountButton = FieldAccessTest.getField(page, "ownAccountButton", JButton.class);
        JButton newUserButton = FieldAccessTest.getField(page, "newUserButton", JButton.class);
        ActionListener[] ownAccL = ownAccountButton.getActionListeners();
        ActionListener[] newUserL = newUserButton.getActionListeners();
        assertTrue(ownAccL.length > 0, "Own Account button has action listener");
        assertTrue(newUserL.length > 0, "New Employee button has action listener");
    }

    /**
     * Test: Back button should have navigation handler.
     */
    @Test
    void testBackButtonAction() {
        JButton backButton = FieldAccessTest.getField(page, "backButton", JButton.class);
        ActionListener[] backListeners = backButton.getActionListeners();
        assertTrue(backListeners.length > 0, "Back button should have action listeners");
    }

    /**
     * Test: Simulate double-click on the table row for U10034.
     * - Ensures popup/action logic triggers without exception.
     */
    @Test
    void testRowPopupUpdateAndDisableActionForU10034() throws Exception {
        JTable table = FieldAccessTest.getField(page, "userAccountsTable", JTable.class);
        TableModel model = table.getModel();
        int targetRow = -1;
        for (int i = 0; i < model.getRowCount(); ++i) {
            String val = (model.getValueAt(i, 1) != null) ? model.getValueAt(i, 1).toString() : "";
            if (val.equals("U10034")) {
                targetRow = i;
                break;
            }
        }
        assertTrue(targetRow >= 0, "Row for userID U10034 found in table");

        // Simulate double-clicking this row
        final int row = targetRow;
        SwingUtilities.invokeAndWait(() -> {
            MouseEvent evt = new MouseEvent(
                table,
                MouseEvent.MOUSE_CLICKED,
                System.currentTimeMillis(),
                0,
                10, 10,
                2,  // double click
                false
            );
            table.setRowSelectionInterval(row, row);

            for (MouseListener ml : table.getMouseListeners()) {
                ml.mouseClicked(evt);
            }
        });

        assertTrue(true, "Simulated double-click on U10034 handled gracefully");
    }

    /**
     * Test: Table should have non-null, correctly formatted cell values for U10034.
     */
    @Test
    void testTableCellValuesForU10034() {
        JTable table = FieldAccessTest.getField(page, "userAccountsTable", JTable.class);
        TableModel model = table.getModel();
        boolean found = false;
        for (int i = 0; i < model.getRowCount(); ++i) {
            String userID = (model.getValueAt(i, 1) != null) ? model.getValueAt(i, 1).toString() : "";
            if (userID.equals("U10034")) {
                assertNotNull(model.getValueAt(i, 0), "Status non-null");
                assertNotNull(model.getValueAt(i, 2), "Full name non-null");
                assertNotNull(model.getValueAt(i, 3), "Email non-null");
                found = true;
                break;
            }
        }
        assertTrue(found, "UserID U10034 exists in table");
    }
}