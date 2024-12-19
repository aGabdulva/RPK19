package org.example;

import javax.persistence.*;
/**
 * Класс "Проект".
 * @author Габдулвалеев Артур
 */

@Entity
@Table(name = "manager.project")
public class Project {
    private int idProject;
    private String projectName;
    private String deadline;
    private Client client;


    @Id
    @Column(name = "id_project")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int getIdProject() {
        return idProject;
    }

    public void setIdProject(int idProject) {
        this.idProject = idProject;
    }

    @Column(name = "project_name")
    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    @Column(name = "deadline")
    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    @ManyToOne
    @JoinColumn(name = "id_customer")
    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }
}