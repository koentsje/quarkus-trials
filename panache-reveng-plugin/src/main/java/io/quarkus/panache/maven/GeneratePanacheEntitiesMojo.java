package io.quarkus.panache.maven;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.maven.execution.MavenSession;
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
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.spi.ConfigProviderResolver;
import org.hibernate.tool.Version;

import io.quarkus.runtime.configuration.ConfigUtils;
import io.quarkus.runtime.configuration.QuarkusConfigFactory;
import io.quarkus.utilities.JavaBinFinder;
import io.smallrye.config.PropertiesConfigSource;
import io.smallrye.config.SmallRyeConfig;

@Mojo(name = "generate-panache-entities", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class GeneratePanacheEntitiesMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(defaultValue = "${project.build.directory}") 
    private File buildDir;

    @Parameter(defaultValue = "${project.build.directory}/generatedClasses") 
    private File outputDir;

    @Parameter 
    private String packageName = null;

    @Parameter(defaultValue = "${session}")
    private MavenSession session;

    @Parameter(defaultValue = "${repositorySystemSession}", readonly = true) 
    private RepositorySystemSession repoSession;

    @Parameter(defaultValue = "${project.remoteProjectRepositories}", readonly = true, required = true) 
    private List<RemoteRepository> repos;

    @Component 
    private RepositorySystem repoSystem;

    @Override
    public void execute() throws MojoFailureException, MojoExecutionException {
        try {
            Process generatePanacheEntitiesProcess = 
            		new ProcessBuilder(createGeneratePanacheEntitiesProcessCommand())
                    	.inheritIO()
                    	.start();
            Runtime.getRuntime().addShutdownHook(new Thread(
            		new Runnable() {
		                @Override
		                public void run() {
		                    generatePanacheEntitiesProcess.destroy();
		                }
            		}, 
            		"Development Mode Shutdown Hook"));
            generatePanacheEntitiesProcess.waitFor();
        } catch (Exception e) {
            throw new MojoFailureException("Failed to run", e);
        }
    }
    
    private List<String> createGeneratePanacheEntitiesProcessCommand() throws Exception {
    	ArrayList<String> result = new ArrayList<String>();
    	result.add(JavaBinFinder.findBin());       
        result.add("-jar");
        result.add(createGeneratePanacheEntitiesJarFile().getAbsolutePath());
        return result;       
    }
    
    private File createGeneratePanacheEntitiesJarFile() throws Exception {
        File result = new File(buildDir, "generate-panache-entities.jar");
        result.delete();
        result.deleteOnExit();
        try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(result))) {
            out.putNextEntry(new ZipEntry("META-INF/MANIFEST.MF"));
            createManifest().write(out);
            out.putNextEntry(new ZipEntry(GeneratePanacheEntitiesContext.CONTEXT));
            createContext().write(out);
        }
        return result;
    }
    
    private Manifest createManifest() throws Exception {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().put(Attributes.Name.CLASS_PATH, createClassPathManifest());
        manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, GeneratePanacheEntitiesMain.class.getName());
        return manifest;
    }
    
    private GeneratePanacheEntitiesContext createContext() {
        final GeneratePanacheEntitiesContext context = new GeneratePanacheEntitiesContext();
        context.setBuildDir(buildDir);
        context.setOutputDir(outputDir);
        context.setPackageName(resolvePackageName());
        context.setRevengFile(getRevengFilePath());
        context.getProperties().putAll(getGeneratorProperties());
        return context;
    }

    private String createClassPathManifest() throws Exception {
        StringBuilder result = new StringBuilder();
        for (ArtifactResult appDep : getDependencies()) {
            result
            	.append(appDep.getArtifact().getFile().toPath().toAbsolutePath().toUri())
            	.append(" ");
        }
        addJarToClassPath(GeneratePanacheEntitiesMain.class, result);
        return result.toString();
    }
   
     private List<ArtifactResult> getDependencies() {
    	String quarkusVersion = GeneratePanacheEntitiesHelper.getQuarkusVersion(project);
    	String[][] mavenInfos = new String[][] {
    		{"io.quarkus", "quarkus-development-mode", quarkusVersion },
    		{"org.hibernate", "hibernate-tools-maven-plugin", Version.getDefault().toString() }
    	};
    	List<ArtifactResult> result = new ArrayList<ArtifactResult>();
    	for (String[] info : mavenInfos) {
    		result.addAll(getDependencies(info[0], info[1], info[2]));
    	}	
    	return result;
    }
    
    private List<ArtifactResult> getDependencies(String groupId, String artefactId, String version) {
        try {
            final DefaultArtifact jar = new DefaultArtifact(groupId, artefactId, "jar",version);
			return repoSystem.resolveDependencies(repoSession,
			        new DependencyRequest()
			                .setCollectRequest(
			                        new CollectRequest()
			                                .setRoot(new Dependency(jar, JavaScopes.RUNTIME))
			                                .setRepositories(repos)))
					.getArtifactResults();
		} catch (DependencyResolutionException e) {
			throw new RuntimeException(e);
		}    	
    }

    private void addJarToClassPath(
    		Class<?> clazz, 
    		StringBuilder classPathManifest) throws Exception {
        URI classUri = clazz
        		.getClassLoader()
                .getResource(clazz.getName().replace('.', '/') + ".class")
                .toURI();
        String jarPath = classUri.getRawSchemeSpecificPart();
        classPathManifest
    		.append(Paths
    				.get(new URI(jarPath.substring(0, jarPath.indexOf('!'))))
    				.toAbsolutePath()
    				.toUri())
    		.append(" ");  	
    }
    
    private URL getApplicationPropertiesFile() {
    	try {
	    	for (Resource resource : project.getBuild().getResources()) {
	    		Path applicationPropertiesPath = Paths
	    				.get(resource.getDirectory())
	    				.resolve("application.properties");
	    		if (Files.exists(applicationPropertiesPath)) {
	    			return applicationPropertiesPath.toFile().toURI().toURL();
	    		}
	    	}
	    	return null;
    	} catch (Exception e) {
    		throw new RuntimeException(e);
    	}
    }
    
    private Properties getGeneratorProperties() {
    	try {
	    	Properties result = new Properties();
	        SmallRyeConfig built = ConfigUtils
	        		.configBuilder(false)
	                .withSources(new PropertiesConfigSource(getApplicationPropertiesFile())).
	                build();
	        QuarkusConfigFactory.setConfig(built);
	        final ConfigProviderResolver cpr = ConfigProviderResolver.instance();
	        final Config existing = cpr.getConfig();
	        if (existing != built) {
	            cpr.releaseConfig(existing);
	            // subsequent calls will get the new config
	        }
	        result.put(
	        		"hibernate.connection.url", 
	        		ConfigProvider.getConfig().getValue(
	        				"quarkus.datasource.url", 
	        				String.class));
	        result.put(
	        		"hibernate.connection.driver_class", 
	        		ConfigProvider.getConfig().getValue(
	        				"quarkus.datasource.driver", 
	        				String.class));
	        result.put(
	        		"hibernate.connection.username", 
	        		ConfigProvider.getConfig().getValue(
	        				"quarkus.datasource.username", 
	        				String.class));
	        Optional<String> password = ConfigProvider.getConfig()
	        		.getOptionalValue(
	        				"quarkus.datasource.password", 
	        				String.class);
	        result.put(
	        		"hibernate.connection.password", 
	        		password.isPresent() ? password.get() : "");
	    	return result;
    	} catch (Exception e) {
    		throw new RuntimeException(e);
    	}
    }
    
    private String resolvePackageName() {
    	String result = packageName;
    	if (result == null) {
    		result = project.getGroupId() + "." + project.getArtifactId();
    	}
    	return result;
    }
    
    private String getRevengFilePath() {
    	try {
	    	for (Resource resource : project.getBuild().getResources()) {
	    		Path revengFilePath = Paths
	    				.get(resource.getDirectory())
	    				.resolve("panache.reveng.xml");
	    		if (Files.exists(revengFilePath)) {
	    			return revengFilePath.toFile().getAbsolutePath();
	    		}
	    	}
	    	return null;
    	} catch (Exception e) {
    		throw new RuntimeException(e);
    	}
    }
    


}
