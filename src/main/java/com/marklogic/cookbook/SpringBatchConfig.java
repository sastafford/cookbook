package com.marklogic.cookbook;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableBatchProcessing
@Import(MigrateSqlDatabaseJob.class)
public class SpringBatchConfig {

    @Bean
    public DatabaseClient databaseClient() {
        DatabaseClientFactory.DigestAuthContext authContext =
                new DatabaseClientFactory.DigestAuthContext("admin", "admin");
        return DatabaseClientFactory.newClient("oscar", 8200, authContext);
    }
}
