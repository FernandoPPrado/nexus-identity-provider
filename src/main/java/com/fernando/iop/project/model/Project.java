package com.fernando.iop.project.model;


import jakarta.persistence.*;


import java.util.UUID;


@Entity
public class Project {

    @Id
    private UUID projectId;

    public Project(UUID projectId) {
        this.projectId = projectId;
    }

    public Project() {
    }

    public UUID getProjectId() {
        return projectId;
    }

    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }
}
