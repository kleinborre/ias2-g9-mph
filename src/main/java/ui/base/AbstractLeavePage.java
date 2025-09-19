package ui.base;

import pojo.Leave;
import service.LeaveService;
import util.SessionManager;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.*;

public abstract class AbstractLeavePage extends JFrame {
    protected JTable leaveTable;
    protected JDateChooser JDateChooser;
    protected LeaveService leaveService;
    protected int employeeID;
    protected String userID;
    protected final String[] leaveTableCols = {
        "Status", "Type", "Start", "End", "Reason", "Date Filed"
    };

    // For mapping table row to leaveID
    protected List<Leave> displayedLeaves = new ArrayList<>();

    public AbstractLeavePage() {
        this.employeeID = SessionManager.getEmployeeID();
        this.userID = SessionManager.getUserID();
        this.leaveService = new LeaveService();
    }

    protected void setComponentReferences(JTable leaveTable, JDateChooser JDateChooser) {
        this.leaveTable = leaveTable;
        this.JDateChooser = JDateChooser;
        setTableModel();
        addDateFilterListener();
        addTableRowActionListener();
        reloadLeaveTable();
    }

    protected void setTableModel() {
        DefaultTableModel model = new DefaultTableModel(null, leaveTableCols) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        leaveTable.setModel(model);
        leaveTable.getTableHeader().setReorderingAllowed(false);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < leaveTable.getColumnCount(); i++) {
            leaveTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }

    protected void addDateFilterListener() {
        if (JDateChooser == null) return;
        JDateChooser.getDateEditor().addPropertyChangeListener(evt -> {
            if ("date".equals(evt.getPropertyName())) {
                reloadLeaveTable();
            }
        });
    }

    // DOUBLE-CLICK SUPPORT WITH YEAR RESTRICTION
    private void addTableRowActionListener() {
        leaveTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = leaveTable.getSelectedRow();
                    if (row < 0 || row >= displayedLeaves.size()) return;
                    Leave selectedLeave = displayedLeaves.get(row);

                    // Restrict editing if the record's start year is before current year
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(selectedLeave.getLeaveStart());
                    int leaveYear = cal.get(Calendar.YEAR);

                    int currentYear = Calendar.getInstance().get(Calendar.YEAR);

                    if (leaveYear < currentYear) {
                        // System dialog: No edit/delete allowed for previous years
                        JOptionPane.showMessageDialog(
                            leaveTable,
                            "Editing or deleting leave records from previous years is not allowed.",
                            "Not Allowed",
                            JOptionPane.INFORMATION_MESSAGE
                        );
                        return;
                    }

                    // Only show Yes/No (Update/Delete) choices, never Cancel
                    int choice = JOptionPane.showOptionDialog(
                            leaveTable,
                            "Do you want to update or delete this leave request?",
                            "Leave Request Action",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            new String[]{"Update", "Delete"},
                            "Update"
                    );
                    if (choice == JOptionPane.YES_OPTION) {
                        onUpdateLeave(selectedLeave);
                    } else if (choice == JOptionPane.NO_OPTION) {
                        // Confirm deletion, Yes/No only
                        int confirm = JOptionPane.showOptionDialog(
                                leaveTable,
                                "This action is irreversible. Are you sure you want to delete this leave request?",
                                "Confirm Deletion",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.WARNING_MESSAGE,
                                null,
                                new String[]{"Yes", "No"},
                                "No"
                        );
                        if (confirm == JOptionPane.YES_OPTION) {
                            leaveService.deleteLeave(selectedLeave.getLeaveID());
                            showDialog("Leave request deleted successfully.");
                            reloadLeaveTable();
                        }
                    }
                }
            }
        });
    }

    protected void reloadLeaveTable() {
        this.employeeID = SessionManager.getEmployeeID();
        if (this.employeeID <= 0) {
            clearTable();
            return;
        }
        Date filterDate = null;
        if (JDateChooser != null && JDateChooser.getDate() != null) {
            filterDate = new Date(JDateChooser.getDate().getTime());
        }
        List<Leave> leaves = leaveService.getLeavesByEmployeeID(employeeID);

        // No filter set, show all
        if (filterDate == null) {
            populateLeaveTable(leaves);
            return;
        }

        // 1. Look for *any* exact date match in the records
        List<Leave> exactMatches = new ArrayList<>();
        for (Leave l : leaves) {
            if (sameDay(l.getLeaveStart(), filterDate) ||
                sameDay(l.getLeaveEnd(), filterDate) ||
                sameDay(l.getDateCreated(), filterDate)) {
                exactMatches.add(l);
            }
        }
        if (!exactMatches.isEmpty()) {
            populateLeaveTable(exactMatches);
            return;
        }

        // 2. If no exact match, show all with matching month/year for any field
        Calendar filterCal = Calendar.getInstance();
        filterCal.setTime(filterDate);
        int filterYear = filterCal.get(Calendar.YEAR);
        int filterMonth = filterCal.get(Calendar.MONTH);

        List<Leave> monthYearMatches = new ArrayList<>();
        for (Leave l : leaves) {
            if (matchesMonthYear(l.getLeaveStart(), filterYear, filterMonth) ||
                matchesMonthYear(l.getLeaveEnd(), filterYear, filterMonth) ||
                matchesMonthYear(l.getDateCreated(), filterYear, filterMonth)) {
                monthYearMatches.add(l);
            }
        }
        populateLeaveTable(monthYearMatches);
    }

    protected void populateLeaveTable(List<Leave> leaves) {
        DefaultTableModel model = (DefaultTableModel) leaveTable.getModel();
        model.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy");
        displayedLeaves.clear();
        for (Leave l : leaves) {
            String status = leaveService.getApprovalStatusName(l.getApprovalStatusID());
            String type = leaveService.getLeaveTypeName(l.getLeaveTypeID());
            String start = sdf.format(l.getLeaveStart());
            String end = sdf.format(l.getLeaveEnd());
            String reason = l.getLeaveReason();
            String created = sdf.format(l.getDateCreated());
            model.addRow(new Object[]{status, type, start, end, reason, created});
            displayedLeaves.add(l);
        }
    }

    protected void clearTable() {
        DefaultTableModel model = (DefaultTableModel) leaveTable.getModel();
        model.setRowCount(0);
        displayedLeaves.clear();
    }

    private boolean sameDay(Date d1, Date d2) {
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        c1.setTime(d1);
        c2.setTime(d2);
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
            && c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH)
            && c1.get(Calendar.DAY_OF_MONTH) == c2.get(Calendar.DAY_OF_MONTH);
    }

    private boolean matchesMonthYear(Date d, int year, int month) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        return c.get(Calendar.YEAR) == year && c.get(Calendar.MONTH) == month;
    }

    // Called on update, to be handled in subclass (page decides what to show next)
    protected abstract void onUpdateLeave(Leave selectedLeave);

    protected void showDialog(String message) {
        JOptionPane.showMessageDialog(this, message, "Notice", JOptionPane.INFORMATION_MESSAGE);
    }
}