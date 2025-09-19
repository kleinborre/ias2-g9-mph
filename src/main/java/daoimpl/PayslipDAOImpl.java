package daoimpl;

import dao.PayslipDAO;
import db.DatabaseConnection;
import pojo.Payslip;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PayslipDAOImpl implements PayslipDAO {

    public PayslipDAOImpl() {}

    @Override
    public Payslip getPayslipByPayslipNo(String payslipNo) throws SQLException {
        String sql = "SELECT * FROM v_motorph_payslip WHERE PAYSILP_NO = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, payslipNo);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPayslip(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Payslip> getPayslipsByEmployeeID(int employeeID) throws SQLException {
        String sql = "SELECT * FROM v_motorph_payslip WHERE EMPLOYEE_ID = ? ORDER BY PERIOD_END_DATE DESC";
        List<Payslip> payslips = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, employeeID);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    payslips.add(mapResultSetToPayslip(rs));
                }
            }
        }
        return payslips;
    }

    @Override
    public List<Payslip> getAllPayslips() throws SQLException {
        String sql = "SELECT * FROM v_motorph_payslip ORDER BY PERIOD_END_DATE DESC";
        List<Payslip> payslips = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                payslips.add(mapResultSetToPayslip(rs));
            }
        }
        return payslips;
    }

    private Payslip mapResultSetToPayslip(ResultSet rs) throws SQLException {
        Payslip p = new Payslip();
        p.setPayslipNo(rs.getString("PAYSILP_NO"));
        p.setEmployeeID(rs.getInt("EMPLOYEE_ID"));
        p.setEmployeeName(rs.getString("EMPLOYEE_NAME"));
        p.setPeriodStartDate(rs.getDate("PERIOD_START_DATE"));
        p.setPeriodEndDate(rs.getDate("PERIOD_END_DATE"));
        p.setEmployeePositionDepartment(rs.getString("EMPLOYEE_POSITION_DEPARTMENT"));
        p.setMonthlyRate(rs.getDouble("Monthly_Rate"));
        p.setDailyRate(rs.getDouble("Daily_Rate"));
        p.setDaysWorked(rs.getDouble("Days_Worked"));
        p.setOvertimeHours(rs.getDouble("Overtime_Hours"));
        p.setGrossIncome(rs.getDouble("GROSS_INCOME"));
        p.setRiceSubsidy(rs.getDouble("Rice_Subsidy"));
        p.setPhoneAllowance(rs.getDouble("Phone_Allowance"));
        p.setClothingAllowance(rs.getDouble("Clothing_Allowance"));
        p.setTotalBenefits(rs.getDouble("TOTAL_BENEFITS"));
        p.setSss(rs.getDouble("SSS"));
        p.setPhilhealth(rs.getDouble("Philhealth"));
        p.setPagibig(rs.getDouble("Pagibig"));
        p.setWithholdingTax(rs.getDouble("Withholding_Tax"));
        p.setTotalDeductions(rs.getDouble("TOTAL_DEDUCTIONS"));
        p.setGrossIncomeDup(rs.getDouble("Gross Income")); // For Jasper or double check
        p.setBenefits(rs.getDouble("Benefits"));
        p.setDeductions(rs.getDouble("Deductions"));
        p.setTakeHomePay(rs.getDouble("TAKE_HOME_PAY"));
        return p;
    }
}