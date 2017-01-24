package com.bravo2zero.alkali;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import com.bravo2zero.alkali.cli.CommandLineParameter;
import com.bravo2zero.alkali.exceptions.DataSourceNotRegisteredException;
import com.bravo2zero.alkali.exceptions.UnknownEnvironmentException;

/**
 * @author bravo2zero
 */
public class DataSourceManager {

	public static final Logger LOGGER = LoggerFactory.getLogger(DataSourceManager.class);

	private String environment;
	private Map<String, BasicDataSource> dataSourceRegistry = new HashMap<>();

	public void initialize(CommandLine commandLine) throws UnknownEnvironmentException, IOException {
		environment = commandLine.getOptionValue(CommandLineParameter.ENVIRONMENT.getShortName());
		InputStream inputStream = DataSourceManager.class.getClassLoader().getResourceAsStream(String.format("%s.properties", environment));
		if (inputStream != null) {
			Properties properties = new Properties();
			properties.load(inputStream);
			Map<String, DataSourceConfig> configMap = new HashMap<>();
			for (Object propertyKey : properties.keySet()) {
				String prefix = ((String) propertyKey).split("\\.")[0];
				DataSourceConfig dbConfig;
				if (configMap.containsKey(prefix)) {
					dbConfig = configMap.get(prefix);
				} else {
					dbConfig = new DataSourceConfig();
					configMap.put(prefix, dbConfig);
				}
				dbConfig.applyProperty((String) propertyKey, properties.getProperty((String) propertyKey));
			}
			createDataSources(configMap, commandLine);
		} else {
			throw new UnknownEnvironmentException("No db properties found for environment:" + environment);
		}
	}

	private void createDataSources(Map<String, DataSourceConfig> configs, CommandLine commandLine) {
		for (String schemaName : configs.keySet()) {
			DataSourceConfig config = configs.get(schemaName);
			initJdbcTemplate(schemaName, config, commandLine);
		}
	}

	private void initJdbcTemplate(String schemaName, DataSourceConfig config, CommandLine commandLine) {
		try {
			BasicDataSource dataSource = new BasicDataSource();
			dataSource.setDriverClassName(config.getDriver());
			dataSource.setUrl(config.getUrl());
			dataSource.setUsername(config.getUser());
			dataSource.setPassword(StringUtils.isEmpty(config.getPassword()) ? commandLine.getOptionValue(CommandLineParameter.PASSWORD.getShortName()) : config.getPassword());
			dataSourceRegistry.put(schemaName, dataSource);
			LOGGER.debug("Registered DataSource:{}", config);
		} catch (Exception e) {
			LOGGER.warn("Exception initializing data source: " + config.toString(), e);
		}
	}

	public BasicDataSource getRegisteredDataSource(String schemaName) throws DataSourceNotRegisteredException {
		if (dataSourceRegistry.containsKey(schemaName)) {
			return dataSourceRegistry.get(schemaName);
		}
		throw new DataSourceNotRegisteredException("No data source registered for name:" + schemaName + ", environment:" + environment);
	}

	class DataSourceConfig {
		private String url;
		private String driver;
		private String user;
		private String password;

		public void applyProperty(String key, String value) {
			String propertyName = key.split("\\.")[1];
			Field field = ReflectionUtils.findField(DataSourceConfig.class, propertyName);
			if (field != null) {
				ReflectionUtils.makeAccessible(field);
				ReflectionUtils.setField(field, this, value);
			}
		}

		public String getUrl() {
			return url;
		}

		public String getDriver() {
			return driver;
		}


		public String getUser() {
			return user;
		}

		public String getPassword() {
			return password;
		}

		@Override
		public String toString() {
			return String.format("{url:\"%s\", driver:\"%s\", user:\"%s\", password:\"%s\"}",
					url, driver, user, StringUtils.hasText(password) ? "***" : "");
		}
	}

}
