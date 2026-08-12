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
				return "org.nasdanika.models.architecture.c4";
			}
		};
		generator.setModelName("c4");
		generator.setVersion("2026.8.0");
		generator.generate(new File("target/generated-model/c4"));
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
		String [] models = new String[] {
				"iam", 
				"seal",
				"lifecycle",
				"governance",
				"work",
				"architecture",
				"threat",
				"mcp",
				"capability",
				"ui",
				"diagram",
				"presentation",
				"drawio",
				"powerpoint",
				"visio",
				"plantuml",
				"odp",
				"pdf",
				"maven",
				"bw5"};
		for (String model: models) {
			generateXcoreModelProject(model);
		}
	}	
	
	private void generateXcoreModelProject(String model) throws Exception {
		Generator generator = new Generator();
		generator.setModelName(model);
		generator.setVersion("2026.8.0");
		generator.generate(new File("target/generated-model/" + model));
	}			
		
}
