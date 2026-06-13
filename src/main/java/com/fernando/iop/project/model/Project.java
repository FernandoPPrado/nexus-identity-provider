package com.fernando.iop.project.model;

import com.fernando.iop.user.model.User;
import jakarta.persistence.*;

import java.util.List;
import java.util.UUID;


@Entity
public class Project {

    @Id
    @Column(unique = true, nullable = false)
    private UUID projectId;

    public Project(UUID projectId) {
        this.projectId = projectId;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }
}
