package org.nasdanika.archetypes.models.xcore.tests;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.nasdanika.archetypes.models.xcore.Generator;

public class TestGenerateXcoreModelProject {

	@Test
	public void testGenerateXcoreModelProject() throws Exception {
		Generator generator = new Generator() {
			@Override
			public String getGroupId() {
				return "org.nasdanika.models.decision.binding";
			}
		};
		generator.setModelName("DecisionBinding");
		generator.setVersion("2026.8.0");
		generator.generate(new File("target/generated-model/decision-binding"));
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
		generator.setModelName("seal");
		generator.setVersion("2026.8.0");
		generator.generate(new File("target/generated-model/seal"));
	}
	
	@Test
	public void testGenerateXcoreModelProjects() throws Exception {
		generateXcoreModelProject("seal");
	}	
	
	private void generateXcoreModelProject(String model) throws Exception {
		Generator generator = new Generator();
		generator.setModelName(model);
		generator.setVersion("2026.8.0");
		generator.generate(new File("target/generated-model/" + model));
	}			
		
}
