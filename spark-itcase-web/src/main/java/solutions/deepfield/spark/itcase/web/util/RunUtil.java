package solutions.deepfield.spark.itcase.web.util;

import java.util.ArrayList;
import java.util.List;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

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

import solutions.deepfield.spark.itcase.core.domain.RunResult;
import solutions.deepfield.spark.itcase.web.exception.ExecutionException;

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
		RunResult result = new RunResult();
		CollectingLogOutputStream output = new CollectingLogOutputStream();
		PumpStreamHandler handler = new PumpStreamHandler(output);
		executor.setStreamHandler(handler);
//
		try {
		executor.execute(cmdLine, resultHandler);
//		Executor executor = new DefaultExecutor();
		
		

		// some time later the result handler callback was invoked so we
		// can safely request the exit value
		int duration = 0;
		while (!resultHandler.hasResult()) {
			resultHandler.waitFor(1000);
			logger.info("Waited [" + ++duration + "] for result");
		}
		} finally {
			result.setLogs(output.getLogs());
		}
		
		ExecuteException e = resultHandler.getException();
		if (e == null) {
			int exitValue = resultHandler.getExitValue();
			result.setResult(exitValue);
			
			return result;
		} else {
			throw new ExecutionException("Unable to execute command: " + e.getMessage(),e, result.getLogs());
		}
		

	}
	
	class CollectingLogOutputStream extends LogOutputStream {

		private List<String> logs = new ArrayList<>();
		
		private void addLog(String log, Object... objects ) {
			logger.info(log, objects);
			
			List<String> textParams = new ArrayList<>();
			for (Object o : objects) {
				textParams.add(o == null ? null : o.toString());
			}
			log = log.replaceFirst("\\{\\}", objects[0].toString());
			log = log.replaceFirst("\\{\\}", objects[1].toString());
			
			logs.add(log);
		}
		
		
		
		public List<String> getLogs() {
			return logs;
		}



		@Override
		protected void processLine(String line, int level) {
			switch (level) {
			case 0:
				addLog("Output {}: {}", level, line);
				break;
			case 1:
				addLog("Error {}: {}", level, line);
				break;
			default:
				addLog("Unknown{}: {}", level, line);
			}

		}

	}
}
