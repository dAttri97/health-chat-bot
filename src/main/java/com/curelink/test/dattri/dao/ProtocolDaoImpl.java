package com.curelink.test.dattri.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.curelink.test.dattri.entity.Protocol;
import com.curelink.test.dattri.repository.ProtocolRepository;

@Component
public class ProtocolDaoImpl implements ProtocolDao {

    private final ProtocolRepository protocolRepository;

    public ProtocolDaoImpl(ProtocolRepository protocolRepository) {
        this.protocolRepository = protocolRepository;
    }

    @Override
    public Optional<Protocol> findByCode(String code) {
        return protocolRepository.findByCode(code);
    }

    @Override
    public List<Protocol> findAll() {
        return protocolRepository.findAllByOrderByCodeAsc();
    }
}
