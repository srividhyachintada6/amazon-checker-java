package com.amazonchecker.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;

/**
 * Cloud-ready Database Configuration supporting PostgreSQL (Render, Neon, Railway, Supabase, AWS RDS)
 * and seamless local fallback (H2 file database) for zero-setup local development and automated testing.
 */
@Configuration
public class DatabaseConfig {

    @Value("${DATABASE_URL:#{null}}")
    private String databaseUrl;

    @Value("${DATABASE_USERNAME:#{null}}")
    private String databaseUsername;

    @Value("${DATABASE_PASSWORD:#{null}}")
    private String databasePassword;

    @Value("${spring.datasource.url:#{null}}")
    private String springDatasourceUrl;

    @Value("${spring.datasource.username:#{null}}")
    private String springDatasourceUser;

    @Value("${spring.datasource.password:#{null}}")
    private String springDatasourcePass;

    @Bean
    @Primary
    public DataSource dataSource() {
        String url = databaseUrl != null && !databaseUrl.isBlank() ? databaseUrl.trim() : springDatasourceUrl;
        String user = databaseUsername != null && !databaseUsername.isBlank() ? databaseUsername.trim() : springDatasourceUser;
        String pass = databasePassword != null ? databasePassword : springDatasourcePass;

        // 1. If DATABASE_URL is in cloud URI format (postgres://user:pass@host:port/db)
        if (url != null && (url.startsWith("postgres://") || url.startsWith("postgresql://"))) {
            try {
                URI uri = new URI(url);
                String host = uri.getHost();
                int port = uri.getPort() > 0 ? uri.getPort() : 5432;
                String path = uri.getPath(); // starts with /
                String dbName = path != null && path.length() > 1 ? path.substring(1) : "";

                if (user == null || user.isBlank()) {
                    String userInfo = uri.getUserInfo();
                    if (userInfo != null && userInfo.contains(":")) {
                        String[] parts = userInfo.split(":", 2);
                        user = parts[0];
                        if (pass == null || pass.isBlank()) {
                            pass = parts[1];
                        }
                    } else if (userInfo != null) {
                        user = userInfo;
                    }
                }

                String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + dbName;
                if (uri.getQuery() != null && !uri.getQuery().isBlank()) {
                    jdbcUrl += "?" + uri.getQuery();
                }

                System.out.println("🐘 Connected to PostgreSQL Database at " + host + ":" + port + "/" + dbName);

                HikariDataSource ds = new HikariDataSource();
                ds.setDriverClassName("org.postgresql.Driver");
                ds.setJdbcUrl(jdbcUrl);
                ds.setUsername(user != null ? user : "postgres");
                ds.setPassword(pass != null ? pass : "");
                ds.setMaximumPoolSize(10);
                applySchemaMigrations(ds);
                return ds;

            } catch (Exception e) {
                System.err.println("⚠️ Could not parse PostgreSQL URI, falling back to raw URL: " + e.getMessage());
            }
        }

        // 2. If URL is a standard JDBC PostgreSQL URL
        if (url != null && url.startsWith("jdbc:postgresql:")) {
            System.out.println("🐘 Connected to PostgreSQL Database via JDBC URL");
            HikariDataSource ds = new HikariDataSource();
            ds.setDriverClassName("org.postgresql.Driver");
            ds.setJdbcUrl(url);
            ds.setUsername(user != null ? user : "postgres");
            ds.setPassword(pass != null ? pass : "");
            ds.setMaximumPoolSize(10);
            applySchemaMigrations(ds);
            return ds;
        }

        // 3. Local Development / Fallback to H2 Database
        String localH2Url = (url != null && url.startsWith("jdbc:h2:")) ? url
                : "jdbc:h2:file:./data/amazon_checker_db;DB_CLOSE_DELAY=-1";
        String localUser = user != null && !user.isBlank() ? user : "sa";
        String localPass = pass != null ? pass : "";

        System.out.println("💾 Running with local development database: " + localH2Url);

        HikariDataSource ds = new HikariDataSource();
        ds.setDriverClassName("org.h2.Driver");
        ds.setJdbcUrl(localH2Url);
        ds.setUsername(localUser);
        ds.setPassword(localPass);
        ds.setMaximumPoolSize(5);
        applySchemaMigrations(ds);
        return ds;
    }

    private void applySchemaMigrations(DataSource ds) {
        try (java.sql.Connection conn = ds.getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            try {
                stmt.execute("ALTER TABLE products ADD COLUMN IF NOT EXISTS store VARCHAR(32) DEFAULT 'AMAZON' NOT NULL");
            } catch (Exception ignored) {}
            try {
                stmt.execute("ALTER TABLE monitoring_results ADD COLUMN IF NOT EXISTS store VARCHAR(32) DEFAULT 'AMAZON' NOT NULL");
            } catch (Exception ignored) {}
        } catch (Exception ignored) {}
    }

    @Bean
    public org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer hibernatePropertiesCustomizer() {
        return hibernateProperties -> {
            hibernateProperties.put("hibernate.hbm2ddl.auto", "update");
            String url = databaseUrl != null && !databaseUrl.isBlank() ? databaseUrl.trim() : springDatasourceUrl;
            if (url != null && (url.startsWith("postgres://") || url.startsWith("postgresql://") || url.startsWith("jdbc:postgresql:"))) {
                hibernateProperties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
            } else {
                hibernateProperties.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
            }
        };
    }
}
