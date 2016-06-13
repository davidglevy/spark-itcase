package solutions.deepfield.spark.itcase.web.domain;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class TestSessionRepository {

	private Map<String,TestSession> sessions = new ConcurrentHashMap<>();
	
	@Autowired
	private SimpMessagingTemplate template;
	
	public String createNewTestSession(String username) {
		String guid = UUID.randomUUID().toString();
		TestSession session = new TestSession(guid, username, template);
		sessions.put(guid, session);
		return guid;
	}

	public TestSession getSession(String id) {
		return sessions.get(id);
	}
	
	
}
