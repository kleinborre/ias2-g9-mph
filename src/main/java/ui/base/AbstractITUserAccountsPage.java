package ui.base;

import pojo.User;
import pojo.Employee;
import service.UserService;
import service.EmployeeService;
import util.SessionManager;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AbstractITUserAccountsPage:
 * - Shared logic for IT's User Accounts Management page.
 * - Features real-time filtering by Employee ID and account status.
 * - Reusable with minimal code in PageITUserAccounts.
 */
public abstract class AbstractITUserAccountsPage extends JFrame {

    protected JTable userAccountsTable;
    protected JComboBox<String> statusFilter;
    protected JComboBox<String> employeeIDComboBox;
    protected JButton ownAccountButton;
    protected JButton newUserButton;
    protected JButton backButton;

    protected UserService userService = new UserService();
    protected EmployeeService employeeService = new EmployeeService();

    private final String[] columnNames = {
            "Account Status", "User ID", "Full Name", "Email"
    };

    /**
     * Main setup for the IT User Accounts Page.
     * - Wires up table, status filter, employee ID filter, and navigation buttons.
     * - DRY and maintains all previous functionality.
     */
    protected void setupUserAccountsPage(
            JTable userAccountsTable,
            JComboBox<String> statusFilter,
            JComboBox<String> employeeIDComboBox,
            JButton ownAccountButton,
            JButton newUserButton,
            JButton backButton
    ) {
        this.userAccountsTable = userAccountsTable;
        this.statusFilter = statusFilter;
        this.employeeIDComboBox = employeeIDComboBox;
        this.ownAccountButton = ownAccountButton;
        this.newUserButton = newUserButton;
        this.backButton = backButton;

        // --- EMPLOYEEID FILTER SETUP ---
        // Populate the employeeIDComboBox with "All" and all employeeIDs.
        reloadEmployeeIDComboBox();

        // Add listeners for filter logic
        employeeIDComboBox.addActionListener(e -> {
            String selected = (String) employeeIDComboBox.getSelectedItem();
            boolean isAll = selected == null || selected.equals("All");
            statusFilter.setEnabled(isAll);
            if (isAll) {
                populateUserAccountsTable(getCurrentStatusFilter());
            } else {
                int employeeID = Integer.parseInt(selected);
                populateUserAccountsTableForEmployee(employeeID);
            }
        });

        // When statusFilter changes, reload table (only if enabled)
        statusFilter.addActionListener(e -> {
            if (statusFilter.isEnabled()) {
                populateUserAccountsTable(getCurrentStatusFilter());
            }
        });

        // Table actions
        setupRowDoubleClickListener();

        // Button navigation
        ownAccountButton.addActionListener(e -> {
            new ui.PageITEmployeeData().setVisible(true);
            this.dispose();
        });

        newUserButton.addActionListener(e -> {
            new ui.PageITEmployeeRegister().setVisible(true);
            this.dispose();
        });

        backButton.addActionListener(e -> {
            new ui.PageITHome().setVisible(true);
            this.dispose();
        });

        // Initial load: Show all users
        populateUserAccountsTable(getCurrentStatusFilter());
    }

    /**
     * Fills employeeIDComboBox with "All" plus all employee IDs.
     */
    protected void reloadEmployeeIDComboBox() {
        employeeIDComboBox.removeAllItems();
        employeeIDComboBox.addItem("All");
        for (Employee emp : employeeService.getAllEmployees()) {
            employeeIDComboBox.addItem(String.valueOf(emp.getEmployeeID()));
        }
    }

    /**
     * Returns the current status filter, or "All" if not set.
     */
    protected String getCurrentStatusFilter() {
        if (statusFilter == null) return "All";
        Object selected = statusFilter.getSelectedItem();
        return selected == null ? "All" : selected.toString();
    }

    /**
     * Populates the table with all users filtered by status.
     */
    protected void populateUserAccountsTable(String filter) {
        List<User> users = userService.getAllUsers();
        List<User> filtered = users.stream()
            .filter(u -> filter.equals("All") || u.getAccountStatus().equalsIgnoreCase(filter))
            .sorted(Comparator.comparing(User::getUserID))
            .collect(Collectors.toList());

        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        for (User u : filtered) {
            Employee emp = employeeService.getEmployeeByUserID(u.getUserID());
            String fullName = (emp != null) ? emp.getLastName() + ", " + emp.getFirstName() : "";
            String email = (emp != null) ? emp.getEmail() : "";
            model.addRow(new Object[]{u.getAccountStatus(), u.getUserID(), fullName, email});
        }
        userAccountsTable.setModel(model);

        // Center "Account Status" and "User ID" columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        userAccountsTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        userAccountsTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
    }

    /**
     * Populates the table with the account of the given employee ID (if user exists).
     */
    protected void populateUserAccountsTableForEmployee(int employeeID) {
        Employee emp = employeeService.getEmployeeByID(employeeID);
        if (emp == null) {
            // Show empty table if not found
            userAccountsTable.setModel(new DefaultTableModel(columnNames, 0));
            return;
        }
        User u = userService.getUserByUserID(emp.getUserID());
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        if (u != null) {
            String fullName = emp.getLastName() + ", " + emp.getFirstName();
            String email = emp.getEmail();
            model.addRow(new Object[]{u.getAccountStatus(), u.getUserID(), fullName, email});
        }
        userAccountsTable.setModel(model);
        // Center columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        userAccountsTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        userAccountsTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
    }

    /**
     * Attaches a double-click handler to the table for record management.
     */
    private void setupRowDoubleClickListener() {
        userAccountsTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                int row = userAccountsTable.rowAtPoint(evt.getPoint());
                if (row < 0) return;
                if (evt.getClickCount() == 2) {
                    String accountStatus = userAccountsTable.getValueAt(row, 0).toString();
                    String userID = userAccountsTable.getValueAt(row, 1).toString();
                    // Get employeeID for success dialog messages
                    Employee emp = employeeService.getEmployeeByUserID(userID);
                    int empID = (emp != null) ? emp.getEmployeeID() : -1;
                    handleAccountRowAction(accountStatus, userID, empID);
                }
            }
        });
    }

    /**
     * Handles double-click actions based on account status.
     * All success dialogs include the Employee ID.
     */
    private void handleAccountRowAction(String accountStatus, String userID, int empID) {
        switch (accountStatus) {
            case "Pending":
                handlePendingAccount(userID, empID);
                break;
            case "Active":
                handleActiveAccount(userID, empID);
                break;
            case "Deactivated":
                handleDeactivatedAccount(userID, empID);
                break;
            case "Rejected":
                JOptionPane.showMessageDialog(this,
                        "This account has been rejected. No further action possible.");
                break;
            default:
                JOptionPane.showMessageDialog(this, "Unknown account status.");
        }
    }

    /**
     * For Pending accounts: Accept or Reject. Dialog always mentions the employee ID.
     */
    private void handlePendingAccount(String userID, int empID) {
        String[] options = {"Approve", "Reject"};
        int choice = JOptionPane.showOptionDialog(this,
                "Approve or reject this account?",
                "Account Approval",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);
        if (choice == 0) {
            updateAccountStatus(userID, "Active");
            JOptionPane.showMessageDialog(this,
                "Employee " + empID + " was approved.");
        } else if (choice == 1) {
            updateAccountStatus(userID, "Rejected");
            JOptionPane.showMessageDialog(this,
                "Employee " + empID + " was rejected.");
        }
        reloadAfterEdit();
    }

    /**
     * For Active accounts: Update or Deactivate. Dialog always mentions the employee ID.
     */
    private void handleActiveAccount(String userID, int empID) {
        String[] options = {"Update", "Deactivate"};
        int choice = JOptionPane.showOptionDialog(this,
                "Update info or deactivate this account?",
                "Manage Account",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);
        if (choice == 0) {
            Employee emp = employeeService.getEmployeeByUserID(userID);
            if (emp != null) {
                SessionManager.setSelectedEmployeeID(emp.getEmployeeID());
                new ui.PageITEmployeeUpdate().setVisible(true);
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Employee not found for this user.");
            }
        } else if (choice == 1) {
            int confirm = JOptionPane.showOptionDialog(this,
                "Are you sure? This cannot be undone.",
                "Deactivate Account",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null, new String[]{"Confirm", "Cancel"}, "Cancel"
            );
            if (confirm == 0) {
                updateAccountStatus(userID, "Deactivated");
                JOptionPane.showMessageDialog(this,
                    "Employee " + empID + " was deactivated.");
                reloadAfterEdit();
            }
        }
    }

    /**
     * For Deactivated accounts: Reactivate (Yes/No). Dialog always mentions the employee ID.
     */
    private void handleDeactivatedAccount(String userID, int empID) {
        String[] options = {"Reactivate", "Cancel"};
        int confirm = JOptionPane.showOptionDialog(this,
                "This account is deactivated. Reactivate this account?",
                "Reactivate Account",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);
        if (confirm == 0) {
            updateAccountStatus(userID, "Active");
            JOptionPane.showMessageDialog(this,
                "Employee " + empID + " was reactivated.");
            reloadAfterEdit();
        }
    }

    /**
     * DRY: All account status updates in one place.
     */
    protected void updateAccountStatus(String userID, String newStatus) {
        User user = userService.getUserByUserID(userID);
        if (user == null) {
            JOptionPane.showMessageDialog(this, "User not found.");
            return;
        }
        user.setAccountStatus(newStatus);
        userService.updateUser(user);
    }

    /**
     * After approve/reject/deactivate/reactivate, reloads table and combobox.
     */
    protected void reloadAfterEdit() {
        reloadEmployeeIDComboBox();
        String selected = (String) employeeIDComboBox.getSelectedItem();
        boolean isAll = selected == null || selected.equals("All");
        if (isAll) {
            populateUserAccountsTable(getCurrentStatusFilter());
        } else {
            int employeeID = Integer.parseInt(selected);
            populateUserAccountsTableForEmployee(employeeID);
        }
    }
}