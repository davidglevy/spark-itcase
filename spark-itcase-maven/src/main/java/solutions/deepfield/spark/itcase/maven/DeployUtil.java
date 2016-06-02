package solutions.deepfield.spark.itcase.maven;

import java.io.File;

import org.apache.maven.plugin.logging.Log;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequestWithBody;
import com.mashape.unirest.request.body.MultipartBody;

public class DeployUtil {

	private Log log;
	
	private String endpoint;
	
	public void deploy(File jar, String groupId, String artifactId, String version, File pom) throws UnirestException {
		// Install the artifact in the remote server.
		log.info("Deploying project artifact");

		HttpRequestWithBody body = Unirest.post(endpoint + "/artifact/deploy");

		MultipartBody multiPartBody = body.field("artifact",
				jar);
		multiPartBody.field("artifactId", artifactId);
		multiPartBody.field("groupId", groupId);
		multiPartBody.field("version", version);
		multiPartBody = multiPartBody.field("pom", pom);

		log.info("Found http response:\n" + multiPartBody.asString().getBody());

	}

	public void setLog(Log log) {
		this.log = log;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	
}
