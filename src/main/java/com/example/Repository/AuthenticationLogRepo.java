package com.example.Repository;

import com.example.Entity.AuthenticationLogs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface AuthenticationLogRepo extends JpaRepository<AuthenticationLogs,Long> {
   @Query("Select c from AuthenticationLogs c where c.date>=sysdate()-1")
    Page<AuthenticationLogs> findAllByDate(PageRequest id);
}
