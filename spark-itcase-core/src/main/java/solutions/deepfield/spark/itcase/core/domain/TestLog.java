package solutions.deepfield.spark.itcase.core.domain;

/**
 * Fragment output of test results which can be sent back to the client.
 * 
 * @author david
 *
 */
public class TestLog {

	private String testClass;
	
	private String testName;
	
	private boolean complete;
	
	private boolean success;

	public String getTestClass() {
		return testClass;
	}

	public void setTestClass(String testClass) {
		this.testClass = testClass;
	}

	public String getTestName() {
		return testName;
	}

	public void setTestName(String testName) {
		this.testName = testName;
	}

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
	
	
}
