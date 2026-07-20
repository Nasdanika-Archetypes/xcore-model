package org.nasdanika.archetypes.models.xcore;

import java.io.File;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.emf.common.util.URI;
import org.nasdanika.common.Input;
import org.nasdanika.common.StreamInput;
import org.nasdanika.common.StringInput;
import org.nasdanika.common.StringOutput;
import org.nasdanika.common.Util;

public class Generator {
	
	private String cliVersion = "2026.2.0";


	public void generate(File outputDir) {
		outputDir.mkdirs();
		URI outputBase = URI.createFileURI(outputDir.getAbsolutePath()).appendSegment("");						
		StringOutput output = StringOutput.of(StringOutput.INSTANCE.base(outputBase));
		try (Stream<StreamInput> inputs = StreamInput.of(Generator.class)) {
			inputs
				.filter(i -> !Util.isBlank(i.getURI().lastSegment()))
				.flatMap(Input.subpath("template/**"))
				.map(StringInput::ofStreamInput)
				.map(Input.mapMatch(this::filterSiteYml, ".github/workflows/site.yml"))
				.map(Input.mapMatch(this::handlersProject, "handlers/.project"))
				.forEach(input -> {
					input.transferTo(output);
				});
		}		
	}
	
	//	.github/workflows/site.yml
	private StringInput filterSiteYml(StringInput input) {
		return input.mapLines(line -> {
			return switch (line.getLineNumber()) {		
				case 27 -> line.mapLine(l -> l.replace("Set up JDK 21", "Set up JDK %s".formatted(getJavaVersion())));
				case 30 -> line.mapLine(l -> l.replace("java-version: '21'", "java-version: '%s'".formatted(getJavaVersion())));
				case 33 -> line.mapLine(l -> l.replace("2026.2.0", getCliVersion()));
				case 44 -> line.mapLine(l -> l.replace(
						"./nsd xcore ../model/model/markdown.xcore doc --diagram=markdown.drawio --doc-stubs --doc-dir=../model/doc save ../model/markdown.xmi", 
						Util.interpolate(
								"./nsd xcore ../model/model/${modelName}.xcore doc --diagram=${modelName}.drawio --doc-stubs --doc-dir=../model/doc save ../model/${modelName}.xmi",
								Map.of("modelName", getModelName())::get)));
				case 49 -> line.mapLine(l -> l.replace("markdown.drawio", getModelName() + ".drawio"));
				default -> line;
			};
		});
		
	}
	
	private StringInput handlersProject(StringInput input) {
		return input.mapLines(line -> {
			return switch (line.getLineNumber()) {		
				default -> line;
			};
		});
		
	}
	
	/**
	 * Short name of the model, e.g. "markdown".
	 */
	private String modelName;
	
	public String getModelName() {
		return modelName;
	}
	
	public void setModelName(String modelName) {
		this.modelName = modelName;
	}
	
	public String getCliVersion() {
		return cliVersion;
	}
	
	public void setCliVersion(String cliVersion) {
		this.cliVersion = cliVersion;
	}

	private String javaVersion = "21";
	
	
	public String getJavaVersion() {
		return javaVersion;
	}
	
	public void setJavaVersion(String javaVersion) {
		this.javaVersion = javaVersion;
	}

}

//	handlers/.project
//	handlers/pom.xml
//	handlers/src/main/java/module-info.java
//	handlers/src/main/java/org/nasdanika/models/markdown/handlers/MarkdownToEcoreArrayResourceContentsHandlerCapabilityFactory.java
//	handlers/src/main/java/org/nasdanika/models/markdown/handlers/MarkdownToEcoreFactory.java
//	handlers/src/main/java/org/nasdanika/models/markdown/handlers/MarkdownToEcoreResourceContentsHandler.java
//	handlers/src/main/java/org/nasdanika/models/markdown/handlers/MarkdownToEcoreResourceContentsHandlerCapabilityFactory.java
//	handlers/src/test/java/org/nasdanika/models/markdown/tests/MarkdownContentsFilteringTests.java
//	model/.project
//	model/doc/readme.md
//	model/model/markdown.xcore
//	model/page-template.yml
//	model/pom.xml
//	model/root-action.yml
//	model/src/main/java/module-info.java
//	model/src/main/java/org/nasdanika/models/markdown/capability/MarkdownArrayResourceContentsHandlerCapabilityFactory.java
//	model/src/main/java/org/nasdanika/models/markdown/capability/MarkdownEPackageResourceSetCapabilityFactory.java
//	model/src/main/java/org/nasdanika/models/markdown/capability/MarkdownResourceContentsHandler.java
//	model/src/main/java/org/nasdanika/models/markdown/capability/MarkdownResourceContentsHandlerCapabilityFactory.java
//	model/src/main/java/org/nasdanika/models/markdown/capability/MarkdownResourceFactoryCapabilityFactory.java
//	model/src/main/java/org/nasdanika/models/markdown/Icon.java
//	model/src/main/java/org/nasdanika/models/markdown/loader/MarkdownVisitor.java
//	model/src/test/java/org/nasdanika/models/markdown/tests/MarkdownTests.java
//	pom.xml
//	README.md
