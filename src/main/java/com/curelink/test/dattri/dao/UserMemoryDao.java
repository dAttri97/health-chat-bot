package com.curelink.test.dattri.dao;

import java.util.List;

import com.curelink.test.dattri.entity.UserMemory;

/**
 * Data access for user long-term memories (per session).
 */
public interface UserMemoryDao {

    UserMemory save(UserMemory memory);

    List<UserMemory> findBySessionId(String sessionId);
}
