package com.example.AdService;

import com.example.Entity.AdMaster;
import com.example.Repository.AdRepository;
import com.example.Repository.JobStatusLogRepository;
import com.example.idcsService.EnumTemplate;
import com.example.idcsService.JobStatusManger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class AdService {

    @Autowired
    private AdRepository adRepository;
    @Autowired
    private JobStatusLogRepository jobsStatusLogRepo;
    @Autowired
    private JobStatusManger jobStatusManger;

    private static final String DIR = "/mnt /data";

    @Scheduled(cron = "0 0 5 * * ?")
    public void scanFiles() {
        File folder = new File(DIR);
        File[] files = folder.listFiles();
        Long jobId = jobStatusManger.setInProgressStatus(EnumTemplate.adUserSyncJob, "");

        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        if (files == null) return;
        for (File file : files) {

            if (!file.getName().endsWith(".csv")) continue;

            try {
                if (file.getName().contains(today)) {
                    if (!isFileReady(file)) {
                        continue;
                    }
                    processCsv(file, jobId);
                    System.out.println("Processing: " + file.getName());
                    jobStatusManger.updateStatus(jobId, true, "", file.getName());

                } else {
                    jobStatusManger.updateStatus(jobId, true, "File not available.", "");

                }
            } catch (Exception e) {

                jobStatusManger.updateStatus(jobId, false, e.getMessage(), file.getName());
                System.out.println("Error in file: " + file.getName());
            }
        }
    }


    private boolean isFileReady(File file) {

        long size1 = file.length();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {
        }

        long size2 = file.length();

        return size1 == size2;
    }


    public void processCsv(File file, Long jobId) {

//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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

