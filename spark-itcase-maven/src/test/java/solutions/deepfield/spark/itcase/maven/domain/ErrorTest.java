package solutions.deepfield.spark.itcase.maven.domain;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import static junit.framework.Assert.*;
import static solutions.deepfield.spark.itcase.maven.test.TestUtil.*;

public class ErrorTest {

	@Test
	public void testRender() {
		Error error = new Error(new Throwable("This is a test message"));
		String result = error.render();
		String trimmed = StringUtils.trim(result);
		
		assertTrue(StringUtils.isNotBlank(result));
		String expectedStart = "<error message=\"This is a test message\" type=\"java.lang.Throwable\"><![CDATA[";
		assertStartsWith(expectedStart, result);
		String expectedTail = "]]></error>";
		assertEndsWith(expectedTail, result);
	}
	
}
