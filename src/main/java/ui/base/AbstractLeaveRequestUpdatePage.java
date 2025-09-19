package ui.base;

import pojo.Leave;
import service.LeaveService;
import service.EmployeeService;
import util.SessionManager;

import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

public abstract class AbstractLeaveRequestUpdatePage extends JFrame {
    protected JComboBox<String> leaveTypeComboBox;
    protected JDateChooser startDateCalendar;
    protected JDateChooser endDateCalendar;
    protected JTextArea leaveReasonTextArea;
    protected JTextField leaveAvailableField;
    protected JButton submitButton;

    protected final LeaveService leaveService = new LeaveService();
    protected final EmployeeService employeeService = new EmployeeService();
    protected int employeeID = SessionManager.getEmployeeID();

    protected Leave editingLeave;

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

    public AbstractLeaveRequestUpdatePage() {}

    // Call after initComponents + before showing form!
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
        // Do not call updateLeaveAllowanceAndUI() here because we set fields via setLeaveData()
    }

    // Call this with the Leave to be edited, right after setComponentReferences
    protected void setLeaveData(Leave leave) {
        this.editingLeave = leave;
        if (leave == null) return;
        // Set all fields
        leaveTypeComboBox.setSelectedItem(getLeaveTypeString(leave.getLeaveTypeID()));
        startDateCalendar.setDate(leave.getLeaveStart());
        endDateCalendar.setDate(leave.getLeaveEnd());
        leaveReasonTextArea.setText(leave.getLeaveReason());
        leaveAvailableField.setText(String.format("%.0f", leave.getLeaveAllowance()));
        // Allow all editing
        leaveTypeComboBox.setEnabled(true);
        startDateCalendar.setEnabled(true);
        endDateCalendar.setEnabled(true);
        leaveReasonTextArea.setEnabled(true);
        submitButton.setEnabled(true);

        currentLeaveTypeID = leave.getLeaveTypeID();
        currentLeaveAllowance = leave.getLeaveAllowance();

        validateForm();
    }

    private void setupListeners() {
        leaveTypeComboBox.addActionListener(e -> onLeaveTypeSelected());

        startDateCalendar.setEnabled(false);
        endDateCalendar.setEnabled(false);

        startDateCalendar.getDateEditor().addPropertyChangeListener(evt -> {
            if ("date".equals(evt.getPropertyName())) {
                if (validateStartDatePicker(true)) {
                    endDateCalendar.setEnabled(true);
                    // Don't reset end date if editing
                } else {
                    endDateCalendar.setEnabled(false);
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
        submitButton.addActionListener(e -> onUpdateSubmitClicked());
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
        endDateCalendar.setEnabled(true);
        validateForm();
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

        if (chosen.isBefore(now)) {
            if (showDialog) showDialog("Start date cannot be in the past.");
            startDateCalendar.setDate(null);
            return false;
        }

        if (chosen.isEqual(now)) {
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
                if (showDialog) showDialog("You cannot pick the current date for this leave type.");
                startDateCalendar.setDate(null);
                return false;
            }
        }

        if (chosen.isAfter(now.plusMonths(18))) {
            if (showDialog) showDialog("Start date cannot be more than 1 year and 6 months ahead.");
            startDateCalendar.setDate(null);
            return false;
        }

        if (currentLeaveTypeID == 1 || currentLeaveTypeID == 3 || currentLeaveTypeID == 6) {
            if (!chosen.isEqual(now) && chosen.isAfter(now.plusDays(7))) {
                if (showDialog) showDialog("You can only set the start date up to 7 days in advance for this leave type.");
                startDateCalendar.setDate(null);
                return false;
            }
        } else if (currentLeaveTypeID == 2) {
            if (chosen.isAfter(now.plusMonths(15))) {
                if (showDialog) showDialog("Vacation leave start cannot be more than 1 year and 3 months ahead.");
                startDateCalendar.setDate(null);
                return false;
            }
        } else if (currentLeaveTypeID == 5) {
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

    // When user clicks Submit (Update)
    private void onUpdateSubmitClicked() {
        if (!validateFormWithDialogs()) return;
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to update this leave request?",
                "Confirm Update", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        // Update the Leave object fields with new values
        editingLeave.setLeaveTypeID(currentLeaveTypeID);
        editingLeave.setLeaveStart(new java.sql.Date(startDateCalendar.getDate().getTime()));
        editingLeave.setLeaveEnd(new java.sql.Date(endDateCalendar.getDate().getTime()));
        editingLeave.setLeaveReason(leaveReasonTextArea.getText().trim());
        // If you want to update allowance, do so here if applicable

        leaveService.updateLeave(editingLeave);

        formDirty = false;
        onUpdateRequestSubmitted();
    }

    protected abstract void onUpdateRequestSubmitted();

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

    private String getLeaveTypeString(int leaveTypeID) {
        for (Map.Entry<String, Integer> entry : LEAVE_TYPE_MAP.entrySet()) {
            if (entry.getValue() == leaveTypeID) return entry.getKey();
        }
        return null;
    }
}