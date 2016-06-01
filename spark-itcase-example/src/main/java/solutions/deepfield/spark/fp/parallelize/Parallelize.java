package solutions.deepfield.spark.fp.parallelize;

import java.util.Arrays;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import solutions.deepfield.spark.fp.ExampleProjectWrapper;

public class Parallelize {

	private static final Logger logger = LoggerFactory.getLogger(Parallelize.class);
	
	public static void main(String[] args) {
		logger.info("Lets parallelize and count");
		try {
			SparkConf config = new SparkConf();
			config.setAppName("first-project:parallelize");

			try (JavaSparkContext context = new JavaSparkContext(config)) {
				logger.info("First create the RDD");
				JavaRDD<String> lines = context.parallelize(Arrays.asList("pandas", "i like pandas"));
				logger.info("RDD Created");
				long lineCount = lines.count();
				
				logger.info("There were {} items in RDD", lineCount);
			}
		} catch (Throwable t) {
			System.err.println("Error processing spark job: " + t.getMessage());
			t.printStackTrace();
		}
		
		
	}
	
}
