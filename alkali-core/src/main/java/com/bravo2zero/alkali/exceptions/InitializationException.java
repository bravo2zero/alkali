package com.bravo2zero.alkali.exceptions;

/**
 * @author bravo2zero
 */
public class InitializationException extends Exception {

	public InitializationException(Throwable cause) {
		super(cause);
	}

	public InitializationException(String message) {
		super(message);
	}

	public InitializationException(String message, Throwable cause) {
		super(message, cause);
	}
}
