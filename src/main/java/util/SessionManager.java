package util;

public class SessionManager {
    // your original login session
    private static String userID;
    private static int    employeeID;

    // for carrying the *selected* employee into the update page
    private static int selectedEmployeeID = -1;

    /** Called at login time */
    public static void setSession(String userID, int employeeID) {
        SessionManager.userID     = userID;
        SessionManager.employeeID = employeeID;
    }

    public static String getUserID() {
        return userID;
    }
    public static int getEmployeeID() {
        return employeeID;
    }

    /** Used by Records→Update flow to stash the chosen row’s ID */
    public static void setSelectedEmployeeID(int id) {
        selectedEmployeeID = id;
    }
    public static int getSelectedEmployeeID() {
        return selectedEmployeeID;
    }

    public static void clearSession() {
        userID = null;
        employeeID = -1;
        selectedEmployeeID = -1;
    }
}