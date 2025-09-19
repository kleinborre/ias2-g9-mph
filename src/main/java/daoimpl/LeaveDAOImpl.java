package daoimpl;

import dao.ManageableRequestDAO;
import db.DatabaseConnection;
import pojo.Leave;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LeaveDAOImpl implements ManageableRequestDAO<Leave> {

    private Connection connection;

    public LeaveDAOImpl() throws SQLException {
        connection = DatabaseConnection.getInstance().getConnection();
    }

    @Override
    public Leave getRequestByID(int requestID) throws SQLException {
        String query = "SELECT * FROM leaves WHERE leaveID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, requestID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToLeave(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Leave> getRequestsByEmployeeID(int employeeID) throws SQLException {
        List<Leave> leaveList = new ArrayList<>();
        String query = "SELECT * FROM leaves WHERE employeeID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, employeeID);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    leaveList.add(mapResultSetToLeave(rs));
                }
            }
        }
        return leaveList;
    }

    @Override
    public List<Leave> getAllRequests() throws SQLException {
        List<Leave> leaveList = new ArrayList<>();
        String query = "SELECT * FROM leaves";
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                leaveList.add(mapResultSetToLeave(rs));
            }
        }
        return leaveList;
    }

    @Override
    public void addRequest(Leave leave) throws SQLException {
        String query = "INSERT INTO leaves (leaveAllowance, leaveStart, leaveEnd, leaveReason, dateCreated, employeeID, approvalStatusID, leaveTypeID) " +
                       "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setDouble(1, leave.getLeaveAllowance());
            stmt.setDate(2, leave.getLeaveStart());
            stmt.setDate(3, leave.getLeaveEnd());
            stmt.setString(4, leave.getLeaveReason());
            stmt.setDate(5, leave.getDateCreated());
            stmt.setInt(6, leave.getEmployeeID());
            stmt.setInt(7, leave.getApprovalStatusID());
            stmt.setInt(8, leave.getLeaveTypeID());
            stmt.executeUpdate();
        }
    }

    @Override
    public void updateApprovalStatus(int requestID, int approvalStatusID) throws SQLException {
        String query = "UPDATE leaves SET approvalStatusID = ? WHERE leaveID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, approvalStatusID);
            stmt.setInt(2, requestID);
            stmt.executeUpdate();
        }
    }

    @Override
    public void deleteRequest(int requestID) throws SQLException {
        String query = "DELETE FROM leaves WHERE leaveID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, requestID);
            stmt.executeUpdate();
        }
    }

    // Helper method to map ResultSet to Leave POJO
    private Leave mapResultSetToLeave(ResultSet rs) throws SQLException {
        Leave leave = new Leave();
        leave.setLeaveID(rs.getInt("leaveID"));
        leave.setLeaveAllowance(rs.getDouble("leaveAllowance"));
        leave.setLeaveStart(rs.getDate("leaveStart"));
        leave.setLeaveEnd(rs.getDate("leaveEnd"));
        leave.setLeaveReason(rs.getString("leaveReason"));
        leave.setDateCreated(rs.getDate("dateCreated"));
        leave.setEmployeeID(rs.getInt("employeeID"));
        leave.setApprovalStatusID(rs.getInt("approvalStatusID"));
        leave.setLeaveTypeID(rs.getInt("leaveTypeID"));
        return leave;
    }

    // Add at the bottom of LeaveDAOImpl
    public String getApprovalStatusName(int approvalStatusID) throws SQLException {
        String query = "SELECT approvalStatus FROM approvalstatus WHERE approvalStatusID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, approvalStatusID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getString("approvalStatus");
            }
        }
        return "";
    }

    public String getLeaveTypeName(int leaveTypeID) throws SQLException {
        String query = "SELECT leaveType FROM leavetype WHERE leaveTypeID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, leaveTypeID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getString("leaveType");
            }
        }
        return "";
    }

    @Override
    public void updateRequest(Leave leave) throws SQLException {
        String query = "UPDATE leaves SET leaveAllowance = ?, leaveStart = ?, leaveEnd = ?, leaveReason = ?, dateCreated = ?, employeeID = ?, approvalStatusID = ?, leaveTypeID = ? WHERE leaveID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setDouble(1, leave.getLeaveAllowance());
            stmt.setDate(2, leave.getLeaveStart());
            stmt.setDate(3, leave.getLeaveEnd());
            stmt.setString(4, leave.getLeaveReason());
            stmt.setDate(5, leave.getDateCreated());
            stmt.setInt(6, leave.getEmployeeID());
            stmt.setInt(7, leave.getApprovalStatusID());
            stmt.setInt(8, leave.getLeaveTypeID());
            stmt.setInt(9, leave.getLeaveID());
            stmt.executeUpdate();
        }
    }
}