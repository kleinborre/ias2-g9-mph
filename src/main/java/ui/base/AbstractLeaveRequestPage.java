package ui.base;

import pojo.Leave;
import service.LeaveService;
import util.SessionManager;
import pojo.Employee;
import service.EmployeeService;

import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

public abstract class AbstractLeaveRequestPage extends JFrame {
    protected JComboBox<String> leaveTypeComboBox;
    protected JDateChooser startDateCalendar;
    protected JDateChooser endDateCalendar;
    protected JTextArea leaveReasonTextArea;
    protected JTextField leaveAvailableField;
    protected JButton submitButton;

    protected final LeaveService leaveService = new LeaveService();
    protected final EmployeeService employeeService = new EmployeeService();
    protected int employeeID = SessionManager.getEmployeeID();

    private int currentLeaveTypeID = -1;
    private double currentLeaveAllowance = 0;
    private boolean formDirty = false;

    private static final LinkedHashMap<String, Integer> LEAVE_TYPE_MAP = new LinkedHashMap<>();
    static {
        LEAVE_TYPE_MAP.put("Sick", 1);
        LEAVE_TYPE_MAP.put("Vacation", 2);
        LEAVE_TYPE_MAP.put("Emergency", 3);
        LEAVE_TYPE_MAP.put("Maternity", 4);
        LEAVE_TYPE_MAP.put("Paternity", 5);
        LEAVE_TYPE_MAP.put("Bereavement", 6);
    }

    public AbstractLeaveRequestPage() {}

    protected void setComponentReferences(
            JComboBox<String> leaveTypeComboBox,
            JDateChooser startDateCalendar,
            JDateChooser endDateCalendar,
            JTextArea leaveReasonTextArea,
            JTextField leaveAvailableField,
            JButton submitButton) {

        this.leaveTypeComboBox = leaveTypeComboBox;
        this.startDateCalendar = startDateCalendar;
        this.endDateCalendar = endDateCalendar;
        this.leaveReasonTextArea = leaveReasonTextArea;
        this.leaveAvailableField = leaveAvailableField;
        this.submitButton = submitButton;

        this.leaveAvailableField.setEditable(false);

        setupListeners();
        updateLeaveAllowanceAndUI();
    }

    private void setupListeners() {
        leaveTypeComboBox.addActionListener(e -> onLeaveTypeSelected());

        startDateCalendar.setEnabled(false);
        endDateCalendar.setEnabled(false);

        startDateCalendar.getDateEditor().addPropertyChangeListener(evt -> {
            if ("date".equals(evt.getPropertyName())) {
                if (validateStartDatePicker(true)) {
                    endDateCalendar.setEnabled(true);
                    endDateCalendar.setDate(null);
                } else {
                    endDateCalendar.setEnabled(false);
                    endDateCalendar.setDate(null);
                }
                validateForm();
            }
        });

        endDateCalendar.getDateEditor().addPropertyChangeListener(evt -> {
            if ("date".equals(evt.getPropertyName())) {
                validateEndDatePicker(true);
                validateForm();
            }
        });

        leaveReasonTextArea.getDocument().addDocumentListener(new DocumentListener() {
            private void filter() {
                SwingUtilities.invokeLater(() -> {
                    String sanitized = sanitizeReasonText(leaveReasonTextArea.getText());
                    if (!sanitized.equals(leaveReasonTextArea.getText())) {
                        leaveReasonTextArea.setText(sanitized);
                    }
                    validateForm();
                });
            }
            public void insertUpdate(DocumentEvent e) { filter(); }
            public void removeUpdate(DocumentEvent e) { filter(); }
            public void changedUpdate(DocumentEvent e) { filter(); }
        });

        ActionListener markDirty = e -> formDirty = true;
        leaveTypeComboBox.addActionListener(markDirty);
        startDateCalendar.getDateEditor().addPropertyChangeListener(evt -> { if ("date".equals(evt.getPropertyName())) formDirty = true; });
        endDateCalendar.getDateEditor().addPropertyChangeListener(evt -> { if ("date".equals(evt.getPropertyName())) formDirty = true; });
        leaveReasonTextArea.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { formDirty = true; }
            public void removeUpdate(DocumentEvent e) { formDirty = true; }
            public void changedUpdate(DocumentEvent e) { formDirty = true; }
        });

        submitButton.setEnabled(false);
        submitButton.addActionListener(e -> onSubmitClicked());
    }

    private void onLeaveTypeSelected() {
        String type = (String) leaveTypeComboBox.getSelectedItem();
        if (type == null || !LEAVE_TYPE_MAP.containsKey(type) || currentLeaveAllowance == 0) {
            startDateCalendar.setEnabled(false);
            endDateCalendar.setEnabled(false);
            currentLeaveTypeID = -1;
            startDateCalendar.setDate(null);
            endDateCalendar.setDate(null);
            validateForm();
            return;
        }

        currentLeaveTypeID = LEAVE_TYPE_MAP.get(type);

        startDateCalendar.setEnabled(true);
        startDateCalendar.setDate(null);
        endDateCalendar.setEnabled(false);
        endDateCalendar.setDate(null);

        validateForm();
    }

    private void updateLeaveAllowanceAndUI() {
        List<Leave> leaves = leaveService.getLeavesByEmployeeID(employeeID);
        double allowance = 0;
        if (!leaves.isEmpty()) {
            leaves.sort(Comparator.comparing(Leave::getDateCreated).reversed());
            allowance = leaves.get(0).getLeaveAllowance();
        }
        currentLeaveAllowance = allowance;
        leaveAvailableField.setText(String.format("%.0f", allowance));

        boolean enabled = allowance > 0;
        leaveTypeComboBox.setEnabled(enabled);
        String type = (String) leaveTypeComboBox.getSelectedItem();
        boolean validType = type != null && LEAVE_TYPE_MAP.containsKey(type);

        startDateCalendar.setEnabled(enabled && validType);
        endDateCalendar.setEnabled(false);
        leaveReasonTextArea.setEnabled(enabled);
        submitButton.setEnabled(false);
        if (!enabled) {
            showDialog("No leaves available. You cannot request new leaves until next year.");
        }
    }

    private void validateForm() {
        boolean valid =
            currentLeaveTypeID > 0 &&
            validateStartDatePicker(false) &&
            validateEndDatePicker(false) &&
            validateReason(false);

        submitButton.setEnabled(valid);
    }

    private boolean validateStartDatePicker(boolean showDialog) {
        if (!startDateCalendar.isEnabled() || startDateCalendar.getDate() == null) return false;
        LocalDate chosen = convertUtilDateToLocal(startDateCalendar.getDate());
        LocalDate now = LocalDate.now();

        // Cannot pick past date
        if (chosen.isBefore(now)) {
            if (showDialog) showDialog("Start date cannot be in the past.");
            startDateCalendar.setDate(null);
            return false;
        }

        // Handle picking current date
        if (chosen.isEqual(now)) {
            // Only for Sick, Emergency, Bereavement AND before 12:00 noon
            if (currentLeaveTypeID == 1 || currentLeaveTypeID == 3 || currentLeaveTypeID == 6) {
                Calendar nowCal = Calendar.getInstance();
                nowCal.setTime(new java.util.Date());
                int hour = nowCal.get(Calendar.HOUR_OF_DAY);
                if (hour >= 12) {
                    if (showDialog) showDialog("You can only file for today before 12:00 noon.");
                    startDateCalendar.setDate(null);
                    return false;
                }
            } else {
                // All other types cannot pick today at all
                if (showDialog) showDialog("You cannot pick the current date for this leave type.");
                startDateCalendar.setDate(null);
                return false;
            }
        }

        // Block too far ahead (1 year 6 months)
        if (chosen.isAfter(now.plusMonths(18))) {
            if (showDialog) showDialog("Start date cannot be more than 1 year and 6 months ahead.");
            startDateCalendar.setDate(null);
            return false;
        }

        // Special rules (future date logic)
        if (currentLeaveTypeID == 1 || currentLeaveTypeID == 3 || currentLeaveTypeID == 6) {
            // Sick, Emergency, Bereavement
            if (!chosen.isEqual(now) && chosen.isAfter(now.plusDays(7))) {
                if (showDialog) showDialog("You can only set the start date up to 7 days in advance for this leave type.");
                startDateCalendar.setDate(null);
                return false;
            }
        } else if (currentLeaveTypeID == 2) {
            // Vacation
            if (chosen.isAfter(now.plusMonths(15))) { // 1 year 3 months
                if (showDialog) showDialog("Vacation leave start cannot be more than 1 year and 3 months ahead.");
                startDateCalendar.setDate(null);
                return false;
            }
        } else if (currentLeaveTypeID == 5) {
            // Paternity
            if (chosen.isAfter(now.plusMonths(8))) {
                if (showDialog) showDialog("Paternity leave can only be set up to 8 months from today.");
                startDateCalendar.setDate(null);
                return false;
            }
        }
        return true;
    }

    private boolean validateEndDatePicker(boolean showDialog) {
        if (!endDateCalendar.isEnabled() || endDateCalendar.getDate() == null) return false;
        if (startDateCalendar.getDate() == null) return false;
        LocalDate start = convertUtilDateToLocal(startDateCalendar.getDate());
        LocalDate end = convertUtilDateToLocal(endDateCalendar.getDate());
        LocalDate now = LocalDate.now();

        if (end.isBefore(start)) {
            if (showDialog) showDialog("End date cannot be before start date.");
            endDateCalendar.setDate(null);
            return false;
        }
        if (end.isEqual(now) || end.isBefore(now)) {
            if (showDialog) showDialog("End date cannot be today or in the past.");
            endDateCalendar.setDate(null);
            return false;
        }
        if (end.isAfter(now.plusMonths(18))) {
            if (showDialog) showDialog("End date cannot be more than 1 year and 6 months ahead.");
            endDateCalendar.setDate(null);
            return false;
        }

        switch (currentLeaveTypeID) {
            case 1:
                if (end.isAfter(start.plusMonths(6))) {
                    if (showDialog) showDialog("Sick leave end date cannot be more than 6 months from start date.");
                    endDateCalendar.setDate(null);
                    return false;
                }
                break;
            case 2:
            case 3:
            case 6:
                long days = java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1;
                if (days > currentLeaveAllowance) {
                    if (showDialog) showDialog("End date exceeds your available leave credits.");
                    endDateCalendar.setDate(null);
                    return false;
                }
                if (currentLeaveAllowance == 0) {
                    if (showDialog) showDialog("You have no leave credits for this leave type.");
                    endDateCalendar.setDate(null);
                    return false;
                }
                break;
            case 4:
                if (end.isAfter(start.plusMonths(4))) {
                    if (showDialog) showDialog("Maternity leave cannot be more than 4 months from start date.");
                    endDateCalendar.setDate(null);
                    return false;
                }
                break;
            case 5:
                LocalDate paternityMax = start.plusMonths(2);
                if (end.isAfter(paternityMax)) {
                    if (showDialog) showDialog("Paternity leave can only be for up to 2 months from start date.");
                    endDateCalendar.setDate(null);
                    return false;
                }
                break;
            default:
        }
        return true;
    }

    private boolean validateReason(boolean showDialog) {
        String text = leaveReasonTextArea.getText();
        if (text == null) return false;
        String cleaned = sanitizeReasonText(text);
        if (cleaned.length() < 8) {
            if (showDialog) showDialog("Reason for leave must be at least 8 characters.");
            return false;
        }
        if (cleaned.length() > 255) {
            if (showDialog) showDialog("Reason for leave cannot exceed 255 characters.");
            return false;
        }
        return true;
    }

    private String sanitizeReasonText(String input) {
        if (input == null) return "";
        StringBuilder out = new StringBuilder();
        char last = 0;
        int len = 0;
        for (char c : input.toCharArray()) {
            if (len >= 255) break;
            if (Character.isLetterOrDigit(c) || c == ' ') {
                out.append(c);
                last = c;
                len++;
            } else if ((c == '.' || c == ',') && last != c) {
                out.append(c);
                last = c;
                len++;
            }
        }
        return out.toString();
    }

    public boolean isFormDirty() { return formDirty; }
    public void resetFormDirty() { formDirty = false; }

    private void onSubmitClicked() {
        if (!validateFormWithDialogs()) return;
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to submit this leave request?",
                "Confirm Submit", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        Leave newLeave = new Leave();
        newLeave.setEmployeeID(employeeID);
        newLeave.setLeaveTypeID(currentLeaveTypeID);
        newLeave.setLeaveAllowance(currentLeaveAllowance - 1);
        newLeave.setLeaveStart(new java.sql.Date(startDateCalendar.getDate().getTime()));
        newLeave.setLeaveEnd(new java.sql.Date(endDateCalendar.getDate().getTime()));
        newLeave.setLeaveReason(leaveReasonTextArea.getText().trim());
        newLeave.setDateCreated(java.sql.Date.valueOf(LocalDate.now()));
        newLeave.setApprovalStatusID(3); // Pending

        leaveService.addLeave(newLeave);

        formDirty = false;
        onRequestSubmitted();
    }

    protected abstract void onRequestSubmitted();

    protected void showDialog(String message) {
        JOptionPane.showMessageDialog(this, message, "Notice", JOptionPane.INFORMATION_MESSAGE);
    }

    private boolean validateFormWithDialogs() {
        if (currentLeaveTypeID <= 0) {
            showDialog("Please select a leave type.");
            return false;
        }
        if (!validateStartDatePicker(true)) return false;
        if (!validateEndDatePicker(true)) return false;
        if (!validateReason(true)) return false;
        return true;
    }

    private LocalDate convertUtilDateToLocal(java.util.Date d) {
        return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    protected int getSelectedLeaveTypeID() {
        String type = (String) leaveTypeComboBox.getSelectedItem();
        return LEAVE_TYPE_MAP.getOrDefault(type, -1);
    }
}