package solutions.deepfield.spark.itcase.maven;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;
import com.mashape.unirest.request.HttpRequestWithBody;
import com.mashape.unirest.request.body.MultipartBody;
import com.mashape.unirest.request.body.RawBody;

import solutions.deepfield.spark.itcase.core.domain.ApplicationStatusResponse;
import solutions.deepfield.spark.itcase.core.domain.RunParams;
import solutions.deepfield.spark.itcase.core.domain.RunResult;
import solutions.deepfield.spark.itcase.core.util.AppUtil;
import solutions.deepfield.spark.itcase.exceptions.SparkITCaseException;

/**
 * Loads the dependencies for the project into the target environment.
 */
@Mojo(name = "sparkItCase",defaultPhase=LifecyclePhase.INTEGRATION_TEST)
public class SparkITCaseMavenPlugin extends AbstractMojo {

	/**
	 * The Maven Project.
	 *
	 */
	@Parameter(property = "project")
	private MavenProject project = null;

	// @Parameter(property = "spark.itcase.endpoint")
	@Parameter
	private String endpoint;

	@Parameter
	private String proxyHost;
	
	@Parameter
	private int proxyPort;
	
	@Parameter(property = "project.build.directory")
	private File buildDir;

	@Parameter(property = "project.basedir")
	private File projectBase;

	@Parameter(property = "project.build.finalName")
	private String finalName;

	@Parameter
	private String classToRun;
	
	private AppUtil appUtil;
	
	private void init() {
		appUtil = new AppUtil();
		appUtil.loadProperties();
		getLog().info("Plugin version is [" + appUtil.getVersion() + "]");
		getLog().info("Plugin build is [" + appUtil.getTimestamp() + "]");
	}
	
	public void execute() throws MojoExecutionException {

		try {

			if (StringUtils.isNotBlank(proxyHost)) {
				Unirest.setProxy(new HttpHost(proxyHost, proxyPort));
			}
			
			init();
			
			checkStatus();
			
			// Install the artifact in the remote server.
			getLog().info("Deploying project artifact");

			HttpRequestWithBody body = Unirest.post(endpoint
					+ "/artifact/deploy");

			MultipartBody multiPartBody = body.field("artifact", new File(
					buildDir.getAbsolutePath() + File.separator + finalName
							+ ".jar"));
			multiPartBody.field("artifactId", project.getArtifactId());
			multiPartBody.field("groupId", project.getGroupId());
			multiPartBody.field("version", project.getVersion());

			File pomFile = new File(projectBase.getAbsoluteFile()
					+ File.separator + "pom.xml");
			multiPartBody = multiPartBody.field("pom", pomFile);

			getLog().info(
					"Found http response:\n"
							+ multiPartBody.asString().getBody());

			getLog().info(
					"Looking for build artifact [" + buildDir.getAbsolutePath()
							+ "]");

			List<Dependency> dependencies = project.getDependencies();

			for (Dependency dependency : dependencies) {
				if (dependency.getScope().equalsIgnoreCase("provided")) {
					getLog().info(
							"Skipping [" + dependency.getGroupId() + ":"
									+ dependency.getArtifactId() + ":"
									+ dependency.getVersion() + "]");
					continue;
				} else {
					getLog().info(
							"Found dependency [" + dependency.getGroupId()
									+ ":" + dependency.getArtifactId() + ":"
									+ dependency.getVersion() + "]");
				}

				Map<String, Object> parameters2 = new HashMap<String, Object>();
				parameters2.put("artifactId", dependency.getArtifactId());
				parameters2.put("groupId", dependency.getGroupId());
				parameters2.put("version", dependency.getVersion());

				HttpResponse<String> resultDep = Unirest
						.get(endpoint + "/artifact/retrieve")
						.queryString(parameters2).asString();
				getLog().info("Found http response:\n" + resultDep.getBody());

			}

			RunParams params = new RunParams();
			params.setArtifactId(project.getArtifactId());
			params.setGroupId(project.getGroupId());
			params.setVersion(project.getVersion());
			params.setClassName(classToRun);
			
			ObjectMapper mapper = new ObjectMapper();
			byte[] dataToSend = mapper.writeValueAsBytes(params);
			
			HttpRequestWithBody requestWithBody = Unirest.post(endpoint
					+ "/spark/run");
			requestWithBody = requestWithBody.header("Content-Type",
					"application/json");
			requestWithBody = requestWithBody.header("Accept",
					"application/json");
			RawBody rawBody = requestWithBody.body(dataToSend);
			getLog().info("About to make request to server");
			HttpResponse<String> response = rawBody.asString();
			
			// TODO Log response and logs.
			
			if (response.getStatus() == 200) {
				getLog().info("Request to run complete: " + response.getBody());
			} else {
				List<String> content = response.getHeaders().get("Content-Type");
				if (content.size() == 1 && content.get(0).toLowerCase().startsWith("application/json")) {
					RunResult result = mapper.readerFor(RunResult.class).readValue(response.getBody());
					
					// TODO Add in logs.
					if (result.getLogs() != null && result.getLogs().size() > 0) {
						for (String log : result.getLogs()) {
							getLog().error("Server log: " + log);
						}
					}
					
					getLog().error("Error stack:\n" + result.getExceptionStack());
					throw new SparkITCaseException("Received error " + result.getExceptionMessage());
				} else {
					getLog().error("Unknown error payload content type: " + content);
					throw new SparkITCaseException("Received error " + response.getStatusText());
				}
			}


		} catch (Exception e) {
			getLog().error(e);
			throw new MojoExecutionException(
					"Error invoking spark itcase server at ["
							+ endpoint + "]:" + e.getMessage(), e);
		}

	}


	/**
	 * Perform a status check on the server.
	 * 
	 * @throws Exception if the status was not HTTP 200 and a matched version.
	 */
	protected void checkStatus() throws Exception {
		GetRequest request = Unirest.get(endpoint + "/status");
		request = request.header("Content-Type",
				"application/json");
		request = request.header("Accept",
				"application/json");
		HttpResponse<String> response = request.asString();
		if (response.getStatus() == 200) {
			ObjectMapper mapper = new ObjectMapper();
			ApplicationStatusResponse status = mapper.readerFor(ApplicationStatusResponse.class).readValue(response.getBody());
			if (status.getVersion().equals(appUtil.getVersion())) {
				getLog().info("Matched client-server version [" + status.getVersion() + "]");
			} else {
				throw new SparkITCaseException("Server version [" + status.getVersion() + "] does not match client [" + appUtil.getVersion() + "]");
			}
			
			getLog().info("Server running as user [" + status.getUser() + "]");
			getLog().info("Server uptime [" + status.getUptime() + "]");
		} else {
			throw new SparkITCaseException("Unable to retrieve status, received [" + response.getStatus() + "], message [" + response.getStatusText() + "]");
		}
	}

}