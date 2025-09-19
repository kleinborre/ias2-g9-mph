package ui.base;

import com.toedter.calendar.JDateChooser;
import pojo.Overtime;
import pojo.Employee;
import service.OvertimeService;
import service.EmployeeService;
import util.SessionManager;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.event.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;

public abstract class AbstractOvertimeAdminPage extends JFrame {

    protected JTable overtimeTable;
    protected JComboBox<String> employeeIDComboBox;
    protected JDateChooser JDateChooser;
    protected final OvertimeService overtimeService = new OvertimeService();
    protected final EmployeeService employeeService = new EmployeeService();

    protected List<Overtime> allOvertimes = new ArrayList<>();
    protected List<Overtime> filteredOvertimes = new ArrayList<>();
    protected Map<Integer, String> employeeNameMap = new HashMap<>();
    private final String[] overtimeTableCols = {
            "Overtime ID", "Employee ID", "Employee Name",
            "Start Date", "End Date", "Reason", "Status"
    };

    protected void setComponentReferences(JTable overtimeTable,
                                          JComboBox<String> employeeIDComboBox,
                                          JDateChooser JDateChooser) {
        this.overtimeTable = overtimeTable;
        this.employeeIDComboBox = employeeIDComboBox;
        this.JDateChooser = JDateChooser;

        setupTableModel();
        loadEmployeeList();
        loadAllOvertimeData();
        applyFiltersAndPopulate();

        addTableDoubleClickListener();
        addFilterListeners();
    }

    private void setupTableModel() {
        DefaultTableModel model = new DefaultTableModel(null, overtimeTableCols) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        overtimeTable.setModel(model);

        // Center all columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < overtimeTable.getColumnCount(); i++)
            overtimeTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);

        overtimeTable.setFillsViewportHeight(true);
        overtimeTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        overtimeTable.getTableHeader().setReorderingAllowed(false);
        overtimeTable.setRowHeight(26);
        overtimeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    // Load all employeeIDs and names for ComboBox, mapping for table display
    private void loadEmployeeList() {
        employeeIDComboBox.removeAllItems();
        employeeIDComboBox.addItem("All");
        employeeNameMap.clear();
        for (Employee emp : employeeService.getAllEmployees()) {
            employeeIDComboBox.addItem(String.valueOf(emp.getEmployeeID()));
            employeeNameMap.put(emp.getEmployeeID(), emp.getFirstName() + " " + emp.getLastName());
        }
    }

    // Load all overtime records from SQL
    protected void loadAllOvertimeData() {
        allOvertimes = overtimeService.getAllOvertimes();
    }

    // Used by refreshButton (call this in UI button)
    public void refreshAllData() {
        loadEmployeeList();
        loadAllOvertimeData();
        JDateChooser.setDate(null);
        employeeIDComboBox.setSelectedIndex(0);
        applyFiltersAndPopulate();
    }

    // Filter logic: mix of employeeID and month/year
    protected void applyFiltersAndPopulate() {
        filteredOvertimes = new ArrayList<>(allOvertimes);

        String selectedID = (String) employeeIDComboBox.getSelectedItem();
        Date pickedDate = (JDateChooser != null) ? JDateChooser.getDate() : null;

        // Filter by employeeID
        if (selectedID != null && !"All".equals(selectedID)) {
            int filterID = Integer.parseInt(selectedID);
            filteredOvertimes.removeIf(ot -> ot.getEmployeeID() != filterID);
        }

        // Filter by month/year if set
        if (pickedDate != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(pickedDate);
            int filterYear = cal.get(Calendar.YEAR);
            int filterMonth = cal.get(Calendar.MONTH); // zero-based

            filteredOvertimes.removeIf(ot -> {
                Calendar otStart = Calendar.getInstance();
                otStart.setTime(ot.getOvertimeStart());
                return otStart.get(Calendar.YEAR) != filterYear ||
                       otStart.get(Calendar.MONTH) != filterMonth;
            });
        }

        populateOvertimeTable(filteredOvertimes);
    }

    private void populateOvertimeTable(List<Overtime> overtimeList) {
        DefaultTableModel model = (DefaultTableModel) overtimeTable.getModel();
        model.setRowCount(0);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        for (Overtime ot : overtimeList) {
            String name = employeeNameMap.getOrDefault(ot.getEmployeeID(), String.valueOf(ot.getEmployeeID()));
            model.addRow(new Object[]{
                    ot.getOvertimeID(),
                    ot.getEmployeeID(),
                    name,
                    sdf.format(ot.getOvertimeStart()),
                    sdf.format(ot.getOvertimeEnd()),
                    ot.getOvertimeReason(),
                    getStatusText(ot.getApprovalStatusID())
            });
        }
    }

    private String getStatusText(int statusID) {
        switch (statusID) {
            case 1: return "Approved";
            case 2: return "Rejected";
            case 3: return "Pending";
            default: return "Unknown";
        }
    }

    // Handles table double-click for approval/rejection
    private void addTableDoubleClickListener() {
        overtimeTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2 && overtimeTable.getSelectedRow() != -1) {
                    int selectedRow = overtimeTable.getSelectedRow();
                    if (selectedRow < 0 || selectedRow >= filteredOvertimes.size()) return;
                    Overtime selected = filteredOvertimes.get(selectedRow);

                    // Check for age >6 months
                    LocalDate today = LocalDate.now();
                    LocalDate otDate = selected.getOvertimeStart().toLocalDateTime().toLocalDate();
                    long daysOld = ChronoUnit.DAYS.between(otDate, today);

                    if (daysOld > 183) { // about 6 months (183 days)
                        int cont = JOptionPane.showConfirmDialog(overtimeTable,
                                "Caution: This overtime request is " + daysOld +
                                " days old. Do you want to continue?",
                                "Caution", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                        if (cont != JOptionPane.YES_OPTION) return;
                    }

                    // Only pending can be changed
                    if (selected.getApprovalStatusID() != 3) {
                        JOptionPane.showMessageDialog(overtimeTable,
                                "Only Pending requests can be approved or rejected.",
                                "Change Not Allowed", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }

                    // Only Approve/Reject -- No Cancel
                    Object[] opts = {"Approve", "Reject"};
                    int opt = JOptionPane.showOptionDialog(
                            overtimeTable,
                            "Approve or Reject this overtime request?",
                            "Approve/Reject Overtime",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null, opts, opts[0]);
                    if (opt == JOptionPane.YES_OPTION) {
                        updateApprovalStatus(selected, 1);
                    } else if (opt == JOptionPane.NO_OPTION) {
                        updateApprovalStatus(selected, 2);
                    }
                }
            }
        });
    }

    private void updateApprovalStatus(Overtime ot, int statusID) {
        try {
            overtimeService.updateApprovalStatus(ot.getOvertimeID(), statusID);
            loadAllOvertimeData();
            applyFiltersAndPopulate();
            JOptionPane.showMessageDialog(overtimeTable,
                    "Overtime request has been " + (statusID == 1 ? "Approved." : "Rejected."),
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(overtimeTable,
                    "Failed to update approval status: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Listeners for filters
    private void addFilterListeners() {
        if (employeeIDComboBox != null) {
            employeeIDComboBox.addActionListener(e -> applyFiltersAndPopulate());
        }
        if (JDateChooser != null && JDateChooser.getDateEditor() != null) {
            JDateChooser.getDateEditor().addPropertyChangeListener(evt -> {
                if ("date".equals(evt.getPropertyName())) {
                    applyFiltersAndPopulate();
                }
            });
        }
    }
}