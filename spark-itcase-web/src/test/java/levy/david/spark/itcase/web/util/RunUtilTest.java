package levy.david.spark.itcase.web.util;

import static org.junit.Assert.assertEquals;

import org.apache.commons.lang.SystemUtils;
import org.junit.Test;
import static org.junit.Assert.*;

public class RunUtilTest {
	
	@Test(expected=Exception.class)
	public void testNonExistent() throws Exception {
		RunUtil util = new RunUtil();
		if (SystemUtils.IS_OS_WINDOWS) {
			int output = util.runCommand("C:\\Temp\\hello_world_2.bat");			
		} else if (SystemUtils.IS_OS_LINUX){
			int output = util.runCommand("/usr/bin/echo_2");						
		} else {
			fail("No system tests for operating system [" + SystemUtils.OS_NAME + "]");
		}
		
	}
	@Test
	public void testExists() throws Exception {
		RunUtil util = new RunUtil();
		
		if (SystemUtils.IS_OS_WINDOWS) {
			int output = util.runCommand("C:\\Temp\\hello_world.bat");
			assertEquals(0, output);
		} else if (SystemUtils.IS_OS_LINUX){
			int output = util.runCommand("/usr/bin/echo");
			assertEquals(0, output);
		} else {
			fail("No system tests for operating system [" + SystemUtils.OS_NAME + "]");
		}
	}
}
