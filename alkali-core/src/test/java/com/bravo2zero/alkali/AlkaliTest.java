package com.bravo2zero.alkali;

import com.bravo2zero.alkali.exceptions.InitializationException;
import com.bravo2zero.alkali.exceptions.ProcessingException;
import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author bravo2zero
 */
public class AlkaliTest {

	private Alkali alkali;

	private BasicDataSource ds1;
	private BasicDataSource ds2;

	@Before
	public void setUp() throws Exception {
		alkali = new Alkali();

		// init 'remote' db;
		ds2 = createDS("jdbc:h2:mem:db2");
		JdbcTemplate ds2Template = new JdbcTemplate(ds2);
		ds2Template.execute("DROP TABLE IF EXISTS TEST_TABLE");
		ds2Template.execute("CREATE TABLE TEST_TABLE(ID INT, NAME VARCHAR(32))");
		ds2Template.execute("INSERT INTO TEST_TABLE VALUES (1, 'ONE')");

		ds1 = createDS("jdbc:h2:mem:db1");
	}

	@Test(expected = InitializationException.class)
	public void testInitialize_nullArgs() throws Exception {
		alkali.initialize(null);
	}

	@Test(expected = InitializationException.class)
	public void testInitialize_emptyArgs() throws Exception {
		alkali.initialize(new String[1]);
	}

	@Test(expected = InitializationException.class)
	public void testInitialize_missingRequiredArgs() throws Exception {
		String[] args = new String[]{"-a value1", "--long value2"};
		alkali.initialize(args);
	}

	@Test
	public void testInitialize() throws Exception {
		// we need options to be initialized in constructor to be able to print out usage info
		assertNotNull(ReflectionTestUtils.getField(alkali, "options"));

		initialize("-d", "dbname", "--env", "env", "-f", "changesets/some-changeset.xml");

		assertNotNull(ReflectionTestUtils.getField(alkali, "context"));
		assertNotNull(ReflectionTestUtils.getField(alkali, "commandLine"));
	}

	@Test(expected = ProcessingException.class)
	public void testExecute_commandNotSupported() throws Exception {
		initialize("-d", "db1", "--env", "local", "-f", "changesets/simple-changeset.xml");
		alkali.execute();
	}

	@Test
	public void testExecuteStatus() throws Exception {
		initialize("-d", "db1", "--env", "local", "-f", "changesets/simple-changeset.xml", "-status");
		alkali.execute();
	}

	@Test
	public void testUpdateWithRemote() throws Exception {
		initialize("-d", "db1", "--env", "local", "-f", "changesets/simple-changeset.xml", "-update");
		alkali.execute();
		assertEquals(1, JdbcTestUtils.countRowsInTable(new JdbcTemplate(ds1),"TEST_TABLE"));
	}

	// --- fixtures & tools

	private void initialize(String... args) throws InitializationException {
		alkali.initialize(args);
	}

	private BasicDataSource createDS(String url) {
		BasicDataSource ds = new BasicDataSource();
		ds.setUrl(url);
		ds.setDriverClassName("org.h2.Driver");
		return ds;
	}

}