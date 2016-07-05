package solutions.deepfield.spark.itcase.maven;

import java.io.File;
import java.lang.annotation.Annotation;
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
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositoryException;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.CollectResult;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactDescriptorException;
import org.eclipse.aether.resolution.ArtifactDescriptorRequest;
import org.eclipse.aether.resolution.ArtifactDescriptorResult;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResult;

import solutions.deepfield.spark.itcase.annotations.SparkITCase;
import solutions.deepfield.spark.itcase.core.util.Booter;

public class TestUtil {

	private Log log;
	
	private String testTarget;
	
	private File buildDir;
	
	private List<Class> testClasses;
	
    private RepositorySystem repoSystem;
    
    private RepositorySystemSession repoSession;
    
    private List<RemoteRepository> projectRepos;
    
    private List<RemoteRepository> pluginRepos;
	
    private MavenProject project;
    
    private URLClassLoader classLoader;
    
	public void init() throws Exception {
		String classDir = buildDir + File.separator + "classes";
		
		if (testTarget == null) {
			testTarget = buildDir + File.separator + "test-classes";
		}
		log.info("Target directory is [" + testTarget + "]");
		
		List<String> files = buildFiles(project.getGroupId(), project.getArtifactId(), project.getVersion());
		log.info("Found deps [" + files.size() + "]");
		
		List<URL> urls = new ArrayList<URL>();
		for (String filePath : files) {
			urls.add(loadUrl(filePath));
		}
		urls.add(loadUrl(classDir));
		urls.add(loadUrl(testTarget));
		
		
		URL[] urlArray = urls.toArray(new URL[urls.size()]);
		classLoader = new URLClassLoader(urlArray, TestUtil.class.getClassLoader());
		//classLoader.loadClass("solutions.deepfield.shs.spark.facade.BinaryFileIngestor");
		
		testClasses = findTestClasses();
		if (testClasses.isEmpty()) {
			log.info("No test classes found");
		} else {
			log.info("Found [" + testClasses.size() + "] test classes");
		}
	}

	private URL loadUrl(String filePath) throws MalformedURLException {
		File classPathFile = new File(filePath);
		if (classPathFile.exists()) {
			log.info("Found class path file: " + classPathFile.getAbsolutePath());
		} else {
			throw new RuntimeException("Unable to load classpath: " + classPathFile.getAbsolutePath());
		}
		URL url = classPathFile.toURL();
		log.info("Found classpath entry " + url + "\n");
		return url;
	}
	
	public List<String> buildFiles(String groupId, String artifactId, String version)
			throws RepositoryException, ArtifactDescriptorException {

		Artifact artifact = new DefaultArtifact(groupId + ":" + artifactId + ":" + version);

		ArtifactDescriptorRequest descriptorRequest = new ArtifactDescriptorRequest();
		descriptorRequest.setArtifact(artifact);
		descriptorRequest.setRepositories(Booter.newRepositories(repoSystem, repoSession));
		ArtifactDescriptorResult descriptorResult = repoSystem.readArtifactDescriptor(repoSession, descriptorRequest);

		CollectRequest request = new CollectRequest();
		request.setRootArtifact(artifact);
		// request.setRepositories(repositories);
		request.setDependencies(descriptorResult.getDependencies());
		request.setManagedDependencies(descriptorResult.getManagedDependencies());

		CollectResult result = repoSystem.collectDependencies(repoSession, request);
		List<String> resultPaths = new ArrayList<String>();

		// Dependency dependency = new Dependency(artifact,"compile");//,
		// "runtime");
		// DefaultDependencyNode root = new DefaultDependencyNode(dependency);
		DependencyRequest artifactRequest = new DependencyRequest(result.getRoot(), new DependencyFilter() {

			@Override
			public boolean accept(DependencyNode node, List<DependencyNode> parents) {
				if (node.getDependency() == null) {
					return true;
				}
				//String scope = node.getDependency().getScope();
				//return StringUtils.isBlank(scope)
				//		|| (!scope.equalsIgnoreCase("provided") && !scope.equalsIgnoreCase("test"));
				return true;
			}
		});

		// artifactRequest.setCollectRequest(request);
		DependencyResult dependencyResult = repoSystem.resolveDependencies(repoSession, artifactRequest);

		for (ArtifactResult artifactResult : dependencyResult.getArtifactResults()) {
			resultPaths.add(artifactResult.getArtifact().getFile().getAbsolutePath());
		}

		return resultPaths;
	}

	
	public boolean hasTests() {
		return testClasses.size() > 0;
	}
	
	private List<Class> findTestClasses() throws MalformedURLException, ClassNotFoundException {

		File file = new File(testTarget);

		// convert the file to URL format
		//URL url = file.toURI().toURL();
		//URL[] urls = new URL[] { url };

		// load this folder into Class loader
		//ClassLoader cl = new URLClassLoader(urls, classLoader);

		Collection<File> classFiles = listFiles(file);
		
		List<Class> testClasses = new ArrayList<>();
		
		for (File classFile : classFiles) {
			log.info("############################ BEGIN ##########################");
			String path = classFile.getAbsolutePath();
			log.info("Found [" + path + "]");
			if (!path.endsWith(".class")) {
				continue;
			} else if (path.indexOf("$") != -1) {
				// Skip generated inner classes.
				continue;
			}
			
			String shortName = path.substring(testTarget.length() + 1, path.length() - 6);
			String[] parts = StringUtils.split(shortName, File.separator);
			String className = StringUtils.join(parts, ".");
			log.info("Found potential test class [" + className + "]");
			// load the Address class in 'c:\\other_classes\\'
			Class cls = classLoader.loadClass(className);
			log.info("Class loaded is [" + cls.getCanonicalName() + "]");
			
			
			boolean found = false;
			for (Annotation a : cls.getAnnotations()) {
				log.info("Found annotation [" + a.annotationType().getCanonicalName() + "]");
				if (a.annotationType().equals(SparkITCase.class)) {
					found = true;
					break;
				}
			}
			
			if (found) {
				log.info("Found test annotation on [" + cls.getCanonicalName() + "]");
				testClasses.add(cls);
			} else {
				log.info("Could not find test annotation on [" + cls.getCanonicalName() + "]");
			}
			
			log.info("Finish cls [" + cls.getCanonicalName() + "]\n\n\n");
		}
		
		return testClasses;
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

	public void setRepoSystem(RepositorySystem repoSystem) {
		this.repoSystem = repoSystem;
	}

	public void setRepoSession(RepositorySystemSession repoSession) {
		this.repoSession = repoSession;
	}

	public void setProjectRepos(List<RemoteRepository> projectRepos) {
		this.projectRepos = projectRepos;
	}

	public void setPluginRepos(List<RemoteRepository> pluginRepos) {
		this.pluginRepos = pluginRepos;
	}

	public void setProject(MavenProject project) {
		this.project = project;
	}

}
