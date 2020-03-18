package io.quarkus.panache.maven;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.spi.ConfigProviderResolver;

import io.quarkus.bootstrap.app.AdditionalDependency;
import io.quarkus.bootstrap.app.QuarkusBootstrap;
import io.quarkus.runtime.configuration.ConfigUtils;
import io.quarkus.runtime.configuration.QuarkusConfigFactory;
import io.smallrye.config.PropertiesConfigSource;
import io.smallrye.config.SmallRyeConfig;

@Mojo(name = "generate-panache-entities", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class GeneratePanacheEntitiesMojo extends AbstractMojo {
	
    @Parameter(readonly = true, required = true, defaultValue = "${project.build.outputDirectory}")
    private File outputDirectory;

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Component
    private RepositorySystem repoSystem;

    @Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
    private RepositorySystemSession repoSession;

    @Parameter(defaultValue = "${project.remoteProjectRepositories}", readonly = true, required = true)
    private List<RemoteRepository> repos;

    @Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			System.out.println(
					"**** Start generating Panache entities for project " + 
					project.getName() + 
					" ****");
//			createBootstrapBuilder();
	        createBootstrapBuilder()
    				.build()
    				.bootstrap()
    				.runInAugmentClassLoader(
    						PanacheEntitiesGeneratorMain.class.getName(), 
    						Collections.emptyMap());
			System.out.println(
					"**** End generating Panache entities for project " + 
					project.getName() + 
					" ****");
		} catch (Exception e) {
			throw new MojoExecutionException(
					"Failed to generate Panache entities", e);
		}
	}	
	
	public void testApplicationProperties() {
        for (Resource resource : project.getResources()) {
        	Path config = Paths
        			.get(resource.getDirectory())
        			.resolve("application.properties");
            if (Files.exists(config)) {
                try {
                    SmallRyeConfig built = ConfigUtils
                    		.configBuilder(false)
                            .withSources(new PropertiesConfigSource(config.toUri().toURL()))
                            .build();
                    QuarkusConfigFactory.setConfig(built);
                    final ConfigProviderResolver cpr = ConfigProviderResolver.instance();
                    final Config existing = cpr.getConfig();
                    if (existing != built) {
                        cpr.releaseConfig(existing);
                        // subsequent calls will get the new config
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
		Optional<String> blah = ConfigProvider
				.getConfig()
				.getOptionalValue("blah", String.class);
		if (blah.isPresent())
			System.out.println("**** " + blah.get() + " ****");		
	}
	
	private QuarkusBootstrap.Builder createBootstrapBuilder() {
		QuarkusBootstrap.Builder result = QuarkusBootstrap
				.builder(outputDirectory.toPath())
				.setProjectRoot(project.getBasedir().toPath())
				.addAdditionalDeploymentArchive(createPathToThisArchive());
//		addAdditionalApplicationArchives(result);
		return result;
	}
	
	private Path createPathToThisArchive() {
        Path path = null;
		try {
			URL thisArchive = getClass().getResource(
					GeneratePanacheEntitiesMojo.class.getSimpleName() + ".class");
	        int endIndex = thisArchive.getPath().indexOf("!");
	        if (endIndex != -1) {
	            path = Paths.get(new URI(thisArchive.getPath().substring(0, endIndex)));
	        } else {
	            path = Paths.get(thisArchive.toURI());
	            path = path.getParent();
	            for (char i : GeneratePanacheEntitiesMojo.class.getName().toCharArray()) {
	                if (i == '.') {
	                    path = path.getParent();
	                }
	            }
	        }
		}
        catch (URISyntaxException e) {
        	throw new RuntimeException(e);
        }
		return path;
	}
	
    private Plugin lookupPanacheMavenPlugin() {
        for (Plugin plugin : project.getBuildPlugins()) {
            if (plugin.getGroupId().equals("io.quarkus")
                    && plugin.getArtifactId().equals("panache-maven-plugin")) {
                for (PluginExecution pluginExecution : plugin.getExecutions()) {
                    if (pluginExecution.getGoals().contains("generate-panache-entities")) {
                        return plugin;
                    }
                }
            }
        }
        return null;
    }
    
    private void addAdditionalApplicationArchives(QuarkusBootstrap.Builder builder) {
        for (ArtifactResult artifactResult : resolveDependencies().getArtifactResults()) { 	
            final Path path = artifactResult
            		.getArtifact()
            		.getFile()
            		.toPath()
            		.toAbsolutePath();
        	builder.addAdditionalApplicationArchive(
        			new AdditionalDependency(path, false, false));
        	System.out.println(path);
        }
    }
    
    private DependencyResult resolveDependencies() {
        try {
			return repoSystem.resolveDependencies(
					repoSession,
			        getPanachePluginJarDependencyRequest());
		} catch (DependencyResolutionException e) {
			throw new RuntimeException(e);
		}
    }
    
    private DependencyRequest getPanachePluginJarDependencyRequest() {
        return new DependencyRequest().setCollectRequest(
        		new CollectRequest()
                        .setRoot(new org.eclipse.aether.graph.Dependency(
                        		getPanacheMavenPluginJar(), 
                        		JavaScopes.RUNTIME))
                        .setRepositories(repos));
    }
    
    private DefaultArtifact getPanacheMavenPluginJar() {
        return new DefaultArtifact(
        		"io.quarkus", 
        		"panache-maven-plugin", 
        		"jar",
        		lookupPanacheMavenPlugin().getVersion());   	
    }

}
