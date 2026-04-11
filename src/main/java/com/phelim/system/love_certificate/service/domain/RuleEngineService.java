package com.phelim.system.love_certificate.service.domain;

import com.phelim.system.love_certificate.entity.RuleConfig;
import com.phelim.system.love_certificate.enums.CertificateType;
import com.phelim.system.love_certificate.repository.RuleConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RuleEngineService {

    private final RuleConfigRepository ruleRepo;

    public CertificateType determineType(int durationDays) {
        List<RuleConfig> rules = ruleRepo.findAllByOrderByMinDaysDesc();
        for (RuleConfig rule : rules) {
            if (durationDays >= rule.getMinDays()) {
                return rule.getType();
            }
        }
        return CertificateType.DEFAULT;
    }
}
