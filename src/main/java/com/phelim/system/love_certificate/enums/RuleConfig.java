package com.phelim.system.love_certificate.entity;

import com.phelim.system.love_certificate.enums.CertificateType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "rule_config")
public class RuleConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "min_days")
    private int minDays;
    @Column(name = "type")
    private CertificateType type;
}
