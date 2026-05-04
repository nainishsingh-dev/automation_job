package com.example.Entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ad_users")
@Data
public class AdMaster {

        @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "audit_seq_gen")
        @SequenceGenerator(name = "audit_seq_gen", sequenceName = "AD_SEQ", allocationSize = 1)
        private Long id;
        private String displayName;
        private String samAccountName;
        private String mail;
        private String description;
        private String department;
        private String office;
        private LocalDateTime whenCreated;
        private String manager;
        private Boolean enabled;
        private Long job_id;
        private LocalDateTime created_timeStamp;

    @PrePersist
    protected void onCreate() {
        created_timeStamp = LocalDateTime.now();  // auto-set at insert
    }
}
