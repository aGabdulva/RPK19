package org.example;

import javax.persistence.*;
/**
 * Класс "Задача".
 */
@Entity
@Table(name = "manager.problem")
public class Problem {
    private int idProblem;
    private String problemName;
    private String problemDeadline;
    private Project project;
    private Employee employee;


    @Id
    @Column(name = "id_problem")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int getIdProblem() {
        return idProblem;
    }

    public void setIdProblem(int idProblem) {
        this.idProblem = idProblem;
    }

    @Column(name = "problem_name")
    public String getProblemName() {
        return problemName;
    }

    public void setProblemName(String problemName) {
        this.problemName = problemName;
    }

    @Column(name = "problem_deadline")
    public String getProblemDeadline() {
        return problemDeadline;
    }

    public void setProblemDeadline(String problemDeadline) {
        this.problemDeadline = problemDeadline;
    }

    @ManyToOne
    @JoinColumn(name = "id_project")
    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    @ManyToOne
    @JoinColumn(name = "id_employee")
    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }
}