package levy.david.spark.itcase.web.controller;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.HttpServletBean;
import org.springframework.web.servlet.ModelAndView;

/*
 * author: Crunchify.com
 * 
 */

@Controller
public class StatusController {

	private static final Logger logger = LoggerFactory.getLogger(StatusController.class);
	
	@PostConstruct
	public void postConstruct() {
		logger.info("Starting up");
	}
	
	// TODO Put in a proper status message response based on build version.
	@RequestMapping("/status")
	public void status(HttpServletResponse response) throws IOException, ServletException {
		response.setContentType("text/plain");
		String output = "<br><div style='text-align:center;'>"
				+ "<h3>********** Hello World, Spring MVC Tutorial</h3>This message is coming from CrunchifyHelloWorld.java **********</div><br><br>";
		byte[] outputBytes = output.getBytes("UTF-8");
		
		response.getOutputStream().write(outputBytes);
	}
}