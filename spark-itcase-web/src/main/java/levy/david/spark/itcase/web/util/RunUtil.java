package levy.david.spark.itcase.web.util;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.LogOutputStream;
import org.apache.commons.exec.PumpStreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import levy.david.spark.itcase.web.domain.RunResult;

@Component
public class RunUtil {
	private static final Logger logger = LoggerFactory.getLogger(RunUtil.class);
	
	public RunResult runCommand(String command) throws Exception {
		CommandLine cmdLine = CommandLine.parse(command);
		DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
//
//		ExecuteWatchdog watchdog = new ExecuteWatchdog(60 * 1000);
		Executor executor = new DefaultExecutor();
		executor.setExitValue(0);
		//executor.setWatchdog(watchdog);
		CollectingLogOutputStream output = new CollectingLogOutputStream();
		PumpStreamHandler handler = new PumpStreamHandler(output);
		executor.setStreamHandler(handler);
//
//		
		executor.execute(cmdLine, resultHandler);
//		Executor executor = new DefaultExecutor();
		
		RunResult result = new RunResult();

		// some time later the result handler callback was invoked so we
		// can safely request the exit value
		int duration = 0;
		while (!resultHandler.hasResult()) {
			resultHandler.waitFor(1000);
			logger.info("Waited [" + ++duration + "] for result");
		}
		
		ExecuteException e = resultHandler.getException();
		if (e == null) {
			int exitValue = resultHandler.getExitValue();
			result.setResult(exitValue);
			return result;
		} else {
			throw new Exception("Unable to execute command: " + e.getMessage(),e);
		}
		

	}
	
	class CollectingLogOutputStream extends LogOutputStream {

		@Override
		protected void processLine(String line, int level) {
			switch (level) {
			case 0:
				logger.info("Output {}: {}", level, line);
				break;
			case 1:
				logger.info("Error {}: {}", level, line);
				break;
			default:
				logger.info("Unknown{}: {}", level, line);
			}

		}

	}
}
