package com.bravo2zero.alkali.change;

import com.bravo2zero.alkali.ApplicationContextHolder;
import com.bravo2zero.alkali.DataSourceManager;
import com.bravo2zero.alkali.exceptions.DataSourceNotRegisteredException;
import liquibase.change.custom.CustomTaskChange;
import liquibase.change.custom.CustomTaskRollback;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.RollbackImpossibleException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;

/**
 * @author bravo2zero
 */
public abstract class AbstractTransactionalChange implements CustomTaskChange, CustomTaskRollback {

	protected Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	protected StringBuilder confirmationMessage = new StringBuilder();

	/**
	 * Execute custom update operations
	 *
	 * @param jdbcTemplate default schema template (all changes on this template will be done inside a TX)
	 * @throws Exception
	 */
	abstract public void update(JdbcTemplate jdbcTemplate) throws Exception;

	/**
	 * Execute custom rollback operations
	 *
	 * @param jdbcTemplate default schema template (all changes on this template will be done inside a TX)
	 * @throws Exception
	 */
	abstract public void rollback(JdbcTemplate jdbcTemplate) throws Exception;

	@Override
	public void execute(Database database) throws CustomChangeException {
		try {
			DataSource defaultDataSource = getDataSource(database);
			final JdbcTemplate defaultTemplate = new JdbcTemplate(defaultDataSource);
			DataSourceTransactionManager txManager = new DataSourceTransactionManager(defaultDataSource);
			TransactionTemplate txTemplate = new TransactionTemplate(txManager);
			txTemplate.execute(new TransactionCallbackWithoutResult() {
				@Override
				protected void doInTransactionWithoutResult(TransactionStatus status) {
					try {
						update(defaultTemplate);
					} catch (Exception e) {
						LOGGER.error("Exception executing update", e);
						status.setRollbackOnly();
					}
				}
			});
		} catch (Exception e) {
			throw new CustomChangeException(e);
		}
	}

	@Override
	public void rollback(Database database) throws CustomChangeException, RollbackImpossibleException {
		try {
			DataSource defaultDataSource = getDataSource(database);
			final JdbcTemplate defaultTemplate = new JdbcTemplate(defaultDataSource);
			DataSourceTransactionManager txManager = new DataSourceTransactionManager(defaultDataSource);
			TransactionTemplate txTemplate = new TransactionTemplate(txManager);
			txTemplate.execute(new TransactionCallbackWithoutResult() {
				@Override
				protected void doInTransactionWithoutResult(TransactionStatus status) {
					try {
						rollback(defaultTemplate);
					} catch (Exception e) {
						LOGGER.error("Exception executing rollback", e);
						status.setRollbackOnly();
					}
				}
			});
		} catch (Exception e) {
			throw new CustomChangeException(e);
		}
	}

	@Override
	public void setUp() throws SetupException {
		// no-op
	}

	@Override
	public ValidationErrors validate(Database database) {
		return null;
	}

	@Override
	public String getConfirmationMessage() {
		return confirmationMessage.toString();
	}

	@Override
	public void setFileOpener(ResourceAccessor resourceAccessor) {
		// no-op
	}

	private DataSource getDataSource(Database liquibaseDb) throws DataSourceNotRegisteredException{
		try{
			return getDataSource(liquibaseDb.getDefaultSchemaName());
		}catch (DataSourceNotRegisteredException e){
			return getDataSource(liquibaseDb.getDefaultCatalogName().toLowerCase());
		}
	}

	private DataSource getDataSource(String schemaName) throws DataSourceNotRegisteredException {
		return (ApplicationContextHolder.getBeanByName("dataSourceManager", DataSourceManager.class)).getRegisteredDataSource(schemaName);
	}

	public JdbcTemplate getJdbcTemplate(String schemaName) throws DataSourceNotRegisteredException {
		return new JdbcTemplate(getDataSource(schemaName));
	}
}
