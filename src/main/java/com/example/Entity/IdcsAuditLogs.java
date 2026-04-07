package com.example.Entity;

import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "audit_logs")
public class IdcsAuditLogs {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "audit_seq_gen")
    @SequenceGenerator(name = "audit_seq_gen", sequenceName = "AUDIT_SEQ", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @Column(name = "LOGIN_ID")
    private String login;

    @Column(name = "APPLICATION_ROLE_NAME")
    private String applicationRoleName;

    @Column(name = "CLIENT_IP")
    private String clientIp;

    @Column(name = "ECID")
    private String ecid;

    @Column(name = "RESPONSE")
    private String response;

    @Column(name = "AUTHENTICATION_LEVEL")
    private String authenticationLevel;

    @Column(name = "SSO_BROWSER")
    private String ssoBrowser;

    @Column(name = "SSO_COMMENTS")
    private String ssoComments;

    @Column(name = "MATCHED_SIGN_ON_POLICY")
    private String matchedSignOnPolicy;

    @Column(name = "MATCHED_SIGN_ON_POLICY_RULE")
    private String matchedSignOnPolicyRule;

    @Column(name = "SSO_SIGN_ON_POLICY_OBLIGATIONS")
    private String ssoSignOnPolicyObligations;

    @Column(name = "PROTECTED_RESOURCE")
    private String protectedResource;

    @Column(name = "USER_AGENT")
    private String userAgent;

    @Column(name = "LOGIN_DATE")
    private LocalDateTime loginDate;
    @Column(name = "JOB_ID")
    private Long jobId;
    @Column(name = "ADMIN_VALUE_ADDED")
    private String adminValueAdded;
    @Column(name = "ADMIN_VALUE_REMOVED")
    private String adminValueRemoved;


}
