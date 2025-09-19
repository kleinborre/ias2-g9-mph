package dao;

import pojo.Employee;
import java.sql.SQLException;
import java.util.List;

public interface EmployeeDAO {

    Employee getEmployeeByID(int employeeID) throws SQLException;

    Employee getEmployeeByUserID(String userID) throws SQLException;

    List<Employee> getAllEmployees() throws SQLException;

    void addEmployee(Employee employee) throws SQLException;

    void updateEmployee(Employee employee) throws SQLException;

    void deleteEmployee(int employeeID) throws SQLException;
}