package solutions.deepfield.spark.itcase.web.controller;

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

import java.util.List;
import java.util.UUID;

import javax.servlet.ServletException;

import solutions.deepfield.spark.itcase.core.LocalRepository;
import solutions.deepfield.spark.itcase.web.domain.RunParams;
import solutions.deepfield.spark.itcase.web.domain.RunResult;
import solutions.deepfield.spark.itcase.web.util.RunUtil;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
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
			String mainJar = repo.buildFile(params.getGroupId(),
					params.getArtifactId(), params.getVersion());
			
			List<String> files = repo.buildFiles(params.getGroupId(),
					params.getArtifactId(), params.getVersion());
			
//			String classpath = "file:/" + StringUtils.join(files, ";file:/");
//			logger.info("Classpath will be: " + classpath);

			String classpath = StringUtils.join(files, ",");
			logger.info("Classpath will be: " + classpath);

			
			RunResult commandResult = runUtil.runCommand("/usr/bin/spark-submit --class " + params.getClassName() + " --jars " + classpath + " " + mainJar);
			logger.info("Command result was: " + commandResult);
			commandResult.setJobId(UUID.randomUUID().toString());
			
			return commandResult;
		} catch (Exception e) {
			throw new ServletException("Unable to build classpath: "
					+ e.getMessage(), e);
		}

	}


}
