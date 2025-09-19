package ui.base;

import pojo.Employee;
import pojo.Leave;
import service.AttendanceService;
import service.EmployeeService;
import service.LeaveService;
import util.SessionManager;
import ui.PageLogin;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractHomePage extends JFrame {

    protected String userID;
    protected int employeeID;

    private Timer clockTimer;
    private LocalDate lastAutoDate = null;

    public boolean isClockedInToday  = false;
    public boolean isClockedOutToday = false;

    private LocalDateTime clockInDateTime  = null;
    private LocalDateTime clockOutDateTime = null;

    protected AttendanceService attendanceService = new AttendanceService();
    protected LeaveService leaveService = new LeaveService(); // Added to support leaveAllowance lookup

    /**
     * Call once you have userID & employeeID.
     */
    protected void initializeHomePage(String userID, int employeeID) {
        this.userID     = userID;
        this.employeeID = employeeID;
        loadEmployeeInfo();

        installProfileClick(getFullNameText());
        installProfileClick(getPositionText());

        updateLeaveAllowanceDisplay(); // --- Always display most recent leave allowance
        startClock();
        refreshClockInOutStatus();
    }

    private void loadEmployeeInfo() {
        try {
            Employee e = new EmployeeService().getEmployeeByID(employeeID);
            if (e != null) {
                getFullNameText().setText(e.getFirstName() + " " + e.getLastName());
                getPositionText().setText(e.getPosition());
            } else {
                getFullNameText().setText("Unknown Employee");
                getPositionText().setText("Unknown Position");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Error loading employee info: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
            ex.printStackTrace();
        }
    }

    private void installProfileClick(JLabel lbl) {
        lbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        Color normal = lbl.getForeground();
        Color hover  = new Color(0, 102, 204);
        lbl.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                String txt = lbl.getText();
                lbl.setText("<html><u>" + txt + "</u></html>");
                lbl.setForeground(hover);
            }
            @Override public void mouseExited(MouseEvent e) {
                String txt = lbl.getText().replaceAll("\\<.*?\\>", "");
                lbl.setText(txt);
                lbl.setForeground(normal);
            }
            @Override public void mouseClicked(MouseEvent e) {
                onProfileLabelClick();
            }
        });
    }

    /** Subclasses implement to open their profile/details page */
    protected abstract void onProfileLabelClick();

    protected void startClock() {
        clockTimer = new Timer(1000, evt -> {
            LocalDateTime now = LocalDateTime.now();

            // update date/time display
            String datePart = now.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"));
            String timePart = now.format(DateTimeFormatter.ofPattern("hh:mm:ss a"));
            String html =
              "<html><div align='center'>"
            +   "<font size='6'>" + datePart + "</font><br>"
            +   "<font size='8'>" + timePart + "</font>"
            + "</div></html>";
            getDateTimeText().setText(html);

            // auto–clock‐out at 6:50 once/day
            if (now.getHour()==6 && now.getMinute()==50) {
                LocalDate today = now.toLocalDate();
                if (!today.equals(lastAutoDate)) {
                    try { autoClockOutYesterday(); }
                    catch (Exception e) { e.printStackTrace(); }
                    lastAutoDate = today;
                    refreshClockInOutStatus();
                }
            }

            // update Clock‐In button state
            updateClockInButtonAvailability();

            // update hours display
            try {
                updateWorkedHoursDisplay(now);
            } catch (SQLException ex) {
                Logger.getLogger(AbstractHomePage.class.getName())
                      .log(Level.SEVERE, null, ex);
            }

            // update leave allowance display dynamically every tick
            updateLeaveAllowanceDisplay();

        });
        clockTimer.setInitialDelay(0);
        clockTimer.start();
    }

    private void autoClockOutYesterday() throws Exception {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalTime cutoff    = LocalTime.of(6,50);
        attendanceService.autoClockOutForDate(employeeID, yesterday, cutoff);
    }

    private void updateClockInButtonAvailability() {
        boolean shouldEnable;
        if (isClockedInToday) {
            shouldEnable = false;
        } else {
            LocalDate today = LocalDate.now();
            DayOfWeek dow   = today.getDayOfWeek();
            LocalTime now   = LocalTime.now();
            boolean weekend = (dow==DayOfWeek.SATURDAY || dow==DayOfWeek.SUNDAY);
            boolean tooEarly= now.isBefore(LocalTime.of(6,50));
            boolean tooLate = now.isAfter(LocalTime.of(16,0));
            shouldEnable = !weekend && !tooEarly && !tooLate;
        }
        getClockInButton().setEnabled(shouldEnable);
    }

    public void performClockIn() {
        try {
            if (isClockedInToday) {
                JOptionPane.showMessageDialog(this,
                  "You have already clocked in today.");
                return;
            }
            boolean ok = attendanceService.clockIn(employeeID);
            if (ok) {
                clockInDateTime = LocalDateTime.now();
                clockOutDateTime = null;
                isClockedInToday = true;
                getClockInText().setText(getCurrentTime());
                getClockOutButton().setEnabled(true);
                JOptionPane.showMessageDialog(this,
                  "Clock-In successful!", "Success",
                  JOptionPane.INFORMATION_MESSAGE
                );
                refreshClockInOutStatus();
            } else {
                JOptionPane.showMessageDialog(this,
                  "Unable to clock in.", "Error",
                  JOptionPane.ERROR_MESSAGE
                );
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
              "Error during Clock-In: " + ex.getMessage(),
              "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    public void performClockOut() {
        try {
            if (!isClockedInToday) {
                JOptionPane.showMessageDialog(this,
                  "You must clock in first.");
                return;
            }
            if (isClockedOutToday) {
                JOptionPane.showMessageDialog(this,
                  "You have already clocked out today.");
                return;
            }
            boolean ok = attendanceService.clockOut(employeeID);
            if (ok) {
                clockOutDateTime = LocalDateTime.now();
                isClockedOutToday = true;
                getClockOutText().setText(getCurrentTime());
                getClockInButton().setEnabled(false);
                getClockOutButton().setEnabled(false);
                JOptionPane.showMessageDialog(this,
                  "Clock-Out successful!", "Success",
                  JOptionPane.INFORMATION_MESSAGE
                );
                refreshClockInOutStatus();
            } else {
                JOptionPane.showMessageDialog(this,
                  "Unable to clock out.", "Error",
                  JOptionPane.ERROR_MESSAGE
                );
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
              "Error during Clock-Out: " + ex.getMessage(),
              "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    public void refreshClockInOutStatus() {
        try {
            var s = attendanceService.getTodayAttendanceStatus(employeeID);

            if (s.isClockedIn()) {
                isClockedInToday = true;
                String inStr = s.getLogIn();
                getClockInText().setText(inStr);
                clockInDateTime = parsePossiblyTimeOnly(inStr);
                getClockOutButton().setEnabled(!s.isClockedOut());
            } else {
                isClockedInToday = false;
                clockInDateTime = null;
                getClockInText().setText("Not Clocked-In");
                getClockOutButton().setEnabled(false);
            }
            if (s.isClockedOut()) {
                isClockedOutToday = true;
                String outStr = s.getLogOut();
                getClockOutText().setText(outStr);
                clockOutDateTime = parsePossiblyTimeOnly(outStr);
            } else {
                isClockedOutToday = false;
                clockOutDateTime = null;
                getClockOutText().setText("Not Clocked-Out");
            }

            updateClockInButtonAvailability();
            updateWorkedHoursDisplay(LocalDateTime.now());
            updateLeaveAllowanceDisplay();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
              "Error refreshing attendance status: " + ex.getMessage(),
              "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    // Dynamically update leavesAvailableText
    protected void updateLeaveAllowanceDisplay() {
        try {
            List<Leave> leaves = leaveService.getLeavesByEmployeeID(employeeID);
            double allowance = 0;
            if (leaves != null && !leaves.isEmpty()) {
                // Find the latest (most recent) leave by dateCreated
                leaves.sort(Comparator.comparing(Leave::getDateCreated).reversed());
                allowance = leaves.get(0).getLeaveAllowance();
            }
            getLeavesAvailableText().setText(String.format("%.0f", allowance));
        } catch (Exception ex) {
            getLeavesAvailableText().setText("0");
        }
    }

    /**
     * If the string is only HH:mm:ss, prepend today’s date;
     * otherwise parse as full yyyy-MM-dd HH:mm:ss.
     */
    private LocalDateTime parsePossiblyTimeOnly(String txt) {
      try {
        if (txt.length() <= 8 && txt.matches("\\d{2}:\\d{2}:\\d{2}")) {
          LocalTime t = LocalTime.parse(txt, DateTimeFormatter.ISO_TIME);
          return LocalDate.now().atTime(t);
        }
        return LocalDateTime.parse(
          txt,
          DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        );
      } catch (Exception ex) {
        return LocalDateTime.now();
      }
    }

    private void updateWorkedHoursDisplay(LocalDateTime now) throws SQLException {
        YearMonth ym = YearMonth.now();
        BigDecimal decHrs = attendanceService
          .getMonthlyWorkedHours(employeeID, ym.getYear(), ym.getMonthValue());
        long totalMin = decHrs.multiply(BigDecimal.valueOf(60))
                              .setScale(0, RoundingMode.HALF_UP)
                              .longValue();
        long mH = totalMin / 60, mM = totalMin % 60;
        String monthPart = String.format("%d hrs, %d min", mH, mM);

        String dailyPart = null;
        if (clockInDateTime != null && clockInDateTime.toLocalDate().equals(now.toLocalDate())) {
            LocalDateTime end = (clockOutDateTime!=null ? clockOutDateTime : now);
            Duration d = Duration.between(clockInDateTime, end);
            long seconds = d.getSeconds();
            long h = seconds / 3600;
            long rem = seconds % 3600;
            long mi = rem / 60;
            long s = rem % 60;
            dailyPart = String.format("%d hrs, %d min, %d sec", h, mi, s);
        }

        String text = (dailyPart!=null)
          ? String.format("<html>%s<br><font size='4'>(today: %s)</font></html>", monthPart, dailyPart)
          : monthPart;

        getTotalWorkedHoursText().setText(text);
    }

    protected void initLogoutButton(JButton logoutButton) {
        logoutButton.addActionListener(e -> {
            int ans = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            if (ans==JOptionPane.YES_OPTION) {
                SessionManager.clearSession();
                new PageLogin().setVisible(true);
                dispose();
            }
        });
    }

    private String getCurrentTime() {
        return LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    // --- Subclasses supply these getters and the click hook ---
    protected abstract JLabel  getFullNameText();
    protected abstract JLabel  getPositionText();
    protected abstract JLabel  getDateTimeText();
    protected abstract JLabel  getClockInText();
    protected abstract JLabel  getClockOutText();
    protected abstract JButton getClockInButton();
    protected abstract JButton getClockOutButton();
    protected abstract JLabel  getTotalWorkedHoursText();
    protected abstract JLabel  getLeavesAvailableText();
}