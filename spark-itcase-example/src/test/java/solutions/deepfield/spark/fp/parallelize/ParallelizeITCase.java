package solutions.deepfield.spark.fp.parallelize;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import solutions.deepfield.spark.itcase.annotations.SparkITCase;
import solutions.deepfield.spark.itcase.annotations.SparkTest;

@SparkITCase
public class ParallelizeITCase {

	private static final Logger logger = LoggerFactory.getLogger(ParallelizeITCase.class);
	
	@SparkTest
	public void testParallelize() {
		logger.info("Running parallelize integration test");
		Parallelize.main(new String[] {});
	}
	
}
