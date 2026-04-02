package com.example.idcsService;

import com.example.Entity.UserDeactivationLogEntity;
import com.example.Entity.UserMaster;
import com.example.Repository.UserDeactivationLogRepo;
import com.example.Repository.UserMasterRepo;
import com.example.Model.OauthToken;
//import com.sun.deploy.net.URLEncoder;
import java.net.URLEncoder;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DeactivateUserService {
    @Autowired
    private UserMasterRepo userMasterRepo;
    @Autowired
    private UserDeactivationLogRepo userDeactivationLogRepo;
    @Value("${sso.domain}")
    private String ssoDomain;
    @Autowired
    private JobStatusManger jobStatusManger;

    @Scheduled(cron = "0 0 18 * * ?")
    public void initiateOffBoardingProcess() throws IOException {
        Long job_id = jobStatusManger.setInProgressStatus("Offboarding_Job");
        StringBuilder errorMessages = new StringBuilder();

        try {
            LocalDate systemDate = LocalDate.now();

            log.info("SSO Deprovisioning Process started on {}", Calendar.getInstance().toInstant());
            List<UserMaster> boardedList = userMasterRepo.newOffBoardedUser();
            if (boardedList.isEmpty()) {
                log.info("No data found for offboarding {}", new Date());
                errorMessages.append("No data found for offboarding");
                jobStatusManger.updateStatus(job_id, true, errorMessages.toString());
                return;
            }
           List<UserMaster> todayOffboardingData=boardedList.stream().filter(users -> (users.getDateOfLeaving().isBefore(systemDate)) || users.getDateOfLeaving().isEqual(systemDate)).collect(Collectors.toList());

            log.info("User found for offboarding {}", todayOffboardingData.size());
            for (UserMaster deBoardedUsers : todayOffboardingData) {
                if(!userDeactivationLogRepo.alreadyDeactivatedFlag(deBoardedUsers.getEmployeeCode()).isPresent())
            {
                    UserDeactivationLogEntity deactivateModel = new UserDeactivationLogEntity();
                    String ocId = getUserOcId(deBoardedUsers.getEmail(), errorMessages);
                    if (!ocId.isEmpty()) {
                        deactivateModel.setOcid(ocId);
                        deactivateModel.setEmp_code(deBoardedUsers.getEmployeeCode());
                        deactivateModel.setEmail(deBoardedUsers.getEmail());
                        deactivateModel.setDate_of_leaving(deBoardedUsers.getDateOfLeaving());
                        deactivateFromIDcs(deactivateModel);
                    }
                }
            }

            jobStatusManger.updateStatus(job_id, true, errorMessages.toString());

        } catch (Exception e) {
            System.out.println(e.getMessage());
            jobStatusManger.updateStatus(job_id, false, e.getMessage());

        } finally {
            log.info("Offboarding process completed");

        }
    }

    private String getUserOcId(String emails, StringBuilder errorMsg) throws IOException {
        String ociId = "";
        try {

            log.info("Deactivation process started for {}", emails);
            String encodedFilter = URLEncoder.encode("emails eq \"" + emails + "\"", String.valueOf(StandardCharsets.UTF_8));
            String encoderAttributes = URLEncoder.encode("userName,active,urn:ietf:params:scim:schemas:idcs:extension:custom:User", String.valueOf(StandardCharsets.UTF_8));
            URL url = new URL(ssoDomain + "/admin/v1/Users?filter=" + encodedFilter + "&attributes=" + encoderAttributes);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setDoOutput(true);
            OauthToken token = IdcsTokenManager.fetchNewToken();
            conn.setRequestProperty("Authorization", "Bearer " + token.getAccessToken());

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String response = reader.lines().collect(Collectors.joining());
                JSONObject json = new JSONObject(response);

                JSONArray jsonArray = new JSONArray(json.getJSONArray("Resources"));
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    JSONObject customUser = jsonArray.getJSONObject(i).getJSONObject("urn:ietf:params:scim:schemas:idcs:extension:custom:User");
                    System.out.println(customUser);
                    ociId = jsonObject.getString("id");
                }
                if (ociId.isEmpty()) {
                    errorMsg.append("User not found in IDCS ").append(emails);
                    log.info("User not found in IDCS {}",emails);
                }

            } catch (Exception e) {
                System.out.println("exception " + e.getMessage());
                errorMsg.append("Error while fetching in IDCS for ").append(emails).append("Error-Msg ").append(e.getMessage());

            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            errorMsg.append("Error while fetching in IDCS for ").append(emails).append("Error-Msg ").append(e.getMessage());

        }
        return ociId;
    }


    private void deactivateFromIDcs(UserDeactivationLogEntity deactivateModel) throws IOException {
        try {

            URL url = new URL(ssoDomain + "/admin/v1/UserStatusChanger/" + URLEncoder.encode(deactivateModel.getOcid(), String.valueOf(StandardCharsets.UTF_8)));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("PUT");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            OauthToken token = IdcsTokenManager.fetchNewToken();
            conn.setRequestProperty("Authorization", "Bearer " + token.getAccessToken());

            JSONObject body = new JSONObject();
            body.put("active", false);
//            body.put("active", true);
            body.put("schemas", new String[]{"urn:ietf:params:scim:schemas:oracle:idcs:UserStatusChanger"});
            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.toString().getBytes(StandardCharsets.UTF_8));
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String response = reader.lines().collect(Collectors.joining());
                JSONObject json = new JSONObject(response);
                boolean currentStatus = json.getBoolean("active");
                String id = json.getString("id");
                System.out.println("following user deactivated " + id + " Status " + currentStatus);
                LocalDateTime now = LocalDateTime.now();
                deactivateModel.setDeactivate_timeStamp(now);
                deactivateModel.setStatus("Success");
                userDeactivationLogRepo.save(deactivateModel);

            } catch (Exception e) {
                System.out.println("exception " + e.getMessage());

            }
        } catch (JSONException e) {
            System.out.println("exception " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}