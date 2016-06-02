package solutions.deepfield.spark.itcase.maven;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.logging.Log;

import solutions.deepfield.spark.itcase.annotations.SparkITCase;

public class TestUtil {

	private Log log;
	
	private String testTarget;
	
	private File buildDir;
	
	private List<Class> testClasses;
	
	public void init() throws MalformedURLException, ClassNotFoundException {
		if (testTarget == null) {
			testTarget = buildDir + File.separator + "test-classes";
		}
		log.info("Target directory is [" + testTarget + "]");
		
		testClasses = findTestClasses();
		if (testClasses.isEmpty()) {
			log.info("No test classes found");
		} else {
			log.info("Found [" + testClasses.size() + "] test classes");
		}
	}
	
	public boolean hasTests() {
		return testClasses.size() > 0;
	}
	
	private List<Class> findTestClasses() throws MalformedURLException, ClassNotFoundException {

		File file = new File(testTarget);

		// convert the file to URL format
		URL url = file.toURI().toURL();
		URL[] urls = new URL[] { url };

		// load this folder into Class loader
		ClassLoader cl = new URLClassLoader(urls, this.getClass().getClassLoader());

		Collection<File> classFiles = listFiles(file);
		
		List<Class> testClasses = new ArrayList<>();
		
		for (File classFile : classFiles) {
			String path = classFile.getAbsolutePath();
			log.info("Found [" + path + "]");
			if (!path.endsWith(".class")) {
				continue;
			}
			
			String shortName = path.substring(testTarget.length() + 1, path.length() - 6);
			String[] parts = StringUtils.split(shortName, File.separator);
			String className = StringUtils.join(parts, ".");
			log.info("Found test class [" + className + "]");
			// load the Address class in 'c:\\other_classes\\'
			Class cls = cl.loadClass(className);
			log.info("Class loaded is [" + cls.getCanonicalName() + "]");
			
			if (cls.getAnnotation(SparkITCase.class) != null) {
				log.info("Could not find test annotation on [" + cls.getCanonicalName() + "]");
				
				testClasses.add(cls);
			} else {
				log.info("Found test annotation on [" + cls.getCanonicalName() + "]");
			}
			
		}
		
		return testClasses;
	}
	
	
	public void distribute() {
		// TODO Implement this after lunch today.
		
	}
	
	
	public Collection<File> listFiles(File input) {
		return FileUtils.listFiles(input, new IOFileFilter() {

			@Override
			public boolean accept(File file) {
				return true;
			}

			@Override
			public boolean accept(File dir, String name) {
				return true;
			}
			
		}, new IOFileFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return true;
			}
			
			@Override
			public boolean accept(File file) {
				return true;
			}
		});
	}

	public void setLog(Log log) {
		this.log = log;
	}

	public void setTestTarget(String testTarget) {
		this.testTarget = testTarget;
	}

	public void setBuildDir(File buildDir) {
		this.buildDir = buildDir;
	}

	public String getTestTarget() {
		return testTarget;
	}

	public List<Class> getTestClasses() {
		return testClasses;
	}



	
	
}
