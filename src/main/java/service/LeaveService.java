package service;

import dao.ManageableRequestDAO;
import daoimpl.LeaveDAOImpl;
import pojo.Leave;
import util.AuditLogger;
import util.SessionManager;

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
            // Audit: creation (userId may be null during batch runs; AuditLogger tolerates nulls safely)
            AuditLogger.log(
                    SessionManager.getUserID(),
                    "LEAVE_CREATED",
                    "leaveID=NEW, employeeID=" + leave.getEmployeeID(),
                    null
            );
        } catch (SQLException e) {
            throw new RuntimeException("Error adding leave request", e);
        }
    }

    public void updateApprovalStatus(int leaveID, int approvalStatusID) {
        try {
            leaveDAO.updateApprovalStatus(leaveID, approvalStatusID);

            // Resolve a human label when possible (does not affect audit if DB lookup fails)
            String statusName;
            try {
                statusName = getApprovalStatusName(approvalStatusID);
            } catch (RuntimeException ex) {
                statusName = String.valueOf(approvalStatusID);
            }

            final String action =
                    ("Approved".equalsIgnoreCase(statusName)) ? "LEAVE_APPROVED" :
                    (("Rejected".equalsIgnoreCase(statusName) || "Declined".equalsIgnoreCase(statusName)) ? "LEAVE_REJECTED"
                                                                                                        : "LEAVE_STATUS_UPDATED");

            AuditLogger.log(
                    SessionManager.getUserID(),
                    action,
                    "leaveID=" + leaveID + ", status=" + approvalStatusID + "(" + statusName + ")",
                    null
            );
        } catch (SQLException e) {
            throw new RuntimeException("Error updating approval status of leave", e);
        }
    }

    public void deleteLeave(int leaveID) {
        try {
            leaveDAO.deleteRequest(leaveID);
            AuditLogger.log(
                    SessionManager.getUserID(),
                    "LEAVE_DELETED",
                    "leaveID=" + leaveID,
                    null
            );
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
            AuditLogger.log(
                    SessionManager.getUserID(),
                    "LEAVE_UPDATED",
                    "leaveID=" + leave.getLeaveID(),
                    null
            );
        } catch (SQLException e) {
            throw new RuntimeException("Error updating leave request", e);
        }
    }
}