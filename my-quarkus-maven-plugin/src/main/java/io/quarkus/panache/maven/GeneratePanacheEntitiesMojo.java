package io.quarkus.panache.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;

import io.quarkus.cli.commands.file.BuildFile;
import io.quarkus.maven.BuildFileMojoBase;
import io.quarkus.platform.descriptor.QuarkusPlatformDescriptor;
import io.quarkus.platform.tools.MessageWriter;

@Mojo(name = "generate-panache-entities", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class GeneratePanacheEntitiesMojo extends BuildFileMojoBase {

    @Component
    protected MavenProject project;
    
    @Override
    public void execute() throws MojoExecutionException {
    	super.project = this.project;
    	super.execute();
    }

	@Override
	protected void doExecute(
			BuildFile buildFile, 
			QuarkusPlatformDescriptor platformDescr, 
			MessageWriter log)
					throws MojoExecutionException {
		System.out.println("*** Generating Panache Entities ***");
	}

}
