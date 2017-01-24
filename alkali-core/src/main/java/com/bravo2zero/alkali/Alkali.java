package com.bravo2zero.alkali;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.resource.FileSystemResourceAccessor;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.log4j.xml.DOMConfigurator;
import com.bravo2zero.alkali.cli.CommandLineParameter;
import com.bravo2zero.alkali.exceptions.InitializationException;
import com.bravo2zero.alkali.exceptions.ProcessingException;
import com.bravo2zero.alkali.utils.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.util.StringUtils;

import java.io.StringWriter;

/**
 * @author bravo2zero
 */
public class Alkali {
	public static final Logger LOGGER = LoggerFactory.getLogger(Alkali.class);

	private CommandLine commandLine;
	private Options options;
	private ApplicationContext context;

	public Alkali() {
		initializeOptions();
	}

	public void printUsageInfo() {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("java -jar dbtool.jar", options, true);
	}

	public void initialize(String[] args) throws InitializationException {
		Stopwatch timer = new Stopwatch();
		try {
			DOMConfigurator.configure(Alkali.class.getClassLoader().getResource("log4j.xml"));
			initializeContext();
			parseArgs(args);
		} catch (Exception e) {
			throw new InitializationException(e);
		}
		LOGGER.info("Initialize finished in {} secs.", timer.secondsElapsed());
	}

	public void execute() throws ProcessingException {
		Stopwatch timer = new Stopwatch();
		checkNotNull("Tool was not properly initialized", commandLine, options, context);
		try {
			process();
		} catch (Exception e) {
			throw new ProcessingException(e);
		}
		LOGGER.info("Processing finished in {} secs.", timer.secondsElapsed());
	}

	private void process() throws Exception {
		DataSourceManager dbManager = (DataSourceManager) context.getBean("dataSourceManager");
		dbManager.initialize(commandLine);
		BasicDataSource dataSource = dbManager.getRegisteredDataSource(
				commandLine.getOptionValue(CommandLineParameter.DATABASE.getShortName()));

		Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(
				new JdbcConnection(dataSource.getConnection()));

		Liquibase liquibase = new liquibase.Liquibase(
				commandLine.getOptionValue(CommandLineParameter.CHANGE_SET.getShortName()),
				new CompositeResourceAccessor(new ClassLoaderResourceAccessor(), new FileSystemResourceAccessor()),
				database);

		// magic
		if (commandLine.hasOption(CommandLineParameter.COMMAND_STATUS.getShortName())) {
			StringWriter writer = new StringWriter();
			liquibase.reportStatus(true, new Contexts(), writer);
			LOGGER.info("Status report for [{}]: {}", liquibase.getChangeLogFile(), writer.toString());
			return;
		}
		if (commandLine.hasOption(CommandLineParameter.COMMAND_UPDATE.getShortName())) {
			liquibase.update(new Contexts());
			return;
		}
		if (commandLine.hasOption(CommandLineParameter.COMMAND_ROLLBACK.getShortName())) {
			try {
				liquibase.rollback(
						Integer.valueOf(commandLine.getOptionValue(CommandLineParameter.COMMAND_ROLLBACK.getShortName())),
						new Contexts(),
						new LabelExpression()
				);
			} catch (NumberFormatException nfe) {
				// not a number, ought to be a tag name
				liquibase.rollback(
						commandLine.getOptionValue(CommandLineParameter.COMMAND_ROLLBACK.getShortName()),
						new Contexts(),
						new LabelExpression()
				);
			}
			return;
		}
		throw new IllegalArgumentException("Command not supported (status, update, rollback)");
	}

	private void initializeContext() {
		context = new GenericXmlApplicationContext("classpath:application-context.xml");
	}

	private void initializeOptions() {
		this.options = new Options();
		for (CommandLineParameter param : CommandLineParameter.values()) {

			Option.Builder builder = Option.builder(param.getShortName())
					.required(param.isRequired())
					.desc(param.getDescription());
			if (param.isHasArgument()) {
				builder.hasArg(true);
				builder.argName(param.getLongName());
			}
			if (StringUtils.hasText(param.getLongName())) {
				builder.longOpt(param.getLongName());
			}
			options.addOption(builder.build());
		}
	}

	private void parseArgs(String[] args) throws ParseException {
		commandLine = new DefaultParser().parse(options, args);
	}

	private void checkNotNull(String errorMessage, Object... params) throws ProcessingException {
		for (Object param : params) {
			if (param == null) {
				throw new ProcessingException(errorMessage);
			}
		}
	}

}
