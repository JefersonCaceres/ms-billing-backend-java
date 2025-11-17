package com.castor.ms_billing_backend_java.application.service;

import com.castor.ms_billing_backend_java.application.mapper.ClientMapper;
import com.castor.ms_billing_backend_java.domain.exception.ClientNotFoundException;
import com.castor.ms_billing_backend_java.domain.model.Client;
import com.castor.ms_billing_backend_java.domain.ports.out.ClientRepositoryPort;
import com.castor.ms_billing_backend_java.domain.ports.out.OracleClientRepositoryPort;
import com.castor.ms_billing_backend_java.infrastructure.helper.ClientLogHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientUseCaseImplTest {

    @Mock
    private ClientRepositoryPort postgresPort;

    @Mock
    private OracleClientRepositoryPort oraclePort;

    @Mock
    private ClientLogHelper logHelper;

    @Mock
    private ClientMapper mapper;

    @InjectMocks
    private ClientUseCaseImpl useCase;


    @Test
    void create_shouldSaveClientAndReplicateToOracleAndLog() {

        Client client = new Client();
        client.setName("Test User");
        client.setEmail("test@mail.com");

        Client saved = new Client();
        saved.setId(1L);
        saved.setName("Test User");
        saved.setEmail("test@mail.com");

        when(postgresPort.save(any())).thenReturn(saved);

        Client result = useCase.create(client);

        assertNotNull(result);
        verify(postgresPort).save(any(Client.class));
        verify(oraclePort).saveClient(eq(1L), eq("Test User"), eq("test@mail.com"), anyBoolean());
        verify(logHelper).log(eq(1L), eq("CREATE"), anyString());
    }

    @Test
    void create_shouldNotFailWhenOracleThrowsException() {

        Client client = new Client();
        client.setName("User");
        client.setEmail("u@mail.com");

        Client saved = new Client();
        saved.setId(1L);
        saved.setName("User");
        saved.setEmail("u@mail.com");

        when(postgresPort.save(any())).thenReturn(saved);
        doThrow(new RuntimeException("Oracle down")).when(oraclePort).saveClient(any(), any(), any(), anyBoolean());

        Client result = useCase.create(client);

        assertNotNull(result);
        verify(postgresPort).save(any());
        verify(logHelper).log(eq(1L), eq("CREATE"), anyString());
    }

    @Test
    void update_shouldUpdateClientReplicateAndLog() {

        // Cliente existente en la base
        Client existing = new Client();
        existing.setId(1L); // 🔥 necesario
        existing.setName("Old");
        existing.setEmail("old@mail.com");

        // Datos nuevos que llegan desde la petición
        Client incoming = new Client();
        incoming.setName("New");
        incoming.setEmail("new@mail.com");

        when(postgresPort.findByDocument("123")).thenReturn(Optional.of(existing));

        // 🔥 Aquí devolvemos el "existing" modificado, como lo haría JPA
        when(postgresPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Client result = useCase.update("123", incoming);

        assertEquals("New", result.getName());

        verify(postgresPort).findByDocument("123");
        verify(postgresPort).save(any());

        // 🔥 EXISTING TIENE ID = 1L, por eso aquí sí debe pasar
        verify(oraclePort).updateClient(
                eq(1L),
                eq("New"),
                eq("new@mail.com"),
                anyBoolean()
        );

        verify(logHelper).log(eq(1L), eq("UPDATE"), anyString());
    }


    @Test
    void update_shouldThrowExceptionWhenNotFound() {
        when(postgresPort.findByDocument("123")).thenReturn(Optional.empty());
        assertThrows(ClientNotFoundException.class, () -> useCase.update("123", new Client()));
    }
    @Test
    void delete_shouldDeleteAndSoftDeleteOracle() {

        Client existing = new Client();
        existing.setId(1L);
        existing.setName("User");
        existing.setEmail("u@mail.com");

        when(postgresPort.findByDocument("123")).thenReturn(Optional.of(existing));

        useCase.deleteByDocument("123");

        verify(postgresPort).deleteByDocument("123");
        verify(oraclePort).updateClient(eq(1L), eq("User"), eq("u@mail.com"), eq(false));
    }

    @Test
    void delete_shouldThrowWhenNotFound() {

        when(postgresPort.findByDocument("123")).thenReturn(Optional.empty());

        assertThrows(ClientNotFoundException.class,
                () -> useCase.deleteByDocument("123")
        );
    }

    @Test
    void find_shouldReturnClient() {
        Client client = new Client();
        client.setId(1L);
        client.setName("User");

        when(postgresPort.findByDocument("123")).thenReturn(Optional.of(client));

        Client result = useCase.findByDocument("123");

        assertEquals("User", result.getName());
    }

    @Test
    void find_shouldThrowExceptionWhenNotFound() {
        when(postgresPort.findByDocument("123")).thenReturn(Optional.empty());
        assertThrows(ClientNotFoundException.class, () -> useCase.findByDocument("123"));
    }
}
