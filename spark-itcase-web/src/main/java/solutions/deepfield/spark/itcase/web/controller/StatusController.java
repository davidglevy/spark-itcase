package solutions.deepfield.spark.itcase.web.controller;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.Period;
import org.joda.time.format.PeriodFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.HttpServletBean;
import org.springframework.web.servlet.ModelAndView;

import solutions.deepfield.spark.itcase.core.domain.ApplicationStatusResponse;
import solutions.deepfield.spark.itcase.core.domain.RunParams;
import solutions.deepfield.spark.itcase.core.domain.RunResult;
import solutions.deepfield.spark.itcase.core.util.AppUtil;

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

@Controller
public class StatusController {

	private static final Logger logger = LoggerFactory.getLogger(StatusController.class);
	
	@Autowired
	private AppUtil appUtil;
	
	private long startupTime;
	
	@PostConstruct
	public void postConstruct() {
		logger.info("Starting up");
		startupTime = System.currentTimeMillis();
	}
	
	// TODO Put in a proper status message response based on build version.
	@RequestMapping("/status")
	public @ResponseBody ApplicationStatusResponse status() {
		ApplicationStatusResponse response = new ApplicationStatusResponse();
		
		Period period = new Period(System.currentTimeMillis() - startupTime);
		response.setUptime(PeriodFormat.getDefault().print(period));
		response.setUser(System.getProperty("user.name"));
		response.setVersion(appUtil.getVersion());
		response.setTimestamp(appUtil.getTimestamp());
		
		return response;
	}
}