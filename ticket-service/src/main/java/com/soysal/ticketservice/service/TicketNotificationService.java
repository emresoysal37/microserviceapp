package com.soysal.ticketservice.service;

import com.soysal.ticketservice.model.Ticket;

public interface TicketNotificationService {

    void sendToQueue(Ticket ticket);
}
