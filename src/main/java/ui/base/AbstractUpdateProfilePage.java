package ui.base;

import pojo.Employee;
import service.EmployeeService;
import util.SessionManager;
import db.DatabaseConnection;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.function.Predicate;

public abstract class AbstractUpdateProfilePage extends JFrame {
    protected EmployeeService employeeService;
    protected Employee currentEmployee;

    private boolean dirty = false;
    private JButton updateButton, cancelButton, backButton;
    private Runnable onCancelOrBack;

    public AbstractUpdateProfilePage() {
        this.employeeService = new EmployeeService();
        int empID = SessionManager.getEmployeeID();
        currentEmployee = employeeService.getEmployeeByID(empID);
        if (currentEmployee == null) {
            JOptionPane.showMessageDialog(
                this,
                "Employee record not found.",
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
            dispose();
        }
    }

    /**
     * Populate all fields after initComponents() and subclass wiring.
     */
    protected void initializeProfileUpdatePage() {
        getEmployeeIDText().setText(String.valueOf(currentEmployee.getEmployeeID()));
        getPositionText().setText(currentEmployee.getPosition());
        getSupervisorText().setText(currentEmployee.getSupervisorName());
        getLastNameText().setText(currentEmployee.getLastName());
        getFirstNameText().setText(currentEmployee.getFirstName());
        getBirthdayText().setText(currentEmployee.getBirthDate().toString());

        getPhoneNumberField().setText(currentEmployee.getPhoneNo());
        getHouseNoField().setText(currentEmployee.getHouseNo());
        getStreetField().setText(currentEmployee.getStreet());
        getBarangayField().setText(currentEmployee.getBarangay());
        getCityField().setText(currentEmployee.getCity());
        getProvinceField().setText(currentEmployee.getProvince());
        getZipCodeField().setText(
            currentEmployee.getZipCode() != null
                ? currentEmployee.getZipCode().toString()
                : ""
        );
    }

    /**
     * Wire the three buttons (Update, Cancel, Back) and the "onCancelOrBack" action.
     * Also initially disables Update until a field changes.
     */
    protected void setButtons(
        JButton updateBtn,
        JButton cancelBtn,
        JButton backBtn,
        Runnable onCancelOrBack
    ) {
        this.updateButton   = updateBtn;
        this.cancelButton   = cancelBtn;
        this.backButton     = backBtn;
        this.onCancelOrBack = onCancelOrBack;

        updateButton.setEnabled(false);
        cancelButton.addActionListener(e -> confirmCancelOrBack());
        backButton  .addActionListener(e -> confirmCancelOrBack());
        updateButton.addActionListener(e -> handleUpdateProfile());
    }

    private void confirmCancelOrBack() {
        if (dirty) {
            int res = JOptionPane.showConfirmDialog(
                this,
                "You have unsaved changes. Discard?",
                "Confirm",
                JOptionPane.YES_NO_OPTION
            );
            if (res == JOptionPane.YES_OPTION) {
                onCancelOrBack.run();
            }
        } else {
            onCancelOrBack.run();
        }
    }

    /**
     * Generic "non-empty" validators for all address parts except ZIP (ZIP has its own formatter).
     */
    protected void attachValidationListeners() {
        addValidator(getHouseNoField(),  t -> !t.trim().isEmpty(), "House No. required");
        addValidator(getStreetField(),   t -> !t.trim().isEmpty(), "Street required");
        addValidator(getBarangayField(), t -> !t.trim().isEmpty(), "Barangay required");
        addValidator(getCityField(),     t -> !t.trim().isEmpty(), "City required");
        addValidator(getProvinceField(), t -> !t.trim().isEmpty(), "Province required");
    }

    private void addValidator(
        JTextField field,
        Predicate<String> validator,
        String tooltip
    ) {
        field.setToolTipText(tooltip);
        field.getDocument().addDocumentListener(new DocumentListener() {
            private void check() {
                field.setBackground(
                    validator.test(field.getText())
                      ? Color.WHITE
                      : new Color(255,230,230)
                );
            }
            @Override public void insertUpdate(DocumentEvent e)  { check(); markDirty(); }
            @Override public void removeUpdate(DocumentEvent e)  { check(); markDirty(); }
            @Override public void changedUpdate(DocumentEvent e) { check(); markDirty(); }
        });
    }

    /**
     * Phone # formatter: strips non-digits, caps at 9 digits, inserts hyphens as ###-###-###.
     */
    protected void attachPhoneFormatter(JTextField phoneField) {
        phoneField.getDocument().addDocumentListener(new DocumentListener() {
            private boolean formatting = false;

            private void formatAndValidate() {
                if (formatting) return;

                // 1) strip non-digits
                String raw = phoneField.getText().replaceAll("\\D", "");
                // 2) drop to max 9
                if (raw.length() > 9) raw = raw.substring(0, 9);

                // 3) build stepwise
                String formatted;
                int len = raw.length();
                if (len >= 7) {
                    formatted = raw.substring(0,3)
                              + "-" + raw.substring(3,6)
                              + "-" + raw.substring(6);
                } else if (len >= 4) {
                    formatted = raw.substring(0,3)
                              + "-" + raw.substring(3);
                } else {
                    formatted = raw;
                }

                boolean valid = raw.isEmpty() || raw.length() == 9;

                SwingUtilities.invokeLater(() -> {
                    formatting = true;
                    phoneField.setText(formatted);
                    phoneField.setBackground(valid
                        ? Color.WHITE
                        : new Color(255,230,230));
                    formatting = false;
                });
                markDirty();
            }
            @Override public void insertUpdate(DocumentEvent e)  { formatAndValidate(); }
            @Override public void removeUpdate(DocumentEvent e)  { formatAndValidate(); }
            @Override public void changedUpdate(DocumentEvent e) { formatAndValidate(); }
        });
    }

    /**
     * ZIP formatter: strips non-digits, caps at 4 digits, leaves red background
     * unless exactly 4 or empty.
     */
    protected void attachZipFormatter(JTextField zipField) {
        zipField.getDocument().addDocumentListener(new DocumentListener() {
            private boolean formatting = false;

            private void formatAndValidate() {
                if (formatting) return;

                // strip non-digits
                String digits = zipField.getText().replaceAll("\\D", "");
                // cap at 4
                if (digits.length() > 4) {
                    digits = digits.substring(0, 4);
                }

                boolean valid = digits.isEmpty() || digits.length() == 4;
                final String text = digits;

                SwingUtilities.invokeLater(() -> {
                    formatting = true;
                    zipField.setText(text);
                    zipField.setBackground(valid
                        ? Color.WHITE
                        : new Color(255,230,230));
                    formatting = false;
                });
                markDirty();
            }

            @Override public void insertUpdate(DocumentEvent e)  { formatAndValidate(); }
            @Override public void removeUpdate(DocumentEvent e)  { formatAndValidate(); }
            @Override public void changedUpdate(DocumentEvent e) { formatAndValidate(); }
        });
    }

    /**
     * Any change in any of these JTextComponents sets the page “dirty” and unlocks Update.
     */
    protected void attachDirtyListeners(JTextComponent... fields) {
        DocumentListener dl = new DocumentListener() {
            private void dirt() { markDirty(); }
            @Override public void insertUpdate(DocumentEvent e)  { dirt(); }
            @Override public void removeUpdate(DocumentEvent e)  { dirt(); }
            @Override public void changedUpdate(DocumentEvent e) { dirt(); }
        };
        for (JTextComponent f : fields) {
            f.getDocument().addDocumentListener(dl);
        }
    }

    private void markDirty() {
        if (!dirty) {
            dirty = true;
            updateButton.setEnabled(true);
        }
    }

    /**
     * Final full‐form validation before writing to DB.
     */
    protected boolean validateProfileFieldsAndShowErrors() {
        boolean valid = true;
        String phoneRaw = getPhoneNumberField().getText().replaceAll("\\D", "");
        if (phoneRaw.length() != 9)           { highlight(getPhoneNumberField());  valid = false; }
        if (getHouseNoField().getText().trim().isEmpty())  { highlight(getHouseNoField());     valid = false; }
        if (getStreetField().getText().trim().isEmpty())   { highlight(getStreetField());      valid = false; }
        if (getBarangayField().getText().trim().isEmpty()) { highlight(getBarangayField());    valid = false; }
        if (getCityField().getText().trim().isEmpty())     { highlight(getCityField());        valid = false; }
        if (getProvinceField().getText().trim().isEmpty()) { highlight(getProvinceField());    valid = false; }
        if (!getZipCodeField().getText().matches("\\d{4}")){ highlight(getZipCodeField());     valid = false; }

        if (!valid) {
            JOptionPane.showMessageDialog(
                this,
                "Please correct the highlighted fields.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE
            );
        }
        return valid;
    }

    private void highlight(JTextField f) {
        f.setBackground(new Color(255,204,204));
    }

    /**
     * On Update click: validate, confirm, persist both employee & address.
     */
    protected void handleUpdateProfile() {
        if (!validateProfileFieldsAndShowErrors()) {
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to update your profile?",
            "Confirm Update",
            JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        // flush into POJO
        currentEmployee.setPhoneNo( getPhoneNumberField().getText().trim() );
        currentEmployee.setHouseNo( getHouseNoField().getText().trim() );
        currentEmployee.setStreet(  getStreetField().getText().trim() );
        currentEmployee.setBarangay(getBarangayField().getText().trim() );
        currentEmployee.setCity(    getCityField().getText().trim() );
        currentEmployee.setProvince(getProvinceField().getText().trim() );
        currentEmployee.setZipCode( Integer.valueOf(getZipCodeField().getText().trim()) );

        // update employee table (phoneNo)
        employeeService.updateEmployee(currentEmployee);

        // update address via join
        String sql =
            "UPDATE address a " +
            "  JOIN employeeaddress ea ON a.addressID = ea.addressID " +
            "SET a.houseNo = ?, a.street = ?, a.barangay = ?, a.city = ?, a.province = ?, a.zipCode = ? " +
            "WHERE ea.employeeID = ?";

        Connection conn = DatabaseConnection.getInstance().getConnection();
        PreparedStatement ps  = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, currentEmployee.getHouseNo());
            ps.setString(2, currentEmployee.getStreet());
            ps.setString(3, currentEmployee.getBarangay());
            ps.setString(4, currentEmployee.getCity());
            ps.setString(5, currentEmployee.getProvince());
            ps.setInt(   6, currentEmployee.getZipCode());
            ps.setInt(   7, currentEmployee.getEmployeeID());
            ps.executeUpdate();

            JOptionPane.showMessageDialog(
                this,
                "Profile updated successfully.",
                "Success",
                JOptionPane.INFORMATION_MESSAGE
            );

            dirty = false;
            updateButton.setEnabled(false);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                this,
                "Failed to update address: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        } finally {
            if (ps != null) try { ps.close(); } catch (SQLException ignored) {}
            // **do not close** the shared connection here
        }
    }

    // --- Subclasses must supply these component getters ---
    protected abstract JLabel     getEmployeeIDText();
    protected abstract JLabel     getPositionText();
    protected abstract JLabel     getSupervisorText();
    protected abstract JLabel     getLastNameText();
    protected abstract JLabel     getFirstNameText();
    protected abstract JLabel     getBirthdayText();

    protected abstract JTextField getPhoneNumberField();
    protected abstract JTextField getHouseNoField();
    protected abstract JTextField getStreetField();
    protected abstract JTextField getBarangayField();
    protected abstract JTextField getCityField();
    protected abstract JTextField getProvinceField();
    protected abstract JTextField getZipCodeField();
}