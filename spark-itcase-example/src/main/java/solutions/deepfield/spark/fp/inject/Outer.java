package solutions.deepfield.spark.fp.inject;

import java.util.Arrays;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import solutions.deepfield.spark.fp.ExampleProjectWrapper;

public class Outer {

	private static final Logger logger = LoggerFactory.getLogger(ExampleProjectWrapper.class);

	public static void main(String[] args) {
		logger.info("Lets parallelize and count");
		try {
			SparkConf config = new SparkConf();
			config.setAppName("first-project:outer-inner");

			try (JavaSparkContext context = new JavaSparkContext(config)) {
				Inner inner = new Inner();

				logger.info("First create the RDD");
				JavaRDD<String> lines = context.parallelize(Arrays.asList("pandas", "i like pandas", "phones"));

				logger.info("Context set on helper class");
				JavaRDD<String> filtered = inner.filter(lines);
				logger.info("Filtered RDD created");
				long lineCount = lines.count();
				logger.info("There were {} items in unfiltered RDD", lineCount);

				lineCount = filtered.count();
				logger.info("There were {} items in filtered RDD", lineCount);

			}
		} catch (Throwable t) {
			System.err.println("Error processing spark job: " + t.getMessage());
			t.printStackTrace();
		}

	}

}
