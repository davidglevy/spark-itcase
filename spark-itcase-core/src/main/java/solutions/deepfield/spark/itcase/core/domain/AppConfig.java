package solutions.deepfield.spark.itcase.core.domain;

public class AppConfig {

	private long port = 10080;
	
	private String sparkCommand;

	public long getPort() {
		return port;
	}

	public void setPort(long port) {
		this.port = port;
	}

	public String getSparkCommand() {
		return sparkCommand;
	}

	public void setSparkCommand(String sparkCommand) {
		this.sparkCommand = sparkCommand;
	}
	
	
	
}
