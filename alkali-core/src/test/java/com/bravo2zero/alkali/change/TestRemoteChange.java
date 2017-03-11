package com.bravo2zero.alkali.change;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;

/**
 * @author bravo2zero
 */
public class TestRemoteChange extends AbstractTransactionalChange {

	public static final Logger LOGGER = LoggerFactory.getLogger(TestRemoteChange.class);

	private String remoteSchemaName;

	@Override
	public void update(JdbcTemplate jdbcTemplate) throws Exception {

		// default template for 'home' db is covered with TX
		jdbcTemplate.execute("select 'default_db_1';");

		// this schema template is not transactional!
		JdbcTemplate remoteDb = getJdbcTemplate(remoteSchemaName);

		remoteDb.query("SELECT * FROM TEST_TABLE", new ResultSetExtractor<ResultSetMetaData>() {
			@Override
			public ResultSetMetaData extractData(ResultSet rs) throws SQLException, DataAccessException {
				ResultSetMetaData meta = rs.getMetaData();
				for(int i = 1 ; i <= meta.getColumnCount(); i++){
					LOGGER.info("{} {} {}",
							meta.getColumnName(i),
							meta.getColumnTypeName(i),
							meta.isAutoIncrement(i));
				}
				return meta;
			}
		});


		for (Map<String, Object> record : remoteDb.queryForList("SELECT * FROM TEST_TABLE")) {
			// insert to 'home' schema
			jdbcTemplate.update("INSERT INTO TEST_TABLE VALUES (?,?)", record.get("ID"),
					record.get("NAME"));
		}
	}

	@Override
	public void rollback(JdbcTemplate jdbcTemplate) throws Exception {
		jdbcTemplate.execute("DELETE FROM TEST_TABLE WHERE NAME = 'ONE'");
	}

	public void setRemoteSchemaName(String remoteSchemaName) {
		this.remoteSchemaName = remoteSchemaName;
	}
}
