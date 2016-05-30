# spark-itcase
Set of tools to run Spark programs remotely during development builds

![Alt text](methodology.png)

# Maven Plugin
Add the Maven plugin to your build like so:

~~~~
			<plugin>
				<groupId>solutions.deepfield</groupId>
				<artifactId>spark-itcase-maven</artifactId>
				<version>1.0.0</version>
				<executions>
					<execution>
						<goals>
							<goal>sparkItCase</goal>
						</goals>
					</execution>
				</executions>
				<configuration>
					<endpoint>http://YOUR_SERVER:10080/rest</endpoint>
					<!-- Proxy is optional -->
					<proxyHost>127.0.0.1</proxyHost>
					<proxyPort>3128</proxyPort>
					<classToRun>YOUR SPARK CLASS</classToRun>
				</configuration>
			</plugin>
~~~~
