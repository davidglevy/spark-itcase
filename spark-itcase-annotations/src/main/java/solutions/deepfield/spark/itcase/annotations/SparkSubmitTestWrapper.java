package solutions.deepfield.spark.itcase.annotations;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SparkSubmitTestWrapper {

	private static final Logger logger = LoggerFactory.getLogger(SparkSubmitTestWrapper.class);
	
	public static void main(String[] args) {
		// Examine arguments; there should be 1 being the test class.
		logger.info("Hello world [" + StringUtils.join(args, ",") + "]");
		
		// Load the class to test.
		
		// Look for SparkTest annotations.
		
		// Look for before methods.
		
		// Look for after methods.
		
		// Loop through each test, executing all befores, the test then all afters.
		
	}
}
