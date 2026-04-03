package com.example.idcsService;

import com.example.Entity.ApplicationAccessLog;
import com.example.Entity.AuthenticationLogs;
import com.example.Entity.IdcsAuditLogs;
import com.example.Entity.JobsStatusLog;
import com.example.Repository.ApplicationAccessRepo;
import com.example.Repository.AuditLogsRepo;
import com.example.Model.OauthToken;
//import com.sun.deploy.net.*;
import java.net.URLEncoder;

import com.example.Repository.AuthenticationLogRepo;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.stream.Collectors;

import static com.example.idcsService.EnumTemplate.*;

@Slf4j
@Service

public class AuditLogReport {
    @Value("${sso.domain}")
    private String ssoDomain;

    @Autowired
    private AuditLogsRepo auditLogReportRepo;
    @Autowired
    private JobStatusManger jobStatusManger;
    @Autowired
    private ApplicationAccessRepo applicationAccessRepo;
    @Autowired
    private AuthenticationLogRepo authenticationLogRepo;

    @Scheduled(cron = "0 0 1 * * ?")
    public void fetchAllTodayAuditEvents() {
        Long job_id = jobStatusManger.setInProgressStatus(auditLog);
        log.info("{} job started | jobId={}", auditLog, job_id);

        try {
            LocalDate today = LocalDate.now(ZoneOffset.UTC);
            LocalDate yesterday = today.minusDays(7);

            String end = today.atStartOfDay(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
            String start = yesterday.atStartOfDay(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
            int startIndex = 0;
            int totalResults = Integer.MAX_VALUE;
            String baseUrl = ssoDomain + "/admin/v1/AuditEvents";
//            String attributes = "actorName,actorDisplayName,eventId,clientip,timeStamp,serviceName,ssoBrowser,ecid,message,ssoComments,ssoUserAgent,ssoMatchedSignOnPolicy,adminValuesAdded,adminValuesRemoved,adminResourceName";


            while (startIndex < totalResults) {
                String filter = String.format("timestamp ge \"%s\" and timestamp lt \"%s\"", start, end);
                String encodedFilter = URLEncoder.encode(filter, StandardCharsets.UTF_8.toString());

                String fullUrl = String.format(baseUrl, encodedFilter, startIndex);

                URL url = new URL(fullUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setDoOutput(true);
                OauthToken token = IdcsTokenManager.fetchNewToken();
                conn.setRequestProperty("Authorization", "Bearer " + token.getAccessToken());

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    String response = reader.lines().collect(Collectors.joining());
                    JSONObject json = new JSONObject(response);
                    JSONArray resources = json.optJSONArray("Resources");
                    totalResults = json.optInt("totalResults", 0);
                    ArrayList<IdcsAuditLogs> logsArrayList = new ArrayList<>();

                    if (resources != null) {
                        for (int i = 0; i < resources.length(); i++) {
                            IdcsAuditLogs idcsAuditLogs = new IdcsAuditLogs();
                            JSONObject event = resources.getJSONObject(i);
                            idcsAuditLogs.setLogin(event.optString("actorName", null));
                            idcsAuditLogs.setResponse(event.optString("eventId", null));
                            idcsAuditLogs.setClientIp(event.optString("clientIp", null));
                            idcsAuditLogs.setApplicationRoleName(event.optString("adminResourceName", null));
                            idcsAuditLogs.setEcid(event.optString("ecId", null));
                            idcsAuditLogs.setAuthenticationLevel(event.optString("ssoAuthnLevel", null));
                            idcsAuditLogs.setSsoComments(event.optString("ssoComments", null));
                            idcsAuditLogs.setMatchedSignOnPolicy(event.optString("ssoMatchedSignOnPolicyName", null));
                            idcsAuditLogs.setSsoBrowser(event.optString("ssoBrowser", null));
                            idcsAuditLogs.setMatchedSignOnPolicyRule(event.optString("ssoMatchedSignOnRule", null));
                            idcsAuditLogs.setSsoSignOnPolicyObligations(event.optString("SSO Sign-On policy obligations", null));
                            idcsAuditLogs.setProtectedResource(event.optString("ssoProtectedResource", null));
                            idcsAuditLogs.setUserAgent(event.optString("ssoUserAgent", null));

                            String timestamp = event.optString("timestamp", null);

                            if (timestamp != null && !timestamp.isEmpty()) {
                                idcsAuditLogs.setLoginDate(OffsetDateTime.parse(timestamp).toLocalDateTime());
                            } else {
                                idcsAuditLogs.setLoginDate(null);
                            }
                            idcsAuditLogs.setJobId(job_id);
                            logsArrayList.add(idcsAuditLogs);
                        }
                        auditLogReportRepo.saveAll(logsArrayList);
                        startIndex++;
                    } else {
                        break; // No more data
                    }
                }
            }

            jobStatusManger.updateStatus(job_id, true, "");
            log.info("{} job completed successfully | jobId={}", auditLog, job_id);

        } catch (Exception e) {
            e.printStackTrace();
            log.info("{} job failed | jobId={}", auditLog, job_id);

            jobStatusManger.updateStatus(job_id, false, e.getMessage());
        }
        jobStatusManger.updateStatus(job_id, true, "");
    }


    private void applicationAccessLogs() {
        Long job_id = jobStatusManger.setInProgressStatus(applicationAccessLog);
        log.info("{} access job started | jobId={}", applicationAccessLog, job_id);

        try {
            LocalDate today = LocalDate.now(ZoneOffset.UTC);
            LocalDate yesterday = today.minusDays(7);

            String end = today.atStartOfDay(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
            String start = yesterday.atStartOfDay(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
            int startIndex = 0;
            int totalResults = Integer.MAX_VALUE;
            String baseUrl = ssoDomain + "/admin/v1/AuditEvents";
//            String attributes = "actorName,actorDisplayName,eventId,clientip,timeStamp,serviceName,ssoBrowser,ecid,message,ssoComments,ssoUserAgent,ssoMatchedSignOnPolicy,adminValuesAdded,adminValuesRemoved,adminResourceName";


            while (startIndex < totalResults) {
                String filter = String.format("timestamp ge \"%s\" and timestamp lt \"%s\" and (eventId eq \"sso.app.access.failure\" or eventId eq \"sso.app.access.success\")", start, end);
                String encodedFilter = URLEncoder.encode(filter, StandardCharsets.UTF_8.toString());

                String fullUrl = String.format(baseUrl, encodedFilter, startIndex);

                URL url = new URL(fullUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setDoOutput(true);
                OauthToken token = IdcsTokenManager.fetchNewToken();
                conn.setRequestProperty("Authorization", "Bearer " + token.getAccessToken());

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    String response = reader.lines().collect(Collectors.joining());
                    JSONObject json = new JSONObject(response);
                    JSONArray resources = json.optJSONArray("Resources");
                    totalResults = json.optInt("totalResults", 0);
                    ArrayList<ApplicationAccessLog> logsArrayList = new ArrayList<>();

                    if (resources != null) {
                        for (int i = 0; i < resources.length(); i++) {
                            com.example.Entity.ApplicationAccessLog applicationAccessLog = new ApplicationAccessLog();
                            JSONObject event = resources.getJSONObject(i);
                            applicationAccessLog.setLogin(event.optString("actorName", null));
                            applicationAccessLog.setResponse(event.optString("eventId", null));
                            applicationAccessLog.setSsoLocalIp(event.optString("ssoLocalIp", null));
                            String timestamp = event.optString("timestamp", null);
                            if (timestamp != null && !timestamp.isEmpty()) {
                                applicationAccessLog.setDate(OffsetDateTime.parse(timestamp).toLocalDateTime());
                            } else {
                                applicationAccessLog.setDate(null);
                            }
                            applicationAccessLog.setJobId(job_id);
                            logsArrayList.add(applicationAccessLog);
                        }
                        applicationAccessRepo.saveAll(logsArrayList);
                        startIndex++;
                    } else {
                        break; // No more data
                    }
                }
            }

            jobStatusManger.updateStatus(job_id, true, "");
            log.info("{} job completed successfully | jobId={}", applicationAccessLog, job_id);

        } catch (Exception e) {
            e.printStackTrace();
            log.info("{} job failed | jobId={}", applicationAccessLog, job_id);

            jobStatusManger.updateStatus(job_id, false, e.getMessage());
        }
        jobStatusManger.updateStatus(job_id, true, "");
    }




    private void authenticationSuccessFailureLogs() {
        Long job_id = jobStatusManger.setInProgressStatus(authenticationLog);
        log.info("{} access job started | jobId={}", authenticationLog, job_id);

        try {
            LocalDate today = LocalDate.now(ZoneOffset.UTC);
            LocalDate yesterday = today.minusDays(7);

            String end = today.atStartOfDay(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
            String start = yesterday.atStartOfDay(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
            int startIndex = 0;
            int totalResults = Integer.MAX_VALUE;
            String baseUrl = ssoDomain + "/admin/v1/AuditEvents";
//            String attributes = "actorName,actorDisplayName,eventId,clientip,timeStamp,serviceName,ssoBrowser,ecid,message,ssoComments,ssoUserAgent,ssoMatchedSignOnPolicy,adminValuesAdded,adminValuesRemoved,adminResourceName";


            while (startIndex < totalResults) {
                String filter = String.format("timestamp ge \"%s\" and timestamp lt \"%s\" and (eventId eq \"sso.session.create.success\" or eventId eq \"sso.authentication.failure\")", start, end);
                String encodedFilter = URLEncoder.encode(filter, StandardCharsets.UTF_8.toString());
                String fullUrl = String.format(baseUrl, encodedFilter, startIndex);
                URL url = new URL(fullUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setDoOutput(true);
                OauthToken token = IdcsTokenManager.fetchNewToken();
                conn.setRequestProperty("Authorization", "Bearer " + token.getAccessToken());

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    String response = reader.lines().collect(Collectors.joining());
                    JSONObject json = new JSONObject(response);
                    JSONArray resources = json.optJSONArray("Resources");
                    totalResults = json.optInt("totalResults", 0);
                    ArrayList<AuthenticationLogs> logsArrayList = new ArrayList<>();

                    if (resources != null) {
                        for (int i = 0; i < resources.length(); i++) {
                            AuthenticationLogs applicationAccessLog = new AuthenticationLogs();
                            JSONObject event = resources.getJSONObject(i);
                            applicationAccessLog.setLogin(event.optString("actorName", null));
                            applicationAccessLog.setResponse(event.optString("eventId", null));

                            String timestamp = event.optString("timestamp", null);
                            if (timestamp != null && !timestamp.isEmpty()) {
                                applicationAccessLog.setDate(OffsetDateTime.parse(timestamp).toLocalDateTime());
                            } else {
                                applicationAccessLog.setDate(null);
                            }
                            applicationAccessLog.setJobId(job_id);
                            logsArrayList.add(applicationAccessLog);
                        }
                        authenticationLogRepo.saveAll(logsArrayList);
                        startIndex++;
                    } else {
                        break; // No more data
                    }
                }
            }

            jobStatusManger.updateStatus(job_id, true, "");
            log.info("{} job completed successfully | jobId={}", authenticationLog, job_id);

        } catch (Exception e) {
            e.printStackTrace();
            log.info("{} job failed | jobId={}", authenticationLog, job_id);

            jobStatusManger.updateStatus(job_id, false, e.getMessage());
        }
        jobStatusManger.updateStatus(job_id, true, "");
    }


}


