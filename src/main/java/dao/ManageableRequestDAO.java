package dao;

import java.sql.SQLException;
import java.util.List;

public interface ManageableRequestDAO<T> {

    T getRequestByID(int requestID) throws SQLException;

    List<T> getRequestsByEmployeeID(int employeeID) throws SQLException;

    List<T> getAllRequests() throws SQLException;

    void addRequest(T request) throws SQLException;

    void updateApprovalStatus(int requestID, int approvalStatusID) throws SQLException;

    void deleteRequest(int requestID) throws SQLException;
    
    void updateRequest(T request) throws SQLException;
}