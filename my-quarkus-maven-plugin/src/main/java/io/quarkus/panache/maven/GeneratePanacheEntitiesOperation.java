package io.quarkus.panache.maven;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.function.BiConsumer;

import org.hibernate.cfg.reveng.OverrideRepository;
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
            GeneratePanacheEntitiesContext context = (GeneratePanacheEntitiesContext)o2.get(GeneratePanacheEntitiesContext.class.getName());
    		ReverseEngineeringStrategy strategy = setupReverseEngineeringStrategy(context);
            MetadataDescriptor jdbcDescriptor = createJdbcDescriptor(strategy, context.getProperties());
            PanacheEntitiesGenerator panacheEntitiesGenerator = new PanacheEntitiesGenerator();
            panacheEntitiesGenerator.setMetadataDescriptor(jdbcDescriptor);
            panacheEntitiesGenerator.setOutputDirectory(context.getOutputDir());
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
    
    private ReverseEngineeringStrategy setupReverseEngineeringStrategy(
    		GeneratePanacheEntitiesContext context) {
        try {
	        String revengStrategy = "org.hibernate.cfg.reveng.DefaultReverseEngineeringStrategy";
	        ReverseEngineeringStrategy strategy = ReverseEngineeringStrategy.class
	        		.cast(Class.forName(revengStrategy).newInstance());
	        ReverseEngineeringSettings settings =
	                new ReverseEngineeringSettings(strategy)
	                        .setDefaultPackageName(context.getPackageName())
	                        .setDetectManyToMany(true)
	                        .setDetectOneToOne(true)
	                        .setDetectOptimisticLock(true)
	                        .setCreateCollectionForForeignKey(true)
	                        .setCreateManyToOneForForeignKey(true);
	        strategy.setSettings(settings);
	        String revengFile = context.getRevengFile();
	        if (revengFile != null) {
	        	File file = new File(revengFile);
	        	if (file.exists()) {
	        		OverrideRepository override = new OverrideRepository();
	        		override.addFile(file);
	        		strategy = override.getReverseEngineeringStrategy(strategy);
	        	}
	        }
        return strategy;
        } catch (Exception e) {
            throw new RuntimeException("RevengStrategy not instanced.", e);
        }
    }
    
}
