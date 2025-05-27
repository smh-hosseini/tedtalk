package com.iodigital.assignment.tedtalks;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

public class TestcontainersConfiguration {

    @Testcontainers
    public static class PostgresTestConfiguration {

        @Bean
        public PostgreSQLContainer<?> postgreSQLContainer() {
            PostgreSQLContainer<?> container = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16"))
                    .withDatabaseName("tedtalk-test-db")
                    .withUsername("test")
                    .withPassword("test");
            container.start();
            return container;
        }
    }

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext context) {
            PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16"))
                    .withDatabaseName("tedtalk-test-db")
                    .withUsername("test")
                    .withPassword("test");

            postgres.start();

            // Log the connection URL to help with debugging
            System.out.println("PostgreSQL container started at: " + postgres.getJdbcUrl());

            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                    context,
                    "spring.datasource.url=" + postgres.getJdbcUrl(),
                    "spring.datasource.username=" + postgres.getUsername(),
                    "spring.datasource.password=" + postgres.getPassword(),
                    "spring.jpa.hibernate.ddl-auto=validate",
                    "spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect"
            );
        }
    }



}
