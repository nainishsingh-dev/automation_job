package com.example;

import com.example.idcsService.AuditLogReport;
import com.example.idcsService.DeactivateUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class Controller {


    @Autowired
    private AuditLogReport auditLogReport;
    @Autowired
    private DeactivateUserService deactivateUserService;


    @GetMapping("/disable")
    String adduser() throws IOException {
        deactivateUserService.initiateOffBoardingProcess();
        return "success";
    }

    @GetMapping("/audit-log")
        String invokeAuditJob()
        {
            auditLogReport.fetchAllTodayAuditEvents();
            return "success";
        }
    @GetMapping("/audit-log/app")
    String invokeAuditJobApp()
    {
        auditLogReport.applicationAccessLogs();
        return "success";
    }
    @GetMapping("/audit-log/auth")
    String invokeAuditJobAuth()
    {
        auditLogReport.authenticationSuccessFailureLogs();
        return "success";
    }


    @GetMapping("/audit-log/noti")
    String invokeAuditJobAuthNot()
    {
        auditLogReport.notificationLogs();
        return "success";
    }
}
