package com.example.Entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "notification_logs")
public class NotificationLogs {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "audit_seq_gen")
    @SequenceGenerator(name = "audit_seq_gen", sequenceName = "NOTIFICATION_SEQ", allocationSize = 1)
    private Long notification_id;
    private String actorName;
    private String clientIp;
    private String eventId;
    private String reasonValue;
    private String actorType;
    private String message;
    private String notificationPushBody;
    private String actorDisplayName;
    private String notificationDeliveryChannel;
    private LocalDateTime timestamp;
    private Long jobId;
    private String notificationDeliveryEmailSubject;
    private String notificationDeliveryEmailTo;
    private String notificationDeliveryEmailFrom;
    private String notificationDeliveryEmailStatus;

}