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
	private File outputDir = null;
	private String packageName = null;
	private String revengFile = null;
	private Properties properties = new Properties();
	
	public File getBuildDir() {
		return buildDir;
	}
	
	public void setBuildDir(File buildDir) {
		this.buildDir = buildDir;
	}
	
	public File getOutputDir() {
		return outputDir;
	}
	
	public void setOutputDir(File outputDir) {
		this.outputDir = outputDir;
	}
	
	public String getPackageName() {
		return packageName;
	}
	
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
	
	public String getRevengFile() {
		return revengFile;
	}
	
	public void setRevengFile(String revengFile) {
		this.revengFile = revengFile;
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
