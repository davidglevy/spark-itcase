package solutions.deepfield.spark.fp.parallelize;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import solutions.deepfield.spark.itcase.annotations.SparkAfter;
import solutions.deepfield.spark.itcase.annotations.SparkBefore;
import solutions.deepfield.spark.itcase.annotations.SparkITCase;
import solutions.deepfield.spark.itcase.annotations.SparkTest;

@SparkITCase
public class ParallelizeITCase {

	private static final Logger logger = LoggerFactory.getLogger(ParallelizeITCase.class);
	
	@SparkBefore
	public void doSomethingBefore() {
		logger.info("Doing something in wrapping driver (e.g. setting up something in HDFS) before main test");
	}

	@SparkTest
	public void testParallelize() {
		logger.info("Running parallelize integration test 0");
		Parallelize.main(new String[] {});
	}

	@SparkTest
	public void testParallelize1() {
		logger.info("Running parallelize integration test 1");
		Parallelize.main(new String[] {});
	}

	@SparkTest
	public void testParallelize2() {
		logger.info("Running parallelize integration test 2");
		Parallelize.main(new String[] {});
	}

	@SparkTest
	public void testParallelize3() {
		logger.info("Running parallelize integration test 3");
		Parallelize.main(new String[] {});
	}

	@SparkTest
	public void testParallelize4() {
		logger.info("Running parallelize integration test 4");
		Parallelize.main(new String[] {});
	}

	@SparkTest
	public void testParallelize5() {
		logger.info("Running parallelize integration test 5");
		Parallelize.main(new String[] {});
	}

	@SparkAfter
	public void doSomethingAfter() {
		logger.info("Doing something in wrapping driver (e.g. setting up something in HDFS) before main test");
	}

}
