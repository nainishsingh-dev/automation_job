package com.example.Entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "authentication_success_failure_log")
public class AuthenticationLogs {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "audit_seq_gen")
    @SequenceGenerator(name = "audit_seq_gen", sequenceName = "AUTH_SEQ", allocationSize = 1)
    @Column(name="ID")
    private Long id;
    @Column(name="LOGIN")
    private String login;
    @Column(name="RESPONSE")
    private String response;
    @Column(name="MSG")
    private String message;
    @Column(name ="PROVIDER")
    private String provider;
    @Column(name = "DATE")
    private LocalDateTime date;
    @Column(name="JOB_ID")
    private Long jobId;

}
