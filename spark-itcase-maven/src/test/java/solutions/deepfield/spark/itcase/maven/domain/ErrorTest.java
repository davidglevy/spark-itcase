package solutions.deepfield.spark.itcase.maven.domain;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import static junit.framework.Assert.*;

public class ErrorTest {

	@Test
	public void testRender() {
		Error error = new Error(new Throwable("This is a test message"));
		String result = error.render();
		String trimmed = StringUtils.trim(result);
		
		assertTrue(StringUtils.isNotBlank(result));
		String expectedStart = "<error message=\"This is a test message\" type=\"java.lang.Throwable\"><![CDATA[";
		String start = trimmed.substring(0, expectedStart.length());
		assertEquals(expectedStart, start);
		String expectedTail = "]]></error>";
		assertEquals(expectedTail, trimmed.substring(trimmed.length() - expectedTail.length()));
	}
	
}
