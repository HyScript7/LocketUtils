package io.github.hyscript7.locketutils.config;

import javax.sql.DataSource;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatabaseConfiguration {
    LocketutilsConfiguration locketutilsConfiguration;
    public DatabaseConfiguration(LocketutilsConfiguration locketutilsConfiguration) {
        this.locketutilsConfiguration = locketutilsConfiguration;
    }

    @Bean
    DataSource getdataSource() {
        String databaseUri = "jdbc:postgresql://" + locketutilsConfiguration.getDatabase().getHostname() + ":" + locketutilsConfiguration.getDatabase().getPort() + "/" + locketutilsConfiguration.getDatabase().getDatabase();
        DataSourceBuilder<? extends DataSource> dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.driverClassName("org.postgresql.Driver");
        dataSourceBuilder.url(databaseUri);
        dataSourceBuilder.username(locketutilsConfiguration.getDatabase().getUsername());
        dataSourceBuilder.password(locketutilsConfiguration.getDatabase().getPassword());
        return dataSourceBuilder.build();
    }
}
