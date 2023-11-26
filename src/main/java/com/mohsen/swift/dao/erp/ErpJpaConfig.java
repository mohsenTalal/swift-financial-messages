package com.mohsen.swift.dao.erp;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Objects;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = "com.Mohsen.swift.dao.erp.repository",
    entityManagerFactoryRef = "erpEntityManagerFactory",
    transactionManagerRef = "erpTransactionManager"
)
public class ErpJpaConfig {

  @Bean
  public LocalContainerEntityManagerFactoryBean erpEntityManagerFactory(
      @Qualifier("erpDataSource") DataSource dataSource,
      EntityManagerFactoryBuilder builder) {
    return builder
        .dataSource(dataSource)
        .packages("com.Mohsen.swift.dao.erp.entity")
        .build();
  }

  @Bean
  public PlatformTransactionManager erpTransactionManager(
      @Qualifier("erpEntityManagerFactory") LocalContainerEntityManagerFactoryBean erpEntityManagerFactory) {
    return new JpaTransactionManager(Objects.requireNonNull(erpEntityManagerFactory.getObject()));
  }
}
