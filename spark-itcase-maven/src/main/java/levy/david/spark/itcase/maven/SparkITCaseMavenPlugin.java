package levy.david.spark.itcase.maven;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.HttpRequestWithBody;
import com.mashape.unirest.request.body.MultipartBody;
import com.mashape.unirest.request.body.RawBody;

/**
 * Loads the dependencies for the project into the target environment.
 */
@Mojo(name = "sayhi")
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
	
	
	public void execute() throws MojoExecutionException {

		try {

			if (StringUtils.isNotBlank(proxyHost)) {
				Unirest.setProxy(new HttpHost(proxyHost, proxyPort));
			}
			
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
			getLog().info("Request to run complete: " + response.getBody());

			
			// String requestBody = new

		} catch (Exception e) {
			getLog().error(e);
			throw new MojoExecutionException(
					"Unable to check version on test server for endpoint ["
							+ endpoint + "]:" + e.getMessage(), e);
		}

	}
}