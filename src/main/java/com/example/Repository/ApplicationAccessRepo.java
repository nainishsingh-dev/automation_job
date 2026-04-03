package com.example.Repository;

import com.example.Entity.ApplicationAccessLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationAccessRepo extends JpaRepository<ApplicationAccessLog,Long> {
}
