package com.castor.ms_billing_backend_java.infrastructure.helper;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class ClientLogHelper {

    private final JdbcTemplate jdbc;

    public ClientLogHelper(@Qualifier("postgresJdbcTemplate") JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void log(Long clientId, String action, String fields) {
        String sql = """
            INSERT INTO billing.client_log (client_id, action, updated_fields)
            VALUES (?, ?, ?)
        """;
        jdbc.update(sql, clientId, action, fields);
    }
}
