package ui.base;

import com.toedter.calendar.JDateChooser;
import pojo.Attendance;
import pojo.Employee;
import service.AttendanceService;
import service.EmployeeService;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public abstract class AbstractAdminAttendancePage extends JFrame {
    protected JTable attendanceTable;
    protected JDateChooser dateChooser;
    protected JComboBox<String> employeeIDComboBox;
    protected JButton printAttendanceButton;

    protected AttendanceService attendanceService = new AttendanceService();
    protected EmployeeService employeeService = new EmployeeService();

    protected String jasperTemplatePath = "/reports/AttendanceAdminReport.jrxml";

    protected void setupAdminAttendancePage(
            JTable attendanceTable, JDateChooser dateChooser,
            JComboBox<String> employeeIDComboBox, JButton printAttendanceButton)
    {
        this.attendanceTable = attendanceTable;
        this.dateChooser = dateChooser;
        this.employeeIDComboBox = employeeIDComboBox;
        this.printAttendanceButton = printAttendanceButton;

        // Fill combo with all employeeIDs that have attendance
        Set<Integer> employeeIDsWithAttendance = new TreeSet<>();
        for (Attendance att : attendanceService.getAllAttendance()) {
            employeeIDsWithAttendance.add(att.getEmployeeID());
        }
        employeeIDComboBox.removeAllItems();
        employeeIDComboBox.addItem(""); // blank = all
        for (Integer empID : employeeIDsWithAttendance) {
            employeeIDComboBox.addItem(String.valueOf(empID));
        }

        employeeIDComboBox.setSelectedIndex(0);
        dateChooser.setDate(null);

        // Listeners for filters
        employeeIDComboBox.addActionListener(e -> updateAdminAttendanceTable());
        dateChooser.getDateEditor().addPropertyChangeListener(evt -> {
            if ("date".equals(evt.getPropertyName())) updateAdminAttendanceTable();
        });

        // Initial show (sets up model and column alignment properly)
        updateAdminAttendanceTable();

        printAttendanceButton.addActionListener(e -> handlePrintAttendance());
    }

    protected void updateAdminAttendanceTable() {
        String empIDStr = (String) employeeIDComboBox.getSelectedItem();
        Date chosenDate = (dateChooser.getDate() != null) ? new Date(dateChooser.getDate().getTime()) : null;
        List<Attendance> records = attendanceService.getAllAttendance();

        // Filter by employeeID if set
        if (empIDStr != null && !empIDStr.isBlank()) {
            int empID = Integer.parseInt(empIDStr);
            records.removeIf(a -> a.getEmployeeID() != empID);
        }

        if (chosenDate != null) {
            Calendar filterCal = Calendar.getInstance();
            filterCal.setTime(chosenDate);
            int year = filterCal.get(Calendar.YEAR);
            int month = filterCal.get(Calendar.MONTH) + 1;
            int day = filterCal.get(Calendar.DAY_OF_MONTH);

            boolean filterToDay = (empIDStr != null && !empIDStr.isBlank());

            List<Attendance> filtered = new ArrayList<>();
            Attendance pickedDay = null;
            for (Attendance a : records) {
                Calendar aCal = Calendar.getInstance();
                aCal.setTime(a.getDate());
                if (aCal.get(Calendar.YEAR) == year && aCal.get(Calendar.MONTH) + 1 == month) {
                    if (filterToDay && aCal.get(Calendar.DAY_OF_MONTH) == day) {
                        pickedDay = a;
                    } else {
                        filtered.add(a);
                    }
                }
            }
            List<Attendance> displayList = new ArrayList<>();
            if (filterToDay && pickedDay != null) displayList.add(pickedDay);
            displayList.addAll(filtered);
            setAdminAttendanceTableModel(displayList);
            return;
        }

        records.sort(Comparator.comparing(Attendance::getDate).thenComparing(Attendance::getEmployeeID));
        setAdminAttendanceTableModel(records);
    }

    protected void setAdminAttendanceTableModel(List<Attendance> displayList) {
        String[] colNames = {"Employee ID", "Date", "Log In", "Log Out", "Worked Hours"};
        DefaultTableModel model = new DefaultTableModel(colNames, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        for (Attendance a : displayList) {
            String dateStr = new SimpleDateFormat("yyyy-MM-dd").format(a.getDate());
            String login = a.getLogIn() != null ? a.getLogIn().toString() : "";
            String logout = a.getLogOut() != null ? a.getLogOut().toString() : "";
            String worked = formatWorkedHours(a.getWorkedHours());
            model.addRow(new Object[]{a.getEmployeeID(), dateStr, login, logout, worked});
        }
        attendanceTable.setModel(model);

        // Only align columns if the correct number exists (prevent crash)
        int colCount = attendanceTable.getColumnCount();
        for (int i = 0; i < colCount; ++i)
            centerAlignColumn(attendanceTable, i);
    }

    protected String formatWorkedHours(double decimalHours) {
        int hours = (int) decimalHours;
        int mins = (int) Math.round((decimalHours - hours) * 60);
        return String.format("%02d hrs, %02d min", hours, mins);
    }

    protected void centerAlignColumn(JTable table, int col) {
        if (col >= table.getColumnCount()) return; // safety
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(col).setCellRenderer(renderer);
    }

    // --- Print only what is shown in the table (filtered/grouped) ---
    protected void handlePrintAttendance() {
        try {
            DefaultTableModel model = (DefaultTableModel) attendanceTable.getModel();
            if (model.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "No attendance data to print.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            // Prepare dataList with exactly the rows in the table
            List<Map<String, ?>> dataList = new ArrayList<>();
            Map<Integer, Employee> empCache = new HashMap<>();
            for (int i = 0; i < model.getRowCount(); i++) {
                int empID = Integer.parseInt(model.getValueAt(i, 0).toString());
                Employee emp = empCache.computeIfAbsent(empID, employeeService::getEmployeeByID);
                String empName = emp != null ? emp.getLastName() + ", " + emp.getFirstName() : "";
                Map<String, Object> row = new HashMap<>();
                row.put("employeeID", empID);
                row.put("employeeName", empName);
                row.put("date", model.getValueAt(i, 1));
                row.put("logIn", model.getValueAt(i, 2));
                row.put("logOut", model.getValueAt(i, 3));
                row.put("workedHours", model.getValueAt(i, 4));
                dataList.add(row);
            }
            // Sort to group by employeeID (for proper pagination in report)
            dataList.sort(Comparator.comparing(m -> Integer.parseInt(m.get("employeeID").toString())));

            Map<String, Object> params = new HashMap<>();

            net.sf.jasperreports.engine.data.JRBeanCollectionDataSource dataSource =
                    new net.sf.jasperreports.engine.data.JRBeanCollectionDataSource(dataList);

            net.sf.jasperreports.engine.JasperReport jasperReport =
                    net.sf.jasperreports.engine.JasperCompileManager.compileReport(
                            getClass().getResourceAsStream(jasperTemplatePath));
            net.sf.jasperreports.engine.JasperPrint jasperPrint =
                    net.sf.jasperreports.engine.JasperFillManager.fillReport(
                            jasperReport, params, dataSource);

            String fileName = "attendance_admin_" + System.currentTimeMillis() + ".pdf";
            File pdfFile = new File(System.getProperty("java.io.tmpdir"), fileName);
            net.sf.jasperreports.engine.JasperExportManager.exportReportToPdfFile(jasperPrint, pdfFile.getAbsolutePath());

            JOptionPane.showMessageDialog(this, "Attendance PDF generated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            Desktop.getDesktop().browse(pdfFile.toURI());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to generate attendance report.\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}