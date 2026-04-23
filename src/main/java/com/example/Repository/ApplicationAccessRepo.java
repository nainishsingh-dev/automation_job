package com.example.Repository;

import com.example.Entity.ApplicationAccessLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;

public interface ApplicationAccessRepo extends JpaRepository<ApplicationAccessLog,Long> {
    @Query("Select c from ApplicationAccessLog c where c.date>=sysdate()-1")
    Page<ApplicationAccessLog> findAllByDate( PageRequest of);
}
