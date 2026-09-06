package com.guilhermesemog.unimove.service;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdminBootstrapGuard {
    private final JdbcClient jdbc;

    public AdminBootstrapGuard(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public void claim() {
        int claimed = jdbc.sql("""
                UPDATE system_metadata
                SET metadata_value = 'claimed', updated_at = CURRENT_TIMESTAMP
                WHERE metadata_key = 'admin_bootstrap'
                  AND metadata_value = 'available'
                  AND NOT EXISTS (SELECT 1 FROM users WHERE role = 'ADMIN')
                """).update();
        if (claimed != 1) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Administrator bootstrap registration is no longer available");
        }
    }
}
