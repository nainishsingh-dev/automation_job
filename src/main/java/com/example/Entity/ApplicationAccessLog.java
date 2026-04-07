package com.example.Entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "application_access_log")
public class ApplicationAccessLog {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "audit_seq_gen")
    @SequenceGenerator(name = "audit_seq_gen", sequenceName = "APP_ACCESS_SEQ", allocationSize = 1)
    @Column(name="ID")
    private Long id;
    @Column(name="LOGIN")
    private String login;
    @Column(name="RESPONSE")
    private String response;
    @Column(name="PROVIDER")
    private String provider;
    @Column(name="DATE")
    private LocalDateTime date;
    @Column(name = "JOB_ID")
    private Long jobId;
    @Column(name="SSO_LOCAL_IP")
    private String ssoLocalIp;
    @Column(name = "APP_NAME")
    private String applicationName;

}
