package com.example.AdService;

import com.example.Entity.AdMaster;
import com.example.Repository.AdRepository;
import com.example.Repository.JobStatusLogRepository;
import com.example.idcsService.EnumTemplate;
import com.example.idcsService.JobStatusManger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.stream.Stream;

@Slf4j
@Service
public class AdService {

    @Autowired
    private AdRepository adRepository;
    @Autowired
    private JobStatusLogRepository jobsStatusLogRepo;
    @Autowired
    private JobStatusManger jobStatusManger;

    private static final String DIR = "/mnt/data";

    @Scheduled(cron = "0 0 10 * * ?")
    public void scanFiles() {
        File folder = new File(DIR);
        File[] fileList = folder.listFiles();
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        if (fileList == null) return;
        File[] files = Arrays.stream(fileList)
                .filter(file1 -> file1.getName().contains(today) && file1.getName().endsWith(".csv"))
                .toArray(File[]::new);

        Long jobId = jobStatusManger.setInProgressStatus(EnumTemplate.adUserSyncJob, "");
        log.info("Ad sync process started jobId {}", jobId);
        log.info("Files available in directory for today {}", files.length);
        if (files.length > 0) {

            for (File file : files) {
                try {
                        processCsv(file, jobId);
                        jobStatusManger.updateStatus(jobId, true, "Processing completed successfully", "");
                        log.info("Ad sync completed successfully jobId {}", jobId);
                } catch (Exception e) {

                    jobStatusManger.updateStatus(jobId, false, e.getMessage(), file.getName());
                    System.out.println("Error in file: " + file.getName());
                    log.error(e.getMessage());
                }
            }
        }
        jobStatusManger.updateStatus(jobId, true, "Files not available for today", "");
        log.info("Ad sync completed successfully jobId {}", jobId);

    }


    public void processCsv(File file, Long jobId) {

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {

            String line;
            boolean header = true;
            while ((line = br.readLine()) != null) {
                if (header) {
                    header = false;
                    continue;
                }
                try {
                    String[] c = line.split(",");
                    if (c.length < 9) continue;
                    AdMaster user = new AdMaster();
                    user.setDisplayName(c[0]);
                    user.setSamAccountName(c[1]);
                    user.setMail(c[2]);
                    user.setDescription(c[3]);
                    user.setDepartment(c[4]);
                    user.setOffice(c[5]);
//                    LocalDateTime whenCreated = LocalDateTime.parse(c[6], formatter);

                    String rawDate = c[6].replace("\"", "").trim();

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy h:mm:ss a");
                    LocalDateTime whenCreated = LocalDateTime.parse(rawDate, formatter);

                    user.setWhenCreated(whenCreated);
                    user.setManager(c[7]);
                    user.setEnabled(Boolean.parseBoolean(c[8]));
                    user.setJob_id(jobId);

                    adRepository.save(user);

                } catch (Exception e) {
                    System.out.println("Bad row: " + line);
                    jobStatusManger.updateStatus(jobId, false, e.getMessage(), file.getName());
                    System.out.println("Error in file: " + file.getName());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}

