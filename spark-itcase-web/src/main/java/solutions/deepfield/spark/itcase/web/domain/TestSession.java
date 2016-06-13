package solutions.deepfield.spark.itcase.web.domain;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.messaging.simp.SimpMessagingTemplate;

import solutions.deepfield.spark.itcase.exceptions.SparkITCaseException;

public class TestSession {

	private SimpMessagingTemplate template;
	
	private String id;
	
	private String user;
	
	private ConcurrentHashMap<String, TestLogStore> logStores = new ConcurrentHashMap<>();

	public TestSession(String id, String user, SimpMessagingTemplate template) {
		super();
		this.id = id;
		this.user = user;
		this.template = template;
	}
	
	public void startTest(String className, String testName) {
		TestLogStore existing = logStores.get(className + "-" + testName);
		if (existing == null) {
			TestLogStore store = new TestLogStore();
			store.setClassName(className);
			store.setTestName(testName);
			store.setUser(user);
			store.setTestSessionId(id);
			store.setTemplate(template);
		} else {
			throw new SparkITCaseException("Test [" + className + "-" + testName + "] has already been started for the test session [" + testName + "]");
		}
	}
	
	
	
}
