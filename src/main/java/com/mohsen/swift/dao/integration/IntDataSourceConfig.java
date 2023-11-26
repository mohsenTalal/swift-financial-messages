package com.mohsen.swift.dao.integration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class IntDataSourceConfig {

  @Bean
  @ConfigurationProperties("spring.datasource.integration-db")
  public DataSourceProperties intDataSourceProperties() {
    return new DataSourceProperties();
  }

  @Bean
  @Primary
  public DataSource intDataSource() {
    return intDataSourceProperties()
        .initializeDataSourceBuilder()
        .build();
  }

  @Bean
  public JdbcTemplate intJdbcTemplate(@Qualifier("intDataSource") DataSource dataSource) {
    return new JdbcTemplate(dataSource);
  }

  @Bean
  public NamedParameterJdbcTemplate intNamedJdbcTemplate(@Qualifier("intDataSource") DataSource dataSource) {
    return new NamedParameterJdbcTemplate(dataSource);
  }
}
