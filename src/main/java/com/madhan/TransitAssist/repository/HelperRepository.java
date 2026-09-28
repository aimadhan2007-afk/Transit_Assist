package com.madhan.TransitAssist.repository;

import com.madhan.TransitAssist.model.Helper;
import com.madhan.TransitAssist.model.HelperAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HelperRepository extends JpaRepository<Helper, Long> {
    List<Helper> findByAvailability(HelperAvailability availability);
    long countByAvailability(HelperAvailability availability);
}
