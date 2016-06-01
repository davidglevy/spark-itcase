package solutions.deepfield.spark.itcase.web.exception;

import java.util.List;

import solutions.deepfield.spark.itcase.exceptions.SparkITCaseException;

public class ExecutionException extends SparkITCaseException {

	private static final long serialVersionUID = 1L;

	private List<String> logs;

	public List<String> getLogs() {
		return logs;
	}

	public ExecutionException(String message, Throwable cause, List<String> logs) {
		super(message, cause);
		this.logs = logs;
	}

	public ExecutionException(String message, List<String> logs) {
		super(message);
		this.logs = logs;
	}
	
	

}
