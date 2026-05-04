package com.example.idcsService;

import com.example.Entity.JobsStatusLog;
import com.example.Repository.JobStatusLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class JobStatusManger {

    @Autowired
    private JobStatusLogRepository jobStatusLogRepository;

    public Long setInProgressStatus(String jobname,String fileName){
        JobsStatusLog jobsStatusLog=new JobsStatusLog();
        jobsStatusLog.setJob_name(jobname);
        jobsStatusLog.setFile_name(fileName);
        jobsStatusLog.setStatus("IN_PROGRESS");
        jobsStatusLog.setStart_timestamp(LocalDateTime.now());
        jobStatusLogRepository.save(jobsStatusLog);
        return jobsStatusLog.getJob_id();
    }

    public void updateStatus(Long jobId,boolean status,String message,String fileName){
        JobsStatusLog jobsStatusLog = jobStatusLogRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job ID not found"));
        jobsStatusLog.setStatus(status ? "SUCCESS" : "FAILURE");
        jobsStatusLog.setEnd_timestamp(LocalDateTime.now());
        jobsStatusLog.setFile_name(fileName);
        jobsStatusLog.setMessage(message);
        jobStatusLogRepository.save(jobsStatusLog);

    }
}
