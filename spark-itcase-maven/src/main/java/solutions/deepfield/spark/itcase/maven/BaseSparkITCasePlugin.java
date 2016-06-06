package solutions.deepfield.spark.itcase.maven;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

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

public class BaseSparkITCasePlugin extends AbstractMojo{

	/**
	 * The Maven Project.
	 *
	 */
	@Parameter(property = "project")
	protected MavenProject project = null;

	@Parameter( defaultValue = "${session}", readonly = true )
    protected MavenSession session;
	
	// @Parameter(property = "spark.itcase.endpoint")
	@Parameter
	protected String endpoint;

	@Parameter
	protected String proxyHost;

	@Parameter
	protected int proxyPort;

	@Parameter(property = "project.build.directory")
	protected File buildDir;

	@Parameter(property = "project.basedir")
	protected File projectBase;

	@Parameter(property = "project.build.finalName")
	protected String finalName;

	protected DeployUtil deployUtil;
	
	protected AppUtil appUtil;

	protected SparkRunUtil runUtil;
	
	protected void init() {
		appUtil = new AppUtil();
		appUtil.loadProperties();
		getLog().info("Plugin version is [" + appUtil.getVersion() + "]");
		getLog().info("Plugin build is [" + appUtil.getTimestamp() + "]");
		
		
		
		deployUtil = new DeployUtil();
		deployUtil.setEndpoint(endpoint);
		deployUtil.setLog(getLog());
		
		runUtil = new SparkRunUtil();
		runUtil.setEndpoint(endpoint);
		runUtil.setLog(getLog());
		
	}

	public void preExecute() throws Exception {
		
	}
	
	public void postExecute() throws Exception {
		
	}
	
	public void execute() throws MojoExecutionException {

		try {

			preExecute();
			
			if (StringUtils.isNotBlank(proxyHost)) {
				Unirest.setProxy(new HttpHost(proxyHost, proxyPort));
			}
			
			init();

			checkStatus();

			
			File jar = new File(buildDir.getAbsolutePath() + File.separator + finalName + ".jar");
			String artifactId = project.getArtifactId();
			String groupId = project.getGroupId();
			String version = project.getVersion();

			File pom = new File(projectBase.getAbsoluteFile() + File.separator + "pom.xml");

			
			deployUtil.deploy(jar, groupId, artifactId, version, pom);

			List<Dependency> dependencies = project.getDependencies();

			for (Dependency dependency : dependencies) {
				if (dependency.getScope().equalsIgnoreCase("provided") || dependency.getScope().equalsIgnoreCase("test")) {
					getLog().info("Skipping [" + dependency.getGroupId() + ":" + dependency.getArtifactId() + ":"
							+ dependency.getVersion() + "]");
					continue;
				} else {
					getLog().info("Found dependency [" + dependency.getGroupId() + ":" + dependency.getArtifactId()
							+ ":" + dependency.getVersion() + "]");
				}

				Map<String, Object> parameters2 = new HashMap<String, Object>();
				parameters2.put("artifactId", dependency.getArtifactId());
				parameters2.put("groupId", dependency.getGroupId());
				parameters2.put("version", dependency.getVersion());

				HttpResponse<String> resultDep = Unirest.get(endpoint + "/artifact/retrieve").queryString(parameters2)
						.asString();
				getLog().info("Found http response:\n" + resultDep.getBody());

			}

			
			postExecute();
			

		} catch (Exception e) {
			getLog().error(e);
			throw new MojoExecutionException(
					"Error invoking spark itcase server at [" + endpoint + "]:" + e.getMessage(), e);
		}

	}




	/**
	 * Perform a status check on the server.
	 * 
	 * @throws Exception
	 *             if the status was not HTTP 200 and a matched version.
	 */
	protected void checkStatus() throws Exception {
		GetRequest request = Unirest.get(endpoint + "/status");
		request = request.header("Content-Type", "application/json");
		request = request.header("Accept", "application/json");
		HttpResponse<String> response = request.asString();
		if (response.getStatus() == 200) {
			ObjectMapper mapper = new ObjectMapper();
			ApplicationStatusResponse status = mapper.readerFor(ApplicationStatusResponse.class)
					.readValue(response.getBody());
			if (status.getVersion().equals(appUtil.getVersion())) {
				getLog().info("Matched client-server version [" + status.getVersion() + "]");
			} else {
				throw new SparkITCaseException("Server version [" + status.getVersion() + "] does not match client ["
						+ appUtil.getVersion() + "]");
			}

			
			getLog().info("Server running as user [" + status.getUser() + "]");
			getLog().info("Server uptime [" + status.getUptime() + "]");
		} else {
			throw new SparkITCaseException("Unable to retrieve status, received [" + response.getStatus()
					+ "], message [" + response.getStatusText() + "]");
		}
	}

	
}
