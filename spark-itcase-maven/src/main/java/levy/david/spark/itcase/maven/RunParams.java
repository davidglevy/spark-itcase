package levy.david.spark.itcase.maven;

import java.util.List;
import java.util.Map;

public class RunParams extends LoadParams {
	
	private String className;
	
	private List<String> parameters;

	@Override
	public String toString() {
		return "RunParams [className=" + className + ", parameters="
				+ parameters + ", groupId=" + getGroupId()
				+ ", artifactId=" + getArtifactId() + ", version="
				+ getVersion() + "]";
	}
	
	
	
}
