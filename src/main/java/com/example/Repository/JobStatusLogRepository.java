package com.example.Repository;

import com.example.Entity.JobsStatusLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobStatusLogRepository extends JpaRepository<JobsStatusLog,Long> {
}
