package ui.base;

import com.toedter.calendar.JDateChooser;
import service.EmployeeService;
import util.LightButton;
import util.BlueButton;
import util.SessionManager;
import db.DatabaseConnection;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.List;
import java.util.Date;

public abstract class AbstractEmployeeUpdatePage extends AbstractEmployeeRegisterPage {
    protected final EmployeeService empSvc = new EmployeeService();

    protected final int selectedEmployeeID = SessionManager.getSelectedEmployeeID();
    private String userID;
    private boolean loading = false;         // Flag to suppress events during load
    private JDateChooser updateCalendar;     // Changed: Now JDateChooser instead of JCalendar
    private Date initialBirthDate;           // To track original date for isDirty()
    private JTextField[] trackedFields;      // For isDirty()
    private String[] initialFieldValues;     // To track original values for isDirty()
    private JComboBox<?>[] trackedCombos;
    private int[] initialComboIndexes;

    /** Call *after* initComponents() in your subclass */
    protected void setupUpdatePage(
        JTextField ln, JTextField fn, JDateChooser dc,
        JTextField prov, JTextField city, JTextField brgy,
        JTextField street, JTextField house, JTextField zip,
        JTextField phone, JTextField sss, JTextField phil,
        JTextField tin, JTextField pagibig,
        JComboBox<String> roleC, JComboBox<String> statusC,
        JComboBox<String> posC, JComboBox<String> deptC,
        JComboBox<String> supC, JComboBox<String> salC,
        LightButton backB, LightButton cancelB, BlueButton confirmB
    ) {
        // Set reference for date chooser (now JDateChooser)
        this.updateCalendar = dc;

        // Call base registration wiring (for validation and input filtering)
        super.setupRegisterPage(
            ln, fn, dc,
            prov, city, brgy,
            street, house, zip,
            phone, sss, phil,
            tin, pagibig,
            roleC, statusC,
            posC, deptC,
            supC, salC,
            backB, cancelB, confirmB
        );

        // Remove all previous listeners from confirm (we want update logic only)
        for (ActionListener al : confirmB.getActionListeners())
            confirmB.removeActionListener(al);

        salC.setEnabled(false);
        confirmB.setText("Update");

        // --- Suppress dirty/enable tracking while loading data ---
        loading = true;
        var e = empSvc.getEmployeeByID(selectedEmployeeID);
        if (e == null) {
            JOptionPane.showMessageDialog(this,
                "Employee record not found.", "Error", JOptionPane.ERROR_MESSAGE);
            confirmB.setEnabled(false);
            return;
        }
        this.userID = e.getUserID();

        ln.setText(e.getLastName());
        fn.setText(e.getFirstName());

        // Set DOB on JDateChooser safely (don't trigger listener)
        initialBirthDate = e.getBirthDate();
        if (initialBirthDate != null) {
            dc.setDate(initialBirthDate);
        }

        prov.setText(e.getProvince());
        city.setText(e.getCity());
        brgy.setText(e.getBarangay());
        street.setText(e.getStreet());
        house.setText(e.getHouseNo());
        zip.setText(e.getZipCode() == null ? "" : e.getZipCode().toString());
        phone.setText(e.getPhoneNo());
        sss.setText(e.getSssNo());
        phil.setText(e.getPhilhealthNo());
        tin.setText(e.getTinNo());
        pagibig.setText(e.getPagibigNo());

        setComboByValue(roleC, fetchRoleIDForEmployee(e.getUserID()) - 1);
        setComboByValue(statusC, getIndexOfID(statusIds, e.getStatusID()));
        setComboByValue(posC,    getIndexOfID(positionIds, e.getPositionID()));
        setComboByValue(deptC,   getIndexOfID(departmentIds, e.getDepartmentID()));
        setComboByValue(supC,    getIndexOfID(supervisorIds, e.getSupervisorID()));

        // Store initial values for isDirty()
        trackedFields = new JTextField[] {
            ln, fn, prov, city, brgy, street, house, zip,
            phone, sss, phil, tin, pagibig
        };
        initialFieldValues = new String[trackedFields.length];
        for (int i = 0; i < trackedFields.length; i++) {
            initialFieldValues[i] = trackedFields[i].getText();
        }
        trackedCombos = new JComboBox<?>[] { roleC, statusC, posC, deptC, supC };
        initialComboIndexes = new int[trackedCombos.length];
        for (int i = 0; i < trackedCombos.length; i++) {
            initialComboIndexes[i] = trackedCombos[i].getSelectedIndex();
        }

        loading = false;

        // Button only enabled after a real edit
        confirmB.setEnabled(false);

        DocumentListener dl = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { if (!loading) confirmB.setEnabled(true); }
            public void removeUpdate(DocumentEvent e) { if (!loading) confirmB.setEnabled(true); }
            public void changedUpdate(DocumentEvent e) { if (!loading) confirmB.setEnabled(true); }
        };
        for (JTextField fld : trackedFields) {
            fld.getDocument().addDocumentListener(dl);
        }
        ActionListener comboListener = ev -> { if (!loading) confirmB.setEnabled(true); };
        for (var cb : trackedCombos)
            cb.addActionListener(comboListener);

        // Listen for JDateChooser changes
        dc.getDateEditor().addPropertyChangeListener("date", ev -> { if (!loading) confirmB.setEnabled(true); });

        // --- UPDATE ACTION LOGIC ---
        confirmB.addActionListener(ev -> {
            var errs = validateAll();
            if (!errs.isEmpty()) {
                JOptionPane.showMessageDialog(
                    this,
                    String.join("\n", errs),
                    "Please correct the following:",
                    JOptionPane.WARNING_MESSAGE
                );
                return;
            }
            if (JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to update this employee?",
                "Confirm",
                JOptionPane.YES_NO_OPTION
            ) != JOptionPane.YES_OPTION) return;
            boolean ok = doUpdate();
            if (ok) onUpdateSuccess();
        });
    }

    // Dirty check for cancel/back navigation
    protected boolean isDirty() {
        // Compare text fields
        for (int i = 0; i < trackedFields.length; i++) {
            if (!trackedFields[i].getText().equals(initialFieldValues[i]))
                return true;
        }
        // Compare combos
        for (int i = 0; i < trackedCombos.length; i++) {
            if (trackedCombos[i].getSelectedIndex() != initialComboIndexes[i])
                return true;
        }
        // Compare date chooser date
        if (!safeDatesEqual(updateCalendar.getDate(), initialBirthDate)) return true;
        return false;
    }

    // Helper: check if two dates are same day (ignoring time)
    private boolean safeDatesEqual(Date d1, Date d2) {
        if (d1 == d2) return true;
        if (d1 == null || d2 == null) return false;
        // Compare year, month, day only
        java.util.Calendar cal1 = java.util.Calendar.getInstance();
        java.util.Calendar cal2 = java.util.Calendar.getInstance();
        cal1.setTime(d1);
        cal2.setTime(d2);
        return  cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR)
            && cal1.get(java.util.Calendar.MONTH) == cal2.get(java.util.Calendar.MONTH)
            && cal1.get(java.util.Calendar.DAY_OF_MONTH) == cal2.get(java.util.Calendar.DAY_OF_MONTH);
    }

    private void setComboByValue(JComboBox<String> combo, int idx) {
        if (idx >= 0 && idx < combo.getItemCount()) combo.setSelectedIndex(idx);
    }
    private int getIndexOfID(List<Integer> ids, Integer id) {
        if (id == null) return -1;
        for (int i = 0; i < ids.size(); i++) if (ids.get(i).equals(id)) return i;
        return -1;
    }

    private boolean doUpdate() {
        try (Connection c = DatabaseConnection.getInstance().getConnection()) {
            c.setAutoCommit(false);

            try (PreparedStatement p = c.prepareStatement(
                "UPDATE authentication SET roleID=? WHERE userID=?"
            )) {
                p.setInt(1, roleCombo.getSelectedIndex() + 1);
                p.setString(2, userID);
                p.executeUpdate();
            }

            try (PreparedStatement p = c.prepareStatement(
                "UPDATE employee SET "
                    + "firstName=?, lastName=?, birthDate=?, phoneNo=?, email=?, "
                    + "statusID=?, positionID=?, departmentID=?, supervisorID=? "
                    + "WHERE employeeID=?"
            )) {
                String fn = firstNameField.getText().trim();
                String ln = lastNameField.getText().trim();
                Date selectedDate = updateCalendar.getDate();
                if (selectedDate == null) throw new Exception("Please select a birth date.");
                p.setString(1, fn);
                p.setString(2, ln);
                p.setDate(3, new java.sql.Date(selectedDate.getTime()));
                p.setString(4, phoneField.getText().trim());
                p.setString(5, makeEmail(fn, ln));
                p.setInt(6, statusIds.get(statusCombo.getSelectedIndex()));
                p.setInt(7, positionIds.get(positionCombo.getSelectedIndex()));
                p.setInt(8, departmentIds.get(departmentCombo.getSelectedIndex()));
                p.setInt(9, supervisorIds.get(supervisorCombo.getSelectedIndex()));
                p.setInt(10, selectedEmployeeID);
                p.executeUpdate();
            }

            try (PreparedStatement p = c.prepareStatement(
                "UPDATE address a "
                    + "JOIN employeeaddress ea ON a.addressID=ea.addressID AND ea.employeeID=? "
                    + "SET houseNo=?, street=?, barangay=?, city=?, province=?, zipCode=?"
            )) {
                p.setInt(1, selectedEmployeeID);
                p.setString(2, houseNoField.getText().trim());
                p.setString(3, streetField.getText().trim());
                p.setString(4, barangayField.getText().trim());
                p.setString(5, cityField.getText().trim());
                p.setString(6, provinceField.getText().trim());
                String rawZip = zipField.getText().trim();
                if (rawZip.isEmpty()) {
                    p.setNull(7, Types.INTEGER);
                } else {
                    p.setInt(7, Integer.parseInt(rawZip));
                }
                p.executeUpdate();
            }

            try (PreparedStatement p = c.prepareStatement(
                "UPDATE govid SET sss=?, philhealth=?, tin=?, pagibig=? WHERE employeeID=?"
            )) {
                p.setString(1, sssField.getText().trim());
                p.setString(2, philField.getText().trim());
                p.setString(3, tinField.getText().trim());
                p.setString(4, pagibigField.getText().trim());
                p.setInt(5, selectedEmployeeID);
                p.executeUpdate();
            }

            c.commit();
            JOptionPane.showMessageDialog(
                this,
                "Employee updated successfully!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE
            );
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                this,
                "Failed to update employee: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
    }

    protected abstract void onUpdateSuccess();

    private String makeEmail(String fn, String ln) {
        String firstInitial = fn.isEmpty() ? "" : fn.substring(0, 1).toLowerCase();
        String lastNoSpace = ln.replaceAll("\\s+", "").toLowerCase();
        return firstInitial + lastNoSpace + "@motor.ph";
    }

    private int fetchRoleIDForEmployee(String userID) {
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement p = c.prepareStatement("SELECT roleID FROM authentication WHERE userID=?")) {
            p.setString(1, userID);
            try (var rs = p.executeQuery()) {
                if (rs.next()) return rs.getInt("roleID");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 1; // default to Employee role
    }
}