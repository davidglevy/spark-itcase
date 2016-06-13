package solutions.deepfield.spark.itcase.maven.domain;

import static org.junit.Assert.*;

import org.apache.commons.lang3.StringUtils;

public class TestUtil {

	public static void assertStartsWith(String expected, String input) {
		if (StringUtils.isBlank(input)) {
			fail("Input is blank so no length");
		}
		
		input =
		
		if (input.length() < expected.length()) {
			fail("The input length is shorted than the expected start length [" + expected.length() + "]");
		}
		
		String 
		
	}
	
}
