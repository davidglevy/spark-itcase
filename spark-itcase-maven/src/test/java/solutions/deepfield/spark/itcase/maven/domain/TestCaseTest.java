package solutions.deepfield.spark.itcase.maven.domain;

import static solutions.deepfield.spark.itcase.maven.test.TestUtil.assertContains;
import static solutions.deepfield.spark.itcase.maven.test.TestUtil.assertEndsWith;
import static solutions.deepfield.spark.itcase.maven.test.TestUtil.assertStartsWith;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestCaseTest {

	private static final String SYSTEM_OUT_FRAGMENT = "<system-out><![CDATA[Here 1]]></system-out>";

	private static final String SYSTEM_ERR_FRAGMENT = "<system-err><![CDATA[Here 2]]></system-err>";

	private static final String ERROR_FRAGMENT = "<error message=\"This is a test message\" type=\"java.lang.Throwable\"><![CDATA[";

	private static final String FAILURE_FRAGMENT = "<failure message=\"This is a test message\" type=\"java.lang.AssertionError\"><![CDATA[";

	private static final String EXPECTED_END = "</testcase>";

	private static final Logger logger = LoggerFactory.getLogger(TestCaseTest.class);
	
	private TestCase target = new TestCase("burp", "example.pkg.BadlyMannered");

	private static String EXPECTED_START = "<testcase name=\"testNonExistent\" classname=\"solutions.deepfield.spark.itcase.web.util.RunUtilTest\" time=\"0.";	

	
	@Test(expected=IllegalStateException.class)
	public void testExceptionNoMarkComplete() {
		target.render();
	}

	@Test
	public void testRenderNoError() {
		target.markComplete();
		String result = target.render().trim();
		assertStartsWith(EXPECTED_START, result);
	}

	@Test
	public void testRenderError() {
		target.markComplete();
		Throwable t = new Throwable("This is a test message");
		Error e = new Error(t);
		target.setError(e);
		target.setSystemOut("Here 1");
		target.setSystemErr("Here 2");
		
		String result = target.render().trim();
		assertStartsWith(EXPECTED_START, result);
		assertContains(ERROR_FRAGMENT, result);
		assertContains(SYSTEM_OUT_FRAGMENT, result);
		assertContains(SYSTEM_ERR_FRAGMENT, result);
		assertEndsWith(EXPECTED_END, result);
		
	}

	@Test
	public void testRenderFailure() {
		target.markComplete();
		Throwable t = new AssertionError("This is a test message");
		Error e = new Error(t);
		target.setFailure(e);
		target.setSystemOut("Here 1");
		target.setSystemErr("Here 2");
		
		String result = target.render().trim();
		assertStartsWith(EXPECTED_START, result);
		assertContains(FAILURE_FRAGMENT, result);
		assertContains(SYSTEM_OUT_FRAGMENT, result);
		assertContains(SYSTEM_ERR_FRAGMENT, result);
		assertEndsWith(EXPECTED_END, result);		
	}
	
	@Test
	public void testSkipped() {
		target.setSkipped(true);
		String result = target.render();
		String expected = "<testcase name=\"testNonExistent\" classname=\"solutions.deepfield.spark.itcase.web.util.RunUtilTest\" time=\"0.0\"/>";
		
	}
	
	
}
