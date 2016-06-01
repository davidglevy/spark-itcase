package solutions.deepfield.spark.fp;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import solutions.deepfield.spark.fp.ExampleProject;

public class ExampleProjectITCase {

	private static final Logger logger = LoggerFactory.getLogger(ExampleProjectITCase.class);
	
	@Test
	public void testMethod() {
		logger.info("We have been tested");
		ExampleProject project = new ExampleProject(ClusterTestWrapper.CONTEXT);
		project.run();
	}
	
}
