package levy.david.spark.itcase.web.domain;

import org.springframework.web.multipart.MultipartFile;

public class DeployParams extends LoadParams {

	MultipartFile artifact;

	public MultipartFile getArtifact() {
		return artifact;
	}

	public void setArtifact(MultipartFile artifact) {
		this.artifact = artifact;
	}
	
	
}
