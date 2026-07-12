package org.nasdanika.archetypes.models.xcore.tests;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.nasdanika.archetypes.models.xcore.Generator;

public class TestGenerateXcoreModelProject {

	@Test
	public void testGenerateXcoreModelProject() throws Exception {
		Generator generator = new Generator();
		generator.generate(new File("target/generated-sources/xcore-model"));
	}	
		
}
