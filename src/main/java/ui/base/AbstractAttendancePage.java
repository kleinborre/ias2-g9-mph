package ui.base;

import com.toedter.calendar.JDateChooser;
import pojo.Attendance;
import pojo.Employee;
import service.AttendanceService;
import service.EmployeeService;
import util.SessionManager;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.math.BigDecimal;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public abstract class AbstractAttendancePage extends JFrame {
    protected JTable attendanceTable;
    protected JDateChooser dateChooser;
    protected JTextField totalWorkedHoursField;
    protected JButton printAttendanceButton;

    protected AttendanceService attendanceService = new AttendanceService();
    protected EmployeeService employeeService = new EmployeeService();

    // -- For use by the extending page
    protected int employeeID;
    protected Employee employee;

    // JasperReport template
    protected String jasperTemplatePath = "/reports/AttendanceEmployeeReport.jrxml"; // must exist

    // Must be called after initComponents and field assignments
    protected void setupAttendancePage(JTable attendanceTable, JDateChooser dateChooser,
                                       JTextField totalWorkedHoursField, JButton printAttendanceButton,
                                       int employeeID) {
        this.attendanceTable = attendanceTable;
        this.dateChooser = dateChooser;
        this.totalWorkedHoursField = totalWorkedHoursField;
        this.printAttendanceButton = printAttendanceButton;
        this.employeeID = employeeID;

        // Get employee details (for report)
        this.employee = employeeService.getEmployeeByID(employeeID);

        // Table: Center worked hours column
        centerAlignColumn(attendanceTable, 3);

        // Attach listener for date changes
        dateChooser.getDateEditor().addPropertyChangeListener(evt -> {
            if ("date".equals(evt.getPropertyName())) {
                if (dateChooser.getDate() != null) {
                    updateAttendanceTableAndHoursFiltered();
                }
            }
        });

        // Initial display: show all records (not filtered by date)
        updateAttendanceTableAndHoursAll();

        // Print Attendance Handler
        printAttendanceButton.addActionListener(e -> handlePrintAttendance());
    }

    // Show all records on initial load (and when date filter is cleared)
    protected void updateAttendanceTableAndHoursAll() {
        List<Attendance> records = attendanceService.getAttendanceByEmployeeID(employeeID);
        loadTableAndTotal(records);
    }

    // Show only filtered (by selected month/year/date)
    protected void updateAttendanceTableAndHoursFiltered() {
        Date chosenDate = (dateChooser.getDate() != null) ? new Date(dateChooser.getDate().getTime()) : null;
        if (chosenDate == null) return;

        Calendar cal = Calendar.getInstance();
        cal.setTime(chosenDate);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1; // Calendar.MONTH is 0-based

        // Fetch all attendance for this employee for selected month/year
        List<Attendance> records = attendanceService.getAttendanceByEmployeeID(employeeID);
        List<Attendance> filtered = new ArrayList<>();
        Attendance pickedDay = null;

        for (Attendance a : records) {
            Calendar rc = Calendar.getInstance();
            rc.setTime(a.getDate());
            if (rc.get(Calendar.YEAR) == year && rc.get(Calendar.MONTH) + 1 == month) {
                if (a.getDate().equals(chosenDate)) {
                    pickedDay = a;
                } else {
                    filtered.add(a);
                }
            }
        }

        // Show picked date on top (if any), then rest of month
        List<Attendance> displayList = new ArrayList<>();
        if (pickedDay != null) displayList.add(pickedDay);
        displayList.addAll(filtered);

        loadTableAndTotal(displayList);
    }

    protected void loadTableAndTotal(List<Attendance> displayList) {
        String[] colNames = {"Date", "Log In", "Log Out", "Worked Hours"};
        DefaultTableModel model = new DefaultTableModel(colNames, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        double totalHours = 0;
        for (Attendance a : displayList) {
            String dateStr = new SimpleDateFormat("yyyy-MM-dd").format(a.getDate());
            String login = a.getLogIn() != null ? a.getLogIn().toString() : "";
            String logout = a.getLogOut() != null ? a.getLogOut().toString() : "";
            String worked = formatWorkedHours(a.getWorkedHours());
            model.addRow(new Object[]{dateStr, login, logout, worked});
            totalHours += a.getWorkedHours();
        }
        attendanceTable.setModel(model);

        // Column alignment: all centered
        centerAlignColumn(attendanceTable, 0);
        centerAlignColumn(attendanceTable, 1);
        centerAlignColumn(attendanceTable, 2);
        centerAlignColumn(attendanceTable, 3);

        // Total worked hours in hrs/min
        totalWorkedHoursField.setText(formatWorkedHours(totalHours));
    }

    // --- Formatting worked hours (double, ex: 8.50) into "08 hrs, 30 min"
    protected String formatWorkedHours(double decimalHours) {
        int hours = (int) decimalHours;
        int mins = (int) Math.round((decimalHours - hours) * 60);
        return String.format("%02d hrs, %02d min", hours, mins);
    }

    // Column alignment helper
    protected void centerAlignColumn(JTable table, int col) {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(col).setCellRenderer(renderer);
    }

    /** Printing: Generates PDF JasperReport using current table data. */
    protected void handlePrintAttendance() {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("employeeNo", String.valueOf(employee.getEmployeeID()));
            params.put("employeeName", employee.getLastName() + ", " + employee.getFirstName());

            // Calculate date range from table model, formatted as 'MMMM dd, yyyy'
            DefaultTableModel model = (DefaultTableModel) attendanceTable.getModel();
            String dateRange = "";
            if (model.getRowCount() > 0) {
                // Parse first and last dates from string in table
                SimpleDateFormat inputFmt = new SimpleDateFormat("yyyy-MM-dd");  // match your table format
                SimpleDateFormat outFmt = new SimpleDateFormat("MMMM dd, yyyy"); // pretty format
                try {
                    Date first = new Date(inputFmt.parse(String.valueOf(model.getValueAt(0, 0))).getTime());
                    Date last = new Date(inputFmt.parse(String.valueOf(model.getValueAt(model.getRowCount() - 1, 0))).getTime());
                    if (first.equals(last)) {
                        dateRange = outFmt.format(first);
                    } else {
                        dateRange = outFmt.format(first) + " to " + outFmt.format(last);
                    }
                } catch (Exception pe) {
                    // fallback to whatever's in table
                    String startDate = String.valueOf(model.getValueAt(0, 0));
                    String endDate = String.valueOf(model.getValueAt(model.getRowCount() - 1, 0));
                    dateRange = startDate.equals(endDate) ? startDate : (startDate + " to " + endDate);
                }
            }
            params.put("dateRange", dateRange);
            params.put("totalWorkedHours", totalWorkedHoursField.getText());

            // Prepare data source
            List<Map<String, ?>> dataList = new ArrayList<>();
            for (int i = 0; i < model.getRowCount(); i++) {
                Map<String, Object> row = new HashMap<>();
                row.put("date", model.getValueAt(i, 0));
                row.put("logIn", model.getValueAt(i, 1));
                row.put("logOut", model.getValueAt(i, 2));
                row.put("workedHours", model.getValueAt(i, 3));
                dataList.add(row);
            }
            net.sf.jasperreports.engine.data.JRBeanCollectionDataSource dataSource =
                    new net.sf.jasperreports.engine.data.JRBeanCollectionDataSource(dataList);

            // Compile and fill report
            net.sf.jasperreports.engine.JasperReport jasperReport =
                    net.sf.jasperreports.engine.JasperCompileManager.compileReport(
                        getClass().getResourceAsStream(jasperTemplatePath)
                    );
            net.sf.jasperreports.engine.JasperPrint jasperPrint =
                    net.sf.jasperreports.engine.JasperFillManager.fillReport(
                        jasperReport, params, dataSource);

            // Export to PDF
            String fileName = "attendance_" + employee.getEmployeeID()
                    + "_" + System.currentTimeMillis() + ".pdf";
            File pdfFile = new File(System.getProperty("java.io.tmpdir"), fileName);
            net.sf.jasperreports.engine.JasperExportManager.exportReportToPdfFile(jasperPrint, pdfFile.getAbsolutePath());

            JOptionPane.showMessageDialog(this, "Attendance PDF generated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            Desktop.getDesktop().browse(pdfFile.toURI());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to generate attendance report.\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}