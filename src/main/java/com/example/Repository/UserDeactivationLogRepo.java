package com.example.Repository;

import com.example.Entity.UserDeactivationLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.Optional;

@EnableJpaRepositories
public interface UserDeactivationLogRepo extends JpaRepository<UserDeactivationLogEntity,Long> {
    @Query("select c.deactivation_id from UserDeactivationLogEntity c where c.emp_code=:employeeCode")
    Optional<UserDeactivationLogEntity> alreadyDeactivatedFlag(String employeeCode);
}
