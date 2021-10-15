package sample;

import rescuecore2.registry.Registry;
import rescuecore2.standard.entities.StandardEntityFactory;
import rescuecore2.standard.entities.StandardPropertyFactory;
import rescuecore2.standard.messages.StandardMessageFactory;

public class URNMapPrinter {

	public static void main(String[] args) {
		Registry.SYSTEM_REGISTRY
				.registerEntityFactory(StandardEntityFactory.INSTANCE);
		Registry.SYSTEM_REGISTRY
				.registerMessageFactory(StandardMessageFactory.INSTANCE);
		Registry.SYSTEM_REGISTRY
				.registerPropertyFactory(StandardPropertyFactory.INSTANCE);
		
		System.out.println(Registry.SYSTEM_REGISTRY.toJSON());
		
		System.out.println(Registry.SYSTEM_REGISTRY.toPython());
	}

}
