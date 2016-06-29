# spark-itcase
Allow developers to run suites of integration tests against Spark Jobs as part of their Maven build cycle and integration builds on build servers. 

# Use Cases
The code here will satisfy the following use cases:
* Developers writing suites of integration tests for a job
* Developers writing code on Windows based IDE's
* Developers writing code remotely from the cluster

# Goals
The spark-itcase project has the following high level goals:
* Reduce build cycle time for developers writing Java or Scala based Spark jobs built with Maven
* Reduce manual tasks required when running Spark jobs (transferring files, creating spark-submit commands).
* Improve code quality by testing on a full cluster earlier in the development lifecycle.
* Allow developers to use a more familar annotated test lifecycle with Before/Test/After phases.
* Execute parallel spark-submit calls to reduce test cycle time.

# How Does it Achieve it's Goals?
* Runs the code during the Maven integration-test phase pre-commit 
* Minimize file transfer by only pushing the project artifact (without dependencies) to the server
* Automatically construct a spark submit invocation with all dependencies
* Test classes are identified (@SparkITTest annotation on type) and methods are found (@SparkTest) and executed.

# Methodology
The following diagram illustrates how the maven plugin interacts with the server module to deploy the artifact, retrieve dependencies then run the job remotely.

![Alt text](methodology.png)

# Installation

## 1.) Server Installation
* Download the binary server from Maven (http://search.maven.org/remotecontent?filepath=solutions/deepfield/spark-itcase-web/1.0.0/spark-itcase-web-1.0.0-bin.zip)
* Unzip the file
* Modify the config in the "conf" directory as required
* Start the server (spark-itcase/bin/spark-itcase-server)

## 2.) Maven Build Plugin
Add the Maven plugin to your build (pom.xml):

~~~~
			<plugin>
				<groupId>solutions.deepfield</groupId>
				<artifactId>spark-itcase-maven</artifactId>
				<version>1.0.0</version>
				<executions>
					<execution>
						<goals>
							<goal>runTests</goal>
						</goals>
					</execution>
				</executions>
				<configuration>
					<endpoint>http://YOUR_SERVER:10080/rest</endpoint>
					<!-- Proxy is optional -->
					<proxyHost>127.0.0.1</proxyHost>
					<proxyPort>3128</proxyPort>
				</configuration>
			</plugin>
~~~~

## 3.) Add Test annotation dependency
Also add the following dependency to utilise the required annotations:
~~~~
		<dependency>
			<groupId>solutions.deepfield</groupId>
			<artifactId>spark-itcase-annotations</artifactId>
			<version>1.0.3</version>
		</dependency>
~~~~
## 4.) Add an annotated test class
Create an integration test class like so - the test method should invoke the static main on your target job.
~~~~
@SparkITCase
public class OuterITCase {

	private static final Logger logger = LoggerFactory.getLogger(OuterITCase.class);
	
	@SparkBefore
	public void setupData() {
		logger.info("Do some HDFS data generation or pre-test clean up");
	}
	
	@SparkTest
	public void testMainNoArgs() {
		logger.info("Testing main method without any arguments");
		Outer.main(new String[] {});
		
		// Add assertions in here.
	}
	
	@SparkTest
	public void testMainSingleArg() {
		logger.info("Testing main method with an argument");
		Outer.main(new String[] { "Example Argument"});
		
		// Add assertions in here.
	}
	
	@SparkAfter
	public void tearDown() {
		logger.info("Do some HDFS data generation or post-test cleanup");
	}
}


~~~~
## 5.) Run Your Build
The "integration-test" phase will run as part of a standard "mvn clean install" command. 
