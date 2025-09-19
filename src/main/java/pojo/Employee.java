package pojo;

import java.sql.Date;

public class Employee {

    private int employeeID;
    private String firstName;
    private String lastName;
    private Date birthDate;
    private String phoneNo;
    private String email;
    private String userID;
    private int statusID;
    private int positionID;
    private int departmentID;
    private int supervisorID;

    private String position;

    // transient fields from govid/status
    private String supervisorName;
    private String statusDesc;
    private String sssNo;
    private String pagibigNo;
    private String philhealthNo;
    private String tinNo;

    // **discrete address fields** (no more fullAddress)
    private String houseNo;
    private String street;
    private String barangay;
    private String city;
    private String province;
    private Integer zipCode;

    // Constructors

    public Employee() {
    }

    public Employee(int employeeID, String firstName, String lastName, Date birthDate,
                    String phoneNo, String email, String userID, int statusID,
                    int positionID, int departmentID, int supervisorID, String position) {
        this.employeeID = employeeID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.phoneNo = phoneNo;
        this.email = email;
        this.userID = userID;
        this.statusID = statusID;
        this.positionID = positionID;
        this.departmentID = departmentID;
        this.supervisorID = supervisorID;
        this.position = position;
    }

    // Getters and Setters

    public int getEmployeeID() { return employeeID; }
    public void setEmployeeID(int employeeID) { this.employeeID = employeeID; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public Date getBirthDate() { return birthDate; }
    public void setBirthDate(Date birthDate) { this.birthDate = birthDate; }

    public String getPhoneNo() { return phoneNo; }
    public void setPhoneNo(String phoneNo) { this.phoneNo = phoneNo; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUserID() { return userID; }
    public void setUserID(String userID) { this.userID = userID; }

    public int getStatusID() { return statusID; }
    public void setStatusID(int statusID) { this.statusID = statusID; }

    public int getPositionID() { return positionID; }
    public void setPositionID(int positionID) { this.positionID = positionID; }

    public int getDepartmentID() { return departmentID; }
    public void setDepartmentID(int departmentID) { this.departmentID = departmentID; }

    public int getSupervisorID() { return supervisorID; }
    public void setSupervisorID(int supervisorID) { this.supervisorID = supervisorID; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public String getSupervisorName() { return supervisorName; }
    public void setSupervisorName(String supervisorName) { this.supervisorName = supervisorName; }

    public String getStatusDesc() { return statusDesc; }
    public void setStatusDesc(String statusDesc) { this.statusDesc = statusDesc; }

    public String getSssNo() { return sssNo; }
    public void setSssNo(String sssNo) { this.sssNo = sssNo; }

    public String getPagibigNo() { return pagibigNo; }
    public void setPagibigNo(String pagibigNo) { this.pagibigNo = pagibigNo; }

    public String getPhilhealthNo() { return philhealthNo; }
    public void setPhilhealthNo(String philhealthNo) { this.philhealthNo = philhealthNo; }

    public String getTinNo() { return tinNo; }
    public void setTinNo(String tinNo) { this.tinNo = tinNo; }

    // **new address getters/setters**

    public String getHouseNo() { return houseNo; }
    public void setHouseNo(String houseNo) { this.houseNo = houseNo; }

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public String getBarangay() { return barangay; }
    public void setBarangay(String barangay) { this.barangay = barangay; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }

    public Integer getZipCode() { return zipCode; }
    public void setZipCode(Integer zipCode) { this.zipCode = zipCode; }
}