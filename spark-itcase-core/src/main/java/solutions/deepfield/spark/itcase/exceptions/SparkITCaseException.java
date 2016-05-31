package solutions.deepfield.spark.itcase.exceptions;

public class SparkITCaseException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public SparkITCaseException() {
		super();
	}


	public SparkITCaseException(String message, Throwable cause) {
		super(message, cause);
	}

	public SparkITCaseException(String message) {
		super(message);
	}
	
}
