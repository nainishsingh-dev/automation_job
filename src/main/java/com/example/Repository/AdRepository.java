package com.example.Repository;

import com.example.Entity.AdMaster;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdRepository extends JpaRepository<AdMaster,Long> {
}
