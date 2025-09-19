package ui.base;

import com.toedter.calendar.JDateChooser;
import pojo.Overtime;
import service.OvertimeService;
import util.SessionManager;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

public abstract class AbstractOvertimeRequestUpdatePage extends AbstractOvertimeRequestPage {

    protected Overtime currentOvertime;
    private boolean listenersInitialized = false;

    // Call after initComponents/setComponentReferences in your UI, passing the selected Overtime POJO
    protected void setOvertimeData(Overtime overtime) {
        if (overtime == null) {
            showWarning("No overtime request selected.");
            return;
        }
        this.currentOvertime = overtime;
        this.employeeID = overtime.getEmployeeID();

        Date startDate = stripTime(new Date(overtime.getOvertimeStart().getTime()));
        int hours = getOvertimeHours(overtime.getOvertimeStart(), overtime.getOvertimeEnd());
        String reason = overtime.getOvertimeReason();

        startDateCalendar.setDate(startDate);
        overtimeHoursJSpinner.setValue(hours);
        overtimeReasonTextArea.setText(reason);

        setInitialValues(startDate, hours, reason);
        formDirty = false;

        validateAndLockDate();
        validateForm();

        // Only attach listeners once per page instance
        if (!listenersInitialized) {
            attachLiveListeners();
            listenersInitialized = true;
        }
    }

    private void attachLiveListeners() {
        startDateCalendar.getDateEditor().addPropertyChangeListener(evt -> {
            if ("date".equals(evt.getPropertyName())) {
                validateAndLockDate();
                updateFormDirty();
                validateForm();
            }
        });

        overtimeHoursJSpinner.addChangeListener(e -> {
            updateFormDirty();
            validateForm();
        });

        overtimeReasonTextArea.getDocument().addDocumentListener(new DocumentListener() {
            void checkLenAndDirty() {
                SwingUtilities.invokeLater(() -> {
                    String text = overtimeReasonTextArea.getText();
                    if (text.length() > 255) overtimeReasonTextArea.setText(text.substring(0, 255));
                    updateFormDirty();
                    validateForm();
                });
            }
            public void insertUpdate(DocumentEvent e) { checkLenAndDirty(); }
            public void removeUpdate(DocumentEvent e) { checkLenAndDirty(); }
            public void changedUpdate(DocumentEvent e) { checkLenAndDirty(); }
        });
    }

    protected void updateFormDirty() {
        Date currentDate = getDateOrNull(startDateCalendar);
        int currentHours = (int) overtimeHoursJSpinner.getValue();
        String currentReason = overtimeReasonTextArea.getText();

        boolean changed = !equalsDate(currentDate, getInitialDate())
                || currentHours != getInitialHours()
                || !currentReason.equals(getInitialReason());
        formDirty = changed;
    }

    @Override
    protected void validateForm() {
        boolean valid =
                startDateCalendar.getDate() != null &&
                overtimeHoursJSpinner.isEnabled() &&
                ((int)overtimeHoursJSpinner.getValue() >= 1) &&
                overtimeReasonTextArea.getText().trim().length() > 0 &&
                overtimeReasonTextArea.getText().trim().length() <= 255;
        submitButton.setEnabled(valid && formDirty);
    }

    protected void validateAndLockDate() {
        Date picked = getDateOrNull(startDateCalendar);
        Date today = stripTime(new Date());
        if (picked != null) {
            Date pickedDay = stripTime(picked);
            long diff = (pickedDay.getTime() - today.getTime()) / (1000 * 60 * 60 * 24);

            if (pickedDay.before(today)) {
                showWarning("You cannot book overtime for past dates.");
                startDateCalendar.setDate(getInitialDate());
                overtimeHoursJSpinner.setEnabled(false);
                return;
            }
            if (diff > 14) {
                showWarning("You cannot book overtime more than 2 weeks in advance.");
                startDateCalendar.setDate(getInitialDate());
                overtimeHoursJSpinner.setEnabled(false);
                return;
            }
            overtimeHoursJSpinner.setEnabled(true);
        } else {
            overtimeHoursJSpinner.setEnabled(false);
        }
    }

    // Call this in your UI's Submit/Update button
    protected boolean tryUpdateRequest() {
        if (currentOvertime == null) {
            showWarning("No overtime request loaded.");
            return false;
        }
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
                "Are you sure you want to update this overtime request?",
                "Confirm Update", JOptionPane.YES_NO_OPTION);
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

        currentOvertime.setOvertimeStart(overtimeStart);
        currentOvertime.setOvertimeEnd(overtimeEnd);
        currentOvertime.setOvertimeReason(reason);
        currentOvertime.setApprovalStatusID(3); // Pending

        try {
            OvertimeService overtimeService = new OvertimeService();
            overtimeService.updateOvertime(currentOvertime);

            setInitialValues(getDateOrNull(startDateCalendar), hours, reason);
            formDirty = false;

            JOptionPane.showMessageDialog(this, "Overtime request updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            validateForm(); // disables submit button after update
            return true;
        } catch (Exception ex) {
            showWarning("Failed to update request: " + ex.getMessage());
            return false;
        }
    }

    // --- Helper methods as before (not changed) ---

    private void setInitialValues(Date startDate, int hours, String reason) {
        try {
            java.lang.reflect.Field f1 = AbstractOvertimeRequestPage.class.getDeclaredField("initialDate");
            java.lang.reflect.Field f2 = AbstractOvertimeRequestPage.class.getDeclaredField("initialHours");
            java.lang.reflect.Field f3 = AbstractOvertimeRequestPage.class.getDeclaredField("initialReason");
            f1.setAccessible(true);
            f2.setAccessible(true);
            f3.setAccessible(true);
            f1.set(this, startDate);
            f2.set(this, hours);
            f3.set(this, reason);
        } catch (Exception e) {
            // fallback: do nothing
        }
    }
    private Date getInitialDate() {
        try {
            java.lang.reflect.Field f = AbstractOvertimeRequestPage.class.getDeclaredField("initialDate");
            f.setAccessible(true);
            return (Date) f.get(this);
        } catch (Exception e) { return null; }
    }
    private int getInitialHours() {
        try {
            java.lang.reflect.Field f = AbstractOvertimeRequestPage.class.getDeclaredField("initialHours");
            f.setAccessible(true);
            return f.getInt(this);
        } catch (Exception e) { return 1; }
    }
    private String getInitialReason() {
        try {
            java.lang.reflect.Field f = AbstractOvertimeRequestPage.class.getDeclaredField("initialReason");
            f.setAccessible(true);
            return (String) f.get(this);
        } catch (Exception e) { return ""; }
    }

    private int getOvertimeHours(Timestamp start, Timestamp end) {
        long ms = end.getTime() - start.getTime();
        int hours = (int) (ms / (1000 * 60 * 60));
        return Math.max(1, Math.min(hours, 4));
    }

    private boolean equalsDate(Date a, Date b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.getTime() == b.getTime();
    }

    private Date getDateOrNull(JDateChooser chooser) {
        Date d = chooser.getDate();
        return (d != null) ? stripTime(d) : null;
    }

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

    protected int getSessionEmployeeID() {
        return SessionManager.getEmployeeID();
    }
    protected String getSessionUserID() {
        return SessionManager.getUserID();
    }
}