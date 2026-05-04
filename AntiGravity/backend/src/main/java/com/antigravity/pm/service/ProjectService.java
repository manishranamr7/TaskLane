package com.antigravity.pm.service;

import com.antigravity.pm.domain.Project;
import com.antigravity.pm.domain.User;
import com.antigravity.pm.domain.Role;
import com.antigravity.pm.dto.ProjectDTO;
import com.antigravity.pm.repository.ProjectRepository;
import com.antigravity.pm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ActivityLogService activityLogService;

    public List<ProjectDTO> getAllProjects() {
        return projectRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public ProjectDTO createProject(ProjectDTO dto, String ownerUsername) {
        User owner = userRepository.findByUsername(ownerUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Project project = new Project();
        project.setName(dto.getName());
        project.setDescription(dto.getDescription());
        project.setDeadline(dto.getDeadline());
        project.setOwner(owner);
        Project saved = projectRepository.save(project);
        
        activityLogService.logActivity(saved, "Project created by " + ownerUsername);
        
        return mapToDTO(saved);
    }
    
    public ProjectDTO updateProject(Long id, ProjectDTO dto, String username) {
        Project project = projectRepository.findById(Objects.requireNonNull(id))
                .orElseThrow(() -> new RuntimeException("Project not found"));
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
                
        // Allow Admin to update, or Owner
        if (user.getRole() != Role.ROLE_ADMIN && !project.getOwner().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized to edit project");
        }
        
        project.setName(dto.getName());
        project.setDescription(dto.getDescription());
        project.setDeadline(dto.getDeadline());
        
        Project saved = projectRepository.save(project);
        activityLogService.logActivity(saved, "Project details updated by " + username);
        
        return mapToDTO(saved);
    }
    
    public void deleteProject(Long id, String username) {
        Project project = projectRepository.findById(Objects.requireNonNull(id))
                .orElseThrow(() -> new RuntimeException("Project not found"));
                
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
                
        // Only Owner or Admin can delete
        if (user.getRole() != Role.ROLE_ADMIN && !project.getOwner().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized to delete project");
        }
        
        project.setDeleted(true);
        projectRepository.save(project);
        
        // Log deletion? Won't be visible in standard queries since deleted=true, 
        // but activity log exists.
    }

    private ProjectDTO mapToDTO(Project project) {
        return new ProjectDTO(project.getId(), project.getName(), project.getDescription(), project.getDeadline());
    }
}
