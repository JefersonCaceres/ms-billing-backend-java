package com.castor.ms_billing_backend_java.infrastructure.adapter.db.oracle;

import com.castor.ms_billing_backend_java.domain.ports.in.OracleInvoicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Types;

@Repository
@RequiredArgsConstructor
public class OracleInvoiceAdapter implements OracleInvoicePort {

    private final JdbcTemplate oracleJdbcTemplate;

    @Override
    public Long createInvoice(Long clientId, Double subtotal, Double tax, Double discount, Double total) {

        return oracleJdbcTemplate.execute((Connection conn) -> {

            CallableStatement stmt = conn.prepareCall(
                    "{ call PR_CREATE_INVOICE(?, ?, ?, ?, ?, ?) }"
            );

            stmt.setLong(1, clientId);
            stmt.setDouble(2, subtotal);
            stmt.setDouble(3, tax);
            stmt.setDouble(4, discount);
            stmt.setDouble(5, total);

            stmt.registerOutParameter(6, Types.NUMERIC);

            stmt.execute();
            return stmt.getLong(6);
        });
    }
}

