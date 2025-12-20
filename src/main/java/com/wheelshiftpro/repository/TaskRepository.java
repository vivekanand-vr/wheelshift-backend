package com.wheelshiftpro.repository;

import com.wheelshiftpro.entity.Task;
import com.wheelshiftpro.enums.TaskPriority;
import com.wheelshiftpro.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for Task entity.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    /**
     * Find tasks by status.
     */
    Page<Task> findByStatus(TaskStatus status, Pageable pageable);

    /**
     * Find tasks by assignee.
     */
    Page<Task> findByAssigneeId(Long assigneeId, Pageable pageable);

    /**
     * Find tasks by status and assignee.
     */
    Page<Task> findByStatusAndAssigneeId(TaskStatus status, Long assigneeId, Pageable pageable);

    /**
     * Find tasks by priority.
     */
    Page<Task> findByPriority(TaskPriority priority, Pageable pageable);

    /**
     * Find overdue tasks.
     */
    @Query("SELECT t FROM Task t WHERE t.dueDate < :currentTime AND t.status NOT IN ('DONE')")
    List<Task> findOverdueTasks(@Param("currentTime") LocalDateTime currentTime);

    /**
     * Find tasks due soon.
     */
    @Query("SELECT t FROM Task t WHERE t.dueDate BETWEEN :startTime AND :endTime AND t.status NOT IN ('DONE')")
    List<Task> findTasksDueSoon(@Param("startTime") LocalDateTime startTime, 
                                 @Param("endTime") LocalDateTime endTime);

    /**
     * Count tasks by status.
     */
    long countByStatus(TaskStatus status);

    /**
     * Count tasks by assignee and status.
     */
    long countByAssigneeIdAndStatus(Long assigneeId, TaskStatus status);
}
