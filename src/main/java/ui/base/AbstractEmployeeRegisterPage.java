package ui.base;

import com.toedter.calendar.JDateChooser;
import service.EmployeeService;
import service.UserService;
import util.LightButton;
import util.BlueButton;
import util.SessionManager;
import db.DatabaseConnection;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

public abstract class AbstractEmployeeRegisterPage extends JFrame {
  // UI components (protected for subclass access)
  protected JTextField lastNameField, firstNameField;
  protected JDateChooser dobCal;  // Changed from JCalendar to JDateChooser
  protected JTextField provinceField, cityField, barangayField,
                       streetField, houseNoField, zipField;
  protected JTextField phoneField, sssField, philField, tinField, pagibigField;
  protected JComboBox<String> roleCombo, statusCombo,
                              positionCombo, departmentCombo,
                              supervisorCombo, salaryCombo;
  protected LightButton backButton, cancelButton;
  protected BlueButton  confirmButton;

  // Data for dropdowns (protected for subclass access)
  protected List<Integer> statusIds, positionIds, departmentIds, supervisorIds;
  private static final String[] ROLE_NAMES = {
    "Employee", "HR", "IT", "Finance", "Manager"
  };

  private final EmployeeService empSvc = new EmployeeService();
  private final UserService     usrSvc = new UserService();
  private final List<JTextField> allFields = new ArrayList<>();
  private boolean dirty = false;

  /**
   * Subclass calls this after initComponents().
   * Subclass is responsible for wiring up navigation.
   */
  protected void setupRegisterPage(
    JTextField ln, JTextField fn, JDateChooser dc,    // Now uses JDateChooser!
    JTextField prov, JTextField city, JTextField brgy,
    JTextField street, JTextField house, JTextField zip,
    JTextField phone, JTextField sss, JTextField phil,
    JTextField tin, JTextField pagibig,
    JComboBox<String> roleC, JComboBox<String> statusC,
    JComboBox<String> posC, JComboBox<String> deptC,
    JComboBox<String> supC, JComboBox<String> salC,
    LightButton backB, LightButton cancelB, BlueButton confirmB
  ) {
    this.lastNameField   = ln;
    this.firstNameField  = fn;
    this.dobCal          = dc;     // JDateChooser assigned
    this.provinceField   = prov;
    this.cityField       = city;
    this.barangayField   = brgy;
    this.streetField     = street;
    this.houseNoField    = house;
    this.zipField        = zip;
    this.phoneField      = phone;
    this.sssField        = sss;
    this.philField       = phil;
    this.tinField        = tin;
    this.pagibigField    = pagibig;
    this.roleCombo       = roleC;
    this.statusCombo     = statusC;
    this.positionCombo   = posC;
    this.departmentCombo = deptC;
    this.supervisorCombo = supC;
    this.salaryCombo     = salC;
    this.backButton      = backB;
    this.cancelButton    = cancelB;
    this.confirmButton   = confirmB;

    // Populate combos
    roleC.setModel(new DefaultComboBoxModel<>(ROLE_NAMES));

    statusIds = empSvc.getAllStatusIDs();
    statusC.setModel(new DefaultComboBoxModel<>(
      empSvc.getAllStatusTypes().toArray(new String[0])
    ));

    positionIds = empSvc.getAllPositionIDs();
    posC.setModel(new DefaultComboBoxModel<>(
      empSvc.getAllPositionNames().toArray(new String[0])
    ));

    departmentIds = empSvc.getAllDepartmentIDs();
    deptC.setModel(new DefaultComboBoxModel<>(
      empSvc.getAllDepartmentNames().toArray(new String[0])
    ));

    var emps = empSvc.getAllEmployees();
    supervisorIds = new ArrayList<>();
    var supNames = new Vector<String>();
    for (var e : emps) {
      supervisorIds.add(e.getEmployeeID());
      supNames.add(e.getLastName() + ", " + e.getFirstName());
    }
    supC.setModel(new DefaultComboBoxModel<>(supNames));

    // Auto-sync department when position changes
    posC.addActionListener(e -> {
      int idx = posC.getSelectedIndex();
      if (idx < 0) return;
      int posID = positionIds.get(idx);
      int dept  = empSvc.getDepartmentIDForPosition(posID);
      for (int i = 0; i < deptC.getItemCount(); i++) {
        if (departmentIds.get(i) == dept) {
          deptC.setSelectedIndex(i);
          break;
        }
      }
    });

    // Filters & max lengths
    Pattern alpha    = Pattern.compile("[a-zA-Z ]*");
    Pattern alphanum = Pattern.compile("[a-zA-Z0-9 .,#\\-]*");

    installFilter(ln,     alpha,    25);
    installFilter(fn,     alpha,    35);
    installFilter(prov,   alpha,    25);
    installFilter(city,   alpha,    25);
    installFilter(brgy,   alphanum, 25);
    installFilter(street, alphanum, 25);
    installFilter(house,  alphanum, 25);
    installFilter(zip,    Pattern.compile("\\d{0,4}"), 4);

    installFilter(phone,  Pattern.compile("[0-9\\-]*"), 11);
    installFilter(sss,    Pattern.compile("[0-9\\-]*"), 12);
    installFilter(phil,   Pattern.compile("\\d*"),      12);
    installFilter(tin,    Pattern.compile("[0-9\\-]*"), 15);
    installFilter(pagibig,Pattern.compile("\\d*"),      12);

    // Min-length highlights
    installMinValidator(ln,    2);
    installMinValidator(fn,    2);
    installMinValidator(prov,  4);
    installMinValidator(city,  4);
    installMinValidator(brgy,  4);
    installMinValidator(street,4);
    installMinValidator(zip,   4);

    // Digit-count highlights
    installDigitHighlighter(sss, 10);
    installDigitHighlighter(tin,  9);
    installPhoneHighlighter(phone);
    installDigitHighlighter(phil, 12);   // PhilHealth needs 12 digits
    installDigitHighlighter(pagibig, 12); // Pag-IBIG needs 12 digits

    // Auto-formatters
    installFormatter(sss,   this::formatSSS);
    installFormatter(tin,   this::formatTIN);
    installFormatter(phone, this::formatPhone);

    // Dirty tracking
    allFields.addAll(List.of(
      ln, fn, prov, city, brgy, street, house, zip,
      phone, sss, phil, tin, pagibig
    ));
    DocumentListener dl = new DocumentListener() {
      public void insertUpdate(DocumentEvent e){fieldBecameDirty();}
      public void removeUpdate(DocumentEvent e){fieldBecameDirty();}
      public void changedUpdate(DocumentEvent e){fieldBecameDirty();}
    };
    allFields.forEach(f ->
      ((AbstractDocument)f.getDocument()).addDocumentListener(dl)
    );
    ActionListener markDirty = ev->fieldBecameDirty();
    for (var c : List.of(roleC,statusC,posC,deptC,supC,salC))
      c.addActionListener(markDirty);

    // Listen for changes to the JDateChooser (instead of JCalendar)
    dc.getDateEditor().addPropertyChangeListener("date", e -> fieldBecameDirty());

    // Confirm button starts locked until something is modified
    confirmButton.setEnabled(false);

    // Confirm action: still handled here so validation & DB logic stays modular.
    confirmButton.addActionListener(ev -> {
      List<String> errors = validateAll();
      if (!errors.isEmpty()) {
        JOptionPane.showMessageDialog(
          this,
          String.join("\n", errors),
          "Please correct the following:",
          JOptionPane.WARNING_MESSAGE
        );
        return;
      }
      if (JOptionPane.showConfirmDialog(
           this,
           "Are you sure you want to create this new employee?",
           "Confirm",
           JOptionPane.YES_NO_OPTION
         ) != JOptionPane.YES_OPTION) return;
      doRegister();
    });

    // Navigation for back/cancel is now responsibility of the UI class!
    // (No setVisible or dispose here)
  }

  /** Called whenever any field changes: unlocks confirmButton. */
  private void fieldBecameDirty() {
    dirty = true;
    if (confirmButton != null && !confirmButton.isEnabled())
      confirmButton.setEnabled(true);
  }

  /** Subclass can call this to ask if unsaved changes are present. */
  protected boolean isDirty() {
    return dirty;
  }

  /**
   * Registers new employee into all necessary tables.
   * This handles only data/DB logic.
   * UI subclass decides navigation.
   */
  private void doRegister() {
    try (Connection c = DatabaseConnection.getInstance().getConnection()) {
      c.setAutoCommit(false);

      // 0) Next empID for userID/etc.
      int nextEmpId = fetchNextEmployeeAutoIncrement(c);

      // 1) accountStatus based on creator role
      String creatorUser = SessionManager.getUserID();
      int creatorRoleID = -1;
      try (PreparedStatement r = c.prepareStatement(
             "SELECT roleID FROM authentication WHERE userID=?"
           )) {
        r.setString(1, creatorUser);
        try (ResultSet rr = r.executeQuery()) {
          if (rr.next()) creatorRoleID = rr.getInt("roleID");
        }
      }
      String acctStatus = (creatorRoleID == 3)
                         ? "Active"
                         : "Pending";

      // 2) authentication
      String email        = makeEmail(firstNameField.getText(), lastNameField.getText());
      String userID       = "U" + nextEmpId;
      String passwordHash = capitalize(lastNameField.getText()) + "@" + nextEmpId;
      int    roleID       = roleCombo.getSelectedIndex() + 1;
      try (PreparedStatement p = c.prepareStatement(
             "INSERT INTO authentication(userID,passwordHash,accountStatus,roleID) VALUES(?,?,?,?)"
           )) {
        p.setString(1, userID);
        p.setString(2, passwordHash);
        p.setString(3, acctStatus);
        p.setInt   (4, roleID);
        p.executeUpdate();
      }

      // 3) compensation
      BigDecimal basic = parseMoney(salaryCombo.getSelectedItem().toString());
      BigDecimal semi  = basic.divide(BigDecimal.valueOf(2));
      BigDecimal hour  = basic.divide(BigDecimal.valueOf(21*8), 2, BigDecimal.ROUND_HALF_UP);
      int compID;
      try (PreparedStatement p = c.prepareStatement(
             "INSERT INTO compensation(basicSalary, semiMonthlySalary, hourlyRate) VALUES(?,?,?)",
             PreparedStatement.RETURN_GENERATED_KEYS
           )) {
        p.setBigDecimal(1, basic);
        p.setBigDecimal(2, semi);
        p.setBigDecimal(3, hour);
        p.executeUpdate();
        try (ResultSet rs = p.getGeneratedKeys()) {
          if (!rs.next()) throw new SQLException("Failed to retrieve compensationID");
          compID = rs.getInt(1);
        }
      }

      // 4) employee
      String phoneFormatted = phoneField.getText();
      int statusID     = statusIds.get(statusCombo.getSelectedIndex());
      int positionID   = positionIds.get(positionCombo.getSelectedIndex());
      int departmentID = departmentIds.get(departmentCombo.getSelectedIndex());
      int supervisorID = supervisorIds.get(supervisorCombo.getSelectedIndex());
      try (PreparedStatement p = c.prepareStatement(
             "INSERT INTO employee(" +
             " firstName,lastName,birthDate,phoneNo,email,userID," +
             " statusID,positionID,departmentID,compensationID,supervisorID" +
             ") VALUES(?,?,?,?,?,?,?,?,?,?,?)"
           )) {
        p.setString(1, firstNameField.getText());
        p.setString(2, lastNameField.getText());
        // JDateChooser returns java.util.Date
        java.util.Date selectedDate = dobCal.getDate();
        if (selectedDate == null)
          throw new SQLException("Please select a valid birth date.");
        p.setDate  (3, new java.sql.Date(selectedDate.getTime()));
        p.setString(4, phoneFormatted);
        p.setString(5, email);
        p.setString(6, userID);
        p.setInt   (7, statusID);
        p.setInt   (8, positionID);
        p.setInt   (9, departmentID);
        p.setInt   (10, compID);
        p.setInt   (11, supervisorID);
        p.executeUpdate();
      }

      // 5) address → employeeaddress
      int addressID;
      try (PreparedStatement p = c.prepareStatement(
             "INSERT INTO address(houseNo,street,barangay,city,province,zipCode) VALUES(?,?,?,?,?,?)",
             PreparedStatement.RETURN_GENERATED_KEYS
           )) {
        p.setString(1, houseNoField.getText());
        p.setString(2, streetField.getText());
        p.setString(3, barangayField.getText());
        p.setString(4, cityField.getText());
        p.setString(5, provinceField.getText());
        p.setInt   (6, Integer.parseInt(zipField.getText()));
        p.executeUpdate();
        try (ResultSet rs = p.getGeneratedKeys()) { rs.next(); addressID = rs.getInt(1); }
      }
      try (PreparedStatement p = c.prepareStatement(
             "INSERT INTO employeeaddress(employeeID,addressID) VALUES(?,?)"
           )) {
        p.setInt(1, nextEmpId);
        p.setInt(2, addressID);
        p.executeUpdate();
      }

      // 6) govid
      try (PreparedStatement p = c.prepareStatement(
             "INSERT INTO govid(sss,philhealth,tin,pagibig,employeeID) VALUES(?,?,?,?,?)"
           )) {
        p.setString(1, sssField.getText());
        p.setString(2, philField.getText());
        p.setString(3, tinField.getText());
        p.setString(4, pagibigField.getText());
        p.setInt   (5, nextEmpId);
        p.executeUpdate();
      }

      c.commit();
      JOptionPane.showMessageDialog(this, "Employee created successfully!");
      onRegisterSuccess(); // Let subclass handle navigation after successful register
    }
    catch (Exception ex) {
      ex.printStackTrace();
      JOptionPane.showMessageDialog(
        this,
        "Failed to create employee: " + ex.getMessage(),
        "Error",
        JOptionPane.ERROR_MESSAGE
      );
    }
  }

  // Subclass must define what to do on success (e.g., navigate page)
  protected abstract void onRegisterSuccess();

  private int fetchNextEmployeeAutoIncrement(Connection c) throws SQLException {
    String sql =
      "SELECT AUTO_INCREMENT " +
      " FROM information_schema.TABLES " +
      " WHERE TABLE_SCHEMA = DATABASE() " +
      "   AND TABLE_NAME   = 'employee'";
    try (PreparedStatement ps = c.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
      if (rs.next()) {
        return rs.getInt("AUTO_INCREMENT");
      }
      throw new SQLException("Could not fetch employee AUTO_INCREMENT");
    }
  }

  protected List<String> validateAll() {
    var errs = new ArrayList<String>();
    if (lastNameField.getText().trim().length()<2)
      errs.add("Please enter a last name (at least 2 letters).");
    if (firstNameField.getText().trim().length()<2)
      errs.add("Please enter a first name (at least 2 letters).");
    if (provinceField.getText().trim().length()<4)
      errs.add("Please enter a province (at least 4 letters).");
    if (cityField.getText().trim().length()<4)
      errs.add("Please enter a city/municipality (at least 4 letters).");
    if (barangayField.getText().trim().length()<4)
      errs.add("Please enter a barangay (at least 4 characters).");
    if (streetField.getText().trim().length()<4)
      errs.add("Please enter a street (at least 4 characters).");
    if (houseNoField.getText().trim().isEmpty())
      errs.add("Please enter a house number.");
    if (zipField.getText().trim().length()!=4)
      errs.add("Please enter a 4-digit ZIP code.");

    if (!phoneField.getText().matches("\\d{3}-\\d{3}-\\d{3}"))
      errs.add("Please enter a phone number in format XXX-XXX-XXX.");

    if (!sssField.getText().matches("\\d{2}-\\d{7}-\\d"))
      errs.add("Please enter an SSS number in format XX-XXXXXXX-X.");

    if (philField.getText().replaceAll("\\D","").length()!=12)
      errs.add("Please enter a 12-digit PhilHealth number.");
    if (pagibigField.getText().replaceAll("\\D","").length()!=12)
      errs.add("Please enter a 12-digit Pag-IBIG number.");

    if (!tinField.getText().matches("\\d{3}-\\d{3}-\\d{3}-000"))
      errs.add("Please enter a TIN in format XXX-XXX-XXX-000.");

    if (roleCombo.getSelectedIndex()<0)
      errs.add("Please select an employee role.");
    if (statusCombo.getSelectedIndex()<0)
      errs.add("Please select an employment status.");
    if (positionCombo.getSelectedIndex()<0)
      errs.add("Please select a position.");
    if (departmentCombo.getSelectedIndex()<0)
      errs.add("Please select a department.");
    if (supervisorCombo.getSelectedIndex()<0)
      errs.add("Please select a supervisor.");
    if (salaryCombo.getSelectedIndex()<0)
      errs.add("Please select a salary.");

    // Adjusted: fetch date from JDateChooser (dobCal)
    java.util.Date selectedDate = dobCal.getDate();
    if (selectedDate == null) {
      errs.add("Please select a date of birth.");
    } else {
      Calendar cal = Calendar.getInstance();
      cal.setTime(selectedDate);
      LocalDate dob = LocalDate.of(
        cal.get(Calendar.YEAR),
        cal.get(Calendar.MONTH) + 1,
        cal.get(Calendar.DAY_OF_MONTH)
      );
      if (Period.between(dob, LocalDate.now()).getYears() < 18)
        errs.add("Employee must be at least 18 years old.");
    }

    return errs;
  }

  // ——— All your existing validators, formatters, and helpers below ———

  private void installFilter(JTextField fld, Pattern p, int maxLen) {
    ((AbstractDocument)fld.getDocument())
      .setDocumentFilter(new PatternFilter(p, maxLen));
  }
  private void installMinValidator(JTextField fld, int minLen) {
    fld.getDocument().addDocumentListener(new DocumentListener(){
      private void upd(){
        fld.setBackground(
          fld.getText().trim().length() < minLen
            ? Color.PINK : Color.WHITE
        );
      }
      public void insertUpdate(DocumentEvent e){upd();}
      public void removeUpdate(DocumentEvent e){upd();}
      public void changedUpdate(DocumentEvent e){upd();}
    });
  }
  private void installDigitHighlighter(JTextField fld, int req) {
    fld.getDocument().addDocumentListener(new DocumentListener(){
      private void upd(){
        int c = fld.getText().replaceAll("\\D","").length();
        fld.setBackground(c < req ? Color.PINK : Color.WHITE);
      }
      public void insertUpdate(DocumentEvent e){upd();}
      public void removeUpdate(DocumentEvent e){upd();}
      public void changedUpdate(DocumentEvent e){upd();}
    });
  }
  private void installPhoneHighlighter(JTextField fld) {
    fld.getDocument().addDocumentListener(new DocumentListener(){
      private void upd(){
        String txt = fld.getText();
        fld.setBackground(txt.matches("\\d{3}-\\d{3}-\\d{3}") 
                          ? Color.WHITE 
                          : Color.PINK);
      }
      public void insertUpdate(DocumentEvent e){upd();}
      public void removeUpdate(DocumentEvent e){upd();}
      public void changedUpdate(DocumentEvent e){upd();}
    });
  }
  private static class PatternFilter extends DocumentFilter {
    private final Pattern pat; private final int maxLen;
    PatternFilter(Pattern p,int m){ pat=p; maxLen=m; }
    @Override public void insertString(FilterBypass fb,int offs,String str,AttributeSet a)
      throws BadLocationException {
      String orig = fb.getDocument().getText(0,fb.getDocument().getLength());
      String cand = orig.substring(0,offs) + str + orig.substring(offs);
      if (cand.length()<=maxLen && pat.matcher(cand).matches())
        super.insertString(fb,offs,str,a);
    }
    @Override public void replace(FilterBypass fb,int offs,int len,String str,AttributeSet a)
      throws BadLocationException {
      String orig = fb.getDocument().getText(0,fb.getDocument().getLength());
      String cand = orig.substring(0,offs) + str + orig.substring(offs+len);
      if (cand.length()<=maxLen && pat.matcher(cand).matches())
        super.replace(fb,offs,len,str,a);
    }
  }
  private void installFormatter(JTextField fld, java.util.function.Function<String,String> fmt) {
    fld.getDocument().addDocumentListener(new DocumentListener(){
      boolean busy=false;
      private void upd(){
        if (busy) return;
        String digits = fld.getText().replaceAll("\\D","");
        String out    = fmt.apply(digits);
        if (!fld.getText().equals(out)) {
          busy = true;
          SwingUtilities.invokeLater(() -> {
            fld.setText(out);
            busy = false;
          });
        }
      }
      public void insertUpdate(DocumentEvent e){upd();}
      public void removeUpdate(DocumentEvent e){upd();}
      public void changedUpdate(DocumentEvent e){upd();}
    });
    }
    private String formatSSS(String d) {
      if (d.length()>10) d=d.substring(0,10);
      if (d.length()<3) return d;
      if (d.length()<=9) return d.substring(0,2) + "-" + d.substring(2);
      return d.substring(0,2) + "-" + d.substring(2,9) + "-" + d.substring(9);
    }
    
    private String formatTIN(String d) {
        d = d.replaceAll("\\D", "");
        if (d.length() == 0) return "";
        if (d.length() <= 3) return d;
        if (d.length() <= 6) return d.substring(0,3) + "-" + d.substring(3);
        if (d.length() <= 9) return d.substring(0,3) + "-" + d.substring(3,6) + "-" + d.substring(6);
        return d.substring(0,3) + "-" + d.substring(3,6) + "-" + d.substring(6,9) + "-" + d.substring(9, Math.min(12, d.length()));
    }

    private String formatPhone(String d) {
      if (d.length()>9) d=d.substring(d.length()-9);
      if (d.length()<=3) return d;
      if (d.length()<=6) return d.substring(0,3)+"-"+d.substring(3);
      return d.substring(0,3)+"-"+d.substring(3,6)+"-"+d.substring(6);
    }
    private String capitalize(String s) {
      if (s.isEmpty()) return s;
      return Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
    }
    private String makeEmail(String fn, String ln) {
      return (fn.charAt(0)+ln).toLowerCase()+"@motor.ph";
    }
    private BigDecimal parseMoney(String s) {
      String clean = s.replaceAll("[₱, ]","");
      return new BigDecimal(clean);
    }
}