package io.quarkus.panache.maven;

import org.hibernate.tool.hbm2x.GenericExporter;

public class PanacheEntitiesGenerator extends GenericExporter {

	private static final String PANACHE_JAVACLASS_FTL = "templates/panache/Pojo.ftl";

	protected void init() {
		setTemplateName(PANACHE_JAVACLASS_FTL);
    	setFilePattern("{package-name}/{class-name}.java");    	
	}

	public PanacheEntitiesGenerator() {
		init();		
	}
    
	public String getName() {
		return "panache-entity-generator";
	}
	
	protected void setupContext() {
		if(!getProperties().containsKey("ejb3")) {
			getProperties().put("ejb3", "true");
		}
		if(!getProperties().containsKey("jdk5")) {
			getProperties().put("jdk5", "true");
		}	
		super.setupContext();
	}
}
