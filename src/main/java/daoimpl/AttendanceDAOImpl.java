package daoimpl;

import dao.AttendanceDAO;
import db.DatabaseConnection;
import pojo.Attendance;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AttendanceDAOImpl implements AttendanceDAO {

    private Connection connection;

    public AttendanceDAOImpl() throws SQLException {
        connection = DatabaseConnection.getInstance().getConnection();
    }

    @Override
    public Attendance getAttendanceByID(int attendanceID) throws SQLException {
        String query = "SELECT * FROM attendance WHERE attendanceID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, attendanceID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAttendance(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Attendance> getAttendanceByEmployeeID(int employeeID) throws SQLException {
        List<Attendance> attendanceList = new ArrayList<>();
        String query = "SELECT * FROM attendance WHERE employeeID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, employeeID);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    attendanceList.add(mapResultSetToAttendance(rs));
                }
            }
        }
        return attendanceList;
    }

    @Override
    public List<Attendance> getAttendanceByDate(Date date) throws SQLException {
        List<Attendance> attendanceList = new ArrayList<>();
        String query = "SELECT * FROM attendance WHERE date = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setDate(1, date);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    attendanceList.add(mapResultSetToAttendance(rs));
                }
            }
        }
        return attendanceList;
    }

    @Override
    public List<Attendance> getAllAttendance() throws SQLException {
        List<Attendance> attendanceList = new ArrayList<>();
        String query = "SELECT * FROM attendance";
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                attendanceList.add(mapResultSetToAttendance(rs));
            }
        }
        return attendanceList;
    }

    @Override
    public void addAttendance(Attendance attendance) throws SQLException {
        String query = "INSERT INTO attendance (date, logIn, logOut, workedHours, employeeID) " +
                       "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setDate(1, attendance.getDate());
            stmt.setTime(2, attendance.getLogIn());
            stmt.setTime(3, attendance.getLogOut());
            stmt.setDouble(4, attendance.getWorkedHours());
            stmt.setInt(5, attendance.getEmployeeID());
            stmt.executeUpdate();
        }
    }

    @Override
    public void updateAttendance(Attendance attendance) throws SQLException {
        String query = "UPDATE attendance SET date = ?, logIn = ?, logOut = ?, workedHours = ?, employeeID = ? " +
                       "WHERE attendanceID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setDate(1, attendance.getDate());
            stmt.setTime(2, attendance.getLogIn());
            stmt.setTime(3, attendance.getLogOut());
            stmt.setDouble(4, attendance.getWorkedHours());
            stmt.setInt(5, attendance.getEmployeeID());
            stmt.setInt(6, attendance.getAttendanceID());
            stmt.executeUpdate();
        }
    }

    @Override
    public void deleteAttendance(int attendanceID) throws SQLException {
        String query = "DELETE FROM attendance WHERE attendanceID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, attendanceID);
            stmt.executeUpdate();
        }
    }

    // Helper method to map ResultSet to Attendance POJO
    private Attendance mapResultSetToAttendance(ResultSet rs) throws SQLException {
        Attendance attendance = new Attendance();
        attendance.setAttendanceID(rs.getInt("attendanceID"));
        attendance.setDate(rs.getDate("date"));
        attendance.setLogIn(rs.getTime("logIn"));
        attendance.setLogOut(rs.getTime("logOut"));
        attendance.setWorkedHours(rs.getDouble("workedHours"));
        attendance.setEmployeeID(rs.getInt("employeeID"));
        return attendance;
    }
}