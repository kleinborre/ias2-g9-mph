package service;

import dao.ManageableRequestDAO;
import daoimpl.OvertimeDAOImpl;
import pojo.Overtime;
import util.AuditLogger;
import util.SessionManager;

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
            // Audit: creation
            AuditLogger.log(
                    SessionManager.getUserID(),
                    "OT_CREATED",
                    "overtimeID=NEW, employeeID=" + overtime.getEmployeeID(),
                    null
            );
        } catch (SQLException e) {
            throw new RuntimeException("Error adding overtime request", e);
        }
    }

    public void updateApprovalStatus(int overtimeID, int approvalStatusID) {
        try {
            overtimeDAO.updateApprovalStatus(overtimeID, approvalStatusID);

            // We don’t have a name resolver here; keep numeric mapping
            final String action =
                    (approvalStatusID == 1) ? "OT_APPROVED" :
                    (approvalStatusID == 2) ? "OT_REJECTED" :
                    "OT_STATUS_UPDATED";

            AuditLogger.log(
                    SessionManager.getUserID(),
                    action,
                    "overtimeID=" + overtimeID + ", status=" + approvalStatusID,
                    null
            );
        } catch (SQLException e) {
            throw new RuntimeException("Error updating approval status of overtime", e);
        }
    }

    public void deleteOvertime(int overtimeID) {
        try {
            overtimeDAO.deleteRequest(overtimeID);
            AuditLogger.log(
                    SessionManager.getUserID(),
                    "OT_DELETED",
                    "overtimeID=" + overtimeID,
                    null
            );
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting overtime", e);
        }
    }

    public void updateOvertime(Overtime overtime) {
        try {
            overtimeDAO.updateRequest(overtime);
            AuditLogger.log(
                    SessionManager.getUserID(),
                    "OT_UPDATED",
                    "overtimeID=" + overtime.getOvertimeID(),
                    null
            );
        } catch (SQLException e) {
            throw new RuntimeException("Error updating overtime request", e);
        }
    }
}