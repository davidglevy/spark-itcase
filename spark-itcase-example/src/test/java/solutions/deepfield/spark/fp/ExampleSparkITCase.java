package solutions.deepfield.spark.fp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import solutions.deepfield.spark.itcase.annotations.SparkITCase;

@SparkITCase
public class ExampleSparkITCase {

	private static final Logger logger = LoggerFactory.getLogger(ExampleSparkITCase.class);
	
	public void hello() {
		logger.info("World");
	}
	
}
