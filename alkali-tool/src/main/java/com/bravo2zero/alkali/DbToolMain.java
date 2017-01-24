package com.bravo2zero.alkali;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author bravo2zero
 */
public class DbToolMain {
	public static final Logger LOGGER = LoggerFactory.getLogger(DbToolMain.class);

	public static void main(String[] args) throws Exception {
		Alkali alkali = new Alkali();
		try {

			alkali.initialize(args);
			alkali.execute();
		} catch (Exception e) {
			LOGGER.error("Exception during db tool execution", e);
			alkali.printUsageInfo();
		}
	}

}
