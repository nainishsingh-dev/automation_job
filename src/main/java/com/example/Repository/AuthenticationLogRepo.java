package com.example.Repository;

import com.example.Entity.AuthenticationLogs;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthenticationLogRepo extends JpaRepository<AuthenticationLogs,Long> {
}
