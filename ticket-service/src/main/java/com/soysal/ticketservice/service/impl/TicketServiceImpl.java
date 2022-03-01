package com.soysal.ticketservice.service.impl;

import com.soysal.client.AccountServiceClient;
import com.soysal.client.contract.AccountDto;
import com.soysal.ticketservice.dto.TicketDto;
import com.soysal.ticketservice.model.PriorityType;
import com.soysal.ticketservice.model.Ticket;
import com.soysal.ticketservice.model.TicketStatus;
import com.soysal.ticketservice.model.es.TicketModel;
import com.soysal.ticketservice.repository.TicketRepository;
import com.soysal.ticketservice.repository.es.TicketElasticRepository;
import com.soysal.ticketservice.service.TicketNotificationService;
import com.soysal.ticketservice.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketElasticRepository ticketElasticRepository;
    private final TicketRepository ticketRepository;
    private final TicketNotificationService ticketNotificationService;
    private final AccountServiceClient accountServiceClient;

    @Override
    @Transactional
    public TicketDto save(TicketDto ticketDto) {

        Ticket ticket = new Ticket();
        ResponseEntity<AccountDto> accountDtoResponseEntity = accountServiceClient.get(ticketDto.getAssignee());

        if (ticketDto.getDescription() == null)
            throw new IllegalArgumentException("Description is not null");

        ticket.setDescription(ticketDto.getDescription());
        ticket.setNotes(ticketDto.getNotes());
        ticket.setTicketDate(ticketDto.getTicketDate());
        ticket.setTicketStatus(TicketStatus.valueOf(ticketDto.getTicketStatus()));
        ticket.setPriorityType(PriorityType.valueOf(ticketDto.getPriorityType()));
        ticket.setAssignee(accountDtoResponseEntity.getBody().getId());

        ticket = ticketRepository.save(ticket);

        TicketModel ticketModel = TicketModel.builder()
                .description(ticket.getDescription())
                .notes(ticket.getNotes())
                .id(ticket.getId())
                .assignee(accountDtoResponseEntity.getBody().getFullName())
                .priorityType(ticket.getPriorityType().getLabel())
                .ticketStatus(ticket.getTicketStatus().getLabel())
                .ticketDate(ticket.getTicketDate()).build();
        ticketElasticRepository.save(ticketModel);

        ticketDto.setId(ticket.getId());

        ticketNotificationService.sendToQueue(ticket);
        return ticketDto;
    }

    @Override
    public TicketDto update(String id, TicketDto ticketDto) {
        return null;
    }

    @Override
    public TicketDto getById(String ticketId) {
        return null;
    }

    @Override
    public Page<TicketDto> getPagination(Pageable pageable) {
        return null;
    }
}
