package com.castor.ms_billing_backend_java.application.service;

import com.castor.ms_billing_backend_java.application.mapper.ClientMapper;
import com.castor.ms_billing_backend_java.application.request.InvoiceCalculationRequest;
import com.castor.ms_billing_backend_java.application.response.InvoiceCalculationResponse;
import com.castor.ms_billing_backend_java.domain.exception.ClientNotFoundException;
import com.castor.ms_billing_backend_java.domain.model.BillingParameter;
import com.castor.ms_billing_backend_java.domain.model.Client;
import com.castor.ms_billing_backend_java.domain.ports.in.OracleInvoicePort;
import com.castor.ms_billing_backend_java.domain.ports.in.TaxServicePort;
import com.castor.ms_billing_backend_java.domain.ports.out.BillingParameterRepositoryPort;
import com.castor.ms_billing_backend_java.domain.ports.out.ClientRepositoryPort;
import com.castor.ms_billing_backend_java.domain.ports.out.OracleClientRepositoryPort;
import com.castor.ms_billing_backend_java.infrastructure.helper.ClientLogHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientUseCaseImplTest {

    @Mock
    private ClientRepositoryPort postgresPort;
    @Mock
    private BillingParameterRepositoryPort parameterPort;
    @Mock
    private TaxServicePort taxServicePort;
    @Mock
    private OracleClientRepositoryPort oraclePort;
    @Mock
    private OracleInvoicePort oracleInvoicePort;
    @Mock
    private ClientLogHelper clientLogHelper;
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
        verify(clientLogHelper).log(eq(1L), eq("CREATE"), anyString());
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
        verify(clientLogHelper).log(eq(1L), eq("CREATE"), anyString());
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

        verify(clientLogHelper).log(eq(1L), eq("UPDATE"), anyString());
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
        existing.setDocument("123");
        existing.setName("User");
        existing.setEmail("u@mail.com");
        existing.setActive(true);

        when(postgresPort.findByDocument("123"))
                .thenReturn(Optional.of(existing));

        // Ejecutar
        useCase.deleteByDocument("123");

        // 🔵 Soft delete: se debe guardar con active = false
        verify(postgresPort).save(argThat(c ->
                c.getId().equals(1L) &&
                        !c.isActive()
        ));

        // 🔵 Log
        verify(clientLogHelper).log(
                eq(1L),
                eq("DELETE"),
                contains("Se elimina logicamente el cliente")
        );

        // 🔵 Replicación en Oracle
        verify(oraclePort).updateClient(
                eq(1L),
                eq("User"),
                eq("u@mail.com"),
                eq(false)
        );
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


    @Test
    void testCreateInvoice_ClientActive() {

        // CLIENTE ACTIVO
        Client client = Client.builder()
                .id(1L)
                .documentType("CC")
                .document("12345")
                .name("Jefferson")
                .email("jeff@mail.com")
                .phone("3000000000")
                .address("Barranquilla")
                .active(true)
                .build();

        when(postgresPort.findByDocument("12345"))
                .thenReturn(Optional.of(client));

        // PARAMETERS
        BillingParameter taxParam = new BillingParameter(
                1L, "TAX", "IVA", "Impuesto general",
                19.0, null, null, true
        );

        BillingParameter discountParam = new BillingParameter(
                2L, "DISCOUNT", "DESCUENTO 10%",
                "Aplica para compras >= 100000",
                10.0, null, 100000.0, true
        );

        when(parameterPort.findActiveParameters())
                .thenReturn(List.of(taxParam, discountParam));

        // REQUEST
        InvoiceCalculationRequest req = new InvoiceCalculationRequest();

        InvoiceCalculationRequest.Item item = new InvoiceCalculationRequest.Item();
        item.setDescription("Prod1");
        item.setQuantity(2);
        item.setUnit_price(100000.0);

        req.setItems(List.of(item));

        //MOCK PYTHON RESPONSE
        InvoiceCalculationResponse pythonResp =
                new InvoiceCalculationResponse(
                        null, null,
                        200000.0, 38000.0, 20000.0, 218000.0,
                        "OK"
                );

        when(taxServicePort.calculate(any(InvoiceCalculationRequest.class)))
                .thenReturn(pythonResp);

        // MOCK ORACLE
        when(oracleInvoicePort.createInvoice(
                eq(1L),
                eq(200000.0),
                eq(38000.0),
                eq(20000.0),
                eq(218000.0)
        )).thenReturn(55L);
        //  EXECUTE
        InvoiceCalculationResponse resp =
                useCase.createInvoice("12345", req);
        //  VALIDACIONES
        assertNotNull(resp);
        assertEquals(55L, resp.getInvoiceId());
        assertEquals(1L, resp.getClientId());
        assertEquals(200000.0, resp.getSubtotal());
        assertEquals(38000.0, resp.getTax());
        assertEquals(20000.0, resp.getDiscount());
        assertEquals(218000.0, resp.getTotal());
        // VERIFY LOG
        verify(clientLogHelper).log(
                eq(1L),
                eq("BILL"),
                contains("Factura generada")
        );
        // VERIFY PYTHON CALL
        verify(taxServicePort, times(1)).calculate(any());
        //  VERIFY ORACLE
        verify(oracleInvoicePort, times(1)).createInvoice(
                eq(1L),
                eq(200000.0),
                eq(38000.0),
                eq(20000.0),
                eq(218000.0)
        );
    }
}
