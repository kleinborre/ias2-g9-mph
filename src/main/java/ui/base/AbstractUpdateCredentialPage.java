package ui.base;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import java.awt.Color;
import java.util.Arrays;
import pojo.User;
import pojo.Employee;
import service.UserService;
import service.EmployeeService;
import util.SessionManager;

public abstract class AbstractUpdateCredentialPage extends JFrame {
    protected UserService userService;
    protected EmployeeService employeeService;
    protected User currentUser;
    protected Employee currentEmployee;

    protected JPasswordField passwordCurrentField;
    protected JPasswordField passwordNewField;
    protected JPasswordField passwordReEnterField;
    protected JLabel errorMessageLabel;

    private boolean isDirty        = false;
    private boolean suppressDirty  = false;   // ← new flag
    private JButton updateButton, cancelButton, backButton;
    private Runnable onCancelOrBack;

    public AbstractUpdateCredentialPage() {
        this.userService     = new UserService();
        this.employeeService = new EmployeeService();
        String userID = SessionManager.getUserID();
        if (userID == null || userID.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Error: No user session found.",
                "Session Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }
        currentUser     = userService.getUserByUserID(userID);
        currentEmployee = employeeService.getEmployeeByID(SessionManager.getEmployeeID());
        if (currentUser == null || currentEmployee == null) {
            JOptionPane.showMessageDialog(this,
                "Error: Could not load your account data.",
                "Data Error", JOptionPane.ERROR_MESSAGE);
            dispose();
        }
    }

    protected void initializeUpdateCredentialPage() {
        getUsernameText().setText(currentUser.getUsername());
        getEmailText   ().setText(currentUser.getEmail());
        getEmployeeIDText().setText(String.valueOf(currentEmployee.getEmployeeID()));
        getPositionText   ().setText(currentEmployee.getPosition());
        getSupervisorText().setText(currentEmployee.getSupervisorName());
    }

    protected void setButtons(
        JButton updateBtn, JButton cancelBtn, JButton backBtn, Runnable onCancelOrBack
    ) {
        this.updateButton   = updateBtn;
        this.cancelButton   = cancelBtn;
        this.backButton     = backBtn;
        this.onCancelOrBack = onCancelOrBack;

        updateButton.setEnabled(false);
        cancelButton.addActionListener(e -> confirmCancelOrBack());
        backButton  .addActionListener(e -> confirmCancelOrBack());
        updateButton.addActionListener(e -> handleUpdate());
    }

    private void confirmCancelOrBack() {
        if (isDirty) {
            int r = JOptionPane.showConfirmDialog(
                this, "You have unsaved changes. Discard?", "Confirm", JOptionPane.YES_NO_OPTION
            );
            if (r == JOptionPane.YES_OPTION) onCancelOrBack.run();
        } else {
            onCancelOrBack.run();
        }
    }

    protected void attachDirtyListeners(JTextComponent... fields) {
        DocumentListener dl = new DocumentListener() {
            private void mark() {
                if (suppressDirty) return;       // ← honor our suppress flag
                if (!isDirty) {
                    isDirty = true;
                    updateButton.setEnabled(true);
                }
            }
            public void insertUpdate(DocumentEvent e)  { mark(); }
            public void removeUpdate(DocumentEvent e)  { mark(); }
            public void changedUpdate(DocumentEvent e) { mark(); }
        };
        Arrays.stream(fields)
              .forEach(f -> f.getDocument().addDocumentListener(dl));
    }

    protected void setPasswordFieldsAndErrorLabel(
        JPasswordField current, JPasswordField next, JPasswordField reenter, JLabel errorLabel
    ) {
        this.passwordCurrentField = current;
        this.passwordNewField     = next;
        this.passwordReEnterField = reenter;
        this.errorMessageLabel    = errorLabel;
    }

    protected boolean validateCurrentPassword() {
        return Arrays.equals(
            passwordCurrentField.getPassword(),
            currentUser.getPassword().toCharArray()
        );
    }

    protected boolean validateNewPasswordStrength(String pw) {
        // at least 1 lower, 1 upper, 1 digit, 1 non‐alphanumeric, min 8 chars
        return pw.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$");
    }

    protected void showError(String msg) {
        errorMessageLabel.setForeground(Color.RED);
        errorMessageLabel.setText(msg);
    }

    protected void clearError() {
        errorMessageLabel.setText("");
    }

    protected void highlightField(JPasswordField f) {
        f.setBackground(new Color(0xFFCCCC));
    }

    protected void resetFieldBackground(JPasswordField f) {
        f.setBackground(Color.WHITE);
    }

    protected boolean validateFieldsAndShowErrors() {
        // skip all validation once we've just updated (isDirty==false)
        if (!isDirty) {
            clearError();
            resetFieldBackground(passwordCurrentField);
            resetFieldBackground(passwordNewField);
            resetFieldBackground(passwordReEnterField);
            return true;
        }

        clearError();
        resetFieldBackground(passwordCurrentField);
        resetFieldBackground(passwordNewField);
        resetFieldBackground(passwordReEnterField);

        String cur = new String(passwordCurrentField.getPassword());
        String nw  = new String(passwordNewField.getPassword());
        String re  = new String(passwordReEnterField.getPassword());

        if (cur.isEmpty()) {
            showError("Current password is required.");
            highlightField(passwordCurrentField);
            return false;
        }
        if (!validateCurrentPassword()) {
            showError("Current password is incorrect.");
            highlightField(passwordCurrentField);
            return false;
        }
        if (nw.isEmpty()) {
            showError("New password is required.");
            highlightField(passwordNewField);
            return false;
        }
        if (nw.equals(cur)) {
            showError("New password must differ from current.");
            highlightField(passwordNewField);
            return false;
        }
        if (nw.contains(cur)) {
            showError("New password must not contain your current password.");
            highlightField(passwordNewField);
            return false;
        }
        if (!validateNewPasswordStrength(nw)) {
            showError("New password too weak.");
            highlightField(passwordNewField);
            return false;
        }
        if (re.isEmpty()) {
            showError("Please re-enter new password.");
            highlightField(passwordReEnterField);
            return false;
        }
        if (!nw.equals(re)) {
            showError("Passwords do not match.");
            highlightField(passwordNewField);
            highlightField(passwordReEnterField);
            return false;
        }
        return true;
    }

    protected void handleUpdate() {
        if (!validateFieldsAndShowErrors()) return;

        int r = JOptionPane.showConfirmDialog(
            this, "Really update your password?", "Confirm Update", JOptionPane.YES_NO_OPTION
        );
        if (r != JOptionPane.YES_OPTION) return;

        String newPw = new String(passwordNewField.getPassword());
        currentUser.setPassword(newPw);

        try {
            userService.updateUser(currentUser);
            JOptionPane.showMessageDialog(this, "Password updated!");

            // ─── CLEAR & RESET ───
            suppressDirty = true;   // ← turn off dirty‐marking
            passwordCurrentField.setText("");
            passwordNewField.setText("");
            passwordReEnterField.setText("");
            suppressDirty = false;  // ← turn it back on

            clearError();
            resetFieldBackground(passwordCurrentField);
            resetFieldBackground(passwordNewField);
            resetFieldBackground(passwordReEnterField);

            isDirty = false;
            updateButton.setEnabled(false);
        } catch (RuntimeException ex) {
            showError("Failed to update: " + ex.getCause().getMessage());
        }
    }

    // subclasses must supply these:
    protected abstract JLabel getUsernameText();
    protected abstract JLabel getEmailText();
    protected abstract JLabel getEmployeeIDText();
    protected abstract JLabel getPositionText();
    protected abstract JLabel getSupervisorText();
}