package ui.base;

import com.toedter.calendar.JDateChooser;
import pojo.Overtime;
import service.OvertimeService;
import util.SessionManager;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.*;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

public abstract class AbstractOvertimeRequestPage extends JFrame {
    protected JDateChooser startDateCalendar;
    protected JSpinner overtimeHoursJSpinner;
    protected JTextArea overtimeReasonTextArea;
    protected JButton submitButton;

    protected int employeeID;
    protected boolean formDirty = false;

    // For initial value checks
    private Date initialDate = null;
    private int initialHours = 1;
    private String initialReason = "";

    // Used to prevent duplicate dialogs on date validation
    private boolean suppressDateDialog = false;

    protected final OvertimeService overtimeService = new OvertimeService();

    // Call this in UI constructor after initComponents
    protected void setComponentReferences(JDateChooser startDateCalendar, JSpinner overtimeHoursJSpinner,
                                          JTextArea overtimeReasonTextArea, JButton submitButton) {
        this.startDateCalendar = startDateCalendar;
        this.overtimeHoursJSpinner = overtimeHoursJSpinner;
        this.overtimeReasonTextArea = overtimeReasonTextArea;
        this.submitButton = submitButton;
        this.employeeID = SessionManager.getEmployeeID();

        // Spinner should start at 1
        if (((int)overtimeHoursJSpinner.getValue()) < 1) {
            overtimeHoursJSpinner.setValue(1);
        }

        // Set initial values for dirty tracking
        initialDate = getDateOrNull(startDateCalendar);
        initialHours = (int) overtimeHoursJSpinner.getValue();
        initialReason = overtimeReasonTextArea.getText();

        overtimeHoursJSpinner.setEnabled(false);
        submitButton.setEnabled(false);

        ((SpinnerNumberModel) overtimeHoursJSpinner.getModel()).setMinimum(1);
        ((SpinnerNumberModel) overtimeHoursJSpinner.getModel()).setMaximum(4);
        ((SpinnerNumberModel) overtimeHoursJSpinner.getModel()).setStepSize(1);

        // Listener: ONLY this one, fires for both valid and invalid changes
        startDateCalendar.getDateEditor().addPropertyChangeListener(evt -> {
            if ("date".equals(evt.getPropertyName()) && !suppressDateDialog) {
                validateAndLockDate();
                updateFormDirty();
                validateForm();
            }
        });

        // Spinner dirty tracking (do not set dirty if value matches initial)
        overtimeHoursJSpinner.addChangeListener(e -> {
            updateFormDirty();
            validateForm();
        });

        // Reason area dirty check and enforce 255 chars
        overtimeReasonTextArea.getDocument().addDocumentListener(new DocumentListener() {
            private void checkLenAndDirty() {
                SwingUtilities.invokeLater(() -> {
                    String text = overtimeReasonTextArea.getText();
                    if (text.length() > 255) {
                        overtimeReasonTextArea.setText(text.substring(0, 255));
                    }
                    updateFormDirty();
                    validateForm();
                });
            }
            public void insertUpdate(DocumentEvent e) { checkLenAndDirty(); }
            public void removeUpdate(DocumentEvent e) { checkLenAndDirty(); }
            public void changedUpdate(DocumentEvent e) { checkLenAndDirty(); }
        });

        // Initial validation
        validateForm();
    }

    // Date validation logic (no duplicate dialog)
    private void validateAndLockDate() {
        Date picked = getDateOrNull(startDateCalendar);
        Date today = stripTime(new Date());
        if (picked != null) {
            Date pickedDay = stripTime(picked);
            long diff = (pickedDay.getTime() - today.getTime()) / (1000 * 60 * 60 * 24);

            // Only show one dialog per invalid event
            if (pickedDay.before(today)) {
                suppressDateDialog = true;
                showWarning("You cannot book overtime for past dates.");
                startDateCalendar.setDate(null);
                overtimeHoursJSpinner.setEnabled(false);
                suppressDateDialog = false;
                return;
            }
            if (diff > 14) {
                suppressDateDialog = true;
                showWarning("You cannot book overtime more than 2 weeks in advance.");
                startDateCalendar.setDate(null);
                overtimeHoursJSpinner.setEnabled(false);
                suppressDateDialog = false;
                return;
            }
            overtimeHoursJSpinner.setEnabled(true);
        } else {
            overtimeHoursJSpinner.setEnabled(false);
        }
    }

    // Dirty check: Only set true if ACTUAL value changed
    private void updateFormDirty() {
        Date currentDate = getDateOrNull(startDateCalendar);
        int currentHours = (int) overtimeHoursJSpinner.getValue();
        String currentReason = overtimeReasonTextArea.getText();

        boolean changed =
            !equalsDate(currentDate, initialDate)
            || currentHours != initialHours
            || !currentReason.equals(initialReason);

        formDirty = changed;
    }

    // Helper: compare two Date objects for equality or both null
    private boolean equalsDate(Date a, Date b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.getTime() == b.getTime();
    }

    // Null-safe date get
    private Date getDateOrNull(JDateChooser chooser) {
        Date d = chooser.getDate();
        return (d != null) ? stripTime(d) : null;
    }

    // Disables submit unless all fields are valid
    protected void validateForm() {
        boolean valid =
                startDateCalendar.getDate() != null &&
                overtimeHoursJSpinner.isEnabled() &&
                ((int)overtimeHoursJSpinner.getValue() >= 1) &&
                overtimeReasonTextArea.getText().trim().length() > 0 &&
                overtimeReasonTextArea.getText().trim().length() <= 255;
        submitButton.setEnabled(valid);
    }

    // For UI: Used by Cancel/Back to check for unsaved changes
    public boolean isFormDirty() {
        return formDirty;
    }

    // Call this in UI's Submit button handler
    protected boolean trySubmitRequest() {
        Date picked = startDateCalendar.getDate();
        if (picked == null) {
            showWarning("Please select a valid date.");
            return false;
        }
        int hours = (int) overtimeHoursJSpinner.getValue();
        if (hours < 1 || hours > 4) {
            showWarning("Overtime hours must be 1 to 4.");
            return false;
        }
        String reason = overtimeReasonTextArea.getText().trim();
        if (reason.isEmpty() || reason.length() > 255) {
            showWarning("Reason is required (max 255 chars).");
            return false;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to submit this overtime request?",
                "Confirm Submission", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return false;

        Calendar c = Calendar.getInstance();
        c.setTime(stripTime(picked));
        c.set(Calendar.HOUR_OF_DAY, 16);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        Timestamp overtimeStart = new Timestamp(c.getTimeInMillis());

        c.add(Calendar.HOUR_OF_DAY, hours);
        Timestamp overtimeEnd = new Timestamp(c.getTimeInMillis());

        Overtime overtime = new Overtime();
        overtime.setOvertimeStart(overtimeStart);
        overtime.setOvertimeEnd(overtimeEnd);
        overtime.setOvertimeReason(reason);
        overtime.setApprovalStatusID(3); // Always pending
        overtime.setEmployeeID(employeeID);

        try {
            overtimeService.addOvertime(overtime);
            // Reset initial state after successful submit
            initialDate = getDateOrNull(startDateCalendar);
            initialHours = (int) overtimeHoursJSpinner.getValue();
            initialReason = overtimeReasonTextArea.getText();
            formDirty = false;
            JOptionPane.showMessageDialog(this, "Overtime request submitted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            return true;
        } catch (Exception ex) {
            showWarning("Failed to submit request: " + ex.getMessage());
            return false;
        }
    }

    // Util: strip time from date
    private Date stripTime(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    private void showWarning(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Warning", JOptionPane.WARNING_MESSAGE);
    }
}