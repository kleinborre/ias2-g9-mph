package service;

import dao.ManageableRequestDAO;
import daoimpl.OvertimeDAOImpl;
import pojo.Overtime;

import java.sql.SQLException;
import java.util.List;

public class OvertimeService {

    private ManageableRequestDAO<Overtime> overtimeDAO;

    public OvertimeService() {
        try {
            overtimeDAO = new OvertimeDAOImpl();
        } catch (SQLException e) {
            throw new RuntimeException("Error initializing OvertimeDAO", e);
        }
    }

    public Overtime getOvertimeByID(int overtimeID) {
        try {
            return overtimeDAO.getRequestByID(overtimeID);
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving overtime by ID", e);
        }
    }

    public List<Overtime> getOvertimesByEmployeeID(int employeeID) {
        try {
            return overtimeDAO.getRequestsByEmployeeID(employeeID);
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving overtimes by employee ID", e);
        }
    }

    public List<Overtime> getAllOvertimes() {
        try {
            return overtimeDAO.getAllRequests();
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving all overtimes", e);
        }
    }

    public void addOvertime(Overtime overtime) {
        try {
            overtimeDAO.addRequest(overtime);
        } catch (SQLException e) {
            throw new RuntimeException("Error adding overtime request", e);
        }
    }

    public void updateApprovalStatus(int overtimeID, int approvalStatusID) {
        try {
            overtimeDAO.updateApprovalStatus(overtimeID, approvalStatusID);
        } catch (SQLException e) {
            throw new RuntimeException("Error updating approval status of overtime", e);
        }
    }

    public void deleteOvertime(int overtimeID) {
        try {
            overtimeDAO.deleteRequest(overtimeID);
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting overtime", e);
        }
    }
    
    public void updateOvertime(Overtime overtime) {
        try {
            overtimeDAO.updateRequest(overtime);
        } catch (SQLException e) {
            throw new RuntimeException("Error updating overtime request", e);
        }
    }

}