package levy.david.spark.itcase.web.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RunUtilTest {
	
	@Test(expected=Exception.class)
	public void testNonExistent() throws Exception {
		RunUtil util = new RunUtil();
		
		int output = util.runCommand("C:\\Temp\\hello_world_2.bat");
	}
	@Test
	public void testExists() throws Exception {
		RunUtil util = new RunUtil();
		
		int output = util.runCommand("C:\\Temp\\hello_world.bat");
		assertEquals(0, output);
	}
}
