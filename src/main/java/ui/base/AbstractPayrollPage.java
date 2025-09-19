package ui.base;

import pojo.Payslip;
import service.PayslipService;
import util.SessionManager;

import com.toedter.calendar.JDateChooser;
import net.sf.jasperreports.engine.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.io.File;
import java.io.InputStream;
import java.sql.Date;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

public abstract class AbstractPayrollPage extends JFrame {

    protected JTable payrollTable;
    protected JTextField totalGrossField;
    protected JTextField totalContributionsField;
    protected JTextField totalDeductionsField;
    protected JTextField totalNetPayField;
    protected JDateChooser dateChooser;

    protected final PayslipService payslipService = new PayslipService();

    // For readable headers
    private final String[] payrollTableCols = {
        "Payslip No", "Employee ID", "Employee Name",
        "Position/Department", "Gross Income",
        "Contributions", "Deductions", "Net Pay"
    };
    // Data keys for population
    private final String[] payrollTableKeys = {
        "PAYSILP_NO", "EMPLOYEE_ID", "EMPLOYEE_NAME",
        "EMPLOYEE_POSITION_DEPARTMENT",
        "GROSS_INCOME", "TOTAL_BENEFITS",
        "TOTAL_DEDUCTIONS", "TAKE_HOME_PAY"
    };

    protected List<Payslip> currentPayslipList = new ArrayList<>();

    protected void setComponentReferences(
        JTable payrollTable,
        JDateChooser dateChooser,
        JTextField totalGrossField,
        JTextField totalContributionsField,
        JTextField totalDeductionsField,
        JTextField totalNetPayField
    ) {
        this.payrollTable = payrollTable;
        this.dateChooser = dateChooser;
        this.totalGrossField = totalGrossField;
        this.totalContributionsField = totalContributionsField;
        this.totalDeductionsField = totalDeductionsField;
        this.totalNetPayField = totalNetPayField;

        // Set model with proper headers
        payrollTable.setModel(new DefaultTableModel(new Object[0][0], payrollTableCols) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });

        // Ensure horizontal scroll always visible
        payrollTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // Set preferred widths (tweak as needed)
        int[] colWidths = {90, 90, 150, 160, 110, 110, 110, 110};
        TableColumnModel colModel = payrollTable.getColumnModel();
        for (int i = 0; i < colWidths.length; i++) {
            colModel.getColumn(i).setPreferredWidth(colWidths[i]);
        }

        // Set alignment: center for all except name and position/department
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        for (int i = 0; i < payrollTableCols.length; i++) {
            if (i != 2 && i != 3) { // Not Employee Name or Position/Dept
                colModel.getColumn(i).setCellRenderer(centerRenderer);
            }
        }
        // Left alignment for name and position
        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        colModel.getColumn(2).setCellRenderer(leftRenderer);
        colModel.getColumn(3).setCellRenderer(leftRenderer);

        // Always put in a scroll pane in NetBeans UI Builder!
        // (this code is here for clarity—NetBeans form will handle this)

        if (dateChooser.getDate() == null) {
            dateChooser.setDate(new java.util.Date());
        }

        dateChooser.getDateEditor().addPropertyChangeListener("date", evt -> refreshPayrollTable());

        refreshPayrollTable();
    }

    protected void refreshPayrollTable() {
        Date selectedDate = dateChooser.getDate() == null
                ? Date.valueOf(LocalDate.now())
                : new Date(dateChooser.getDate().getTime());

        List<Payslip> payslips = getPayslipsForMonth(selectedDate);
        this.currentPayslipList = payslips;

        DefaultTableModel model = (DefaultTableModel) payrollTable.getModel();
        model.setRowCount(0);
        DecimalFormat pesoFmt = new DecimalFormat("#,##0.00");
        for (Payslip p : payslips) {
            model.addRow(new Object[] {
                p.getPayslipNo(),
                p.getEmployeeID(),
                p.getEmployeeName(),
                p.getEmployeePositionDepartment(),
                pesoFmt.format(p.getGrossIncome()),
                pesoFmt.format(p.getTotalBenefits()),
                pesoFmt.format(p.getTotalDeductions()),
                pesoFmt.format(p.getTakeHomePay())
            });
        }

        double gross = payslips.stream().mapToDouble(Payslip::getGrossIncome).sum();
        double contrib = payslips.stream().mapToDouble(Payslip::getTotalBenefits).sum();
        double deduct = payslips.stream().mapToDouble(Payslip::getTotalDeductions).sum();
        double net   = payslips.stream().mapToDouble(Payslip::getTakeHomePay).sum();

        totalGrossField.setText(pesoFmt.format(gross));
        totalContributionsField.setText(pesoFmt.format(contrib));
        totalDeductionsField.setText(pesoFmt.format(deduct));
        totalNetPayField.setText(pesoFmt.format(net));
    }

    protected List<Payslip> getPayslipsForMonth(Date date) {
        if (date == null) return Collections.emptyList();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int filterMonth = cal.get(Calendar.MONTH) + 1;
        int filterYear = cal.get(Calendar.YEAR);

        List<Payslip> allPayslips = payslipService.getAllPayslips();
        return allPayslips.stream()
                .filter(p -> {
                    if (p.getPeriodEndDate() == null) return false;
                    Calendar pc = Calendar.getInstance();
                    pc.setTime(p.getPeriodEndDate());
                    return (pc.get(Calendar.MONTH) + 1 == filterMonth)
                        && (pc.get(Calendar.YEAR) == filterYear);
                })
                .collect(Collectors.toList());
    }

    protected boolean isTableEmpty() {
        return currentPayslipList == null || currentPayslipList.isEmpty();
    }

    protected void printPayrollReport(Window parent) {
        if (isTableEmpty()) {
            JOptionPane.showMessageDialog(parent,
                "No payroll data to print for the selected period.",
                "No Data", JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        try {
            InputStream reportStream = getClass().getClassLoader().getResourceAsStream("reports/PayrollReport.jrxml");
            if (reportStream == null) {
                throw new RuntimeException("Could not find PayrollReport.jrxml in resources/reports");
            }
            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

            SimpleDateFormat fmt = new SimpleDateFormat("MMMM yyyy");
            Map<String, Object> params = new HashMap<>();
            params.put("payrollPeriod", fmt.format(dateChooser.getDate()));
            params.put("totalGross",  totalGrossField.getText());
            params.put("totalBenefits", totalContributionsField.getText());
            params.put("totalDeductions", totalDeductionsField.getText());
            params.put("totalNetPay", totalNetPayField.getText());

            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(currentPayslipList);

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, dataSource);

            String filename = "PayrollReport_" + fmt.format(dateChooser.getDate()).replace(" ", "_") + ".pdf";
            String outputDir = System.getProperty("user.home") + File.separator + "Desktop";
            File outFile = new File(outputDir, filename);

            JasperExportManager.exportReportToPdfFile(jasperPrint, outFile.getAbsolutePath());

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(outFile);
            } else {
                JOptionPane.showMessageDialog(parent, "PDF saved to: " + outFile.getAbsolutePath());
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parent,
                "Failed to generate report: " + e.getMessage(),
                "Print Error", JOptionPane.ERROR_MESSAGE
            );
        }
    }

    protected String getUserID() { return SessionManager.getUserID(); }
    protected int getEmployeeID() { return SessionManager.getEmployeeID(); }
}