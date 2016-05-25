# spark-itcase
Set of tools to run Spark programs remotely during development builds

# Maven Plugin
Add the Maven plugin to your build like so:

~~~~
			<plugin>
				<groupId>levy.david.spark</groupId>
				<artifactId>spark-itcase-maven</artifactId>
				<version>1.0.0</version>
				<executions>
					<execution>
						<phase>integration-test</phase>
						<goals>
							<goal>sayhi</goal>
						</goals>
					</execution>
				</executions>
				<configuration>
					<endpoint>http://toot-wk-1.lab1.com:10080/rest</endpoint>
					<!-- Proxy is optional -->
					<proxyHost>127.0.0.1</proxyHost>
					<proxyPort>3128</proxyPort>
					<classToRun>levy.david.spark.fp.ExampleProjectWrapper</classToRun>
				</configuration>
			</plugin>
~~~~
