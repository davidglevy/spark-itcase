package solutions.deepfield.spark.itcase.annotations;

import java.lang.reflect.Method;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SparkSubmitTestWrapper {

	private static final Logger logger = LoggerFactory.getLogger(SparkSubmitTestWrapper.class);

	public static void main(String[] args) {
		try {

			// Examine arguments; there should be 1 being the test class.
			if (args.length < 2) {
				throw new Exception("Did not specify class to run or test method");
			}

			String classToRun = args[0];
			logger.info("Test class is [" + classToRun + "]");

			// Load the class to test.
			Class testClass = SparkSubmitTestWrapper.class.getClassLoader().loadClass(classToRun);
			logger.info("Loaded test class");
			
			// Get the test method name from second argument.
			String methodToTest = args[1];
			Method method = testClass.getMethod(methodToTest);
			if (method == null) {
				throw new Exception("Unable to find test method [" + method + "] with zero arguments");
			}
			
			Object instance = testClass.newInstance();
			
			// Look for before methods.

			// Look for after methods.

			// Execute the test method.
			method.invoke(instance);
		} catch (Exception e) {
			logger.error("Error processing command: " + e.getMessage(), e);
			System.exit(10);
		}
	}
}
