package com.phelim.system.love_certificate.repository;

import com.phelim.system.love_certificate.entity.LoveStory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LoveStoryRepository extends JpaRepository<LoveStory, String> {

    Optional<LoveStory> findBySessionIdAndActiveTrue(String sessionId);
    List<LoveStory> findBySessionIdOrderByVersionDesc(String sessionId);
    Optional<LoveStory> findFirstBySessionIdAndActiveTrue(String sessionId);
}
