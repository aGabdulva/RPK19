package org.example;

import javax.persistence.*;
/**
 * Класс "сотрудник".
 */
@Entity
@Table(name = "manager.employee")
public class Employee {
    private int idEmployee;
    private String employeeName;
    private String employeeContacts;
    private String employeeJobtitle;

    @Id
    @Column(name = "id_employee")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int getIdEmployee() {
        return idEmployee;
    }

    public void setIdEmployee(int idEmployee) {
        this.idEmployee = idEmployee;
    }

    @Column(name = "employee_name")
    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    @Column(name = "employee_contacts")
    public String getEmployeeContacts() {
        return employeeContacts;
    }

    public void setEmployeeContacts(String employeeContacts) {
        this.employeeContacts = employeeContacts;
    }

    @Column(name = "employee_jobtitle")
    public String getEmployeeJobtitle() {
        return employeeJobtitle;
    }

    public void setEmployeeJobtitle(String employeeJobtitle) {
        this.employeeJobtitle = employeeJobtitle;
    }

}