package com.example.Entity;

import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_deactivation_log")
@Data
public class UserDeactivationLogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "audit_seq_gen")
    @SequenceGenerator(name = "audit_seq_gen", sequenceName = "DEACTIVATION_SEQ", allocationSize = 1)
    private Long deactivation_id;
    private String email;
    private String ocid;
    private LocalDate date_of_leaving;
    private String emp_code;
    private String status;
    private LocalDateTime deactivate_timeStamp;
    private LocalDateTime created_timeStamp;
    @PrePersist
    protected void onCreate() {
        created_timeStamp = LocalDateTime.now();  // auto-set at insert
    }


}

