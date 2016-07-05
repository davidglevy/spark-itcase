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
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.impl.RemoteRepositoryManager;
import org.eclipse.aether.repository.RemoteRepository;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;

import solutions.deepfield.spark.itcase.annotations.SparkSubmitTestWrapper;
import solutions.deepfield.spark.itcase.annotations.SparkTest;
import solutions.deepfield.spark.itcase.exceptions.SparkITCaseException;

/**
 * Loads the dependencies for the project into the target environment.
 */
@Mojo(name = "runTests", defaultPhase = LifecyclePhase.INTEGRATION_TEST, requiresDependencyResolution = ResolutionScope.TEST)
public class RunTestsPlugin extends BaseSparkITCasePlugin {

	@Parameter(property = "maven.compiler.testTarget")
	private String testTarget;

	/**
     * The entry point to Aether, i.e. the component doing all the work.
     * 
     * @component
     */
	@Component
    private RepositorySystem repoSystem;
    
    /**
     * The current repository/network configuration of Maven.
     * 
     */
    @Parameter(defaultValue="${repositorySystemSession}",readonly=true)
    private RepositorySystemSession repoSession;
    
    /**
     * The project's remote repositories to use for the resolution of project dependencies.
     * 
     */
    @Parameter(defaultValue="${project.remoteProjectRepositories}",readonly=true)
    private List<RemoteRepository> projectRepos;
    
    /**
     * The project's remote repositories to use for the resolution of plugins and their dependencies.
     * 
     */
    @Parameter(defaultValue="${project.remotePluginRepositories}",readonly=true)
    private List<RemoteRepository> pluginRepos;
	
	@Parameter
	private int threadCount = 5;

	@Override
	public void postExecute() throws Exception {

		TestUtil testUtil = new TestUtil();
		testUtil.setBuildDir(buildDir);
		testUtil.setTestTarget(testTarget);
		testUtil.setLog(getLog());
		testUtil.setProject(project);
		testUtil.setPluginRepos(pluginRepos);
		testUtil.setProjectRepos(projectRepos);
		testUtil.setRepoSession(repoSession);
		testUtil.setRepoSystem(repoSystem);

		testUtil.init();

		if (!testUtil.hasTests()) {
			throw new SparkITCaseException("No tests found in [" + testUtil.getTestTarget() + "]");
		}

		// First create the test artifact
		// -- make a spark-itcase dir
		File outputDir = new File(buildDir.getAbsolutePath() + File.separator + "spark-itcase");
		getLog().info("About to delete [" + outputDir.getAbsolutePath() + "]");
		if (outputDir.exists()) {
			// FileUtils.deleteDirectory(outputDir);
		}
		outputDir.mkdir();

		// -- zip test classes to that dir
		File testJar = new File(
				outputDir.getAbsolutePath() + File.separator + project.getArtifactId() + "-spark-itcase.jar");
		compressZipfile(testUtil.getTestTarget(), testJar.getAbsolutePath());

		// Second, make a pom based on internal template with 2 dependencies
		StringBuilder dependencyText = new StringBuilder();

		String dependencyFrag = null;
		try (InputStream is = this.getClass().getResourceAsStream("/dependency_fragment.xml");
				ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			IOUtils.copy(is, out);
			dependencyFrag = new String(out.toByteArray());
		}

		// -- All test dependencies from this project (which will be runtime on
		// the new project).
		List<Dependency> dependencies = project.getDependencies();
		for (Dependency dependency : dependencies) {
			String groupId = dependency.getGroupId();
			String artifactId = dependency.getArtifactId();
			String version = dependency.getVersion();

			if (!dependency.getScope().equalsIgnoreCase("test")) {
				continue;
			} else {
				getLog().info("Found test dependency [" + groupId + ":" + artifactId + ":" + version + "]");
			}

			addDependency(dependencyText, dependencyFrag, groupId, artifactId, version);
		}

		// -- The project as a main dependency
		String groupId = project.getGroupId();
		String version = project.getVersion();
		addDependency(dependencyText, dependencyFrag, groupId, project.getArtifactId(), version);
		// -- The project containing the SparkSubmitTestWrapper
		// Add this plugin as a dependency so we get annotations and the
		// wrapper.
		addDependency(dependencyText, dependencyFrag, "solutions.deepfield", "spark-itcase-annotations",
				appUtil.getVersion());

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

		List<List<String>> listOfTests = new ArrayList<>();
		for (Class testClass : testUtil.getTestClasses()) {
			getLog().info("Examining test class [" + testClass + "]");
			// TODO Add inner method to run each annotated test method in class.
			// Invoke spark/run with SparkSubmitWrapper; main JAR is ???

			for (Method m : testClass.getMethods()) {
				getLog().info("Found method " + m.getName());
				// We have to do this in case the class loader has 2 copies of the class.
				//if (m.getAnnotation(SparkTest.class) != null) {
				if (findTestAnnotation(m)) {
					String methodName = m.getName();
					getLog().info("Found test method [" + methodName + "]");
					List<String> parameters = new ArrayList<>();
					parameters.add(testClass.getCanonicalName());
					parameters.add(methodName);
					listOfTests.add(parameters);
				} 
			}

		}
		
		// Now execute the tests.
		LinkedBlockingQueue<List<String>> queue = new LinkedBlockingQueue<>(listOfTests);
		queue.iterator();
		CountDownLatch latch = new CountDownLatch(threadCount);
		List<TestRunner> runners = new ArrayList<>();
		List<Thread> threads = new ArrayList<>();
		for (int i = 0; i < threadCount; i++) {
			TestRunner runner = new TestRunner(queue, latch, groupId, artifactId, version);
			runners.add(runner);
			threads.add(new Thread(runner));
		}

		getLog().info("About to execute [" + listOfTests.size() + "] spread over [" + threads.size() + "]");
		for (Thread t : threads) {
			t.start();
		}
		latch.await();
		Unirest.shutdown();

		// TODO Add in thread error logging.

	}

	public boolean findTestAnnotation(Method m) {
		for (Annotation a : m.getAnnotations()) {
			if (a.annotationType().getCanonicalName().equals(SparkTest.class.getCanonicalName())) {
				return true;
			}
		}
		return false;
	}
	
	private void addDependency(StringBuilder dependencyText, String dependencyFrag, String groupId, String artifactId,
			String version) throws Exception {
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
		ZipEntry entry = new ZipEntry("META-INF/MANIFEST.MF");
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
				ZipEntry entry = new ZipEntry(
						sourceDir.replace(rootDir + File.separator, "").replaceAll("\\\\", "/") + "/" + file.getName());
				out.putNextEntry(entry);

				FileInputStream in = new FileInputStream(sourceDir + File.separator + file.getName());
				IOUtils.copy(in, out);
				IOUtils.closeQuietly(in);
			}
		}
	}

	class TestRunner implements Runnable {

		private LinkedBlockingQueue<List<String>> queue;

		private CountDownLatch latch;

		private String groupId;

		private String artifactId;

		private String version;

		private boolean testHadError = false;

		public TestRunner(LinkedBlockingQueue<List<String>> queue, CountDownLatch latch, String groupId,
				String artifactId, String version) {
			super();
			this.queue = queue;
			this.latch = latch;
			this.groupId = groupId;
			this.artifactId = artifactId;
			this.version = version;
		}

		@Override
		public void run() {
			List<String> params = null;
			while(null != (params = queue.poll())) {
				try {
					runUtil.run(SparkSubmitTestWrapper.class.getCanonicalName(), params, artifactId, groupId, version);
				} catch (Exception e) {
					getLog().error("Error running [" + params + "] " + e.getMessage(), e);
					testHadError = true;
					break;
				}
			}
			latch.countDown();
		}

		public boolean isTestHadError() {
			return testHadError;
		}

	}
}