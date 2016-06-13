package solutions.deepfield.spark.itcase.maven.domain;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestCaseTest {

	private static final Logger logger = LoggerFactory.getLogger(TestCaseTest.class);
	
	TestCase target = new TestCase("burp", "example.pkg.BadlyMannered");

	@Test(expected=IllegalStateException.class)
	public void testExceptionNoMarkComplete() {
		target.render();
	}

	@Test
	public void testRenderNoError() {
		target.markComplete();
		String result = target.render().trim();
		String expected = "<testcase name=\"testNonExistent\" classname=\"solutions.deepfield.spark.itcase.web.util.RunUtilTest\" time=\"0.01\">";
		
		assertEquals(expected, result);
	}
	
}
