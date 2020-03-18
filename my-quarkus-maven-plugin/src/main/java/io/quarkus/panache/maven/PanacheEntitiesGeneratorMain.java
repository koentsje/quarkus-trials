package io.quarkus.panache.maven;

import java.util.Map;
import java.util.function.BiConsumer;

import io.quarkus.bootstrap.app.CuratedApplication;

public class PanacheEntitiesGeneratorMain  implements BiConsumer<CuratedApplication, Map<String, Object>> {

	@Override
	public void accept(CuratedApplication t, Map<String, Object> u) {
		System.out.println("hola");
	}

}
