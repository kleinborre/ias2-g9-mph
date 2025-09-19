package daoimpl;

import dao.EmployeeDAO;
import db.DatabaseConnection;
import pojo.Employee;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDAOImpl implements EmployeeDAO {

    public EmployeeDAOImpl() {
        // nothing to do here any more
    }

    @Override
    public Employee getEmployeeByID(int employeeID) throws SQLException {
        String sql = "SELECT * FROM employee WHERE employeeID = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, employeeID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEmployee(rs);
                }
            }
        }
        return null;
    }

    @Override
    public Employee getEmployeeByUserID(String userID) throws SQLException {
        String sql = "SELECT * FROM employee WHERE userID = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, userID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEmployee(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Employee> getAllEmployees() throws SQLException {
        List<Employee> list = new ArrayList<>();
        String sql = "SELECT * FROM employee";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToEmployee(rs));
            }
        }
        return list;
    }

    @Override
    public void addEmployee(Employee employee) throws SQLException {
        String sql = "INSERT INTO employee " +
                     "(firstName, lastName, birthDate, phoneNo, email, userID, statusID, positionID, departmentID, supervisorID) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, employee.getFirstName());
            ps.setString(2, employee.getLastName());
            ps.setDate(3, employee.getBirthDate());
            ps.setString(4, employee.getPhoneNo());
            ps.setString(5, employee.getEmail());
            ps.setString(6, employee.getUserID());
            ps.setInt(7, employee.getStatusID());
            ps.setInt(8, employee.getPositionID());
            ps.setInt(9, employee.getDepartmentID());
            ps.setInt(10, employee.getSupervisorID());
            ps.executeUpdate();
        }
    }

    @Override
    public void updateEmployee(Employee employee) throws SQLException {
        String sql = "UPDATE employee SET " +
                     "firstName = ?, lastName = ?, birthDate = ?, phoneNo = ?, email = ?, " +
                     "statusID = ?, positionID = ?, departmentID = ?, supervisorID = ? " +
                     "WHERE employeeID = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, employee.getFirstName());
            ps.setString(2, employee.getLastName());
            ps.setDate(3, employee.getBirthDate());
            ps.setString(4, employee.getPhoneNo());
            ps.setString(5, employee.getEmail());
            ps.setInt(6, employee.getStatusID());
            ps.setInt(7, employee.getPositionID());
            ps.setInt(8, employee.getDepartmentID());
            ps.setInt(9, employee.getSupervisorID());
            ps.setInt(10, employee.getEmployeeID());
            ps.executeUpdate();
        }
    }

    @Override
    public void deleteEmployee(int employeeID) throws SQLException {
        String sql = "DELETE FROM employee WHERE employeeID = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, employeeID);
            ps.executeUpdate();
        }
    }

    private Employee mapResultSetToEmployee(ResultSet rs) throws SQLException {
        Employee e = new Employee();
        e.setEmployeeID   (rs.getInt("employeeID"));
        e.setFirstName    (rs.getString("firstName"));
        e.setLastName     (rs.getString("lastName"));
        e.setBirthDate    (rs.getDate("birthDate"));
        e.setPhoneNo      (rs.getString("phoneNo"));
        e.setEmail        (rs.getString("email"));
        e.setUserID       (rs.getString("userID"));
        e.setStatusID     (rs.getInt("statusID"));
        e.setPositionID   (rs.getInt("positionID"));
        e.setDepartmentID (rs.getInt("departmentID"));
        e.setSupervisorID (rs.getInt("supervisorID"));
        return e;
    }
}