package com.example.idcsService;

import com.example.Entity.IdcsAuditLogs;
import com.example.Entity.JobsStatusLog;
import com.example.Repository.AuditLogsRepo;
import com.example.Model.OauthToken;
//import com.sun.deploy.net.*;
import java.net.URLEncoder;
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
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.stream.Collectors;
@Service

public class AuditLogReport {
    @Value("${sso.domain}")
    private String ssoDomain;

    @Autowired
    private AuditLogsRepo auditLogReportRepo;
    @Autowired
    private JobStatusManger jobStatusManger;
    @Scheduled(cron = "0 0 1 * * ?")

    public void fetchAllTodayAuditEvents() {
        Long job_id= jobStatusManger.setInProgressStatus("Audit_Log");

        try {
            LocalDate today = LocalDate.now(ZoneOffset.UTC);
            LocalDate yesterday = today.minusDays(7);

            String end = today.atStartOfDay(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
            String start = yesterday.atStartOfDay(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
            int startIndex = 0;
            int totalResults = Integer.MAX_VALUE;
            String baseUrl = ssoDomain+"/admin/v1/AuditEvents";
            String attributes = "actorName,actorDisplayName,eventId,clientip,timeStamp,serviceName,ssoBrowser,ecid,message,ssoComments,ssoUserAgent,ssoMatchedSignOnPolicy,adminValuesAdded,adminValuesRemoved,adminResourceName";


            while (startIndex < totalResults) {
                String filter = String.format(
                        "timestamp ge \"%s\" and timestamp lt \"%s\" and(eventId eq \"sso.session.create.success\" or eventId eq \"sso.session.modify.success\" or eventId eq \"sso.authentication.failure\" or eventId eq \"admin.user.create.success\" or eventId eq \"admin.user.update.success\" or eventId eq \"admin.user.delete.success\" or eventId eq \"idcs.login.success\" or eventId eq \"idcs.login.failure\") and not (actorDisplayName eq \"ID Bridge\" or actorDisplayName eq \"idcssso\")",
                        start, end
                );

                System.out.println("page index +"+startIndex);
                String encodedFilter = URLEncoder.encode(filter, StandardCharsets.UTF_8.toString());

                String fullUrl = String.format(
                        "%s?attributeSets=all&attributes=%s&filter=%s&startIndex=%d",
                        baseUrl, attributes, encodedFilter, startIndex
                );

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
                    ArrayList<IdcsAuditLogs> logsArrayList=new ArrayList<>();
                    System.out.println("total pages "+totalResults);

                    if (resources != null) {
                        for (int i = 0; i < resources.length(); i++) {
                            IdcsAuditLogs idcsAuditLogs=new IdcsAuditLogs();
                            JSONObject event = resources.getJSONObject(i);
                            idcsAuditLogs.setEventId(event.optString("eventId", null));
                            idcsAuditLogs.setActorName(event.optString("actorName", null));
                            idcsAuditLogs.setClientIp(event.optString("clientIp", null));
                            idcsAuditLogs.setActorDisplayName(event.optString("actorDisplayName", null));
                            idcsAuditLogs.setServiceName(event.optString("serviceName", null));
                            idcsAuditLogs.setEcId(event.optString("ecId", null));
                            idcsAuditLogs.setMessage(event.optString("message", null));
                            idcsAuditLogs.setSsoComments(event.optString("ssoComments", null));
                            idcsAuditLogs.setTimestamp(event.optString("timestamp", null));
                            idcsAuditLogs.setSsoBrowser(event.optString("ssoBrowser", null));
                            idcsAuditLogs.setId(event.optString("id", null));
                            idcsAuditLogs.setSsoMatchedSignOnPolicy(event.optString("ssoMatchedSignOnPolicy", null));
                            idcsAuditLogs.setAdminValueAdded(event.optString("adminValuesAdded", null));
                            idcsAuditLogs.setAdminValueRemoved(event.optString("adminValuesRemoved", null));
                            idcsAuditLogs.setAdminResourceName(event.optString("adminResourceName", null));

                            logsArrayList.add(idcsAuditLogs);
                        }
                        auditLogReportRepo.saveAll(logsArrayList);
                        startIndex ++;
                    } else {
                        break; // No more data
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            jobStatusManger.updateStatus(job_id,false,e.getMessage());
        }
        jobStatusManger.updateStatus(job_id,true,"");
    }
}


