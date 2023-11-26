package com.mohsen.swift.dao.integration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
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
    basePackages = "com.Mohsen.swift.dao.integration.repository",
    entityManagerFactoryRef = "intEntityManagerFactory",
    transactionManagerRef = "intTransactionManager"
)
public class IntJpaConfig {

  @Bean
  @Primary
  public LocalContainerEntityManagerFactoryBean intEntityManagerFactory(
      @Qualifier("intDataSource") DataSource dataSource,
      EntityManagerFactoryBuilder builder) {
    return builder
        .dataSource(dataSource)
        .packages("com.Mohsen.swift.dao.integration.entity")
        .build();
  }

  @Bean
  @Primary
  public PlatformTransactionManager intTransactionManager(
      @Qualifier("intEntityManagerFactory") LocalContainerEntityManagerFactoryBean intEntityManagerFactory) {
    return new JpaTransactionManager(Objects.requireNonNull(intEntityManagerFactory.getObject()));
  }
}
