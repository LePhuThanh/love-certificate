package com.phelim.system.love_certificate.repository;

import com.phelim.system.love_certificate.entity.RuleConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RuleConfigRepository extends JpaRepository<RuleConfig, Long> {
    List<RuleConfig> findAllByOrderByMinDaysDesc();
}
