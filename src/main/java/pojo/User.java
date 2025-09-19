package pojo;

public class User {
    
    private String userID;             // authentication.userID
    private String username;           // will map to employee.firstName + " " + employee.lastName OR store separately if you want
    private String email;              // employee.email
    private String password;           // authentication.passwordHash
    private String userRole;           // userrole.role
    private int positionID;            // employee.positionID
    private String accountStatus;   

    // Constructors

    public User() {
    }

    public User(String userID, String username, String email, String password, String userRole, int positionID, String accountStatus) {
        this.userID = userID;
        this.username = username;
        this.email = email;
        this.password = password;
        this.userRole = userRole;
        this.positionID = positionID;
        this.accountStatus = accountStatus;
    }

    // Getters and Setters

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public int getPositionID() {
        return positionID;
    }

    public void setPositionID(int positionID) {
        this.positionID = positionID;
    }
    
    public String getAccountStatus() {
        return accountStatus;
    }
    
    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }
}