package solutions.deepfield.spark.itcase.maven.domain;

import org.apache.commons.lang.StringUtils;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormat;

import solutions.deepfield.spark.itcase.exceptions.SparkITCaseException;

public class TestCase {
	private String name;
	
	private String className;
	
	private long startTime = System.currentTimeMillis();
	
	private long endTime = -1;
	
	private Error error;
	
	private Error failure;
	
	private String systemOut;
	
	private String systemErr;
	
	private boolean skipped;
	
	public TestCase(String name, String className) {
		super();
		this.name = name;
		this.className = className;
	}

	public void markComplete() {
		endTime = System.currentTimeMillis();
	}

	public String render() {
		String duration = "";
		
		if (skipped) {
			duration = "0.0";
		} else if (endTime > -1) {
			duration = Double.toString(((double)(endTime - startTime)) / 1000.0d); 
		} else {
			throw new IllegalStateException("MarkComplete has not yet been called");
		}
		
		String partial = String.format("<testcase name=\"%s\" classname=\"%s\" time=\"%s\"", name, className, duration);
		StringBuilder builder = new StringBuilder(partial);
		
		if (error == null && failure == null) {
			builder.append("/>");
			return builder.toString();
		}
		// Add in error and system out/err
		builder.append(">\n");

		if (failure != null) {
			builder.append(failure.render());
		}
		
		if (error != null) {
			builder.append(error.render());
		}
		
		if (StringUtils.isNotBlank(systemOut)) {
			builder.append("    <system-out><![CDATA[");
			builder.append(systemOut);
			builder.append("]]></system-out>\n");
		}
		if (StringUtils.isNotBlank(systemErr)) {
			builder.append("    <system-err><![CDATA[");
			builder.append(systemErr);
			builder.append("]]></system-err>\n");
		}
		
		builder.append("</testcase>\n");
		
		return builder.toString();
	}

	public void setError(Error error) {
		this.error = error;
	}

	public void setSystemOut(String systemOut) {
		this.systemOut = systemOut;
	}

	public void setSystemErr(String systemErr) {
		this.systemErr = systemErr;
	}

	public void setFailure(Error failure) {
		failure.setElement("failure");
		this.failure = failure;
		
	}

	public boolean hasError() {
		return error != null;
	}
	
	public boolean hasFailure() {
		return failure != null;
	}
	
	public void setSkipped(boolean skipped) {
		this.skipped = skipped;
	}
	
	public boolean isSkipped() {
		return this.skipped;
	}
}
