package com.turno.los.service.impl;

import com.turno.los.entity.Agent;
import com.turno.los.entity.Customer;
import com.turno.los.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final JdbcTemplate jdbc;

    @Override
    public void pushToAgent(Agent agent, String message) {
        if (agent == null) return;
        log.info("[PUSH→AGENT] {}: {}", agent.getName(), message);
        jdbc.update("insert into notifications(channel, recipient, message) values (?,?,?)",
                "PUSH", agent.getName(), message);
    }

    @Override
    public void pushToManager(Agent manager, String message) {
        if (manager == null) return;
        log.info("[PUSH→MANAGER] {}: {}", manager.getName(), message);
        jdbc.update("insert into notifications(channel, recipient, message) values (?,?,?)",
                "PUSH", manager.getName(), message);
    }

    @Override
    public void smsToCustomer(Customer customer, String message) {
        log.info("[SMS→CUSTOMER] {} ({}): {}", customer.getName(), customer.getPhone(), message);
        jdbc.update("insert into notifications(channel, recipient, message) values (?,?,?)",
                "SMS", customer.getPhone(), message);
    }
}
