package io.quarkus.panache.maven;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Properties;

public class GeneratePanacheEntitiesContext implements Serializable {
	
	static final String CONTEXT = "META-INF/generate-panache-entities-context.dat";

	private static final long serialVersionUID = 1L;
	
	private File buildDir = null;
	private Properties properties = new Properties();
	
	public File getBuildDir() {
		return buildDir;
	}
	
	public void setBuildDir(File buildDir) {
		this.buildDir = buildDir;
	}
	
	public Properties getProperties() {
		return properties;
	}
	
    public void write(OutputStream out) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ObjectOutputStream obj = new ObjectOutputStream(new DataOutputStream(bytes));
        obj.writeObject(this);
        obj.close();
        out.write(bytes.toByteArray());
    }	

}
