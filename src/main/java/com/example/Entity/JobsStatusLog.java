package com.example.Entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "job_status_log")
public class JobsStatusLog {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "audit_seq_gen")
    @SequenceGenerator(name = "audit_seq_gen", sequenceName = "JOB_SEQ", allocationSize = 1)
  private  Long job_id;
  private  String job_name;
  private  String status;
  private LocalDateTime start_timestamp;
  private  LocalDateTime end_timestamp;
  private String message;

}
