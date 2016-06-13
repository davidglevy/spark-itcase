package solutions.deepfield.spark.itcase.web.util;

import static org.junit.Assert.assertEquals;

import org.apache.commons.lang.SystemUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import solutions.deepfield.spark.itcase.core.domain.RunResult;
import solutions.deepfield.spark.itcase.exceptions.SparkITCaseException;
import solutions.deepfield.spark.itcase.web.util.RunUtil;
import static org.junit.Assert.*;

public class RunUtilTest {

	private static final Logger logger = LoggerFactory.getLogger(RunUtilTest.class);
	
	//@Test(expected=Exception.class)
	@Test
	public void testNonExistent() throws Exception {
		logger.info("Here");
		fail("Test failure");
		
		RunUtil util = new RunUtil();
		if (SystemUtils.IS_OS_WINDOWS) {
			util.runCommand("C:\\Temp\\hello_world_2.bat");			
		} else if (SystemUtils.IS_OS_LINUX){
			util.runCommand("/usr/bin/echo_2");						
		} else {
			fail("No system tests for operating system [" + SystemUtils.OS_NAME + "]");
		}
		
	}
	@Test
	public void testExists() throws Exception {
		logger.info("Here");
		RunUtil util = new RunUtil();
		
		if (SystemUtils.IS_OS_WINDOWS) {
			RunResult output = util.runCommand("C:\\Temp\\hello_world.bat");
			
			assertEquals(0, output.getResult());
			assertEquals(3, output.getLogs().size());
		} else if (SystemUtils.IS_OS_LINUX){
			RunResult output = util.runCommand("/usr/bin/echo");
			assertEquals(0, output.getResult());
		} else {
			fail("No system tests for operating system [" + SystemUtils.OS_NAME + "]");
		}
		
		throw new SparkITCaseException("here is an unexpected exception");
	}
}
