package service;

import dao.AttendanceDAO;
import daoimpl.AttendanceDAOImpl;
import pojo.Attendance;

import java.sql.*;
import java.util.List;

import db.DatabaseConnection;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public class AttendanceService {

    private AttendanceDAO attendanceDAO;

    public AttendanceService() {
        try {
            attendanceDAO = new AttendanceDAOImpl();
        } catch (SQLException e) {
            throw new RuntimeException("Error initializing AttendanceDAO", e);
        }
    }

    public Attendance getAttendanceByID(int attendanceID) {
        try {
            return attendanceDAO.getAttendanceByID(attendanceID);
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving attendance by ID", e);
        }
    }

    public List<Attendance> getAttendanceByEmployeeID(int employeeID) {
        try {
            return attendanceDAO.getAttendanceByEmployeeID(employeeID);
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving attendance by employeeID", e);
        }
    }

    public List<Attendance> getAttendanceByDate(Date date) {
        try {
            return attendanceDAO.getAttendanceByDate(date);
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving attendance by date", e);
        }
    }

    public List<Attendance> getAllAttendance() {
        try {
            return attendanceDAO.getAllAttendance();
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving all attendance records", e);
        }
    }

    public void addAttendance(Attendance attendance) {
        try {
            attendanceDAO.addAttendance(attendance);
        } catch (SQLException e) {
            throw new RuntimeException("Error adding attendance record", e);
        }
    }

    public void updateAttendance(Attendance attendance) {
        try {
            attendanceDAO.updateAttendance(attendance);
        } catch (SQLException e) {
            throw new RuntimeException("Error updating attendance record", e);
        }
    }

    public void deleteAttendance(int attendanceID) {
        try {
            attendanceDAO.deleteAttendance(attendanceID);
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting attendance record", e);
        }
    }

    // Clock-in and clock out

    public boolean clockIn(int employeeID) throws SQLException {
        String sql = 
            "INSERT INTO attendance (employeeID, date, logIn) " +
            "VALUES (?, CURRENT_DATE, CURRENT_TIME)";
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, employeeID);
            return p.executeUpdate() > 0;
        }
    }

    public boolean clockOut(int employeeID) throws SQLException {
        // now also calculate workedHours in decimal hours to 2dp
        String sql =
            "UPDATE attendance SET " +
            "  logOut = CURRENT_TIME, " +
            "  workedHours = ROUND( TIMESTAMPDIFF(SECOND, logIn, CURRENT_TIME )/3600, 2 ) " +
            "WHERE employeeID = ? AND date = CURRENT_DATE AND logOut IS NULL";

        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, employeeID);
            return p.executeUpdate() > 0;
        }
    }

    /** Holds today’s in/out **and** the workedHours field. */
    public static class AttendanceStatus {
        private boolean     clockedIn;
        private boolean     clockedOut;
        private String      logIn;
        private String      logOut;
        private BigDecimal  workedHours = BigDecimal.ZERO;

        public boolean isClockedIn()               { return clockedIn; }
        public void    setClockedIn(boolean v)     { clockedIn = v; }
        public boolean isClockedOut()              { return clockedOut; }
        public void    setClockedOut(boolean v)    { clockedOut = v; }
        public String  getLogIn()                  { return logIn; }
        public void    setLogIn(String v)          { logIn = v; }
        public String  getLogOut()                 { return logOut; }
        public void    setLogOut(String v)         { logOut = v; }
        public BigDecimal getWorkedHours()         { return workedHours; }
        public void       setWorkedHours(BigDecimal w) {
            workedHours = (w != null ? w : BigDecimal.ZERO);
        }
    }

    public AttendanceStatus getTodayAttendanceStatus(int employeeID) throws SQLException {
        AttendanceStatus status = new AttendanceStatus();

        String sql =
            "SELECT logIn, logOut, workedHours " +
            "FROM attendance " +
            "WHERE employeeID = ? AND date = CURRENT_DATE";

        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, employeeID);
            try (ResultSet r = p.executeQuery()) {
                if (r.next()) {
                    Time in  = r.getTime("logIn");
                    Time out = r.getTime("logOut");

                    if (in != null) {
                        status.setClockedIn(true);
                        status.setLogIn(in.toString());
                    } else {
                        status.setClockedIn(false);
                        status.setLogIn("Not Clocked-In");
                    }

                    if (out != null) {
                        status.setClockedOut(true);
                        status.setLogOut(out.toString());
                    } else {
                        status.setClockedOut(false);
                        status.setLogOut("Not Clocked-Out");
                    }

                    status.setWorkedHours(r.getBigDecimal("workedHours"));
                } else {
                    status.setClockedIn(false);
                    status.setClockedOut(false);
                    status.setLogIn("Not Clocked-In");
                    status.setLogOut("Not Clocked-Out");
                    status.setWorkedHours(BigDecimal.ZERO);
                }
            }
        }

        return status;
    }
    
    // For displaying WorkedHours per month in Home Dashboard
    public BigDecimal getMonthlyWorkedHours(int employeeID, int year, int month) throws SQLException {
        String sql =
            "SELECT COALESCE(SUM(workedHours),0) AS total " +
            "FROM attendance " +
            "WHERE employeeID = ? AND YEAR(`date`) = ? AND MONTH(`date`) = ?";
        try ( Connection c = DatabaseConnection.getInstance().getConnection();
              PreparedStatement p = c.prepareStatement(sql) ) {
            p.setInt(1, employeeID);
            p.setInt(2, year);
            p.setInt(3, month);
            try ( ResultSet r = p.executeQuery() ) {
                if (r.next()) {
                    return r.getBigDecimal("total");
                } else {
                    return BigDecimal.ZERO;
                }
            }
        }
    }
    
    /**
     * If yesterday (or any given date) has no logOut, force
     * a logOut at `cutoff` and compute workedHours accordingly.
     */
    public boolean autoClockOutForDate(int employeeID, LocalDate date, LocalTime cutoff)
        throws SQLException
    {
        String sql =
          "UPDATE attendance SET "
        + "  logOut = ?, "
        + "  workedHours = ROUND( "
        + "    TIMESTAMPDIFF(SECOND, logIn, ?) / 3600, 2 "
        + "  ) "
        + "WHERE employeeID = ? AND `date` = ? AND logOut IS NULL";
        try ( Connection c = DatabaseConnection.getInstance().getConnection();
              PreparedStatement ps = c.prepareStatement(sql) )
        {
            ps.setTime(1, java.sql.Time.valueOf(cutoff));
            ps.setTime(2, java.sql.Time.valueOf(cutoff));
            ps.setInt (3, employeeID);
            ps.setDate(4, java.sql.Date.valueOf(date));
            return ps.executeUpdate() > 0;
        }
    }
}