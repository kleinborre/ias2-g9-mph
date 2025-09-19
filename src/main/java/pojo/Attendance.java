package pojo;

import java.sql.Date;
import java.sql.Time;

public class Attendance {

    private int attendanceID;
    private Date date;
    private Time logIn;
    private Time logOut;
    private double workedHours;
    private int employeeID;

    // Constructors

    public Attendance() {
    }

    public Attendance(int attendanceID, Date date, Time logIn, Time logOut, double workedHours, int employeeID) {
        this.attendanceID = attendanceID;
        this.date = date;
        this.logIn = logIn;
        this.logOut = logOut;
        this.workedHours = workedHours;
        this.employeeID = employeeID;
    }

    // Getters and Setters

    public int getAttendanceID() {
        return attendanceID;
    }

    public void setAttendanceID(int attendanceID) {
        this.attendanceID = attendanceID;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Time getLogIn() {
        return logIn;
    }

    public void setLogIn(Time logIn) {
        this.logIn = logIn;
    }

    public Time getLogOut() {
        return logOut;
    }

    public void setLogOut(Time logOut) {
        this.logOut = logOut;
    }

    public double getWorkedHours() {
        return workedHours;
    }

    public void setWorkedHours(double workedHours) {
        this.workedHours = workedHours;
    }

    public int getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(int employeeID) {
        this.employeeID = employeeID;
    }
}