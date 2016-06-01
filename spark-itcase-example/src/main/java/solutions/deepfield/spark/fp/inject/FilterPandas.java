package solutions.deepfield.spark.fp.inject;

import org.apache.spark.api.java.function.Function;

public class FilterPandas implements Function<String, Boolean> {

	private static final long serialVersionUID = 1L;

	@Override
	public Boolean call(String v1) throws Exception {
		return v1.contains("pandas");
	}

}
