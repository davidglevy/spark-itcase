package solutions.deepfield.spark.fp.inject;

import org.apache.spark.api.java.JavaRDD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Inner {

	private static final Logger logger = LoggerFactory.getLogger(Inner.class);
	
	public JavaRDD<String> filter(JavaRDD<String> lines) {
		return lines.filter(new FilterPandas());
	}
	
}
