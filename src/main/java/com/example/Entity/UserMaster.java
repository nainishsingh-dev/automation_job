package com.example.Entity;
import lombok.Data;
import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "sso_user_master")
@Data
public class UserMaster {
    @Id
    @Column(name = "EMPLOYEE_CODE", length = 255)
    private String employeeCode;

    @Column(name = "CATEGORY", length = 255)
    private String category;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @Column(name = "DATE_OF_JOINING")
    private LocalDateTime dateOfJoining;

    @Column(name = "DATE_OF_LEAVING")
    private LocalDate dateOfLeaving;

    @Column(name = "EMAIL", length = 255)
    private String email;

    @Column(name = "FIRST_NAME", length = 255)
    private String firstName;

    @Column(name = "LAST_NAME", length = 255)
    private String lastName;

    @Column(name = "MIDDLE_NAME", length = 255)
    private String middleName;

    @Column(name = "REPORTING_MANAGER_CODE", length = 255)
    private String reportingManagerCode;

    @Column(name = "REPORTING_MANAGER_NAME", length = 255)
    private String reportingManagerName;

    @Column(name = "DEPARTMENT_DESCRIPTION", length = 255)
    private String departmentDescription;

    @Column(name = "MODIFIED_DATE")
    private LocalDateTime modifiedDate;

    @Column(name = "JOB_CREATED_DATE")
    private LocalDateTime jobCreatedDate;
    @Column(name="HIERARCHYCATEGORY_DESCRIPTION")
    private String hierarchyCategoryDescription;

}
