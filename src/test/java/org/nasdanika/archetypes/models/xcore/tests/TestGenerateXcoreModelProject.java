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
	
	@Test
	public void testGenerateA2UIXcoreModelProject() throws Exception {
		Generator generator = new Generator();
		generator.setModelName("a2ui");
		generator.setVersion("2026.8.0");
		generator.generate(new File("target/generated-model/a2ui"));
	}	
	
	@Test
	public void testGenerateRoleXcoreModelProject() throws Exception {
		Generator generator = new Generator();
		generator.setModelName("role");
		generator.setVersion("2026.8.0");
		generator.generate(new File("target/generated-model/role"));
	}	
	
		
}
