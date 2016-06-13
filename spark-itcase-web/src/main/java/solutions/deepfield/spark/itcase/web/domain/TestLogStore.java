package solutions.deepfield.spark.itcase.web.domain;

import org.springframework.messaging.simp.SimpMessagingTemplate;

import solutions.deepfield.spark.itcase.core.TestLog;

public class TestLogStore {

	private SimpMessagingTemplate template;

	private String testSessionId;

	private String className;

	private String testName;

	private String user;

	public void logMessage(boolean complete, String message) {
		TestLog log = new TestLog();
		log.setComplete(complete);
		log.setMessage(message);
		template.convertAndSendToUser(user, "/test/" + testSessionId + "/" + className + "/" + testName, message);
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getTestSessionId() {
		return testSessionId;
	}

	public void setTestSessionId(String testSessionId) {
		this.testSessionId = testSessionId;
	}

	public SimpMessagingTemplate getTemplate() {
		return template;
	}

	public void setTemplate(SimpMessagingTemplate template) {
		this.template = template;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getTestName() {
		return testName;
	}

	public void setTestName(String testName) {
		this.testName = testName;
	}

}
