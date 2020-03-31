package io.quarkus.panache.maven;

import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;

public class GeneratePanacheEntitiesHelper {
	
    public static String getQuarkusVersion(MavenProject project) {
    	for (Dependency dependency : project.getDependencyManagement().getDependencies()) {
    		if ("io.quarkus".equals(dependency.getGroupId()) 
    				&& "quarkus-hibernate-orm-panache".equals(dependency.getArtifactId())) {
    			return dependency.getVersion();
    		}
    	}
    	return null;
    }
    
}
