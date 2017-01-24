package com.bravo2zero.alkali.change;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

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
