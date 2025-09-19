package pojo;

import java.sql.Timestamp;

public class Overtime {

    private int overtimeID;
    private Timestamp overtimeStart;
    private Timestamp overtimeEnd;
    private String overtimeReason;
    private int approvalStatusID;
    private int employeeID;

    // Constructors

    public Overtime() {
    }

    public Overtime(int overtimeID, Timestamp overtimeStart, Timestamp overtimeEnd, String overtimeReason,
                    int approvalStatusID, int employeeID) {
        this.overtimeID = overtimeID;
        this.overtimeStart = overtimeStart;
        this.overtimeEnd = overtimeEnd;
        this.overtimeReason = overtimeReason;
        this.approvalStatusID = approvalStatusID;
        this.employeeID = employeeID;
    }

    // Getters and Setters

    public int getOvertimeID() {
        return overtimeID;
    }

    public void setOvertimeID(int overtimeID) {
        this.overtimeID = overtimeID;
    }

    public Timestamp getOvertimeStart() {
        return overtimeStart;
    }

    public void setOvertimeStart(Timestamp overtimeStart) {
        this.overtimeStart = overtimeStart;
    }

    public Timestamp getOvertimeEnd() {
        return overtimeEnd;
    }

    public void setOvertimeEnd(Timestamp overtimeEnd) {
        this.overtimeEnd = overtimeEnd;
    }

    public String getOvertimeReason() {
        return overtimeReason;
    }

    public void setOvertimeReason(String overtimeReason) {
        this.overtimeReason = overtimeReason;
    }

    public int getApprovalStatusID() {
        return approvalStatusID;
    }

    public void setApprovalStatusID(int approvalStatusID) {
        this.approvalStatusID = approvalStatusID;
    }

    public int getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(int employeeID) {
        this.employeeID = employeeID;
    }
}