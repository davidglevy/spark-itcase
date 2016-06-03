package solutions.deepfield.spark.itcase.web.util;

import java.io.File;

import javax.annotation.PostConstruct;

import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import solutions.deepfield.spark.itcase.core.domain.AppConfig;
import solutions.deepfield.spark.itcase.exceptions.SparkITCaseException;

@Component
public class ConfigUtil {
	
	private static final Logger logger = LoggerFactory.getLogger(ConfigUtil.class);
	
	private AppConfig config;
	
	@PostConstruct
	public void init() throws Exception {
		String confDir = System.getProperty("CONF_DIR");
		String confFilePath = confDir + File.separator + "config.json";
		File confFile = new File(confFilePath);
		
		ObjectMapper mapper = new ObjectMapper();
		config = mapper.readValue(confFile, AppConfig.class);
		if (config.getPort() < 1 || config.getPort() > 65535) {
			throw new SparkITCaseException("Illegal port value: " + config.getPort());
		} else {
			logger.info("Spark port will run on [" + config.getPort() + "]");
		}
		
		if (StringUtils.isBlank(config.getSparkCommand())) {
			throw new SparkITCaseException("No spark command specified");
		} else {
			File sparkCommand = new File(config.getSparkCommand());
			if (sparkCommand.exists() && sparkCommand.canExecute()) {
				logger.info("Spark command is [{}]", sparkCommand.getAbsolutePath());
			} else {
				throw new SparkITCaseException("Unable to execute spark command [" + sparkCommand.getAbsolutePath() + "]");
			}
		}
		
	}
	
	public int getPort() {
		return (int)config.getPort();
	}
	
	public String getSparkCommand() {
		return config.getSparkCommand();
	}
	
}
