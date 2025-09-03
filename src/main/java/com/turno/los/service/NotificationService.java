package com.turno.los.service;

import com.turno.los.entity.Agent;
import com.turno.los.entity.Customer;

public interface NotificationService {
    void pushToAgent(Agent agent, String message);
    void pushToManager(Agent manager, String message);
    void smsToCustomer(Customer customer, String message);
}
