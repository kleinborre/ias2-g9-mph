package dao;

import pojo.Payslip;
import java.sql.SQLException;
import java.util.List;

public interface PayslipDAO {
    Payslip getPayslipByPayslipNo(String payslipNo) throws SQLException;
    List<Payslip> getPayslipsByEmployeeID(int employeeID) throws SQLException;
    List<Payslip> getAllPayslips() throws SQLException;
}