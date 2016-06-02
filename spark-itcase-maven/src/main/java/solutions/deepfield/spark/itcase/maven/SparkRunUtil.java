package solutions.deepfield.spark.itcase.maven;

import java.io.IOException;
import java.util.List;

import org.apache.maven.plugin.logging.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequestWithBody;
import com.mashape.unirest.request.body.RawBody;

import solutions.deepfield.spark.itcase.core.domain.RunParams;
import solutions.deepfield.spark.itcase.core.domain.RunResult;
import solutions.deepfield.spark.itcase.exceptions.SparkITCaseException;

public class SparkRunUtil {

	private String endpoint;
	
	private Log log;
	
	public void run(String jobClass, List<String> params1, String artifactId, String groupId, String version)
			throws JsonProcessingException, UnirestException, IOException {
		RunParams params = new RunParams();
		params.setArtifactId(artifactId);
		params.setGroupId(groupId);
		params.setVersion(version);
		params.setClassName(jobClass);
		params.setParameters(params1);

		ObjectMapper mapper = new ObjectMapper();
		byte[] dataToSend = mapper.writeValueAsBytes(params);

		HttpRequestWithBody requestWithBody = Unirest.post(endpoint + "/spark/run");
		requestWithBody = requestWithBody.header("Content-Type", "application/json");
		requestWithBody = requestWithBody.header("Accept", "application/json");
		RawBody rawBody = requestWithBody.body(dataToSend);
		log.info("About to make request to server");
		HttpResponse<String> response = rawBody.asString();

		List<String> content = response.getHeaders().get("Content-Type");
		if (content.size() == 1 && content.get(0).toLowerCase().startsWith("application/json")) {
			RunResult result = mapper.readerFor(RunResult.class).readValue(response.getBody());

			// TODO Add in logs.
			if (result.getLogs() != null && result.getLogs().size() > 0) {
				for (String resultLog : result.getLogs()) {
					log.info("Server log: " + resultLog);
				}
			}
		}

		if (response.getStatus() == 200) {
			log.info("Request to run complete: " + response.getBody());
		} else {
			content = response.getHeaders().get("Content-Type");
			if (content.size() == 1 && content.get(0).toLowerCase().startsWith("application/json")) {
				RunResult result = mapper.readerFor(RunResult.class).readValue(response.getBody());

				// TODO Add in logs.
				if (result.getLogs() != null && result.getLogs().size() > 0) {
					for (String resultLog : result.getLogs()) {
						log.error("Server log: " + resultLog);
					}
				}

				log.error("Error stack:\n" + result.getExceptionStack());
				throw new SparkITCaseException("Received error " + result.getExceptionMessage());
			} else {
				log.error("Unknown error payload content type: " + content);
				throw new SparkITCaseException("Received error " + response.getStatusText());
			}
		}
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public void setLog(Log log) {
		this.log = log;
	}

	
}
