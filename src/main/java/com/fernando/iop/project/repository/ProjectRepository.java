package com.fernando.iop.project.repository;

import com.fernando.iop.project.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

    boolean existsByProjectId(UUID projectId);

}
