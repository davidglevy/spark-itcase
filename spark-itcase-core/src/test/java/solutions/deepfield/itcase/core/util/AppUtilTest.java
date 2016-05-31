package solutions.deepfield.itcase.core.util;

import org.junit.Before;
import org.junit.Test;

import solutions.deepfield.spark.itcase.core.util.AppUtil;

import static org.junit.Assert.*;

import org.codehaus.plexus.util.StringUtils;

public class AppUtilTest {

	AppUtil target;
	
	@Before
	public void setup() {
		target = new AppUtil();
	}
	
	@Test
	public void testWorks() {
		target.loadProperties();
		assertTrue(StringUtils.isNotBlank(target.getTimestamp()));
		assertTrue(StringUtils.isNotBlank(target.getVersion()));
		
	}
	
}
