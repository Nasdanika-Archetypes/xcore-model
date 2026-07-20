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
	
	/**
	 * Maven groupId for the generated model, e.g. "org.nasdanika.models.markdown".
	 * Also used for the module names, e.g. "org.nasdanika.models.markdown.handlers"
	 * and package names, e.g. "org.nasdanika.models.markdown.handlers".
	 * This method returns "org.nasdanika.models." + getModelName().
	 * Override for non-Nasdanika models.
	 * @return
	 */
	private String getGroupId() {
		return "org.nasdanika.models." + getModelName();
	}

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
//				.map(Input.mapMatch(this::handlersPomXml, "handlers/pom.xml"))
//				.map(Input.mapMatch(this::handlersSrcMainJavaModuleInfoJava, "handlers/src/main/java/module-info.java"))
//				.map(Input.mapMatch(this::handlersMarkdownToEcoreArrayResourceContentsHandlerCapabilityFactory, "handlers/src/main/java/org/nasdanika/models/markdown/handlers/MarkdownToEcoreArrayResourceContentsHandlerCapabilityFactory.java"))
//				.map(Input.mapMatch(this::handlersMarkdownToEcoreFactory, "handlers/src/main/java/org/nasdanika/models/markdown/handlers/MarkdownToEcoreFactory.java"))
//				.map(Input.mapMatch(this::handlersMarkdownToEcoreResourceContentsHandler, "handlers/src/main/java/org/nasdanika/models/markdown/handlers/MarkdownToEcoreResourceContentsHandler.java"))
//				.map(Input.mapMatch(this::handlersMarkdownToEcoreResourceContentsHandlerCapabilityFactory, "handlers/src/main/java/org/nasdanika/models/markdown/handlers/MarkdownToEcoreResourceContentsHandlerCapabilityFactory.java"))
//				.map(Input.mapMatch(this::handlersMarkdownContentsFilteringTests, "handlers/src/test/java/org/nasdanika/models/markdown/tests/MarkdownContentsFilteringTests.java"))
//				.map(Input.mapMatch(this::modelProject, "model/.project"))
//				.map(Input.mapMatch(this::modelDocReadmeMd, "model/doc/readme.md"))
//				.map(Input.mapMatch(this::modelModelMarkdownXcore, "model/model/markdown.xcore"))
//				.map(Input.mapMatch(this::modelPageTemplateYml, "model/page-template.yml"))
//				.map(Input.mapMatch(this::modelPomXml, "model/pom.xml"))
//				.map(Input.mapMatch(this::modelRootActionYml, "model/root-action.yml"))
//				.map(Input.mapMatch(this::modelSrcMainJavaModuleInfoJava, "model/src/main/java/module-info.java"))
//				.map(Input.mapMatch(this::modelMarkdownArrayResourceContentsHandlerCapabilityFactory, "model/src/main/java/org/nasdanika/models/markdown/capability/MarkdownArrayResourceContentsHandlerCapabilityFactory.java"))
//				.map(Input.mapMatch(this::modelMarkdownEPackageResourceSetCapabilityFactory, "model/src/main/java/org/nasdanika/models/markdown/capability/MarkdownEPackageResourceSetCapabilityFactory.java"))
//				.map(Input.mapMatch(this::modelMarkdownResourceContentsHandler, "model/src/main/java/org/nasdanika/models/markdown/capability/MarkdownResourceContentsHandler.java"))
//				.map(Input.mapMatch(this::modelMarkdownResourceContentsHandlerCapabilityFactory, "model/src/main/java/org/nasdanika/models/markdown/capability/MarkdownResourceContentsHandlerCapabilityFactory.java"))
//				.map(Input.mapMatch(this::modelMarkdownResourceFactoryCapabilityFactory, "model/src/main/java/org/nasdanika/models/markdown/capability/MarkdownResourceFactoryCapabilityFactory.java"))
//				.map(Input.mapMatch(this::modelIcon, "model/src/main/java/org/nasdanika/models/markdown/Icon.java"))
//				.map(Input.mapMatch(this::modelMarkdownVisitor, "model/src/main/java/org/nasdanika/models/markdown/loader/MarkdownVisitor.java"))
//				.map(Input.mapMatch(this::modelMarkdownTests, "model/src/test/java/org/nasdanika/models/markdown/tests/MarkdownTests.java"))
//				.map(Input.mapMatch(this::pomXml, "pom.xml"))
//				.map(Input.mapMatch(this::readmeMd, "README.md"))
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
			case 3 -> line.mapLine(l -> l.replace(
					"<name>org.nasdanika.models.markdown.handlers</name>", 
					"<name>%s.handlers</name>".formatted(getGroupId())));
			default -> line;
			};
		});

	}

	//	handlers/pom.xml
	private StringInput handlersPomXml(StringInput input) {
		return input.mapLines(line -> {
			return switch (line.getLineNumber()) {
				default -> line;
			};
		});

	}

	//	handlers/src/main/java/module-info.java
	private StringInput handlersSrcMainJavaModuleInfoJava(StringInput input) {
		return input.mapLines(line -> {
			return switch (line.getLineNumber()) {
				default -> line;
			};
		});

	}

	//	handlers/src/main/java/org/nasdanika/models/markdown/handlers/MarkdownToEcoreArrayResourceContentsHandlerCapabilityFactory.java
	private StringInput handlersMarkdownToEcoreArrayResourceContentsHandlerCapabilityFactory(StringInput input) {
		return input.mapLines(line -> {
			return switch (line.getLineNumber()) {
				default -> line;
			};
		});

	}

	//	handlers/src/main/java/org/nasdanika/models/markdown/handlers/MarkdownToEcoreFactory.java
	private StringInput handlersMarkdownToEcoreFactory(StringInput input) {
		return input.mapLines(line -> {
			return switch (line.getLineNumber()) {
				default -> line;
			};
		});

	}

	//	handlers/src/main/java/org/nasdanika/models/markdown/handlers/MarkdownToEcoreResourceContentsHandler.java
	private StringInput handlersMarkdownToEcoreResourceContentsHandler(StringInput input) {
		return input.mapLines(line -> {
			return switch (line.getLineNumber()) {
				default -> line;
			};
		});

	}

	//	handlers/src/main/java/org/nasdanika/models/markdown/handlers/MarkdownToEcoreResourceContentsHandlerCapabilityFactory.java
	private StringInput handlersMarkdownToEcoreResourceContentsHandlerCapabilityFactory(StringInput input) {
		return input.mapLines(line -> {
			return switch (line.getLineNumber()) {
				default -> line;
			};
		});

	}

	//	handlers/src/test/java/org/nasdanika/models/markdown/tests/MarkdownContentsFilteringTests.java
	private StringInput handlersMarkdownContentsFilteringTests(StringInput input) {
		return input.mapLines(line -> {
			return switch (line.getLineNumber()) {
				default -> line;
			};
		});

	}

	//	model/.project
	private StringInput modelProject(StringInput input) {
		return input.mapLines(line -> {
			return switch (line.getLineNumber()) {
				default -> line;
			};
		});

	}

	//	model/doc/readme.md
	private StringInput modelDocReadmeMd(StringInput input) {
		return input.mapLines(line -> {
			return switch (line.getLineNumber()) {
				default -> line;
			};
		});

	}

	//	model/model/markdown.xcore
	private StringInput modelModelMarkdownXcore(StringInput input) {
		return input.mapLines(line -> {
			return switch (line.getLineNumber()) {
				default -> line;
			};
		});

	}

	//	model/page-template.yml
	private StringInput modelPageTemplateYml(StringInput input) {
		return input.mapLines(line -> {
			return switch (line.getLineNumber()) {
				default -> line;
			};
		});

	}

	//	model/pom.xml
	private StringInput modelPomXml(StringInput input) {
		return input.mapLines(line -> {
			return switch (line.getLineNumber()) {
				default -> line;
			};
		});

	}

	//	model/root-action.yml
	private StringInput modelRootActionYml(StringInput input) {
		return input.mapLines(line -> {
			return switch (line.getLineNumber()) {
				default -> line;
			};
		});

	}

	//	model/src/main/java/module-info.java
	private StringInput modelSrcMainJavaModuleInfoJava(StringInput input) {
		return input.mapLines(line -> {
			return switch (line.getLineNumber()) {
				default -> line;
			};
		});

	}

	//	model/src/main/java/org/nasdanika/models/markdown/capability/MarkdownArrayResourceContentsHandlerCapabilityFactory.java
	private StringInput modelMarkdownArrayResourceContentsHandlerCapabilityFactory(StringInput input) {
		return input.mapLines(line -> {
			return switch (line.getLineNumber()) {
				default -> line;
			};
		});

	}

	//	model/src/main/java/org/nasdanika/models/markdown/capability/MarkdownEPackageResourceSetCapabilityFactory.java
	private StringInput modelMarkdownEPackageResourceSetCapabilityFactory(StringInput input) {
		return input.mapLines(line -> {
			return switch (line.getLineNumber()) {
				default -> line;
			};
		});

	}

	//	model/src/main/java/org/nasdanika/models/markdown/capability/MarkdownResourceContentsHandler.java
	private StringInput modelMarkdownResourceContentsHandler(StringInput input) {
		return input.mapLines(line -> {
			return switch (line.getLineNumber()) {
				default -> line;
			};
		});

	}

	//	model/src/main/java/org/nasdanika/models/markdown/capability/MarkdownResourceContentsHandlerCapabilityFactory.java
	private StringInput modelMarkdownResourceContentsHandlerCapabilityFactory(StringInput input) {
		return input.mapLines(line -> {
			return switch (line.getLineNumber()) {
				default -> line;
			};
		});

	}

	//	model/src/main/java/org/nasdanika/models/markdown/capability/MarkdownResourceFactoryCapabilityFactory.java
	private StringInput modelMarkdownResourceFactoryCapabilityFactory(StringInput input) {
		return input.mapLines(line -> {
			return switch (line.getLineNumber()) {
				default -> line;
			};
		});

	}

	//	model/src/main/java/org/nasdanika/models/markdown/Icon.java
	private StringInput modelIcon(StringInput input) {
		return input.mapLines(line -> {
			return switch (line.getLineNumber()) {
				default -> line;
			};
		});

	}

	//	model/src/main/java/org/nasdanika/models/markdown/loader/MarkdownVisitor.java
	private StringInput modelMarkdownVisitor(StringInput input) {
		return input.mapLines(line -> {
			return switch (line.getLineNumber()) {
				default -> line;
			};
		});

	}

	//	model/src/test/java/org/nasdanika/models/markdown/tests/MarkdownTests.java
	private StringInput modelMarkdownTests(StringInput input) {
		return input.mapLines(line -> {
			return switch (line.getLineNumber()) {
				default -> line;
			};
		});

	}

	//	pom.xml
	private StringInput pomXml(StringInput input) {
		return input.mapLines(line -> {
			return switch (line.getLineNumber()) {
				default -> line;
			};
		});

	}

	//	README.md
	private StringInput readmeMd(StringInput input) {
		return input.mapLines(line -> {
			return switch (line.getLineNumber()) {
				default -> line;
			};
		});

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
