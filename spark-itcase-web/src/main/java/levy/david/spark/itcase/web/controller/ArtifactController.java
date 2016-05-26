package levy.david.spark.itcase.web.controller;

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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import levy.david.spark.itcase.core.LocalRepository;
import levy.david.spark.itcase.web.domain.DeployParams;
import levy.david.spark.itcase.web.domain.LoadParams;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.codehaus.plexus.util.StringUtils;
import org.eclipse.aether.installation.InstallationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.HttpServletBean;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ArtifactController {

	private static final Logger logger = LoggerFactory
			.getLogger(ArtifactController.class);

	@PostConstruct
	public void postConstruct() {
		logger.info("Starting up");
	}

	@RequestMapping("/artifact/retrieve")
	public void retrieve(LoadParams params, BindingResult binding,
			HttpServletResponse response) throws IOException, ServletException {
		logger.info("processing [" + params + "]");

		LocalRepository repo = new LocalRepository();
		repo.initialize();

		response.setContentType("text/plain");

		repo.retrieveArtifact(params.getGroupId(), params.getArtifactId(),
				params.getVersion(), response.getOutputStream());
	}

	@RequestMapping(value = "/artifact/deploy", method = { RequestMethod.POST })
	public void deploy(HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
		if (ServletFileUpload.isMultipartContent(request)) {

			// Create a factory for disk-based file items
			FileItemFactory factory = new DiskFileItemFactory();

			// Create a new file upload handler
			ServletFileUpload upload = new ServletFileUpload(factory);

			// Parse the request
			List<FileItem> items;
			try {
				items = upload.parseRequest(request);
			} catch (FileUploadException e) {
				throw new ServletException("Unable to parse file upload request");
			}

			String groupId = null;
			String artifactId = null;
			String version = null;
			InputStream jarInStream = null;
			InputStream pomInStream = null;
			for (FileItem item : items) {
				if (item.isFormField()) {
					if (item.getFieldName().equals("groupId")) {
						groupId = item.getString();
					} else if (item.getFieldName().equals("artifactId")) {
						artifactId = item.getString();
					} else if (item.getFieldName().equals("version")) {
						version = item.getString();
					}
				} else {
					if (item.getFieldName().equals("artifact")) {
						jarInStream = item.getInputStream();
					} else if (item.getFieldName().equals("pom")) {
						pomInStream = item.getInputStream();
					}
				}
			}
			// TODO Validate all were populated.
			if (StringUtils.isBlank(groupId)) {
				errorOut(response, "groupId parameter can not be blank");
			}
			if (StringUtils.isBlank(artifactId)) {
				errorOut(response, "artifactId parameter can not be blank");
			}
			if (StringUtils.isBlank(version)) {
				errorOut(response, "version parameter can not be blank");
			}


			// Extract the file and the artifactId, groupId and versionId.

			LocalRepository repo = new LocalRepository();
			repo.initialize();

			try {
				repo.deployArtifact(groupId, artifactId, version, jarInStream, pomInStream);
			} catch (Exception e) {
				errorOut(response, "Unable to deploy artifact: " + e.getMessage(), e);
			}
			if (jarInStream != null) {
				jarInStream.close();
			}

		} else {
			errorOut(response, "Was expecting a multipart form upload");
			
		}

		response.setStatus(200);
		response.setContentType("text/plain");
		response.getOutputStream().write("Deployment Complete".getBytes());

	}

	private void errorOut(HttpServletResponse response, String message) throws IOException {
		errorOut(response, message, null);
	}

	
	private void errorOut(HttpServletResponse response, String message, Throwable t) throws IOException {
		response.setStatus(500);
		response.setContentType("text/plain");
		response.getOutputStream().write(message.getBytes());
		if (t == null) {
			throw new RuntimeException(message);
		} else {
			throw new RuntimeException(message, t);
		}
	}
}