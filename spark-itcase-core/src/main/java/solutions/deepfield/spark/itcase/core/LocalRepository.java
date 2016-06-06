package solutions.deepfield.spark.itcase.core;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.util.StringUtils;
import org.eclipse.aether.RepositoryException;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.CollectResult;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.graph.DefaultDependencyNode;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.installation.InstallRequest;
import org.eclipse.aether.installation.InstallationException;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactDescriptorException;
import org.eclipse.aether.resolution.ArtifactDescriptorRequest;
import org.eclipse.aether.resolution.ArtifactDescriptorResult;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import solutions.deepfield.spark.itcase.core.util.Booter;
import solutions.deepfield.spark.itcase.core.util.ConsoleTransferListener;
import solutions.deepfield.spark.itcase.core.util.ServletOutTransferListener;

public class LocalRepository {

	private static final Logger logger = LoggerFactory.getLogger(LocalRepository.class);

	RepositorySystem system;

	public void initialize() {
		system = Booter.newRepositorySystem();
	}
	
	/**
	 * Load a dependency from our Maven subsystem.
	 * 
	 * @param groupId
	 * @param artifactId
	 * @param version
	 */
	public void retrieveArtifact(String groupId, String artifactId, String version, OutputStream outStream) {
		ServletOutTransferListener listener = new ServletOutTransferListener(outStream, new ConsoleTransferListener());
		RepositorySystemSession session = Booter.newRepositorySystemSession(system, listener);

		Artifact artifact = new DefaultArtifact(groupId + ":" + artifactId + ":" + version);

		ArtifactRequest artifactRequest = new ArtifactRequest();
		artifactRequest.setArtifact(artifact);
		List<RemoteRepository> repositories = Booter.newRepositories(system, session);
		artifactRequest.setRepositories(repositories);

		ArtifactResult artifactResult;
		try {
			artifactResult = system.resolveArtifact(session, artifactRequest);
			artifact = artifactResult.getArtifact();
		} catch (ArtifactResolutionException e) {
			logger.error("Unable to retrieve artifact: " + e.getMessage(), e);
			return;
		}

		ArtifactDescriptorRequest descriptorRequest = new ArtifactDescriptorRequest();
		descriptorRequest.setArtifact(artifact);
		descriptorRequest.setRepositories(Booter.newRepositories(system, session));
		ArtifactDescriptorResult descriptorResult;
		try {
			descriptorResult = system.readArtifactDescriptor(session, descriptorRequest);
		} catch (ArtifactDescriptorException e) {
			logger.error("Unable to retrieve hierarchy: " + e.getMessage(), e);
			return;
		}

		CollectRequest request = new CollectRequest();
		request.setRootArtifact(artifact);
		request.setRepositories(repositories);
		request.setDependencies(descriptorResult.getDependencies());
		request.setManagedDependencies(descriptorResult.getManagedDependencies());

		try {
			system.collectDependencies(session, request);
		} catch (DependencyCollectionException e) {
			logger.error("Unable to retrieve dependencies: " + e.getMessage(), e);
		}

	}

	public void deployArtifact(String groupId, String artifactId, String version, InputStream jarInStream,
			InputStream pomInStream) throws InstallationException, IOException {
		RepositorySystemSession session = Booter.newRepositorySystemSession(system, null);

		File tmp = File.createTempFile("jar-deploy-tmp-", ".jar");
		try (FileOutputStream fout = new FileOutputStream(tmp)) {
			IOUtils.copy(jarInStream, fout);
		}

		File pomTmp = File.createTempFile("jar-deploy-tmp-", ".xml");
		try (FileOutputStream fout = new FileOutputStream(pomTmp)) {
			IOUtils.copy(pomInStream, fout);
		}

		Artifact jarArtifact = new DefaultArtifact(groupId, artifactId, "", "jar", version, null, tmp);

		Artifact pomArtifact = new DefaultArtifact(groupId, artifactId, "", "pom", version, null, pomTmp);

		try {
			InstallRequest installRequest = new InstallRequest();
			installRequest.addArtifact(jarArtifact).addArtifact(pomArtifact);
			system.install(session, installRequest);
		} finally {
			tmp.delete();
		}

	}

	public String buildFile(String groupId, String artifactId, String version) throws RepositoryException {
		RepositorySystemSession session = Booter.newRepositorySystemSession(system, null);

		Artifact artifact = new DefaultArtifact(groupId + ":" + artifactId + ":" + version);

		ArtifactRequest artifactRequest = new ArtifactRequest();
		artifactRequest.setArtifact(artifact);
		List<RemoteRepository> repositories = Booter.newRepositories(system, session);
		artifactRequest.setRepositories(repositories);

		ArtifactResult artifactResult = system.resolveArtifact(session, artifactRequest);
		artifact = artifactResult.getArtifact();
		return artifact.getFile().getAbsolutePath();
	}

	public List<String> buildFiles(String groupId, String artifactId, String version)
			throws RepositoryException, ArtifactDescriptorException {

		RepositorySystemSession session = Booter.newRepositorySystemSession(system, null);

		Artifact artifact = new DefaultArtifact(groupId + ":" + artifactId + ":" + version);

		ArtifactDescriptorRequest descriptorRequest = new ArtifactDescriptorRequest();
		descriptorRequest.setArtifact(artifact);
		descriptorRequest.setRepositories(Booter.newRepositories(system, session));
		ArtifactDescriptorResult descriptorResult = system.readArtifactDescriptor(session, descriptorRequest);

		CollectRequest request = new CollectRequest();
		request.setRootArtifact(artifact);
		// request.setRepositories(repositories);
		request.setDependencies(descriptorResult.getDependencies());
		request.setManagedDependencies(descriptorResult.getManagedDependencies());

		CollectResult result = system.collectDependencies(session, request);
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
				String scope = node.getDependency().getScope();
				return StringUtils.isBlank(scope)
						|| (!scope.equalsIgnoreCase("provided") && !scope.equalsIgnoreCase("test"));
			}
		});

		// artifactRequest.setCollectRequest(request);
		DependencyResult dependencyResult = system.resolveDependencies(session, artifactRequest);

		for (ArtifactResult artifactResult : dependencyResult.getArtifactResults()) {
			

			resultPaths.add(artifactResult.getArtifact().getFile().getAbsolutePath());
		}

		return resultPaths;
	}

}
