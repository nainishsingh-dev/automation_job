package com.example.Entity;

import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "sso_audit_logs")
public class IdcsAuditLogs {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "audit_seq_gen")
    @SequenceGenerator(name = "audit_seq_gen", sequenceName = "AUDIT_SEQ", allocationSize = 1)
    private Long sid;
    private String eventId;
    private String ssoMatchedSignOnPolicy;
    private String actorDisplayName;
    private String ssoUserAgent;
    private String actorName;
    private String serviceName;
    private String ssoBrowser;
    private String message;
    private String ecId;
    private String clientIp;
    private boolean meterAsOPCService;
    private String id;
    private String ssoComments;
    private String timestamp;
    private LocalDateTime createdTimeStamp;
    private String adminValueAdded;
    private String adminValueRemoved;
    private String adminResourceName;
    @PrePersist
    void createdTimeStamp()
    {
        this.createdTimeStamp= LocalDateTime.now();
    }


}
