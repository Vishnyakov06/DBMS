package com.example.DBApp;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DbConnectionTest implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public DbConnectionTest(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        String sql = "SELECT current_database()";
        String dbName = jdbcTemplate.queryForObject(sql, String.class);
        System.out.println("✅ Подключено к БД: " + dbName);

    }
}