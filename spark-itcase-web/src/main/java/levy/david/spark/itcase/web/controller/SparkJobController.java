package levy.david.spark.itcase.web.controller;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletException;

import levy.david.spark.itcase.core.LocalRepository;
import levy.david.spark.itcase.web.domain.LoadParams;
import levy.david.spark.itcase.web.domain.RunParams;
import levy.david.spark.itcase.web.domain.RunResult;
import levy.david.spark.itcase.web.util.RunUtil;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteStreamHandler;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.LogOutputStream;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SparkJobController {

	private static final Logger logger = LoggerFactory
			.getLogger(SparkJobController.class);

	@Autowired
	RunUtil runUtil;
	
	@RequestMapping(value = "/spark/run", method = RequestMethod.POST)
	public @ResponseBody RunResult runJob(@RequestBody RunParams params)
			throws ServletException {
		logger.info("Received: {}", params);

		LocalRepository repo = new LocalRepository();
		repo.initialize();

		try {
			List<String> files = repo.buildFiles(params.getGroupId(),
					params.getArtifactId(), params.getVersion());
			String classpath = "file:/" + StringUtils.join(files, ";file:/");
			logger.info("Classpath will be: " + classpath);

			RunResult commandResult = runUtil.runCommand("spark-submit --class " + params.getClassName() + " --jars " + classpath);
			logger.info("Command result was: " + commandResult);
			commandResult.setJobId(UUID.randomUUID().toString());
			
			return commandResult;
		} catch (Exception e) {
			throw new ServletException("Unable to build classpath: "
					+ e.getMessage(), e);
		}

	}


}
