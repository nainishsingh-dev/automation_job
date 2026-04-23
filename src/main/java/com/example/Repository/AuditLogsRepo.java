package com.example.Repository;

import com.example.Entity.ApplicationAccessLog;
import com.example.Entity.IdcsAuditLogs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AuditLogsRepo extends JpaRepository<IdcsAuditLogs,Long> {
    @Query("Select c from IdcsAuditLogs c where c.loginDate>=sysdate()-1")
    Page<IdcsAuditLogs> findAllByDate(PageRequest of);
}
