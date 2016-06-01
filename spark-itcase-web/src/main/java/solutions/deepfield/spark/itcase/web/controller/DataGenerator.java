package solutions.deepfield.spark.itcase.web.controller;

import java.io.IOException;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class DataGenerator {

	private static final Logger logger = LoggerFactory.getLogger(DataGenerator.class);
		
	private String[] randomWords = new String[] {"One", "Two", "Three", "Four", "Panda"};
	
	private long startTime = System.currentTimeMillis() / 1000;
	
	private Random random = new Random();
	
	// TODO Put in a proper status message response based on build version.
	@RequestMapping("/generate")
	public synchronized void status(HttpServletResponse response) throws ServletException, IOException {
		long currentTime = System.currentTimeMillis() / 1000;
		
		int messages = ((int)(currentTime - startTime)) * random.nextInt(5);
		// Reset the start time so we don't get too many messages.
		startTime = currentTime;
		
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < messages; i++) {
			if (i > 0) {
				result.append(" ");
			}
			result.append(randomWords[random.nextInt(randomWords.length)]);
		}
		result.append("\n");
		
		response.setStatus(200);
		response.setContentType("text/plain");
		response.getOutputStream().write(result.toString().getBytes());
		
	}

	
}
