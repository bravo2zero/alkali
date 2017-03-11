package com.bravo2zero.alkali.change;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * Creates a basic copy of a table (no data) in the target schema using
 * provided reference table.
 *
 * @author bravo2zero
 */
public class BasicCopyTable extends AbstractTransactionalChange {

	public static final Logger LOGGER = LoggerFactory.getLogger(BasicCopyTable.class);

	private String referenceSchemaName;
	private String referenceTableName;

	@Override
	public void update(JdbcTemplate jdbcTemplate) throws Exception {

		JdbcTemplate reference = getJdbcTemplate(referenceSchemaName);
		final StringBuilder stmt = new StringBuilder("CREATE TABLE `" + referenceTableName + "` (");

		reference.query("SELECT * FROM " + referenceTableName, new ResultSetExtractor<ResultSetMetaData>() {
			@Override
			public ResultSetMetaData extractData(ResultSet rs) throws SQLException, DataAccessException {
				ResultSetMetaData meta = rs.getMetaData();
				for (int i = 1; i <= meta.getColumnCount(); i++) {
					if (i > 1) {
						stmt.append(", ");
					}
					stmt.append(meta.getColumnName(i))
							.append(" ")
							.append(meta.getColumnTypeName(i))
							.append("(").append(meta.getPrecision(i)).append(")");
					if (meta.isNullable(i) == ResultSetMetaData.columnNoNulls) {
						stmt.append(" NOT NULL");
					}
				}
				return meta;
			}
		});
		stmt.append(") DEFAULT CHARSET=utf8;");
		jdbcTemplate.execute(stmt.toString());
		LOGGER.info("from {}.{}: {}", referenceSchemaName, referenceTableName, stmt.toString());
	}

	@Override
	public void rollback(JdbcTemplate jdbcTemplate) throws Exception {
		jdbcTemplate.execute("DROP TABLE IF EXISTS " + referenceTableName);

	}

	public void setReferenceSchemaName(String referenceSchemaName) {
		this.referenceSchemaName = referenceSchemaName;
	}

	public void setReferenceTableName(String referenceTableName) {
		this.referenceTableName = referenceTableName;
	}
}
