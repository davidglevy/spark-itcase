package solutions.deepfield.spark.itcase.maven;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

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

/**
 * Loads the dependencies for the project into the target environment.
 */
@Mojo(name = "runJobs", defaultPhase = LifecyclePhase.INTEGRATION_TEST)
public class RunJobsPlugin extends BaseSparkITCasePlugin {

	@Parameter
	protected String classToRun;
	
	
	
	@Override
	public void postExecute() throws Exception {
		super.postExecute();
		
		String jobClass = classToRun;
		List<String> params1 = new ArrayList<String>();
		
		String artifactId = project.getArtifactId();
		String groupId = project.getGroupId();
		String version = project.getVersion();
		
		runUtil.run(jobClass, params1, artifactId, groupId, version);

	}


	
}