package service;

import dao.PayslipDAO;
import daoimpl.PayslipDAOImpl;
import pojo.Payslip;

import java.sql.SQLException;
import java.util.List;

public class PayslipService {

    private final PayslipDAO payslipDAO;

    public PayslipService() {
        this.payslipDAO = new PayslipDAOImpl();
    }

    public Payslip getPayslipByPayslipNo(String payslipNo) {
        try {
            return payslipDAO.getPayslipByPayslipNo(payslipNo);
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving payslip by payslipNo", e);
        }
    }

    public List<Payslip> getPayslipsByEmployeeID(int employeeID) {
        try {
            return payslipDAO.getPayslipsByEmployeeID(employeeID);
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving payslips by employeeID", e);
        }
    }

    public List<Payslip> getAllPayslips() {
        try {
            return payslipDAO.getAllPayslips();
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving all payslips", e);
        }
    }
}