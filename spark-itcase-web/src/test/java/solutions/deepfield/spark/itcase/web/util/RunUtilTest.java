package solutions.deepfield.spark.itcase.web.util;

import static org.junit.Assert.assertEquals;

import org.apache.commons.lang.SystemUtils;
import org.junit.Test;

import solutions.deepfield.spark.itcase.core.domain.RunResult;
import solutions.deepfield.spark.itcase.web.util.RunUtil;

import static org.junit.Assert.*;

public class RunUtilTest {
	
	@Test(expected=Exception.class)
	public void testNonExistent() throws Exception {
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
	}
}
