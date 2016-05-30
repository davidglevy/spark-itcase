package solutions.deepfield.spark.itcase.web.controller;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.ExceptionHandler;

public class BaseController {

	@ExceptionHandler({RuntimeException.class})
	  public void runtimeError(HttpServletResponse response, Exception e) {
			
		
	  }
	
}
