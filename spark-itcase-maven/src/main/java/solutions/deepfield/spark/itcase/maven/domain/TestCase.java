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
	
	private String systemOut;
	
	private String systemErr;
	
	public TestCase(String name, String className) {
		super();
		this.name = name;
		this.className = className;
	}

	public void markComplete() {
		endTime = System.currentTimeMillis();
	}

	public String render() {
		if (endTime == -1) {
			throw new IllegalStateException("MarkComplete has not yet been called");
		}
		
		
		String duration = Double.toString(((double)(endTime - startTime)) / 1000.0d); 
		
		String partial = String.format("<testcase name=\"%s\" classname=\"%s\" time=\"%s\"", name, className, duration);
		StringBuilder builder = new StringBuilder(partial);
		
		if (error == null) {
			builder.append("/>");
			return builder.toString();
		}
		// Add in error and system out/err
		builder.append(">\n");
		builder.append(error.render());
		if (StringUtils.isNotBlank(systemOut)) {
			builder.append("    <system-out><![CDATA[");
			builder.append(systemOut);
			builder.append("]]></system-out>");
		}
		if (StringUtils.isNotBlank(systemErr)) {
			builder.append("    <system-out><![CDATA[");
			builder.append(systemErr);
			builder.append("]]></system-out>");
		}
		
		
		return "";
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
	
	
}
