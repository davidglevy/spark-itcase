package solutions.deepfield.spark.fp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import solutions.deepfield.spark.fp.inject.Outer;
import solutions.deepfield.spark.itcase.annotations.SparkITCase;
import solutions.deepfield.spark.itcase.annotations.SparkTest;

@SparkITCase
public class ExampleSparkITCase {

	private static final Logger logger = LoggerFactory.getLogger(ExampleSparkITCase.class);
	
	@SparkTest
	public void hello() {
		logger.info("Hello World from test method");
		
		Outer.main(new String[] {});
	}
	
}
