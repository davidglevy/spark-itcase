package solutions.deepfield.spark.fp.inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import solutions.deepfield.spark.fp.inject.Outer;
import solutions.deepfield.spark.itcase.annotations.SparkITCase;
import solutions.deepfield.spark.itcase.annotations.SparkTest;

@SparkITCase
public class OuterITCase {

	private static final Logger logger = LoggerFactory.getLogger(OuterITCase.class);
	
	@SparkTest
	public void testMainNoArgs() {
		logger.info("Testing main method without any arguments");
		Outer.main(new String[] {});
	}
	
	@SparkTest
	public void testMainSingleArg() {
		logger.info("Testing main method with an argument");
		Outer.main(new String[] { "Example Argument"});
	}
	
}
