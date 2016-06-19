package solutions.deepfield.spark.itcase.maven.test;

import static org.junit.Assert.*;

import org.apache.commons.lang3.StringUtils;

public class TestUtil {

	public static void assertContains(String fragment, String input) {
		if (!input.contains(fragment)) {
			fail("Expected fragment [" + fragment + "] within input [" + StringUtils.left(input, 200) + "]");
		}
	}
	
	public static void assertStartsWith(String expected, String input) {
		if (StringUtils.isBlank(input)) {
			fail("Input is blank so no length");
		}
		
		input = input.trim();
		expected = input.trim();
		
		if (input.length() < expected.length()) {
			fail("The input length is shorted than the expected start length [" + expected.length() + "]");
		}
		
		String prefix = input.substring(0, expected.length());
		assertEquals(expected, prefix);
	}

	
	
	public static void assertEndsWith(String expected, String input) {
		if (StringUtils.isBlank(input)) {
			fail("Input is blank so no length");
		}
		
		input = input.trim();
		expected = input.trim();
		
		if (input.length() < expected.length()) {
			fail("The input length is shorted than the expected start length [" + expected.length() + "]");
		}
		
		String postfix = input.substring(input.length() - expected.length());
		assertEquals(expected, postfix);
	}

}
