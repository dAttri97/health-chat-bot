package com.curelink.test.dattri.dao;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.curelink.test.dattri.entity.UserMemory;
import com.curelink.test.dattri.repository.UserMemoryRepository;

@Component
public class UserMemoryDaoImpl implements UserMemoryDao {

    private final UserMemoryRepository userMemoryRepository;

    public UserMemoryDaoImpl(UserMemoryRepository userMemoryRepository) {
        this.userMemoryRepository = userMemoryRepository;
    }

    @Override
    @Transactional
    public UserMemory save(UserMemory memory) {
        return userMemoryRepository.save(memory);
    }

    @Override
    public List<UserMemory> findBySessionId(String sessionId) {
        return userMemoryRepository.findByChatSession_IdOrderByCreatedAtDesc(sessionId);
    }
}
