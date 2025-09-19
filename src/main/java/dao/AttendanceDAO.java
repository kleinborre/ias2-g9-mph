package dao;

import pojo.Attendance;
import java.sql.SQLException;
import java.util.List;

public interface AttendanceDAO {

    Attendance getAttendanceByID(int attendanceID) throws SQLException;

    List<Attendance> getAttendanceByEmployeeID(int employeeID) throws SQLException;

    List<Attendance> getAttendanceByDate(java.sql.Date date) throws SQLException;

    List<Attendance> getAllAttendance() throws SQLException;

    void addAttendance(Attendance attendance) throws SQLException;

    void updateAttendance(Attendance attendance) throws SQLException;

    void deleteAttendance(int attendanceID) throws SQLException;
}