package com.bravo2zero.alkali.exceptions;

/**
 * @author bravo2zero
 */
public class ProcessingException extends Exception {

	public ProcessingException(Throwable cause) {
		super(cause);
	}

	public ProcessingException(String message) {
		super(message);
	}

	public ProcessingException(String message, Throwable cause) {
		super(message, cause);
	}
}
