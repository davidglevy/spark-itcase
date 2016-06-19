package solutions.deepfield.spark.itcase.maven.domain;

import java.util.ArrayList;
import java.util.List;

import solutions.deepfield.spark.itcase.exceptions.SparkITCaseException;

public class TestSuite {

	private long startTime = System.currentTimeMillis();
	
	private long endTime;
	
	private List<TestCase> testCases = new ArrayList<>();
	
	public TestSuite() {
		
	}
	
	public String render() {
		endTime = System.currentTimeMillis();
		
		StringBuilder result = new StringBuilder("<testsuite>\n");
		for (TestCase testCase : testCases) {
			result.append(testCase.render());
		}
		
		return result.toString();
	}
	
	public void addTestCase(TestCase input) {
		this.testCases.add(input);
	}
	
	public void markComplete() {
		endTime = System.currentTimeMillis();
	}
	
	protected int getFailures() {
		int count = 0;
		for (TestCase testCase : testCases) {
			if (testCase.hasFailure()) {
				count++;
			}
		}
		return count;
	}
	
	protected int getErrors() {
		int count = 0;
		for (TestCase testCase : testCases) {
			if (testCase.hasError()) {
				count++;
			}
		}
		return count;
	}

	protected int getSkipped() {
		int count = 0;
		for (TestCase testCase : testCases) {
			if (testCase.isSkipped()) {
				count++;
			}
		}
		return count;
	}
	
	
}
