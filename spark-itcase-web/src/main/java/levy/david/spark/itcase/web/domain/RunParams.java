package levy.david.spark.itcase.web.domain;

import java.util.List;
import java.util.Map;

public class RunParams extends LoadParams {

	private String className;

	private List<String> parameters;

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public List<String> getParameters() {
		return parameters;
	}

	public void setParameters(List<String> parameters) {
		this.parameters = parameters;
	}

	@Override
	public String toString() {
		return "RunParams [className=" + className + ", parameters=" + parameters + ", groupId=" + getGroupId()
				+ ", artifactId=" + getArtifactId() + ", version=" + getVersion() + "]";
	}

}
