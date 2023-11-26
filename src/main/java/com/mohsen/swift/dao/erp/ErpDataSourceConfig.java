package com.mohsen.swift.dao.erp;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class ErpDataSourceConfig {

  @Bean
  @ConfigurationProperties("spring.datasource.erp-db")
  public DataSourceProperties erpDataSourceProperties() {
    return new DataSourceProperties();
  }

  @Bean
  public DataSource erpDataSource() {
    return erpDataSourceProperties()
        .initializeDataSourceBuilder()
        .build();
  }

  @Bean
  public JdbcTemplate erpJdbcTemplate(@Qualifier("erpDataSource") DataSource dataSource) {
    return new JdbcTemplate(dataSource);
  }

  @Bean
  public NamedParameterJdbcTemplate erpNamedJdbcTemplate(@Qualifier("erpDataSource") DataSource dataSource) {
    return new NamedParameterJdbcTemplate(dataSource);
  }
}
