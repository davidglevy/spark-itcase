package solutions.deepfield.spark.itcase.maven.domain;

import solutions.deepfield.spark.itcase.exceptions.SparkITCaseException;

public class TestSuite {

	private long startTime = System.currentTimeMillis();
	
	private long endTime;
	
	public TestSuite() {
		
	}
	
	public void markComplete() {
		endTime = System.currentTimeMillis();
	}
	
	protected int getFailures() {
		throw new SparkITCaseException("Not Yet Implemented"); 
	}
	
	protected int getErrors() {
		throw new SparkITCaseException("Not Yet Implemented"); 
	}

	protected int getSkipped() {
		throw new SparkITCaseException("Not Yet Implemented");
	}
	
	
}
