package com.example.Repository;

import com.example.Entity.IdcsAuditLogs;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogsRepo extends JpaRepository<IdcsAuditLogs,Long> {
}
