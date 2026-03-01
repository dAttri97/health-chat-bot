package com.curelink.test.dattri.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.curelink.test.dattri.entity.Protocol;

public interface ProtocolRepository extends JpaRepository<Protocol, String> {

    Optional<Protocol> findByCode(String code);

    List<Protocol> findAllByOrderByCodeAsc();
}
