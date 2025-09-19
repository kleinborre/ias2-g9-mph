package ui.base;

import util.LightButton;
import util.SessionManager;
import service.EmployeeService;
import service.UserService;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * AbstractEmployeeRecordsPage:
 * Shared logic for all Employee Records pages, used by HR and Admin.
 * Features:
 * - Filtering by status or employee ID
 * - Real-time updates on add/delete
 * - Safe action dialogs for Update, Deactivate, or Delete
 * - DRY, modular, and keeps all existing integrations
 */
public abstract class AbstractEmployeeRecordsPage extends JFrame {
    private final EmployeeService employeeService = new EmployeeService();
    private final UserService userService = new UserService();

    /**
     * One-time call in constructor. Wires up:
     * - Table model and formatting
     * - EmployeeID combobox and status filter
     * - Button actions
     * - Table double-click for record actions
     */
    protected void setupRecordsPage(
            JTable table,
            JComboBox<?> statusFilter,
            JComboBox<String> employeeIDComboBox,
            LightButton ownRecordButton,
            LightButton newEmployeeButton,
            LightButton backButton,
            Runnable onOwnRecord,
            Runnable onNewEmployee,
            Runnable onBack
    ) {
        // === TABLE INITIALIZATION ===
        String[] cols = {
                "Employee ID", "Account Status", "Employment Status", "Name",
                "Birthdate", "Contact Number", "Address", "Position",
                "Department", "Immediate Supervisor", "SSS No.",
                "Philhealth No.", "TIN No.", "Pag-Ibig No."
        };
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table.setModel(model);
        table.createDefaultColumnsFromModel();

        // Always show table header
        Container vp = table.getParent();
        if (vp instanceof JViewport) {
            Container sp = vp.getParent();
            if (sp instanceof JScrollPane) {
                ((JScrollPane)sp).setColumnHeaderView(table.getTableHeader());
            }
        }
        // Columns scroll horizontally
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        // Center all cells and headers
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        table.setDefaultRenderer(Object.class, center);
        JTableHeader hdr = table.getTableHeader();
        ((DefaultTableCellRenderer)hdr.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);

        // === EMPLOYEEID FILTER ===
        // Fill employeeIDComboBox with "All" and all employee IDs
        employeeIDComboBox.removeAllItems();
        employeeIDComboBox.addItem("All");
        for (var emp : employeeService.getAllEmployees()) {
            employeeIDComboBox.addItem(String.valueOf(emp.getEmployeeID()));
        }
        // Helper for repopulating after add/delete
        Runnable reloadEmployeeIDComboBox = () -> {
            employeeIDComboBox.removeAllItems();
            employeeIDComboBox.addItem("All");
            for (var emp : employeeService.getAllEmployees()) {
                employeeIDComboBox.addItem(String.valueOf(emp.getEmployeeID()));
            }
        };

        // === FILTERING LOGIC ===
        // a) Filter by EmployeeID. When "All", unlock status filter and show all
        employeeIDComboBox.addActionListener(e -> {
            String empSel = (String)employeeIDComboBox.getSelectedItem();
            boolean isAll = empSel == null || empSel.equals("All");
            statusFilter.setEnabled(isAll); // Only filter status when "All"
            if (isAll) {
                reloadTableAsync(table, String.valueOf(statusFilter.getSelectedItem()));
            } else {
                reloadTableForEmployee(table, Integer.parseInt(empSel));
            }
        });

        // b) StatusFilter: Only responds if enabled (i.e., employeeID is "All")
        statusFilter.addActionListener(e -> {
            if (statusFilter.isEnabled()) {
                reloadTableAsync(table, String.valueOf(statusFilter.getSelectedItem()));
            }
        });

        // === BUTTON ACTIONS ===
        ownRecordButton  .addActionListener(e -> onOwnRecord.run());
        newEmployeeButton.addActionListener(e -> onNewEmployee.run());
        backButton       .addActionListener(e -> onBack.run());

        // === ALL RELOAD ===
        // Reloads comboBox and table, used after add/delete
        Runnable reloadAll = () -> {
            reloadEmployeeIDComboBox.run();
            String empSel = (String)employeeIDComboBox.getSelectedItem();
            boolean isAll = empSel == null || empSel.equals("All");
            if (isAll) reloadTableAsync(table, String.valueOf(statusFilter.getSelectedItem()));
            else reloadTableForEmployee(table, Integer.parseInt(empSel));
        };

        // === INITIAL TABLE LOAD ===
        reloadTableAsync(table, String.valueOf(statusFilter.getSelectedItem()));

        // === TABLE DOUBLE-CLICK ACTIONS ===
        // Shows context-sensitive dialog for each accountStatus
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() != -1) {
                    int row = table.getSelectedRow();
                    int empID = (Integer)table.getModel().getValueAt(row, 0);
                    String accountStatus = String.valueOf(table.getModel().getValueAt(row, 1));
                    String userID = employeeService.getEmployeeByID(empID).getUserID();

                    // PENDING: "Update" or "Delete"
                    if (accountStatus.equalsIgnoreCase("Pending")) {
                        Object[] opts = {"Update", "Delete"};
                        int choice = JOptionPane.showOptionDialog(
                                AbstractEmployeeRecordsPage.this,
                                "Update info or delete employee?",
                                "Pending Employee",
                                JOptionPane.DEFAULT_OPTION,
                                JOptionPane.QUESTION_MESSAGE,
                                null, opts, opts[0]
                        );
                        if (choice == 0) { // Update
                            SessionManager.setSelectedEmployeeID(empID);
                            new ui.PageHREmployeeUpdate().setVisible(true);
                            dispose();
                        } else if (choice == 1) { // Delete
                            int confirm = JOptionPane.showOptionDialog(
                                    AbstractEmployeeRecordsPage.this,
                                    "Are you sure? This cannot be undone.",
                                    "Delete Employee",
                                    JOptionPane.DEFAULT_OPTION,
                                    JOptionPane.WARNING_MESSAGE,
                                    null, new String[]{"Confirm", "Cancel"}, "Cancel"
                            );
                            if (confirm == 0) {
                                employeeService.deleteEmployee(empID);
                                userService.deleteUser(userID);
                                reloadAll.run();
                                JOptionPane.showMessageDialog(AbstractEmployeeRecordsPage.this,
                                        "Employee " + empID + " was deleted.");
                            }
                        }
                    }
                    // ACTIVE: "Update" or "Deactivate"
                    else if (accountStatus.equalsIgnoreCase("Active")) {
                        Object[] opts = {"Update", "Deactivate"};
                        int choice = JOptionPane.showOptionDialog(
                                AbstractEmployeeRecordsPage.this,
                                "Update info or deactivate employee?",
                                "Active Employee",
                                JOptionPane.DEFAULT_OPTION,
                                JOptionPane.QUESTION_MESSAGE,
                                null, opts, opts[0]
                        );
                        if (choice == 0) { // Update
                            SessionManager.setSelectedEmployeeID(empID);
                            new ui.PageHREmployeeUpdate().setVisible(true);
                            dispose();
                        } else if (choice == 1) { // Deactivate
                            int confirm = JOptionPane.showOptionDialog(
                                    AbstractEmployeeRecordsPage.this,
                                    "Are you sure? This cannot be undone.",
                                    "Deactivate Employee",
                                    JOptionPane.DEFAULT_OPTION,
                                    JOptionPane.WARNING_MESSAGE,
                                    null, new String[]{"Confirm", "Cancel"}, "Cancel"
                            );
                            if (confirm == 0) {
                                pojo.User user = userService.getUserByUserID(userID);
                                user.setAccountStatus("Deactivated");
                                userService.updateUser(user);
                                reloadAll.run();
                                JOptionPane.showMessageDialog(AbstractEmployeeRecordsPage.this,
                                        "Employee " + empID + " was deactivated.");
                            }
                        }
                    }
                    // DEACTIVATED: Show info
                    else if (accountStatus.equalsIgnoreCase("Deactivated")) {
                        JOptionPane.showMessageDialog(AbstractEmployeeRecordsPage.this,
                                "Account is deactivated.\nContact IT for reactivation.",
                                "Deactivated",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                    }
                    // REJECTED: "Delete" or "Cancel"
                    else if (accountStatus.equalsIgnoreCase("Rejected")) {
                        Object[] opts = {"Delete", "Cancel"};
                        int choice = JOptionPane.showOptionDialog(
                                AbstractEmployeeRecordsPage.this,
                                "This account was rejected. Delete this record?",
                                "Rejected Employee",
                                JOptionPane.DEFAULT_OPTION,
                                JOptionPane.WARNING_MESSAGE,
                                null, opts, opts[1]
                        );
                        if (choice == 0) {
                            employeeService.deleteEmployee(empID);
                            userService.deleteUser(userID);
                            reloadAll.run();
                            JOptionPane.showMessageDialog(AbstractEmployeeRecordsPage.this,
                                    "Employee " + empID + " was deleted.");
                        }
                    }
                }
            }
        });
        // === END OF SETUP ===
    }

    // Reloads table for current status filter ("All", "Active", etc.)
    // Called for normal filter operations
    private void reloadTableAsync(JTable table, String filterStatus) {
        new SwingWorker<List<Object[]>, Void>() {
            @Override protected List<Object[]> doInBackground() {
                return employeeService.getAllEmployeeRecords(filterStatus);
            }
            @Override protected void done() {
                try {
                    var rows = get();
                    var m = (DefaultTableModel) table.getModel();
                    m.setRowCount(0);
                    for (var row : rows) m.addRow(row);
                    adjustColumnWidths(table);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    // Reloads table for a single employee (by ID)
    // Used when filtering by employeeID
    private void reloadTableForEmployee(JTable table, int employeeID) {
        new SwingWorker<List<Object[]>, Void>() {
            @Override protected List<Object[]> doInBackground() {
                pojo.Employee emp = employeeService.getEmployeeByID(employeeID);
                if (emp == null) return java.util.Collections.emptyList();
                List<Object[]> rows = employeeService.getAllEmployeeRecords("All");
                for (Object[] row : rows) {
                    if (row[0] instanceof Integer && ((Integer) row[0]) == employeeID)
                        return java.util.Collections.singletonList(row);
                }
                return java.util.Collections.emptyList();
            }
            @Override protected void done() {
                try {
                    var rows = get();
                    var m = (DefaultTableModel) table.getModel();
                    m.setRowCount(0);
                    for (var row : rows) m.addRow(row);
                    adjustColumnWidths(table);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    // Utility: auto-resize columns to fit contents
    private void adjustColumnWidths(JTable table) {
        TableColumnModel cm = table.getColumnModel();
        JTableHeader hdr = table.getTableHeader();
        TableCellRenderer hdrR = hdr.getDefaultRenderer();
        for (int col = 0; col < table.getColumnCount(); col++) {
            int maxW = 50;
            Component hc = hdrR.getTableCellRendererComponent(
                    table, cm.getColumn(col).getHeaderValue(), false, false, -1, col
            );
            maxW = Math.max(maxW, hc.getPreferredSize().width);
            for (int row = 0; row < table.getRowCount(); row++) {
                TableCellRenderer cr = table.getCellRenderer(row, col);
                Component c = table.prepareRenderer(cr, row, col);
                maxW = Math.max(maxW, c.getPreferredSize().width);
            }
            cm.getColumn(col).setPreferredWidth(maxW + 10);
        }
    }
}