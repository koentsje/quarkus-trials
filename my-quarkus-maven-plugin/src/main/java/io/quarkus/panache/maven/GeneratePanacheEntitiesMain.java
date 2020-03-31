package io.quarkus.panache.maven;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Collections;

import io.quarkus.bootstrap.app.QuarkusBootstrap;

public class GeneratePanacheEntitiesMain implements Closeable {

    private Closeable realCloseable;

    public static void main(String... args) throws Exception {  
    	GeneratePanacheEntitiesMain generatePanacheEntitiesMain = null;
    	try {
    		InputStream inputStream = GeneratePanacheEntitiesMain.class
    				.getClassLoader()
    				.getResourceAsStream(GeneratePanacheEntitiesContext.CONTEXT);
    		GeneratePanacheEntitiesContext context = 
    				(GeneratePanacheEntitiesContext) new ObjectInputStream(
    						new DataInputStream(inputStream)).readObject();
    		generatePanacheEntitiesMain = new GeneratePanacheEntitiesMain();
    		generatePanacheEntitiesMain.start(context);
    	} catch (Exception e) {
    		throw new RuntimeException(e);
    	} finally {
    		generatePanacheEntitiesMain.close();
    	}
    }

    private Closeable start(GeneratePanacheEntitiesContext context) throws Exception {
        return (Closeable)QuarkusBootstrap
        		.builder(context.getBuildDir().toPath())
                .setMode(QuarkusBootstrap.Mode.DEV)
                .build()
                .bootstrap()
                .runInAugmentClassLoader(
                		GeneratePanacheEntitiesOperation.class.getName(),
                        Collections.singletonMap(
                        		GeneratePanacheEntitiesContext.class.getName(), 
                        		context));
    }

    @Override
    public void close() throws IOException {
        if (realCloseable != null) {
            realCloseable.close();
        }
    }
}
