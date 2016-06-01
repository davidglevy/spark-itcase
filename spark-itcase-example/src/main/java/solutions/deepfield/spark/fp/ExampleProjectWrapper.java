package solutions.deepfield.spark.fp;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scala.Tuple2;

public class ExampleProjectWrapper {

	private static final Logger logger = LoggerFactory.getLogger(ExampleProjectWrapper.class);

	@SuppressWarnings("serial")
	public static void main(String[] args) {
		logger.info("Here 1");
		try {
			// SparkConf config = new SparkConf();
			// config.setAppName("example-project");
			// logger.info("Here 2");
			// try (JavaSparkContext context = new JavaSparkContext(config)) {
			//
			// ExampleProject p = new ExampleProject(context);
			// p.run();
			//
			// }

			SparkConf config = new SparkConf();
			config.setAppName("example-project");

			try (JavaSparkContext context = new JavaSparkContext(config)) {
				JavaRDD<String> textFile = context.textFile("example_data.txt");
				// FlatMapFunction<String, String> = new FlatMapFunction<>
				logger.info("Here 3");

				JavaRDD<String> words = textFile.flatMap(new FlatMapFunction<String, String>() {

					@Override
					public Iterable<String> call(String s) {
						return Arrays.asList(s.split(" "));
					}
				});
				logger.info("Here 4");

				JavaPairRDD<String, Integer> pairs = words.mapToPair(new PairFunction<String, String, Integer>() {
					public Tuple2<String, Integer> call(String s) {
						return new Tuple2<String, Integer>(s, 1);
					}
				});
				JavaPairRDD<String, Integer> counts = pairs.reduceByKey(new Function2<Integer, Integer, Integer>() {
					public Integer call(Integer a, Integer b) {
						return a + b;
					}
				});
				logger.info("Here 5");
				SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");

				String outputFolder = "example_out_" + format.format(new Date());

				counts.saveAsTextFile(outputFolder);
				logger.info("Here 6");

			}
		} catch (Throwable t) {
			System.err.println("Error processing spark job: " + t.getMessage());
			t.printStackTrace();
		}
	}
}
