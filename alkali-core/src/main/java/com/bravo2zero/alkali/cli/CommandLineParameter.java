package com.bravo2zero.alkali.cli;

/**
 * @author bravo2zero
 */
public enum CommandLineParameter {
	PASSWORD("p", "password", "optional password to use when not specified in environment properties config", true, false),
	CHANGE_SET("f", "changeset", "path to changeset file", true, true),
	ENVIRONMENT("e", "env", "target environment name", true, true),
	DATABASE("d", "database", "target database", true, true),

	COMMAND_STATUS("status", "", "analogue to calling 'liquibase status --verbose'", false, false),
	COMMAND_UPDATE("update","","execute changeset update on target database",false,false),
	COMMAND_ROLLBACK("rollback","","execute rollback to tag or changes count (rollbackCount X)",true,false);

	String shortName;
	String longName;
	String description;
	boolean hasArgument;
	boolean required;

	private CommandLineParameter(String shortName, String longName, String description, boolean hasArgument, boolean required) {
		this.shortName = shortName;
		this.longName = longName;
		this.description = description;
		this.hasArgument = hasArgument;
		this.required = required;
	}

	public String getShortName() {
		return shortName;
	}

	public String getLongName() {
		return longName;
	}

	public String getDescription() {
		return description;
	}

	public boolean isHasArgument() {
		return hasArgument;
	}

	public boolean isRequired() {
		return required;
	}
}
