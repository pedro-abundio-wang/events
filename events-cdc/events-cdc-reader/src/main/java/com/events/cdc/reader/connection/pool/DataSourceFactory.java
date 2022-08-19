package com.events.cdc.reader.connection.pool;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.util.Properties;

public class DataSourceFactory {

  public static DataSource createDataSource(
      String jdbcUrl,
      String driverClassName,
      String username,
      String password,
      ConnectionPoolProperties connectionPoolProperties) {

    Properties config = new Properties();

    config.setProperty("jdbcUrl", jdbcUrl);
    config.setProperty("driverClassName", driverClassName);
    config.setProperty("username", username);
    config.setProperty("password", password);
    config.setProperty("initializationFailTimeout", String.valueOf(Long.MAX_VALUE));
    config.setProperty("connectionTestQuery", "select 1");

    connectionPoolProperties.getProperties().forEach(config::setProperty);

    return new HikariDataSource(new HikariConfig(config));
  }
}
