package pojo;

import java.sql.Date;

public class Payslip {

    private String payslipNo;
    private int employeeID;
    private String employeeName;
    private Date periodStartDate;
    private Date periodEndDate;
    private String employeePositionDepartment;
    private double monthlyRate;
    private double dailyRate;
    private double daysWorked;
    private double overtimeHours;
    private double grossIncome;
    private double riceSubsidy;
    private double phoneAllowance;
    private double clothingAllowance;
    private double totalBenefits;
    private double sss;
    private double philhealth;
    private double pagibig;
    private double withholdingTax;
    private double totalDeductions;
    private double grossIncomeDup; // "Gross Income" (duplicate for view, mapped for JasperReport)
    private double benefits;
    private double deductions;
    private double takeHomePay;

    public Payslip() {}

    // Getters and setters
    public String getPayslipNo() { return payslipNo; }
    public void setPayslipNo(String payslipNo) { this.payslipNo = payslipNo; }

    public int getEmployeeID() { return employeeID; }
    public void setEmployeeID(int employeeID) { this.employeeID = employeeID; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public Date getPeriodStartDate() { return periodStartDate; }
    public void setPeriodStartDate(Date periodStartDate) { this.periodStartDate = periodStartDate; }

    public Date getPeriodEndDate() { return periodEndDate; }
    public void setPeriodEndDate(Date periodEndDate) { this.periodEndDate = periodEndDate; }

    public String getEmployeePositionDepartment() { return employeePositionDepartment; }
    public void setEmployeePositionDepartment(String employeePositionDepartment) { this.employeePositionDepartment = employeePositionDepartment; }

    public double getMonthlyRate() { return monthlyRate; }
    public void setMonthlyRate(double monthlyRate) { this.monthlyRate = monthlyRate; }

    public double getDailyRate() { return dailyRate; }
    public void setDailyRate(double dailyRate) { this.dailyRate = dailyRate; }

    public double getDaysWorked() { return daysWorked; }
    public void setDaysWorked(double daysWorked) { this.daysWorked = daysWorked; }

    public double getOvertimeHours() { return overtimeHours; }
    public void setOvertimeHours(double overtimeHours) { this.overtimeHours = overtimeHours; }

    public double getGrossIncome() { return grossIncome; }
    public void setGrossIncome(double grossIncome) { this.grossIncome = grossIncome; }

    public double getRiceSubsidy() { return riceSubsidy; }
    public void setRiceSubsidy(double riceSubsidy) { this.riceSubsidy = riceSubsidy; }

    public double getPhoneAllowance() { return phoneAllowance; }
    public void setPhoneAllowance(double phoneAllowance) { this.phoneAllowance = phoneAllowance; }

    public double getClothingAllowance() { return clothingAllowance; }
    public void setClothingAllowance(double clothingAllowance) { this.clothingAllowance = clothingAllowance; }

    public double getTotalBenefits() { return totalBenefits; }
    public void setTotalBenefits(double totalBenefits) { this.totalBenefits = totalBenefits; }

    public double getSss() { return sss; }
    public void setSss(double sss) { this.sss = sss; }

    public double getPhilhealth() { return philhealth; }
    public void setPhilhealth(double philhealth) { this.philhealth = philhealth; }

    public double getPagibig() { return pagibig; }
    public void setPagibig(double pagibig) { this.pagibig = pagibig; }

    public double getWithholdingTax() { return withholdingTax; }
    public void setWithholdingTax(double withholdingTax) { this.withholdingTax = withholdingTax; }

    public double getTotalDeductions() { return totalDeductions; }
    public void setTotalDeductions(double totalDeductions) { this.totalDeductions = totalDeductions; }

    public double getGrossIncomeDup() { return grossIncomeDup; }
    public void setGrossIncomeDup(double grossIncomeDup) { this.grossIncomeDup = grossIncomeDup; }

    public double getBenefits() { return benefits; }
    public void setBenefits(double benefits) { this.benefits = benefits; }

    public double getDeductions() { return deductions; }
    public void setDeductions(double deductions) { this.deductions = deductions; }

    public double getTakeHomePay() { return takeHomePay; }
    public void setTakeHomePay(double takeHomePay) { this.takeHomePay = takeHomePay; }
}