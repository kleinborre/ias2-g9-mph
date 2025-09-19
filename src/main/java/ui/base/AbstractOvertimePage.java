package ui.base;

import pojo.Overtime;
import service.OvertimeService;
import util.SessionManager;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.sql.Timestamp;

public abstract class AbstractOvertimePage extends JFrame {
    protected JTable overtimeTable;
    protected JDateChooser JDateChooser;
    protected final OvertimeService overtimeService = new OvertimeService();

    protected int employeeID;
    protected String userID;
    protected List<Overtime> displayedOvertimes = new ArrayList<>();

    private static final String[] overtimeTableCols = {
        "Approval Status", "Reason", "Start DateTime", "End DateTime"
    };

    protected void setComponentReferences(JTable overtimeTable, JDateChooser JDateChooser) {
        this.overtimeTable = overtimeTable;
        this.JDateChooser = JDateChooser;
        this.userID = SessionManager.getUserID();
        this.employeeID = SessionManager.getEmployeeID();

        setTableModel();
        addDateFilterListener();

        // Always load all overtime records for this employee on startup
        reloadOvertimeTable();

        // Double-click listener for row actions (update/delete)
        addTableDoubleClickListener();
    }

    private void setTableModel() {
        DefaultTableModel model = new DefaultTableModel(null, overtimeTableCols) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        overtimeTable.setModel(model);
        overtimeTable.getTableHeader().setReorderingAllowed(false);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < overtimeTable.getColumnCount(); i++) {
            overtimeTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        overtimeTable.setFillsViewportHeight(true);
        overtimeTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        overtimeTable.setRowHeight(28);
    }

    private void addDateFilterListener() {
        if (JDateChooser != null && JDateChooser.getDateEditor() != null) {
            JDateChooser.getDateEditor().addPropertyChangeListener(evt -> {
                if ("date".equals(evt.getPropertyName())) {
                    reloadOvertimeTable();
                }
            });
        }
    }

    protected void reloadOvertimeTable() {
        if (employeeID == 0) {
            displayedOvertimes.clear();
            populateOvertimeTable(Collections.emptyList());
            return;
        }
        List<Overtime> all = overtimeService.getOvertimesByEmployeeID(employeeID);

        // No filter: show all
        if (JDateChooser == null || JDateChooser.getDate() == null) {
            populateOvertimeTable(all);
            return;
        }

        // Filter logic: day first, fallback to month
        Date filterDate = JDateChooser.getDate();
        Calendar filterCal = Calendar.getInstance();
        filterCal.setTime(filterDate);

        int filterYear = filterCal.get(Calendar.YEAR);
        int filterMonth = filterCal.get(Calendar.MONTH); // zero-based
        int filterDay = filterCal.get(Calendar.DAY_OF_MONTH);

        List<Overtime> dayMatches = new ArrayList<>();
        List<Overtime> monthMatches = new ArrayList<>();

        for (Overtime ot : all) {
            Timestamp start = ot.getOvertimeStart();
            Timestamp end = ot.getOvertimeEnd();

            Calendar startCal = Calendar.getInstance();
            startCal.setTime(start);
            Calendar endCal = Calendar.getInstance();
            endCal.setTime(end);

            // Day match
            boolean matchStartDay = (startCal.get(Calendar.YEAR) == filterYear)
                    && (startCal.get(Calendar.MONTH) == filterMonth)
                    && (startCal.get(Calendar.DAY_OF_MONTH) == filterDay);
            boolean matchEndDay = (endCal.get(Calendar.YEAR) == filterYear)
                    && (endCal.get(Calendar.MONTH) == filterMonth)
                    && (endCal.get(Calendar.DAY_OF_MONTH) == filterDay);

            if (matchStartDay || matchEndDay) {
                dayMatches.add(ot);
            }

            // Month match (add ALL OTs for the chosen month)
            boolean matchStartMonth = (startCal.get(Calendar.YEAR) == filterYear)
                    && (startCal.get(Calendar.MONTH) == filterMonth);
            boolean matchEndMonth = (endCal.get(Calendar.YEAR) == filterYear)
                    && (endCal.get(Calendar.MONTH) == filterMonth);

            if ((matchStartMonth || matchEndMonth) && !monthMatches.contains(ot)) {
                monthMatches.add(ot);
            }
        }

        // If there are any records for the exact day, show only those
        if (!dayMatches.isEmpty()) {
            populateOvertimeTable(dayMatches);
        } else {
            // Otherwise, show everything in the chosen month
            populateOvertimeTable(monthMatches);
        }
    }

    private void populateOvertimeTable(List<Overtime> overtimes) {
        DefaultTableModel model = (DefaultTableModel) overtimeTable.getModel();
        model.setRowCount(0);
        displayedOvertimes.clear();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        for (Overtime ot : overtimes) {
            model.addRow(new Object[]{
                getApprovalStatusName(ot.getApprovalStatusID()),
                ot.getOvertimeReason(),
                sdf.format(ot.getOvertimeStart()),
                sdf.format(ot.getOvertimeEnd())
            });
            displayedOvertimes.add(ot);
        }
    }

    protected String getApprovalStatusName(int id) {
        switch (id) {
            case 1: return "Approved";
            case 2: return "Rejected";
            case 3: return "Pending";
            default: return "Unknown";
        }
    }

    // --- Double-click handler for update/delete with Cancel option REMOVED ---
    private void addTableDoubleClickListener() {
        overtimeTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2 && overtimeTable.getSelectedRow() != -1) {
                    int selectedRow = overtimeTable.getSelectedRow();
                    if (selectedRow >= 0 && selectedRow < displayedOvertimes.size()) {
                        Overtime selectedOvertime = displayedOvertimes.get(selectedRow);

                        // Only Update and Delete (no Cancel)
                        String[] options = {"Update", "Delete"};
                        int choice = JOptionPane.showOptionDialog(
                                overtimeTable,
                                "What do you want to do with this overtime request?",
                                "Overtime Request",
                                JOptionPane.YES_NO_OPTION, // Only Yes/No
                                JOptionPane.QUESTION_MESSAGE,
                                null,
                                options,
                                options[0]
                        );

                        if (choice == 0) { // Update
                            onOvertimeUpdateSelected(selectedOvertime);
                        } else if (choice == 1) { // Delete
                            int confirm = JOptionPane.showConfirmDialog(
                                    overtimeTable,
                                    "Are you sure you want to delete this overtime request?",
                                    "Confirm Delete",
                                    JOptionPane.YES_NO_OPTION
                            );
                            if (confirm == JOptionPane.YES_OPTION) {
                                try {
                                    overtimeService.deleteOvertime(selectedOvertime.getOvertimeID());
                                    reloadOvertimeTable();
                                    JOptionPane.showMessageDialog(
                                            overtimeTable,
                                            "Overtime request deleted successfully.",
                                            "Success",
                                            JOptionPane.INFORMATION_MESSAGE
                                    );
                                } catch (Exception ex) {
                                    JOptionPane.showMessageDialog(
                                            overtimeTable,
                                            "Failed to delete overtime request: " + ex.getMessage(),
                                            "Error",
                                            JOptionPane.ERROR_MESSAGE
                                    );
                                }
                            }
                        }
                        // No Cancel: user can click the window X to dismiss the dialog
                    }
                }
            }
        });
    }

    // --- ABSTRACT: UI class must implement what to do on update ---
    protected abstract void onOvertimeUpdateSelected(Overtime selectedOvertime);
}