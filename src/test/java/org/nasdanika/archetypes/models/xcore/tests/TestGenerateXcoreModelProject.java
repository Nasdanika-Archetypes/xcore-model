package org.nasdanika.archetypes.models.xcore.tests;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.nasdanika.archetypes.models.xcore.Generator;

public class TestGenerateXcoreModelProject {

	@Test
	public void testGenerateXcoreModelProject() throws Exception {
		Generator generator = new Generator();
		generator.setModelName("transilvania");
		generator.setCliVersion("1.0.0");
		generator.setJavaVersion("23");
		
		generator.generate(new File("target/generated-model/xcore-model"));
	}	
		
}
