$srcDir = "backend/src/main/java/com/antigravity/pm"

$dirs = @(
    "$srcDir/domain",
    "$srcDir/repository",
    "$srcDir/security",
    "$srcDir/dto",
    "$srcDir/service",
    "$srcDir/controller",
    "$srcDir/exception"
)

foreach ($dir in $dirs) {
    New-Item -ItemType Directory -Force -Path $dir | Out-Null
}

$userContent = @"
package com.antigravity.pm.domain;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;
}
"@
Set-Content -Path "$srcDir/domain/User.java" -Value $userContent

$roleContent = @"
package com.antigravity.pm.domain;

public enum Role {
    ROLE_ADMIN,
    ROLE_MEMBER
}
"@
Set-Content -Path "$srcDir/domain/Role.java" -Value $roleContent

$projectContent = @"
package com.antigravity.pm.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "projects")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Project name cannot be empty")
    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Task> tasks = new ArrayList<>();
}
"@
Set-Content -Path "$srcDir/domain/Project.java" -Value $projectContent

$taskContent = @"
package com.antigravity.pm.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Task title is required")
    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskStatus status; 

    @Column(name = "due_date")
    private LocalDate dueDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private User assignee;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
"@
Set-Content -Path "$srcDir/domain/Task.java" -Value $taskContent

$taskStatusContent = @"
package com.antigravity.pm.domain;

public enum TaskStatus {
    TODO, DOING, DONE
}
"@
Set-Content -Path "$srcDir/domain/TaskStatus.java" -Value $taskStatusContent

$userRepoContent = @"
package com.antigravity.pm.repository;

import com.antigravity.pm.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Boolean existsByUsername(String username);
}
"@
Set-Content -Path "$srcDir/repository/UserRepository.java" -Value $userRepoContent

$projectRepoContent = @"
package com.antigravity.pm.repository;

import com.antigravity.pm.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByOwnerId(Long ownerId);
}
"@
Set-Content -Path "$srcDir/repository/ProjectRepository.java" -Value $projectRepoContent

$taskRepoContent = @"
package com.antigravity.pm.repository;

import com.antigravity.pm.domain.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByAssigneeId(Long assigneeId);
}
"@
Set-Content -Path "$srcDir/repository/TaskRepository.java" -Value $taskRepoContent

$globalExContent = @"
package com.antigravity.pm.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleResourceNotFound(ResourceNotFoundException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }
}
"@
Set-Content -Path "$srcDir/exception/GlobalExceptionHandler.java" -Value $globalExContent

$resourceNotFoundContent = @"
package com.antigravity.pm.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
"@
Set-Content -Path "$srcDir/exception/ResourceNotFoundException.java" -Value $resourceNotFoundContent

Write-Host "Generated Domain, Repository, and Exception files successfully."
