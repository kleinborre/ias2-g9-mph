package ui.base;

import pojo.Leave;
import pojo.Employee;
import service.LeaveService;
import service.EmployeeService;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.event.*;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

public abstract class AbstractLeaveAdminPage extends JFrame {

    protected JTable leaveTable;
    protected JDateChooser JDateChooser;
    protected JComboBox<String> employeeIDComboBox;
    protected JButton refreshButton;

    protected LeaveService leaveService;
    protected EmployeeService employeeService;

    protected List<Leave> displayedLeaves = new ArrayList<>();
    protected List<Integer> employeeIDs = new ArrayList<>();

    private static final String[] leaveTableCols = {
        "Leave ID", "Employee ID", "Approval Status", "Leave Type", "Leave Start", "Leave End", "Leave Reason"
    };

    public AbstractLeaveAdminPage() {
        leaveService = new LeaveService();
        employeeService = new EmployeeService();
    }

    // Connect to concrete UI components
    protected void setComponentReferences(
            JTable leaveTable,
            JDateChooser JDateChooser,
            JComboBox<String> employeeIDComboBox,
            JButton refreshButton
    ) {
        this.leaveTable = leaveTable;
        this.JDateChooser = JDateChooser;
        this.employeeIDComboBox = employeeIDComboBox;
        this.refreshButton = refreshButton;

        setTableModel();
        populateEmployeeIDComboBox();
        addTableRowActionListener();
        addDateFilterListener();
        addEmployeeComboBoxListener();
        addRefreshButtonListener();
        reloadLeaveTable();
    }

    private void setTableModel() {
        DefaultTableModel model = new DefaultTableModel(null, leaveTableCols) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        leaveTable.setModel(model);
        leaveTable.getTableHeader().setReorderingAllowed(false);

        // Center all columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < leaveTable.getColumnCount(); i++) {
            leaveTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Auto resize columns and always scrollable horizontally
        leaveTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int i = 0; i < leaveTable.getColumnCount(); i++) {
            leaveTable.getColumnModel().getColumn(i).setPreferredWidth(130);
        }
    }

    // Fill employee combo with only IDs (as String)
    private void populateEmployeeIDComboBox() {
        employeeIDs.clear();
        employeeIDComboBox.removeAllItems();
        employeeIDComboBox.addItem("All"); // default option

        List<Employee> employees = employeeService.getAllEmployees();
        for (Employee e : employees) {
            employeeIDs.add(e.getEmployeeID());
            employeeIDComboBox.addItem(String.valueOf(e.getEmployeeID()));
        }
    }

    // Sync filter on employee combo
    private void addEmployeeComboBoxListener() {
        employeeIDComboBox.addActionListener(e -> reloadLeaveTable());
    }

    // Sync filter on JDateChooser
    private void addDateFilterListener() {
        if (JDateChooser == null) return;
        JDateChooser.getDateEditor().addPropertyChangeListener(evt -> {
            if ("date".equals(evt.getPropertyName())) {
                reloadLeaveTable();
            }
        });
    }

    // Refresh button resets all filters
    private void addRefreshButtonListener() {
        if (refreshButton != null) {
            refreshButton.addActionListener(e -> {
                employeeIDComboBox.setSelectedIndex(0); // "All"
                if (JDateChooser != null) JDateChooser.setDate(null);
                reloadLeaveTable();
            });
        }
    }

    // Main table row double-click
    private void addTableRowActionListener() {
        leaveTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() != 2) return;
                int row = leaveTable.getSelectedRow();
                if (row < 0 || row >= displayedLeaves.size()) return;

                Leave leave = displayedLeaves.get(row);

                String status = leaveService.getApprovalStatusName(leave.getApprovalStatusID());
                if (!"Pending".equals(status)) {
                    JOptionPane.showMessageDialog(
                        leaveTable,
                        "This leave request is already finalized and cannot be edited.",
                        "Already Finalized",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                    return;
                }

                // Check if pending leave is > 6 months old
                LocalDate now = LocalDate.now();
                LocalDate created = leave.getDateCreated().toLocalDate();
                long days = ChronoUnit.DAYS.between(created, now);
                if (days > 183) {
                    JOptionPane.showMessageDialog(
                        leaveTable,
                        "Warning: This request is " + days + " days old and still pending approval.",
                        "Very Old Request",
                        JOptionPane.WARNING_MESSAGE
                    );
                }

                // Approve/Reject action
                int decision = JOptionPane.showOptionDialog(
                    leaveTable,
                    "Approve or Reject this pending leave request?\nOnce chosen, this cannot be changed.",
                    "Action Required",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    new String[]{"Approve", "Reject"},
                    "Approve"
                );

                if (decision == JOptionPane.YES_OPTION) {
                    finalizeLeaveApproval(leave, true);
                } else if (decision == JOptionPane.NO_OPTION) {
                    finalizeLeaveApproval(leave, false);
                }
            }
        });
    }

    private void finalizeLeaveApproval(Leave leave, boolean approve) {
        if (!"Pending".equals(leaveService.getApprovalStatusName(leave.getApprovalStatusID())))
            return;

        leave.setApprovalStatusID(approve ? 1 : 2);
        leaveService.updateLeave(leave);
        reloadLeaveTable();

        JOptionPane.showMessageDialog(
            this,
            approve ? "Leave approved." : "Leave rejected.",
            "Action Complete",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    // Reload table applying BOTH filters
    protected void reloadLeaveTable() {
        List<Leave> allLeaves = leaveService.getAllLeaves();

        String selectedEmp = (String) employeeIDComboBox.getSelectedItem();
        Integer employeeID = null;
        if (selectedEmp != null && !"All".equals(selectedEmp)) {
            try {
                employeeID = Integer.parseInt(selectedEmp);
            } catch (NumberFormatException ex) {
                employeeID = null;
            }
        }

        Date filterDate = null;
        Integer filterMonth = null, filterYear = null;
        if (JDateChooser != null && JDateChooser.getDate() != null) {
            LocalDate d = JDateChooser.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            filterMonth = d.getMonthValue();
            filterYear = d.getYear();
        }

        List<Leave> result = new ArrayList<>();
        for (Leave l : allLeaves) {
            boolean empMatch = (employeeID == null) || (l.getEmployeeID() == employeeID);
            boolean monthYearMatch = true;

            if (filterMonth != null && filterYear != null) {
                monthYearMatch = matchMonthYear(l.getLeaveStart(), filterMonth, filterYear)
                              || matchMonthYear(l.getLeaveEnd(), filterMonth, filterYear)
                              || matchMonthYear(l.getDateCreated(), filterMonth, filterYear);
            }
            if (empMatch && monthYearMatch) result.add(l);
        }
        populateLeaveTable(result);
    }

    private void populateLeaveTable(List<Leave> leaves) {
        DefaultTableModel model = (DefaultTableModel) leaveTable.getModel();
        model.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        displayedLeaves.clear();
        for (Leave l : leaves) {
            model.addRow(new Object[]{
                l.getLeaveID(),
                l.getEmployeeID(),
                leaveService.getApprovalStatusName(l.getApprovalStatusID()),
                leaveService.getLeaveTypeName(l.getLeaveTypeID()),
                sdf.format(l.getLeaveStart()),
                sdf.format(l.getLeaveEnd()),
                l.getLeaveReason()
            });
            displayedLeaves.add(l);
        }
        // Auto-resize table viewport to fit
        leaveTable.revalidate();
        leaveTable.repaint();
    }

    // True if the sql.Date d matches given month and year
    private boolean matchMonthYear(Date d, int month, int year) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        return (c.get(Calendar.YEAR) == year) && (c.get(Calendar.MONTH) + 1 == month);
    }
}