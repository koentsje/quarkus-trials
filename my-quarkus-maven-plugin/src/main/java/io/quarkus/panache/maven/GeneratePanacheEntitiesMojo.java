package io.quarkus.panache.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "generate-panache-entities", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class GeneratePanacheEntitiesMojo extends AbstractMojo {

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		System.out.println("*** Generating Panache Entities ***");
	}

}
