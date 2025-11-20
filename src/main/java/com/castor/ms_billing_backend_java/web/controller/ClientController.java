package com.castor.ms_billing_backend_java.web.controller;

import com.castor.ms_billing_backend_java.application.dto.ClientDto;
import com.castor.ms_billing_backend_java.application.mapper.ClientMapper;
import com.castor.ms_billing_backend_java.application.request.InvoiceCalculationRequest;
import com.castor.ms_billing_backend_java.application.response.InvoiceCalculationResponse;
import com.castor.ms_billing_backend_java.domain.model.Client;
import com.castor.ms_billing_backend_java.domain.ports.in.ClientUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

    private final ClientUseCase clientUseCase;

    public ClientController(ClientUseCase clientUseCase) {
        this.clientUseCase = clientUseCase;
    }

    @PostMapping("/create")
    public ResponseEntity<ClientDto> create(@RequestBody ClientDto dto) {
        Client client = ClientMapper.toDomain(dto);
        Client saved = clientUseCase.create(client);
        return ResponseEntity.ok(ClientMapper.toDto(saved));
    }

    @GetMapping("/get/{document}")
    public ResponseEntity<ClientDto> getByDocument(@PathVariable String document) {
        Client client = clientUseCase.findByDocument(document);
        return ResponseEntity.ok(ClientMapper.toDto(client));
    }

    @PutMapping("update/{document}")
    public ResponseEntity<ClientDto> update(
            @PathVariable String document,
            @RequestBody ClientDto dto
    ) {
        Client client = ClientMapper.toDomain(dto);
        Client updated = clientUseCase.update(document, client);
        return ResponseEntity.ok(ClientMapper.toDto(updated));
    }

    @DeleteMapping("delete/{document}")
    public ResponseEntity<Void> delete(@PathVariable String document) {
        clientUseCase.deleteByDocument(document);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/bill/{document}")
    public ResponseEntity<InvoiceCalculationResponse> create(
            @PathVariable String document,
            @RequestBody InvoiceCalculationRequest request) {
        return ResponseEntity.ok(clientUseCase.createInvoice(document, request));
    }
}
