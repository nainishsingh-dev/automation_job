package com.example.idcsService;

import com.example.Entity.ApplicationAccessLog;
import com.example.Entity.AuthenticationLogs;
import com.example.Entity.IdcsAuditLogs;
import com.example.Entity.NotificationLogs;
import com.example.Repository.ApplicationAccessRepo;
import com.example.Repository.AuditLogsRepo;
import com.example.Repository.AuthenticationLogRepo;
import com.example.Repository.NotificationRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.example.idcsService.EnumTemplate.auditLog;
import static com.example.idcsService.EnumTemplate.csvReportJob;

@Service
public class CsvUtility {
    @Autowired
    private AuditLogsRepo auditLogReportRepo;
    @Autowired
    private JobStatusManger jobStatusManger;
    @Autowired
    private ApplicationAccessRepo applicationAccessRepo;
    @Autowired
    private AuthenticationLogRepo authenticationLogRepo;
    @Autowired
    private NotificationRepo notificationRepo;

    @Value("${report.base.path}")
    private String basePath;

    @Value("${report.page.size}")
    private int pageSize;


    public static String escape(String value) {
        if (value == null) return "";
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }


    private final StringBuilder errorBuffer = new StringBuilder();
    @Scheduled(cron = "0 0 5 * * ?")
    public void generateAllCsvReports() {
        Long job_id = jobStatusManger.setInProgressStatus(csvReportJob,"");
        errorBuffer.setLength(0);

        try {
            LocalDate YESTERDAY = LocalDate.now().minusDays(1);
            String subPath = YESTERDAY.getYear() + "/"
                    + YESTERDAY.getMonth() + "/"
                    + YESTERDAY.getDayOfMonth() + "-" + YESTERDAY.getMonth();

            Path fullPath = Paths.get(basePath, subPath);
            Files.createDirectories(fullPath);

            generateNotificationCsv(fullPath,YESTERDAY);
            generateAuthenticationCsv(fullPath,YESTERDAY);
            generateApplicationAccessCsv(fullPath,YESTERDAY);
            generateIdcsAuditLog(fullPath,YESTERDAY);
            if(errorBuffer.length()>0)
            {
                jobStatusManger.updateStatus(job_id,true,errorBuffer.toString(),"");

            }
            else {
                jobStatusManger.updateStatus(job_id,true,"","");

            }

        } catch (IOException e) {
            System.err.println("Error creating directories or generating reports: " + e.getMessage());
            e.printStackTrace();
            jobStatusManger.updateStatus(job_id,false,"Error creating directories or generating reports: " + e.getMessage(),"");
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            jobStatusManger.updateStatus(job_id,false,"Unexpected error:: " + e.getMessage(),"");
            e.printStackTrace();
        }
    }

    // ---------------- NOTIFICATION CSV ----------------
    private void generateNotificationCsv(Path directory,LocalDate YESTERDAY) {
//        String file = directory + "/notification_logs_" + YESTERDAY + ".csv";
        Path filePath = directory.resolve("notification_logs_" + YESTERDAY + ".csv");

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePath.toUri()))) {
            writer.write("NOTIFICATION_ID,ACTOR_NAME,CLIENT_IP,EVENT_ID,REASON_VALUE,ACTOR_TYPE,MESSAGE,NOTIFICATION_PUSH_BODY,ACTOR_DISPLAY_NAME,NOTIFICATION_DELIVERY_CHANNEL,CREATED_TIMESTAMP,NOTIFICATION_DELIVERY_EMAIL_SUBJECT,NOTIFICATION_DELIVERY_EMAIL_TO,NOTIFICATION_DELIVERY_EMAIL_FROM,NOTIFICATION_DELIVERY_EMAIL_STATUS\n");

            int page = 0;
            Page<NotificationLogs> result;

            do {
                result = notificationRepo.findAllByDate(PageRequest.of(page, pageSize, Sort.by("id")));
                for (NotificationLogs log : result.getContent()) {
                    writer.write(
                            log.getNotification_id() + "," +
                                    CsvUtility.escape(log.getActorName()) + "," +
                                    CsvUtility.escape(log.getClientIp()) + "," +
                                    CsvUtility.escape(log.getEventId()) + "," +
                                    CsvUtility.escape(log.getReasonValue()) + "," +
                                    CsvUtility.escape(log.getActorType()) + "," +
                                    CsvUtility.escape(log.getMessage()) + "," +
                                    CsvUtility.escape(log.getNotificationPushBody()) + "," +
                                    CsvUtility.escape(log.getActorDisplayName()) + "," +
                                    CsvUtility.escape(log.getNotificationDeliveryChannel()) + "," +
                                    log.getCreatedTimestamp() + "," +
                                    CsvUtility.escape(log.getNotificationDeliveryEmailSubject()) + "," +
                                    CsvUtility.escape(log.getNotificationDeliveryEmailTo()) + "," +
                                    CsvUtility.escape(log.getNotificationDeliveryEmailFrom()) + "," +
                                    CsvUtility.escape(log.getNotificationDeliveryEmailStatus()) + "\n"
                    );
                }
                page++;
            } while (result.hasNext());

        } catch (IOException e) {
            System.err.println("Error writing Notification CSV: " + e.getMessage());
            errorBuffer.append("Error writing Notification CSV: ").append(e.getMessage()).append(",");
            e.printStackTrace();
        }
    }

    // ---------------- AUTHENTICATION CSV ----------------
    private void generateAuthenticationCsv(Path directory,LocalDate YESTERDAY) {
//        String file = directory + "/authentication_logs_" + YESTERDAY + ".csv";
        Path filePath = directory.resolve("authentication_logs_" + YESTERDAY + ".csv");

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePath.toUri()))) {
            writer.write("LOGIN,PROVIDER,RESPONSE,MESSAGE,LOG_DATE\n");

            int page = 0;
            Page<AuthenticationLogs> result;

            do {
                result = authenticationLogRepo.findAllByDate(PageRequest.of(page, pageSize, Sort.by("id")));
                for (AuthenticationLogs log : result.getContent()) {
                    writer.write(
                            CsvUtility.escape(log.getLogin()) + "," +
                                    log.getProvider() + "," +
                                    log.getResponse() + "," +
                                    log.getMessage() + "," +
                                    log.getDate() + "\n"
                    );
                }
                page++;
            } while (result.hasNext());

        } catch (IOException e) {
            System.err.println("Error writing Authentication CSV: " + e.getMessage());
            errorBuffer.append("Error writing Authentication CSV: ").append(e.getMessage()).append(",");
            e.printStackTrace();
        }
    }

    // ---------------- APPLICATION ACCESS CSV ----------------
    private void generateApplicationAccessCsv(Path directory,LocalDate YESTERDAY) {
//        String file = directory + "/application_access_logs_" + YESTERDAY + ".csv";
        Path filePath = directory.resolve("application_access_logs_" + YESTERDAY + ".csv");

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePath.toUri()))) {
            writer.write("LOGIN,RESPONSE,PROVIDER,LOG_DATE,SSO_LOCAL_IP,APP_NAME\n");

            int page = 0;
            Page<ApplicationAccessLog> result;

            do {
                result = applicationAccessRepo.findAllByDate(PageRequest.of(page, pageSize));
                for (ApplicationAccessLog log : result.getContent()) {
                    writer.write(
                            CsvUtility.escape(log.getLogin()) + "," +
                                    log.getResponse() + "," +
                                    log.getProvider() + "," +
                                    log.getDate() + "," +
                                    log.getSsoLocalIp() + "," +
                                    CsvUtility.escape(log.getApplicationName()) + "\n"
                    );
                }
                page++;
            } while (result.hasNext());

        } catch (IOException e) {
            System.err.println("Error writing Application Access CSV: " + e.getMessage());
            errorBuffer.append("Error writing Application Access CSV: ").append(e.getMessage()).append(",");
            e.printStackTrace();
        }
    }

    // ---------------- IDCS AUDIT LOG CSV ----------------
    private void generateIdcsAuditLog(Path directory,LocalDate YESTERDAY) {
//        String file = directory + "/audit_logs_" + YESTERDAY + ".csv";
        Path filePath = directory.resolve("audit_logs_" + YESTERDAY + ".csv");

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePath.toUri()))) {
            writer.write("LOGIN,APPLICATION_ROLE_NAME,CLIENT_IP,ECID,RESPONSE,AUTHENTICATION_LEVEL,SSO_BROWSER,SSO_COMMENTS,MATCHED_SIGN_ON_POLICY,MATCHED_SIGN_ON_POLICY_RULE,SSO_SIGN_ON_POLICY_OBLIGATIONS,PROTECTED_RESOURCE,USER_AGENT,LOGIN_DATE,JOB_ID,ADMIN_VALUE_ADDED,ADMIN_VALUE_REMOVED\n");

            int page = 0;
            Page<IdcsAuditLogs> result;

            do {
                result = auditLogReportRepo.findAllByDate(PageRequest.of(page, pageSize));
                for (IdcsAuditLogs log : result.getContent()) {
                    writer.write(
                            CsvUtility.escape(log.getLogin()) + "," +
                                    CsvUtility.escape(log.getApplicationRoleName()) + "," +
                                    CsvUtility.escape(log.getClientIp()) + "," +
                                    CsvUtility.escape(log.getEcid()) + "," +
                                    CsvUtility.escape(log.getResponse()) + "," +
                                    CsvUtility.escape(log.getAuthenticationLevel()) + "," +
                                    CsvUtility.escape(log.getSsoBrowser()) + "," +
                                    CsvUtility.escape(log.getSsoComments()) + "," +
                                    CsvUtility.escape(log.getMatchedSignOnPolicy()) + "," +
                                    CsvUtility.escape(log.getMatchedSignOnPolicyRule()) + "," +
                                    CsvUtility.escape(log.getSsoSignOnPolicyObligations()) + "," +
                                    CsvUtility.escape(log.getProtectedResource()) + "," +
                                    CsvUtility.escape(log.getUserAgent()) + "," +
                                    (log.getLoginDate() != null ? log.getLoginDate().toString() : "") + "," +
                                    (log.getJobId() != null ? log.getJobId().toString() : "") + "," +
                                    CsvUtility.escape(log.getAdminValueAdded()) + "," +
                                    CsvUtility.escape(log.getAdminValueRemoved()) + "\n"
                    );
                }
                page++;
            } while (result.hasNext());

        } catch (IOException e) {
            System.err.println("Error writing IDCS Audit CSV: " + e.getMessage());
            errorBuffer.append("Error writing IDCS Audit CSV: ").append(e.getMessage());
            e.printStackTrace();
        }
    }
    }
