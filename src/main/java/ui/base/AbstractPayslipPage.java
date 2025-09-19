package ui.base;

import java.io.InputStream;
import pojo.Payslip;
import service.PayslipService;
import util.SessionManager;
import java.util.*;
import java.text.*;
import javax.swing.*;
import service.EmployeeService;

public abstract class AbstractPayslipPage extends JFrame {

    protected JComboBox<String> payslipPeriodComboBox;
    protected com.toedter.calendar.JDateChooser jDateChooser;
    protected JTextField monthlyRateField, hourlyRateField, daysWorkedField, hoursWorkedField, overtimeField, overtimePayField,
            totalGrossIncomeField, riceSubsidyField, phoneAllowanceField, clothingAllowanceField, totalBenefitsField,
            sssField, philhealthField, pagibigField, withholdingTaxField, totalDeductionsField, grossIncomeField,
            benefitsField, deductionsField, takeHomePayField;
    protected JButton printPayslipButton;
    protected JButton backButton;

    protected PayslipService payslipService;
    protected List<Payslip> payslipList;
    protected Payslip currentPayslip;
    protected boolean showingPlaceholderPayslip = true;
    protected String currentPeriodLabel = "";

    public AbstractPayslipPage() {
        int employeeID = SessionManager.getEmployeeID();
        this.payslipService = new PayslipService();
        this.payslipList = payslipService.getPayslipsByEmployeeID(employeeID);
    }

    protected void setComponentReferences(
            JComboBox<String> payslipPeriodComboBox,
            com.toedter.calendar.JDateChooser jDateChooser,
            JTextField monthlyRateField, JTextField hourlyRateField, JTextField daysWorkedField, JTextField hoursWorkedField,
            JTextField overtimeField, JTextField overtimePayField, JTextField totalGrossIncomeField, JTextField riceSubsidyField,
            JTextField phoneAllowanceField, JTextField clothingAllowanceField, JTextField totalBenefitsField,
            JTextField sssField, JTextField philhealthField, JTextField pagibigField, JTextField withholdingTaxField,
            JTextField totalDeductionsField, JTextField grossIncomeField, JTextField benefitsField, JTextField deductionsField,
            JTextField takeHomePayField, JButton printPayslipButton, JButton backButton
    ) {
        this.payslipPeriodComboBox = payslipPeriodComboBox;
        this.jDateChooser = jDateChooser;
        this.monthlyRateField = monthlyRateField;
        this.hourlyRateField = hourlyRateField;
        this.daysWorkedField = daysWorkedField;
        this.hoursWorkedField = hoursWorkedField;
        this.overtimeField = overtimeField;
        this.overtimePayField = overtimePayField;
        this.totalGrossIncomeField = totalGrossIncomeField;
        this.riceSubsidyField = riceSubsidyField;
        this.phoneAllowanceField = phoneAllowanceField;
        this.clothingAllowanceField = clothingAllowanceField;
        this.totalBenefitsField = totalBenefitsField;
        this.sssField = sssField;
        this.philhealthField = philhealthField;
        this.pagibigField = pagibigField;
        this.withholdingTaxField = withholdingTaxField;
        this.totalDeductionsField = totalDeductionsField;
        this.grossIncomeField = grossIncomeField;
        this.benefitsField = benefitsField;
        this.deductionsField = deductionsField;
        this.takeHomePayField = takeHomePayField;
        this.printPayslipButton = printPayslipButton;
        this.backButton = backButton;

        // 1. Set JDateChooser to current date immediately
        if (this.jDateChooser != null) {
            this.jDateChooser.setDate(new Date());
        }

        // 2. ComboBox must have payslip numbers but not select anything on init
        fillPayslipComboBoxInitial();
        // 3. On load, always show placeholder for current month
        setPlaceholderPayslipForCurrentMonth();
        updatePayslipFields();

        // 4. Register events (must be last)
        setupPayslipEvents();
    }

    // ComboBox contains payslip numbers, but shows blank until user picks
    private void fillPayslipComboBoxInitial() {
        payslipPeriodComboBox.removeAllItems();
        for (Payslip p : payslipList) {
            payslipPeriodComboBox.addItem(p.getPayslipNo());
        }
        payslipPeriodComboBox.setSelectedItem(null); // Remain blank even if items exist
    }

    private void setupPayslipEvents() {
        if (payslipPeriodComboBox != null) {
            payslipPeriodComboBox.addActionListener(e -> {
                Object selected = payslipPeriodComboBox.getSelectedItem();
                if (selected == null || "".equals(selected.toString().trim())) {
                    setPlaceholderPayslipForCurrentMonth();
                    updatePayslipFields();
                    return;
                }
                Payslip p = payslipList.stream()
                        .filter(slip -> slip.getPayslipNo().equals(selected.toString()))
                        .findFirst().orElse(null);
                if (p != null) {
                    if (jDateChooser != null) jDateChooser.setDate(p.getPeriodEndDate());
                    currentPayslip = p;
                    showingPlaceholderPayslip = false;
                    currentPeriodLabel = getPeriodLabel(p.getPeriodStartDate(), p.getPeriodEndDate());
                }
                updatePayslipFields();
            });
        }

        if (jDateChooser != null) {
            jDateChooser.getDateEditor().addPropertyChangeListener(evt -> {
                if ("date".equals(evt.getPropertyName())) {
                    Date selected = jDateChooser.getDate();
                    if (selected == null) {
                        setPlaceholderPayslipForCurrentMonth();
                        updatePayslipFields();
                        return;
                    }
                    // Find payslip for selected month/year
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(selected);
                    int selYear = cal.get(Calendar.YEAR);
                    int selMonth = cal.get(Calendar.MONTH) + 1;
                    Payslip found = null;
                    for (Payslip p : payslipList) {
                        Date end = p.getPeriodEndDate();
                        if (end != null) {
                            Calendar payslipCal = Calendar.getInstance();
                            payslipCal.setTime(end);
                            int payslipYear = payslipCal.get(Calendar.YEAR);
                            int payslipMonth = payslipCal.get(Calendar.MONTH) + 1;
                            if (payslipYear == selYear && payslipMonth == selMonth) {
                                found = p;
                                break;
                            }
                        }
                    }
                    if (found != null) {
                        currentPayslip = found;
                        showingPlaceholderPayslip = false;
                        payslipPeriodComboBox.setSelectedItem(found.getPayslipNo());
                        currentPeriodLabel = getPeriodLabel(found.getPeriodStartDate(), found.getPeriodEndDate());
                    } else {
                        payslipPeriodComboBox.setSelectedItem(null); // Blank combo
                        setPlaceholderPayslipForDate(selected);
                    }
                    updatePayslipFields();
                }
            });
        }

        if (printPayslipButton != null) {
            printPayslipButton.addActionListener(e -> handlePrintPayslip());
        }
    }

    private void setPlaceholderPayslipForCurrentMonth() {
        setPlaceholderPayslipForDate(new Date());
    }

    private void setPlaceholderPayslipForDate(Date date) {
        Calendar cal = Calendar.getInstance();
        if (date != null) cal.setTime(date);

        cal.set(Calendar.DAY_OF_MONTH, 1);
        Date periodStart = cal.getTime();
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date periodEnd = cal.getTime();

        currentPayslip = null;
        showingPlaceholderPayslip = true;
        currentPeriodLabel = getPeriodLabel(periodStart, periodEnd);
    }

    private String getPeriodLabel(Date periodStart, Date periodEnd) {
        return new SimpleDateFormat("MMMM dd, yyyy").format(periodStart) +
                " to " + new SimpleDateFormat("MMMM dd, yyyy").format(periodEnd);
    }

    protected void updatePayslipFields() {
        JTextField[] fields = {
                monthlyRateField, hourlyRateField, daysWorkedField, hoursWorkedField, overtimeField, overtimePayField,
                totalGrossIncomeField, riceSubsidyField, phoneAllowanceField, clothingAllowanceField, totalBenefitsField,
                sssField, philhealthField, pagibigField, withholdingTaxField, totalDeductionsField, grossIncomeField,
                benefitsField, deductionsField, takeHomePayField
        };
        if (showingPlaceholderPayslip || currentPayslip == null) {
            for (JTextField field : fields) if (field != null) field.setText("");
            return;
        }
        monthlyRateField.setText(formatAmount(currentPayslip.getMonthlyRate()));
        double hourlyRate = currentPayslip.getDailyRate() / 8.0;
        hourlyRateField.setText(formatAmount(hourlyRate));
        double daysWorked = currentPayslip.getDaysWorked() + (currentPayslip.getOvertimeHours() / 8.0);
        daysWorkedField.setText(String.format("%.2f days", daysWorked));
        double hoursWorked = (currentPayslip.getDaysWorked() * 8.0) + currentPayslip.getOvertimeHours();
        hoursWorkedField.setText(String.format("%.2f", hoursWorked));
        overtimeField.setText(formatAmount(currentPayslip.getOvertimeHours()));
        overtimePayField.setText(formatAmount((currentPayslip.getDailyRate() / 8.0) * currentPayslip.getOvertimeHours()));
        totalGrossIncomeField.setText(formatAmount(currentPayslip.getGrossIncome()));
        riceSubsidyField.setText(formatAmount(currentPayslip.getRiceSubsidy()));
        phoneAllowanceField.setText(formatAmount(currentPayslip.getPhoneAllowance()));
        clothingAllowanceField.setText(formatAmount(currentPayslip.getClothingAllowance()));
        totalBenefitsField.setText(formatAmount(currentPayslip.getTotalBenefits()));
        sssField.setText(formatAmount(currentPayslip.getSss()));
        philhealthField.setText(formatAmount(currentPayslip.getPhilhealth()));
        pagibigField.setText(formatAmount(currentPayslip.getPagibig()));
        withholdingTaxField.setText(formatAmount(currentPayslip.getWithholdingTax()));
        totalDeductionsField.setText(formatAmount(currentPayslip.getTotalDeductions()));
        grossIncomeField.setText(formatAmount(currentPayslip.getGrossIncome()));
        benefitsField.setText(formatAmount(currentPayslip.getBenefits()));
        deductionsField.setText(formatAmount(currentPayslip.getDeductions()));
        takeHomePayField.setText(formatAmount(currentPayslip.getTakeHomePay()));
    }

    protected String formatAmount(double amount) {
        return String.format("%.2f", amount);
    }

    protected String formatPeso(double amount) {
        return "₱" + String.format("%,.2f", amount);
    }

    protected void handlePrintPayslip() {
        // Guard: block print if no payslip is selected or fields are empty
        if (showingPlaceholderPayslip || currentPayslip == null ||
                takeHomePayField.getText() == null || takeHomePayField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No payslip selected or payslip data is incomplete. Please select a valid payslip period.",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            Map<String, Object> row = new HashMap<>();
            Map<String, Object> params = new HashMap<>();
            String payslipNo = currentPayslip.getPayslipNo();
            int employeeID = currentPayslip.getEmployeeID();

            EmployeeService empService = new EmployeeService();
            pojo.Employee emp = empService.getEmployeeByID(employeeID);

            String employeeIdStr = (emp != null) ? String.valueOf(emp.getEmployeeID()) : "--";
            String employeeNameStr = (emp != null) ? (emp.getLastName() + ", " + emp.getFirstName()) : "--";
            String positionDept = (emp != null) ? (emp.getPosition() + " / " + empService.getDepartmentName(emp.getDepartmentID())) : "--";

            String periodStart = new SimpleDateFormat("MMMM dd, yyyy").format(currentPayslip.getPeriodStartDate());
            String periodEnd = new SimpleDateFormat("MMMM dd, yyyy").format(currentPayslip.getPeriodEndDate());

            params.put("EMPLOYEE_ID", employeeIdStr);
            params.put("EMPLOYEE_NAME", employeeNameStr);
            params.put("POSITION_DEPT", positionDept);
            params.put("PAYSLIP_NO", payslipNo);
            params.put("PERIOD_LABEL", periodStart + " to " + periodEnd);
            params.put("PERIOD_START", periodStart);
            params.put("PERIOD_END", periodEnd);
            params.put("MONTH_YEAR", currentPeriodLabel);
            params.put("MONTH_LABEL", currentPeriodLabel);

            row.put("monthlyRate", formatAmount(currentPayslip.getMonthlyRate()));
            row.put("dailyRate", formatAmount(currentPayslip.getDailyRate()));
            row.put("daysWorked", formatAmount(currentPayslip.getDaysWorked()));
            row.put("overtimeHours", formatAmount(currentPayslip.getOvertimeHours()));
            row.put("grossIncome", formatAmount(currentPayslip.getGrossIncome()));
            row.put("riceSubsidy", formatAmount(currentPayslip.getRiceSubsidy()));
            row.put("phoneAllowance", formatAmount(currentPayslip.getPhoneAllowance()));
            row.put("clothingAllowance", formatAmount(currentPayslip.getClothingAllowance()));
            row.put("totalBenefits", formatAmount(currentPayslip.getTotalBenefits()));
            row.put("sss", formatAmount(currentPayslip.getSss()));
            row.put("philhealth", formatAmount(currentPayslip.getPhilhealth()));
            row.put("pagibig", formatAmount(currentPayslip.getPagibig()));
            row.put("withholdingTax", formatAmount(currentPayslip.getWithholdingTax()));
            row.put("totalDeductions", formatAmount(currentPayslip.getTotalDeductions()));
            row.put("benefits", formatAmount(currentPayslip.getBenefits()));
            row.put("deductions", formatAmount(currentPayslip.getDeductions()));
            row.put("netPay", formatPeso(currentPayslip.getTakeHomePay()));

            List<Map<String, Object>> dataList = Collections.singletonList(row);
            net.sf.jasperreports.engine.data.JRBeanCollectionDataSource dataSource =
                    new net.sf.jasperreports.engine.data.JRBeanCollectionDataSource(dataList);

            InputStream reportStream = getClass().getResourceAsStream("/reports/PayslipReport.jrxml");
            if (reportStream == null) {
                JOptionPane.showMessageDialog(this, "Payslip template not found. Make sure /reports/PayslipReport.jrxml is in the classpath.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            net.sf.jasperreports.engine.JasperReport jasperReport =
                    net.sf.jasperreports.engine.JasperCompileManager.compileReport(reportStream);
            net.sf.jasperreports.engine.JasperPrint jasperPrint =
                    net.sf.jasperreports.engine.JasperFillManager.fillReport(
                            jasperReport, params, dataSource);
            String fileName = "payslip_" + payslipNo + "_" + System.currentTimeMillis() + ".pdf";
            java.io.File pdfFile = new java.io.File(System.getProperty("java.io.tmpdir"), fileName);
            net.sf.jasperreports.engine.JasperExportManager.exportReportToPdfFile(jasperPrint, pdfFile.getAbsolutePath());
            JOptionPane.showMessageDialog(this, "Payslip PDF generated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            java.awt.Desktop.getDesktop().browse(pdfFile.toURI());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to generate payslip report.\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}