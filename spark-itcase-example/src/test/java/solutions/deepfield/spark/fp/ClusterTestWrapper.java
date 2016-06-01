package solutions.deepfield.spark.fp;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import solutions.deepfield.spark.fp.ExampleProject;
import solutions.deepfield.spark.fp.ExampleProjectWrapper;

public class ClusterTestWrapper {
	private static final Logger logger = LoggerFactory
			.getLogger(ExampleProjectWrapper.class);

	public static JavaSparkContext CONTEXT;
	
	@SuppressWarnings("serial")
	public static void main(String[] args) {
		logger.info("Loading cluster test wrapper");
		
		try {
			SparkConf config = new SparkConf();
			config.setAppName("example-project");
			logger.info("Here 2");
			try (JavaSparkContext context = new JavaSparkContext(config)) {
				ClusterTestWrapper.CONTEXT = context;
				
				JUnitCore junit = new JUnitCore();
				Result result = junit.run(ExampleProjectITCase.class);
				
				ExampleProject p = new ExampleProject(context);
				p.run();
				
			}
		} catch (Throwable t) {
			System.err.println("Error processing spark job: " + t.getMessage());
			t.printStackTrace();
		}
	}

	
}
