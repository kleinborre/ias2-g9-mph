package pojo;

import java.sql.Date;

public class Leave {

    private int leaveID;
    private double leaveAllowance;
    private Date leaveStart;
    private Date leaveEnd;
    private String leaveReason;
    private Date dateCreated;
    private int employeeID;
    private int approvalStatusID;
    private int leaveTypeID;

    // Constructors

    public Leave() {
    }

    public Leave(int leaveID, double leaveAllowance, Date leaveStart, Date leaveEnd, String leaveReason,
                 Date dateCreated, int employeeID, int approvalStatusID, int leaveTypeID) {
        this.leaveID = leaveID;
        this.leaveAllowance = leaveAllowance;
        this.leaveStart = leaveStart;
        this.leaveEnd = leaveEnd;
        this.leaveReason = leaveReason;
        this.dateCreated = dateCreated;
        this.employeeID = employeeID;
        this.approvalStatusID = approvalStatusID;
        this.leaveTypeID = leaveTypeID;
    }

    // Getters and Setters

    public int getLeaveID() {
        return leaveID;
    }

    public void setLeaveID(int leaveID) {
        this.leaveID = leaveID;
    }

    public double getLeaveAllowance() {
        return leaveAllowance;
    }

    public void setLeaveAllowance(double leaveAllowance) {
        this.leaveAllowance = leaveAllowance;
    }

    public Date getLeaveStart() {
        return leaveStart;
    }

    public void setLeaveStart(Date leaveStart) {
        this.leaveStart = leaveStart;
    }

    public Date getLeaveEnd() {
        return leaveEnd;
    }

    public void setLeaveEnd(Date leaveEnd) {
        this.leaveEnd = leaveEnd;
    }

    public String getLeaveReason() {
        return leaveReason;
    }

    public void setLeaveReason(String leaveReason) {
        this.leaveReason = leaveReason;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public int getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(int employeeID) {
        this.employeeID = employeeID;
    }

    public int getApprovalStatusID() {
        return approvalStatusID;
    }

    public void setApprovalStatusID(int approvalStatusID) {
        this.approvalStatusID = approvalStatusID;
    }

    public int getLeaveTypeID() {
        return leaveTypeID;
    }

    public void setLeaveTypeID(int leaveTypeID) {
        this.leaveTypeID = leaveTypeID;
    }
}