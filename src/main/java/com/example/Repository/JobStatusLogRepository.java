package com.example.Repository;

import com.example.Entity.JobsStatusLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface JobStatusLogRepository extends JpaRepository<JobsStatusLog,Long> {

    @Query("SELECT COUNT(cd) > 0 FROM JobsStatusLog cd WHERE cd.file_name = :name")
    boolean existsByFileName(String name);
}
