package com.phelim.system.love_certificate.model;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Milestone {
    private int days;
    private String label;
}
