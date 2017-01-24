package com.bravo2zero.alkali.utils;

/**
 * @author bravo2zero
 */
public class Stopwatch {
	private long startTime;

	public Stopwatch() {
		this.startTime = System.currentTimeMillis();
	}

	public long secondsElapsed(){
		return (System.currentTimeMillis() - startTime) / 1000;
	}
}
