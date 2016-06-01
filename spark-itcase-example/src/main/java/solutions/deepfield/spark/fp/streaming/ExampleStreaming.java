package solutions.deepfield.spark.fp.streaming;

import java.util.Arrays;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scala.Tuple2;
import solutions.deepfield.spark.fp.parallelize.Parallelize;

public class ExampleStreaming {

	private static final Logger logger = LoggerFactory.getLogger(Parallelize.class);
	
	private static boolean runningInTest = true;
	
	private static int secondsToRun = 10;
	
	public static void main(String[] args) {
		logger.info("About to run spark streaming job");
		try {
			// Create a local StreamingContext with two working thread and batch interval of 1 second
			SparkConf config = new SparkConf();
			config.setAppName("first-project:streaming");
			config.set("spark.streaming.stopGracefullyOnShutdown", "true");
			
			logger.info("Setting up streaming context");
			try (JavaStreamingContext jssc = new JavaStreamingContext(config, Durations.seconds(1))) {

				logger.info("Set up streaming context");
				
				logger.info("About to attempt to open text socket");
				// Create a DStream that will connect to hostname:port, like *:9999
				JavaReceiverInputDStream<String> lines = jssc.socketTextStream("localhost", 9999);
				
				logger.info("Going to try read words from the socket");
				
				// Split each line into words
				JavaDStream<String> words = lines.flatMap(
				  new FlatMapFunction<String, String>() {
				    @Override public Iterable<String> call(String x) {
				      return Arrays.asList(x.split(" "));
				    }
				  });
				
				
				logger.info("About to map words to pairs");
				
				// Count each word in each batch
				JavaPairDStream<String, Integer> pairs = words.mapToPair(
				  new PairFunction<String, String, Integer>() {
				    @Override public Tuple2<String, Integer> call(String s) {
				      return new Tuple2<String, Integer>(s, 1);
				    }
				  });
				
				logger.info("About to do word counts");
				
				JavaPairDStream<String, Integer> wordCounts = pairs.reduceByKey(
				  new Function2<Integer, Integer, Integer>() {
				    @Override public Integer call(Integer i1, Integer i2) {
				      return i1 + i2;
				    }
				  });

				logger.info("Doing the word count");
				
				// Print the first ten elements of each RDD generated in this DStream to the console
				wordCounts.print();
				
				
				jssc.start();
				
				if (runningInTest) {
					logger.info("About to sleep");
					Thread.currentThread().sleep(secondsToRun * 1000);
					logger.info("Waking up");
					jssc.stop(true, true);
					logger.info("Shutting context down");
				} else {
					jssc.awaitTermination();
				}
			}
		} catch (Throwable t) {
			System.err.println("Error processing spark job: " + t.getMessage());
			t.printStackTrace();
		}

		
	}
	
}
