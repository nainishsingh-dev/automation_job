package com.example.Repository;

import com.example.Entity.UserMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface UserMasterRepo extends JpaRepository<UserMaster,Long> {
//

//    @Query("Select c from UserMaster c WHERE (TRUNC(c.modifiedDate) = TRUNC(SYSDATE) and c.dateOfLeaving is not null) OR TRUNC(c.dateOfLeaving) = TRUNC(SYSDATE)")
    @Query("SELECT c FROM UserMaster c WHERE c.hierarchyCategoryDescription IN ('N4', 'N5 and Below') AND (trunc(c.dateOfLeaving) = trunc(sysdate) OR (trunc(c.modifiedDate) = trunc(sysdate) AND trunc(c.dateOfLeaving)<trunc(sysdate)))")
    List<UserMaster> newOffBoardedUser();
}
