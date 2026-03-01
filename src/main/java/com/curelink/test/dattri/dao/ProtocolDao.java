package com.curelink.test.dattri.dao;

import java.util.List;
import java.util.Optional;

import com.curelink.test.dattri.entity.Protocol;

/**
 * Data access for protocols (e.g. fever, stomach ache, refund).
 */
public interface ProtocolDao {

    Optional<Protocol> findByCode(String code);

    List<Protocol> findAll();
}
