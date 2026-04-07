package com.example.Repository;

import com.example.Entity.NotificationLogs;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepo extends JpaRepository<NotificationLogs,Long> {
}
