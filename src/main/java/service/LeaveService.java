package service;

import dao.ManageableRequestDAO;
import daoimpl.LeaveDAOImpl;
import pojo.Leave;

import java.sql.SQLException;
import java.util.List;

public class LeaveService {

    private ManageableRequestDAO<Leave> leaveDAO;

    public LeaveService() {
        try {
            leaveDAO = new LeaveDAOImpl();
        } catch (SQLException e) {
            throw new RuntimeException("Error initializing LeaveDAO", e);
        }
    }

    public Leave getLeaveByID(int leaveID) {
        try {
            return leaveDAO.getRequestByID(leaveID);
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving leave by ID", e);
        }
    }

    public List<Leave> getLeavesByEmployeeID(int employeeID) {
        try {
            return leaveDAO.getRequestsByEmployeeID(employeeID);
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving leaves by employee ID", e);
        }
    }

    public List<Leave> getAllLeaves() {
        try {
            return leaveDAO.getAllRequests();
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving all leaves", e);
        }
    }

    public void addLeave(Leave leave) {
        try {
            leaveDAO.addRequest(leave);
        } catch (SQLException e) {
            throw new RuntimeException("Error adding leave request", e);
        }
    }

    public void updateApprovalStatus(int leaveID, int approvalStatusID) {
        try {
            leaveDAO.updateApprovalStatus(leaveID, approvalStatusID);
        } catch (SQLException e) {
            throw new RuntimeException("Error updating approval status of leave", e);
        }
    }

    public void deleteLeave(int leaveID) {
        try {
            leaveDAO.deleteRequest(leaveID);
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting leave", e);
        }
    }

    public String getApprovalStatusName(int approvalStatusID) {
        try {
            return ((LeaveDAOImpl) leaveDAO).getApprovalStatusName(approvalStatusID);
        } catch (SQLException e) {
            throw new RuntimeException("Error getting approval status", e);
        }
    }

    public String getLeaveTypeName(int leaveTypeID) {
        try {
            return ((LeaveDAOImpl) leaveDAO).getLeaveTypeName(leaveTypeID);
        } catch (SQLException e) {
            throw new RuntimeException("Error getting leave type", e);
        }
    }

    public void updateLeave(Leave leave) {
        try {
            leaveDAO.updateRequest(leave);
        } catch (SQLException e) {
            throw new RuntimeException("Error updating leave request", e);
        }
    }
}