package solutions.deepfield.spark.itcase.maven;

import java.io.ByteArrayOutputStream;

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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;

import solutions.deepfield.spark.itcase.exceptions.SparkITCaseException;

/**
 * Loads the dependencies for the project into the target environment.
 */
@Mojo(name = "runTests", defaultPhase = LifecyclePhase.INTEGRATION_TEST)
public class RunTestsPlugin extends BaseSparkITCasePlugin {

	@Parameter(property = "maven.compiler.testTarget")
	private String testTarget;

	
	@Override
	public void postExecute() throws Exception {

		TestUtil testUtil = new TestUtil();
		testUtil.setBuildDir(buildDir);
		testUtil.setTestTarget(testTarget);
		testUtil.setLog(getLog());
		testUtil.init();

		
		if (!testUtil.hasTests()) {
			throw new SparkITCaseException("No tests found in [" + testUtil.getTestTarget() + "]");
		}
		
		// First create the test artifact
		// -- make a spark-itcase dir
		File outputDir = new File(buildDir.getAbsolutePath() + File.separator + "spark-itcase");
		getLog().info("About to delete [" + outputDir.getAbsolutePath() + "]");
		if (outputDir.exists()) {
			//FileUtils.deleteDirectory(outputDir);
		}
		outputDir.mkdir();

		// -- zip test classes to that dir
		File testJar = new File(outputDir.getAbsolutePath() + File.separator + project.getArtifactId() + "-spark-itcase.jar");
		compressZipfile(testUtil.getTestTarget(), testJar.getAbsolutePath());
		

		
		// Second, make a pom based on internal template with 2 dependencies
		StringBuilder dependencyText = new StringBuilder();
		
		String dependencyFrag = null;
		try (InputStream is = this.getClass().getResourceAsStream("/dependency_fragment.xml");
				ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			IOUtils.copy(is, out);
			dependencyFrag = new String(out.toByteArray());
		}
		

		// -- All test dependencies from this project (which will be runtime on the new project).
		List<Dependency> dependencies = project.getDependencies();
		for (Dependency dependency : dependencies) {
			String groupId = dependency.getGroupId();
			String artifactId = dependency.getArtifactId();
			String version = dependency.getVersion();
			
			if (!dependency.getScope().equalsIgnoreCase("test")) {
				continue;
			} else {
				getLog().info("Found test dependency [" + groupId + ":" + artifactId
						+ ":" + version + "]");
			}

			addDependency(dependencyText, dependencyFrag, groupId, artifactId, version);
		}

		// -- The project as a main dependency
		String groupId = project.getGroupId();
		String version = project.getVersion();
		addDependency(dependencyText, dependencyFrag, groupId, project.getArtifactId(), version);
		// -- The project containing the SparkSubmitTestWrapper
		// Add this plugin as a dependency so we get annotations and the wrapper.
		addDependency(dependencyText, dependencyFrag, "solutions.deepfield", "spark-itcase-maven", appUtil.getVersion());
		
		
		String pomTemplate = null;
		try (InputStream is = this.getClass().getResourceAsStream("/pom_template.xml");
				ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			IOUtils.copy(is, out);
			pomTemplate = new String(out.toByteArray());
		}
		
		String artifactId = project.getArtifactId() + "-spark-itcase-wrapper";
		pomTemplate = pomTemplate.replace("@ARTIFACT_ID@", artifactId);
		pomTemplate = pomTemplate.replace("@NAME@", artifactId);
		pomTemplate = pomTemplate.replace("@GROUP_ID@", groupId);
		pomTemplate = pomTemplate.replace("@VERSION@", version);
		pomTemplate = pomTemplate.replace("@DEPENDENCIES@", dependencyText);

		getLog().info("Dynamic pom with wrapping class is:\n" + pomTemplate);
		
		File pomFile = new File(outputDir.getAbsolutePath() + File.separator + "pom.xml");
		try (FileOutputStream out = new FileOutputStream(pomFile)) {
			out.write(pomTemplate.getBytes());
		}

		deployUtil.deploy(testJar, groupId, artifactId, version, pomFile);
		
		for (Class testClass : testUtil.getTestClasses()) {
			getLog().info("Running test class [" + testClass + "]");
			// TODO Add inner method to run each annotated test method in class.
			// Invoke spark/run with SparkSubmitWrapper; main JAR is ???
			List<String> parameters = new ArrayList<>();
			parameters.add(testClass.getCanonicalName());
			runUtil.run(SparkSubmitTestWrapper.class.getCanonicalName(), parameters, artifactId, groupId, version);
			
		}
		
		
	}

	private void addDependency(StringBuilder dependencyText, String dependencyFrag, String groupId, String artifactId, String version) throws Exception {
		String modFrag = dependencyFrag.replace("@GROUP_ID@", groupId);
		modFrag = modFrag.replace("@ARTIFACT_ID@", artifactId);
		modFrag = modFrag.replace("@VERSION@", version);
		dependencyText.append(modFrag);
		
		Map<String, Object> parameters2 = new HashMap<String, Object>();
		parameters2.put("artifactId", artifactId);
		parameters2.put("groupId", groupId);
		parameters2.put("version", version);

		HttpResponse<String> resultDep = Unirest.get(endpoint + "/artifact/retrieve").queryString(parameters2)
				.asString();
		getLog().info("Found http response:\n" + resultDep.getBody());
		
	}
	
	public void compressZipfile(String sourceDir, String outputFile) throws Exception {
	    getLog().info("Source dir is [" + sourceDir + "]");
		ZipOutputStream zipFile = new ZipOutputStream(new FileOutputStream(outputFile));
	    compressDirectoryToZipfile(sourceDir, sourceDir, zipFile);
	    
	    // Add in a manifest.
	    ZipEntry entry = new ZipEntry("META-INF" + File.separator + "MANIFEST.MF");
	    zipFile.putNextEntry(entry);
	    String text = "Manifest-Version: 1.0\nTool: sparkItCase";
	    zipFile.write(text.getBytes());
	    
	    IOUtils.closeQuietly(zipFile);
	}

	private void compressDirectoryToZipfile(String rootDir, String sourceDir, ZipOutputStream out) throws Exception {
		getLog().info("Compressing test dir [" + sourceDir + "]");
		File sourceDirFile = new File(sourceDir);
		if (sourceDirFile.listFiles() == null) {
			getLog().warn("No files in directory [" + sourceDir + "]");
			return;
		}
		
		for (File file : sourceDirFile.listFiles()) {
	        if (file.isDirectory()) {
	            compressDirectoryToZipfile(rootDir, sourceDir + File.separator + file.getName(), out);
	        } else {
	            ZipEntry entry = new ZipEntry(sourceDir.replace(rootDir, "") + file.getName());
	            out.putNextEntry(entry);
	            
	            FileInputStream in = new FileInputStream(sourceDir + File.separator + file.getName());
	            IOUtils.copy(in, out);
	            IOUtils.closeQuietly(in);
	        }
	    }
	}
}