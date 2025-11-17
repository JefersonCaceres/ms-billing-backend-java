package com.castor.ms_billing_backend_java.infrastructure.adapter.db.oracle;

import com.castor.ms_billing_backend_java.domain.ports.out.OracleClientRepositoryPort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class OracleClientRepositoryAdapter implements OracleClientRepositoryPort {

    private final JdbcTemplate jdbcTemplate;

    public OracleClientRepositoryAdapter(
            @Qualifier("oracleJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void saveClient(Long id, String name, String email, boolean active) {

        String sql = """
                INSERT INTO CLIENT (ID, NAME, EMAIL, IS_ACTIVE)
                VALUES (?, ?, ?, ?)
                """;

        jdbcTemplate.update(sql,
                id,
                name,
                email,
                active ? "Y" : "N");
    }

    @Override
    public void updateClient(Long id, String name, String email, boolean active) {

        String sql = """
                UPDATE CLIENT
                SET NAME = ?, EMAIL = ?, IS_ACTIVE = ?
                WHERE ID = ?
                """;

        jdbcTemplate.update(sql,
                name,
                email,
                active ? "Y" : "N",
                id);
    }
}