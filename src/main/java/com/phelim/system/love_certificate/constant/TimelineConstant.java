package com.phelim.system.love_certificate.constant;

import com.phelim.system.love_certificate.model.Milestone;

import java.util.List;

public class TimelineConstant {

    public static final List<Milestone> MILESTONES = List.of(
            new Milestone(0, "First Day 💖"),
            new Milestone(30, "1 Month 🎉"),
            new Milestone(100, "100 Days 💕"),
            new Milestone(365, "1 Year Anniversary 🥂"),
            new Milestone(730, "2 Years Anniversary 💎"),
            new Milestone(1825, "5 Years Anniversary 🎉"),
            new Milestone(3650, "10 Years Anniversary 🏆")
    );

    private TimelineConstant() {
        // prevent instantiation
    }
}
