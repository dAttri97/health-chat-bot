package com.curelink.test.dattri.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.curelink.test.dattri.entity.UserMemory;

public interface UserMemoryRepository extends JpaRepository<UserMemory, String> {

    List<UserMemory> findByChatSession_IdOrderByCreatedAtDesc(String sessionId);
}
