package model;

import java.math.BigDecimal;
import java.sql.Date;


public class Employee {
    private int employeeId;
    private String fullName;
    private String cnic;
    private String email;
    private String phone;
    private int roleId;
    private Date hireDate;
    private BigDecimal salary;
    private String status;

    public Employee(){}

    public Employee(String fullName, String cnic, String email, String phone,
                    int roleId, BigDecimal salary) {
        this.fullName = fullName;
        this.cnic = cnic;
        this.email = email;
        this.phone = phone;
        this.roleId = roleId;
        this.salary = salary;
        this.status = "ACTIVE";
    }

    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getCnic() { return cnic; }
    public void setCnic(String cnic) { this.cnic = cnic; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public int getRoleId() { return roleId; }
    public void setRoleId(int roleId) { this.roleId = roleId; }

    public Date getHireDate() { return hireDate; }
    public void setHireDate(Date hireDate) { this.hireDate = hireDate; }

    public BigDecimal getSalary() { return salary; }
    public void setSalary(BigDecimal salary) { this.salary = salary; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
