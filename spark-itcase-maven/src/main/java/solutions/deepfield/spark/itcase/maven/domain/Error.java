package solutions.deepfield.spark.itcase.maven.domain;

import org.apache.commons.lang.exception.ExceptionUtils;

public class Error {

	private String message;
	
	private String type;
	
	private String body;

	public Error(Throwable t) {
		message = t.getMessage();
		type = t.getClass().getCanonicalName();
		body = ExceptionUtils.getStackTrace(t);
	}
	
	public Error(String message, String type, String body) {
		super();
		this.message = message;
		this.type = type;
		this.body = body;
	}

	public String render() {
		StringBuilder b = new StringBuilder("<error message=\"");
		b.append(message);
		b.append("\" type=\"");
		b.append(type);
		b.append("\"><![CDATA[");
		b.append(body);
		b.append("]]></error>");
		return b.toString();
	}
	
	
}
