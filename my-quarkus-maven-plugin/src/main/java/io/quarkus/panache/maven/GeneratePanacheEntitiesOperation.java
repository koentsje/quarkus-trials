package io.quarkus.panache.maven;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import java.util.function.BiConsumer;

import org.hibernate.cfg.reveng.ReverseEngineeringSettings;
import org.hibernate.cfg.reveng.ReverseEngineeringStrategy;
import org.hibernate.tool.api.metadata.MetadataDescriptor;
import org.hibernate.tool.api.metadata.MetadataDescriptorFactory;

import io.quarkus.bootstrap.app.CuratedApplication;

public class GeneratePanacheEntitiesOperation implements 
		BiConsumer<CuratedApplication, Map<String, Object>>, 
		Closeable {
	
    @Override
    public void accept(CuratedApplication curatedApplication, Map<String, Object> o2) {
    	try {
    		ReverseEngineeringStrategy strategy = setupReverseEngineeringStrategy();
            GeneratePanacheEntitiesContext context = (GeneratePanacheEntitiesContext)o2.get(GeneratePanacheEntitiesContext.class.getName());
            String buildDir = context.getBuildDir().getAbsolutePath();
           MetadataDescriptor jdbcDescriptor = createJdbcDescriptor(strategy, context.getProperties());
            PanacheEntitiesGenerator panacheEntitiesGenerator = new PanacheEntitiesGenerator();
            panacheEntitiesGenerator.setMetadataDescriptor(jdbcDescriptor);
            Path generationFolder = FileSystems.getDefault().getPath(buildDir, "generatedClasses");
            System.out.println("generationFolder: " + generationFolder.toString());
            panacheEntitiesGenerator.setOutputDirectory(generationFolder.toFile());
            panacheEntitiesGenerator.start();
    	} catch (Exception e) {
    		throw new RuntimeException(e);
    	}
    }
    
 	@Override
	public void close() throws IOException {
		// Nothing to do
	}
    
    private MetadataDescriptor createJdbcDescriptor(ReverseEngineeringStrategy strategy, Properties properties) {
        return MetadataDescriptorFactory
                .createJdbcDescriptor(
                        strategy,
                        properties,
                        true);
    }
    
    public ReverseEngineeringStrategy setupReverseEngineeringStrategy() {
        try {
	        String revengStrategy = "org.hibernate.cfg.reveng.DefaultReverseEngineeringStrategy";
	        ReverseEngineeringStrategy strategy = ReverseEngineeringStrategy.class
	        		.cast(Class.forName(revengStrategy).newInstance());
	        ReverseEngineeringSettings settings =
	                new ReverseEngineeringSettings(strategy)
	                        .setDefaultPackageName("org.bar")
	                        .setDetectManyToMany(true)
	                        .setDetectOneToOne(true)
	                        .setDetectOptimisticLock(true)
	                        .setCreateCollectionForForeignKey(true)
	                        .setCreateManyToOneForForeignKey(true);
	        strategy.setSettings(settings);
        return strategy;
        } catch (Exception e) {
            throw new RuntimeException("RevengStrategy not instanced.", e);
        }
    }
    
}
