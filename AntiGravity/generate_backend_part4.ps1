$srcDir = "backend/src/main/java/com/antigravity/pm"

$dtoDir = "$srcDir/dto"
$serviceDir = "$srcDir/service"
$controllerDir = "$srcDir/controller"

$authDtos = @"
package com.antigravity.pm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthRequest {
    private String username;
    private String password;
}
"@
Set-Content -Path "$dtoDir/AuthRequest.java" -Value $authDtos

$registerDto = @"
package com.antigravity.pm.dto;

import com.antigravity.pm.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    private String username;
    private String password;
    private Role role;
}
"@
Set-Content -Path "$dtoDir/RegisterRequest.java" -Value $registerDto

$authResponseDto = @"
package com.antigravity.pm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;
    private String username;
    private String role;
}
"@
Set-Content -Path "$dtoDir/AuthResponse.java" -Value $authResponseDto

$projectDto = @"
package com.antigravity.pm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectDTO {
    private Long id;
    private String name;
    private String description;
}
"@
Set-Content -Path "$dtoDir/ProjectDTO.java" -Value $projectDto

$authService = @"
package com.antigravity.pm.service;

import com.antigravity.pm.domain.User;
import com.antigravity.pm.dto.AuthRequest;
import com.antigravity.pm.dto.AuthResponse;
import com.antigravity.pm.dto.RegisterRequest;
import com.antigravity.pm.repository.UserRepository;
import com.antigravity.pm.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    public AuthResponse register(RegisterRequest request) {
        if(userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole() != null ? request.getRole() : com.antigravity.pm.domain.Role.ROLE_MEMBER);
        userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String jwtToken = jwtService.generateToken(userDetails);
        return new AuthResponse(jwtToken, user.getUsername(), user.getRole().name());
    }

    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        User user = userRepository.findByUsername(request.getUsername()).orElseThrow();
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String jwtToken = jwtService.generateToken(userDetails);
        return new AuthResponse(jwtToken, user.getUsername(), user.getRole().name());
    }
}
"@
Set-Content -Path "$serviceDir/AuthService.java" -Value $authService

$projectService = @"
package com.antigravity.pm.service;

import com.antigravity.pm.domain.Project;
import com.antigravity.pm.domain.User;
import com.antigravity.pm.dto.ProjectDTO;
import com.antigravity.pm.exception.ResourceNotFoundException;
import com.antigravity.pm.repository.ProjectRepository;
import com.antigravity.pm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public List<ProjectDTO> getAllProjects() {
        return projectRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public ProjectDTO createProject(ProjectDTO dto, String ownerUsername) {
        User owner = userRepository.findByUsername(ownerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Project project = new Project();
        project.setName(dto.getName());
        project.setDescription(dto.getDescription());
        project.setOwner(owner);
        Project saved = projectRepository.save(project);
        return mapToDTO(saved);
    }

    private ProjectDTO mapToDTO(Project project) {
        return new ProjectDTO(project.getId(), project.getName(), project.getDescription());
    }
}
"@
Set-Content -Path "$serviceDir/ProjectService.java" -Value $projectService

$taskService = @"
package com.antigravity.pm.service;

import com.antigravity.pm.domain.Project;
import com.antigravity.pm.domain.Task;
import com.antigravity.pm.domain.TaskStatus;
import com.antigravity.pm.dto.TaskDTO;
import com.antigravity.pm.exception.ResourceNotFoundException;
import com.antigravity.pm.repository.ProjectRepository;
import com.antigravity.pm.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;

    public List<TaskDTO> getTasksByProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        return project.getTasks().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<TaskDTO> getAllTasks() {
        return taskRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public TaskDTO createTask(Long projectId, TaskDTO dto) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        Task task = new Task();
        task.setTitle(dto.getTitle());
        task.setStatus(dto.getStatus() != null ? dto.getStatus() : TaskStatus.TODO);
        task.setDueDate(dto.getDueDate());
        task.setProject(project);
        Task saved = taskRepository.save(task);
        return mapToDTO(saved);
    }

    public TaskDTO updateTaskStatus(Long taskId, TaskStatus status) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        task.setStatus(status);
        return mapToDTO(taskRepository.save(task));
    }

    private TaskDTO mapToDTO(Task task) {
        return new TaskDTO(task.getId(), task.getTitle(), task.getStatus(), task.getDueDate());
    }
}
"@
Set-Content -Path "$serviceDir/TaskService.java" -Value $taskService

$authController = @"
package com.antigravity.pm.controller;

import com.antigravity.pm.dto.AuthRequest;
import com.antigravity.pm.dto.AuthResponse;
import com.antigravity.pm.dto.RegisterRequest;
import com.antigravity.pm.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
"@
Set-Content -Path "$controllerDir/AuthController.java" -Value $authController

$projectController = @"
package com.antigravity.pm.controller;

import com.antigravity.pm.dto.ProjectDTO;
import com.antigravity.pm.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;

    @GetMapping
    public ResponseEntity<List<ProjectDTO>> getAllProjects() {
        return ResponseEntity.ok(projectService.getAllProjects());
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ProjectDTO> createProject(@RequestBody ProjectDTO projectDTO, Authentication authentication) {
        return ResponseEntity.ok(projectService.createProject(projectDTO, authentication.getName()));
    }
}
"@
Set-Content -Path "$controllerDir/ProjectController.java" -Value $projectController

$taskController = @"
package com.antigravity.pm.controller;

import com.antigravity.pm.domain.TaskStatus;
import com.antigravity.pm.dto.TaskDTO;
import com.antigravity.pm.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @GetMapping
    public ResponseEntity<List<TaskDTO>> getAllTasks() {
        return ResponseEntity.ok(taskService.getAllTasks());
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<TaskDTO>> getTasksByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(taskService.getTasksByProject(projectId));
    }

    @PostMapping("/project/{projectId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<TaskDTO> createTask(@PathVariable Long projectId, @RequestBody TaskDTO taskDTO) {
        return ResponseEntity.ok(taskService.createTask(projectId, taskDTO));
    }

    @PutMapping("/{taskId}/status")
    public ResponseEntity<TaskDTO> updateTaskStatus(@PathVariable Long taskId, @RequestParam TaskStatus status) {
        return ResponseEntity.ok(taskService.updateTaskStatus(taskId, status));
    }
}
"@
Set-Content -Path "$controllerDir/TaskController.java" -Value $taskController

$analyticsController = @"
package com.antigravity.pm.controller;

import com.antigravity.pm.dto.ProjectAnalyticsDTO;
import com.antigravity.pm.service.ProjectAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {
    private final ProjectAnalyticsService analyticsService;

    @GetMapping("/project/{projectId}")
    public ResponseEntity<ProjectAnalyticsDTO> getProjectAnalytics(@PathVariable Long projectId) {
        return ResponseEntity.ok(analyticsService.getProjectAnalytics(projectId));
    }
}
"@
Set-Content -Path "$controllerDir/AnalyticsController.java" -Value $analyticsController
