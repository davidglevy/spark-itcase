package solutions.deepfield.spark.itcase.core.util;

import java.io.InputStream;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import solutions.deepfield.spark.itcase.exceptions.SparkITCaseException;

/**
 * Utility class which loads application property information
 * 
 * @author david
 * @since 1.0.1
 */
@Component
public class AppUtil {

	private static final Logger logger = LoggerFactory.getLogger(AppUtil.class);

	private static final String APP_PROPERTIES_FILE = "/app.properties";

	private static final String BUILD_VERSION_PROP = "build.version";

	private static final String BUILD_TIMESTAMP_PROP = "build.timestamp";

	private Properties props;

	@PostConstruct
	public void loadProperties() {
		try (InputStream is = this.getClass().getResourceAsStream(APP_PROPERTIES_FILE)) {
			props = new Properties();
			props.load(is);
		} catch (Exception e) {
			throw new SparkITCaseException("Unable to load application properties: " + e.getMessage(), e);
		}

		testProperty(BUILD_VERSION_PROP);
		testProperty(BUILD_TIMESTAMP_PROP);
	}
	
	private void testProperty(String propertyName) {
		String propValue = props.getProperty(propertyName);
		if (StringUtils.isBlank(propValue)) {
			throw new SparkITCaseException("Property [" + propertyName + "] is blank");
		} else {
			logger.info("Property [{}] is [{}]", propertyName, propValue);
		}
		
	}
	
	public String getVersion() {
		return props.getProperty(BUILD_VERSION_PROP);
	}
	
	public String getTimestamp() {
		return props.getProperty(BUILD_TIMESTAMP_PROP);
	}

}
