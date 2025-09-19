package ui.base;

import javax.swing.JLabel;
import service.EmployeeService;
import pojo.Employee;
import util.SessionManager;

public abstract class AbstractProfilePage extends javax.swing.JFrame {

    protected EmployeeService employeeService;

    public AbstractProfilePage() {
        employeeService = new EmployeeService();
    }

    protected void initializeProfilePage() {
        try {
            int employeeID = SessionManager.getEmployeeID();
            Employee emp = employeeService.getEmployeeByID(employeeID);
            if (emp == null) {
                javax.swing.JOptionPane.showMessageDialog(this, "Employee record not found.");
                return;
            }

            // map all the simple fields
            getEmployeeIDText().setText(String.valueOf(emp.getEmployeeID()));
            getPositionText().setText(emp.getPosition());
            getFirstNameText().setText(emp.getFirstName());
            getLastNameText().setText(emp.getLastName());
            getBirthdayText().setText(emp.getBirthDate().toString());
            getPhoneNumberText().setText(emp.getPhoneNo());
            getSupervisorText().setText(emp.getSupervisorName());
            getStatusText().setText(emp.getStatusDesc());
            getSSSNumberText().setText(emp.getSssNo());
            getPagibigNumberText().setText(emp.getPagibigNo());
            getPhilhealthNumberText().setText(emp.getPhilhealthNo());
            getTINNumberText().setText(emp.getTinNo());

            // now combine address lines
            StringBuilder addr = new StringBuilder("<html>");
            addr.append(emp.getHouseNo()).append(" ").append(emp.getStreet()).append("<br>")
                .append(emp.getBarangay()).append("<br>")
                .append(emp.getCity()).append("<br>")
                .append(emp.getProvince());
            if (emp.getZipCode() != null) {
                addr.append(" ").append(emp.getZipCode());
            }
            addr.append("</html>");
            getAddressText().setText(addr.toString());

        } catch (Exception ex) {
            javax.swing.JOptionPane.showMessageDialog(this, "Error loading profile: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // Abstract methods to be implemented by the concrete page

    protected abstract JLabel getEmployeeIDText();
    protected abstract JLabel getPositionText();
    protected abstract JLabel getFirstNameText();
    protected abstract JLabel getLastNameText();
    protected abstract JLabel getBirthdayText();
    protected abstract JLabel getPhoneNumberText();
    protected abstract JLabel getAddressText();
    protected abstract JLabel getSupervisorText();
    protected abstract JLabel getStatusText();
    protected abstract JLabel getSSSNumberText();
    protected abstract JLabel getPagibigNumberText();
    protected abstract JLabel getPhilhealthNumberText();
    protected abstract JLabel getTINNumberText();
}