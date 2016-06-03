package solutions.deepfield.spark.itcase.annotations;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

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
			
			List<Method> befores = new ArrayList<Method>();
			Class examineTarget = testClass;
			
			// Look for before methods.
			while (examineTarget != null) {
				for (Method m : testClass.getMethods()) {
					if (m.getAnnotation(SparkBefore.class) == null) {
						continue;
					}
					if (m.getParameterTypes().length > 0) {
						throw new Exception("Found @SparkBefore annotation on method with arguments");
					}
					befores.add(m);
				}
				examineTarget = examineTarget.getSuperclass();
			}

			// Look for after methods.
			List<Method> afters = new ArrayList<Method>();
			examineTarget = testClass;
			while (examineTarget != null) {
				for (Method m : testClass.getMethods()) {
					if (m.getAnnotation(SparkAfter.class) == null) {
						continue;
					}
					if (m.getParameterTypes().length > 0) {
						throw new Exception("Found @SparkAfter annotation on method with arguments");
					}
					afters.add(m);
				}
				examineTarget = examineTarget.getSuperclass();
			}
			
			
			Object instance = testClass.newInstance();

			// Invoke befores
			for (Method before : befores) {
				logger.info("Invoking before method [" + before.getDeclaringClass() + "#" + before.getName() + "]");
				before.invoke(instance);
				logger.info("Finished before method [" + before.getDeclaringClass() + "#" + before.getName() + "]");
			}
			
			// Execute the test method.
			method.invoke(instance);
			
			// Invoke afters
			for (Method after : afters) {
				logger.info("Invoking after method [" + after.getDeclaringClass() + "#" + after.getName() + "]");
				after.invoke(instance);
				logger.info("Finished after method [" + after.getDeclaringClass() + "#" + after.getName() + "]");
			}

		} catch (Exception e) {
			logger.error("Error processing command: " + e.getMessage(), e);
			System.exit(10);
		}
	}
}
