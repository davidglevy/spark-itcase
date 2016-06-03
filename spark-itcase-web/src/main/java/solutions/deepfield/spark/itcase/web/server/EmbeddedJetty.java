package solutions.deepfield.spark.itcase.web.server;

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

import java.io.File;
import java.io.IOException;

import javax.servlet.MultipartConfigElement;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import solutions.deepfield.spark.itcase.web.util.ConfigUtil;

public class EmbeddedJetty {

	private static final Logger logger = LoggerFactory.getLogger(EmbeddedJetty.class);

	private static final int DEFAULT_PORT = 10080;
	private static final String CONTEXT_PATH = "/";
	private static final String MAPPING_URL = "/rest/*";

	public static void main(String[] args) throws Exception {
		try {
			logger.info("Starting server");
			new EmbeddedJetty().startJetty(getPortFromArgs(args));
			logger.info("Server started");
		} catch (Throwable t) {
			logger.error("Server error received: " + t.getMessage(), t);
		}
	}

	private static int getPortFromArgs(String[] args) {
		if (args.length > 0) {
			try {
				int result = Integer.valueOf(args[0]);
				logger.info("Jetty port from command line is [" + result + "]");
				return result;
			} catch (NumberFormatException ignore) {
			}
		}
		logger.info("Jetty port from default is [" + DEFAULT_PORT + "]");
		return DEFAULT_PORT;
	}

	private void startJetty(int port) throws Exception {
		logger.info("Configuring Spring Context");
		WebApplicationContext context = getContext();
		ConfigUtil config = context.getBean(ConfigUtil.class);
		
		
		logger.info("Creating Jetty");
		Server server = new Server(config.getPort());

		logger.info("Initializing Jetty Spring context");
		server.setHandler(getServletContextHandler(context));
		logger.info("Starting Jetty Server");
		server.start();
		logger.info("Joining Jetty Server");
		server.join();
		logger.info("Server started, joined and running.");
	}

	private static ServletContextHandler getServletContextHandler(WebApplicationContext context) throws IOException {
		ServletContextHandler contextHandler = new ServletContextHandler();
		contextHandler.setErrorHandler(null);
		contextHandler.setContextPath(CONTEXT_PATH);
		ServletHolder holder = new ServletHolder(new DispatcherServlet(context));

		contextHandler.addServlet(holder, MAPPING_URL);
		contextHandler.addEventListener(new ContextLoaderListener(context));
		ResourceHandler resHandler = new ResourceHandler();
		resHandler.setBaseResource(Resource.newClassPathResource("/webapp"));
		contextHandler.setHandler(resHandler);
		//contextHandler.setResourceBase(new ClassPathResource("/webapp").getURI().toString());
		return contextHandler;
	}

	private static WebApplicationContext getContext() {
		XmlWebApplicationContext context = new XmlWebApplicationContext();
		context.setConfigLocations("classpath:spark-itcase-servlet.xml");
		context.start();
		return context;

		// AnnotationConfigWebApplicationContext context = new
		// AnnotationConfigWebApplicationContext();
		// context.setConfigLocation(CONFIG_LOCATION);
		// context.getEnvironment().setDefaultProfiles(DEFAULT_PROFILE);
		// return context;
	}

}
