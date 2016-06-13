package solutions.deepfield.spark.itcase.core;

public class TestLog {
	
	private boolean complete;

	private boolean success;

	private boolean failureMessage;
	
	private boolean errorMessage;
	
	private String message;

	public boolean isComplete() {
		return complete;
	}

	public void setComplete(boolean complete) {
		this.complete = complete;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public boolean isFailureMessage() {
		return failureMessage;
	}

	public void setFailureMessage(boolean failureMessage) {
		this.failureMessage = failureMessage;
	}

	public boolean isErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(boolean errorMessage) {
		this.errorMessage = errorMessage;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	
	
}
