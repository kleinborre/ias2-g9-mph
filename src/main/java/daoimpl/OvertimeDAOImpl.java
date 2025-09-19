package daoimpl;

import dao.ManageableRequestDAO;
import db.DatabaseConnection;
import pojo.Overtime;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OvertimeDAOImpl implements ManageableRequestDAO<Overtime> {

    private Connection connection;

    public OvertimeDAOImpl() throws SQLException {
        connection = DatabaseConnection.getInstance().getConnection();
    }

    @Override
    public Overtime getRequestByID(int requestID) throws SQLException {
        String query = "SELECT * FROM overtime WHERE overtimeID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, requestID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToOvertime(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Overtime> getRequestsByEmployeeID(int employeeID) throws SQLException {
        List<Overtime> overtimeList = new ArrayList<>();
        String query = "SELECT * FROM overtime WHERE employeeID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, employeeID);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    overtimeList.add(mapResultSetToOvertime(rs));
                }
            }
        }
        return overtimeList;
    }

    @Override
    public List<Overtime> getAllRequests() throws SQLException {
        List<Overtime> overtimeList = new ArrayList<>();
        String query = "SELECT * FROM overtime";
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                overtimeList.add(mapResultSetToOvertime(rs));
            }
        }
        return overtimeList;
    }

    @Override
    public void addRequest(Overtime overtime) throws SQLException {
        String query = "INSERT INTO overtime (overtimeStart, overtimeEnd, overtimeReason, approvalStatusID, employeeID) " +
                       "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setTimestamp(1, overtime.getOvertimeStart());
            stmt.setTimestamp(2, overtime.getOvertimeEnd());
            stmt.setString(3, overtime.getOvertimeReason());
            stmt.setInt(4, overtime.getApprovalStatusID());
            stmt.setInt(5, overtime.getEmployeeID());
            stmt.executeUpdate();
        }
    }

    @Override
    public void updateApprovalStatus(int requestID, int approvalStatusID) throws SQLException {
        String query = "UPDATE overtime SET approvalStatusID = ? WHERE overtimeID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, approvalStatusID);
            stmt.setInt(2, requestID);
            stmt.executeUpdate();
        }
    }

    @Override
    public void deleteRequest(int requestID) throws SQLException {
        String query = "DELETE FROM overtime WHERE overtimeID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, requestID);
            stmt.executeUpdate();
        }
    }

    // Helper method to map ResultSet to Overtime POJO
    private Overtime mapResultSetToOvertime(ResultSet rs) throws SQLException {
        Overtime overtime = new Overtime();
        overtime.setOvertimeID(rs.getInt("overtimeID"));
        overtime.setOvertimeStart(rs.getTimestamp("overtimeStart"));
        overtime.setOvertimeEnd(rs.getTimestamp("overtimeEnd"));
        overtime.setOvertimeReason(rs.getString("overtimeReason"));
        overtime.setApprovalStatusID(rs.getInt("approvalStatusID"));
        overtime.setEmployeeID(rs.getInt("employeeID"));
        return overtime;
    }

    @Override
    public void updateRequest(Overtime overtime) throws SQLException {
        String query = "UPDATE overtime SET overtimeStart = ?, overtimeEnd = ?, overtimeReason = ?, approvalStatusID = ?, employeeID = ? WHERE overtimeID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setTimestamp(1, overtime.getOvertimeStart());
            stmt.setTimestamp(2, overtime.getOvertimeEnd());
            stmt.setString(3, overtime.getOvertimeReason());
            stmt.setInt(4, overtime.getApprovalStatusID());
            stmt.setInt(5, overtime.getEmployeeID());
            stmt.setInt(6, overtime.getOvertimeID());
            stmt.executeUpdate();
        }
    }
}