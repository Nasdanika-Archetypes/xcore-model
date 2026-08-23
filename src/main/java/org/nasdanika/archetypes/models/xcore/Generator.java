package org.nasdanika.archetypes.models.xcore;

import java.io.File;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.emf.common.util.URI;
import org.nasdanika.common.Input;
import org.nasdanika.common.StreamInput;
import org.nasdanika.common.StringInput;
import org.nasdanika.common.StringInput.Line;
import org.nasdanika.common.StringOutput;
import org.nasdanika.common.Util;

public class Generator {
	
	private String cliVersion = "2026.6.0";
	
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
	 * Java name of the model, e.g. "Markdown".
	 * This implementation returns StringUtils.capitalize(getModelName()).
	 * @return
	 */
	public String getModelJavaName() {
		return StringUtils.capitalize(getModelName());
	}
	
	/**
	 * Maven groupId for the generated model, e.g. "org.nasdanika.models.markdown".
	 * Also used for the module names, e.g. "org.nasdanika.models.markdown.handlers"
	 * and package names, e.g. "org.nasdanika.models.markdown.handlers".
	 * This method returns "org.nasdanika.models." + getModelName().
	 * Override for non-Nasdanika models.
	 * @return
	 */
	public String getGroupId() {
		return "org.nasdanika.models." + getModelName();
	}
	
	public String getNsURI() {
		return "https://%s.models.nasdanika.org".formatted(getModelName());
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
				.map(Input.mapMatch(this::handlersPomXml, "handlers/pom.xml"))
				.map(Input.mapMatch(this::handlersSrcMainJavaModuleInfoJava, "handlers/src/main/java/module-info.java"))
				.map(Input.mapMatch(this::handlersMarkdownToEcoreArrayResourceContentsHandlerCapabilityFactory, "handlers/src/main/java/org/nasdanika/models/markdown/handlers/MarkdownToEcoreArrayResourceContentsHandlerCapabilityFactory.java"))
				.map(Input.mapMatch(this::handlersMarkdownToEcoreFactory, "handlers/src/main/java/org/nasdanika/models/markdown/handlers/MarkdownToEcoreFactory.java"))
				.map(Input.mapMatch(this::handlersMarkdownToEcoreResourceContentsHandler, "handlers/src/main/java/org/nasdanika/models/markdown/handlers/MarkdownToEcoreResourceContentsHandler.java"))
				.map(Input.mapMatch(this::handlersMarkdownToEcoreResourceContentsHandlerCapabilityFactory, "handlers/src/main/java/org/nasdanika/models/markdown/handlers/MarkdownToEcoreResourceContentsHandlerCapabilityFactory.java"))
				.map(Input.mapMatch(this::handlersMarkdownContentsFilteringTests, "handlers/src/test/java/org/nasdanika/models/markdown/handlers/tests/MarkdownContentsFilteringTests.java"))
				.map(Input.mapMatch(this::modelProject, "model/.project"))
				.map(Input.mapMatch(this::modelDocReadmeMd, "model/doc/readme.md"))
				.map(Input.mapMatch(this::modelModelMarkdownXcore, "model/src/main/resources/org/nasdanika/models/markdown/markdown.xcore"))
//				.map(Input.mapMatch(this::modelPageTemplateYml, "model/page-template.yml"))
				.map(Input.mapMatch(this::modelPomXml, "model/pom.xml"))
				.map(Input.mapMatch(this::modelRootActionYml, "model/root-action.yml"))
				.map(Input.mapMatch(this::modelSrcMainJavaModuleInfoJava, "model/src/main/java/module-info.java"))
				.map(Input.mapMatch(this::modelMarkdownArrayResourceContentsHandlerCapabilityFactory, "model/src/main/java/org/nasdanika/models/markdown/capability/MarkdownArrayResourceContentsHandlerCapabilityFactory.java"))
				.map(Input.mapMatch(this::modelMarkdownEPackageResourceSetCapabilityFactory, "model/src/main/java/org/nasdanika/models/markdown/capability/MarkdownEPackageResourceSetCapabilityFactory.java"))
				.map(Input.mapMatch(this::modelMarkdownResourceContentsHandler, "model/src/main/java/org/nasdanika/models/markdown/capability/MarkdownResourceContentsHandler.java"))
				.map(Input.mapMatch(this::modelMarkdownResourceContentsHandlerCapabilityFactory, "model/src/main/java/org/nasdanika/models/markdown/capability/MarkdownResourceContentsHandlerCapabilityFactory.java"))
				.map(Input.mapMatch(this::modelMarkdownResourceFactoryCapabilityFactory, "model/src/main/java/org/nasdanika/models/markdown/capability/MarkdownResourceFactoryCapabilityFactory.java"))
				.map(Input.mapMatch(this::modelIcon, "model/src/main/java/org/nasdanika/models/markdown/Icon.java"))
//				.map(Input.mapMatch(this::modelMarkdownVisitor, "model/src/main/java/org/nasdanika/models/markdown/loader/MarkdownVisitor.java"))
				.map(Input.mapMatch(this::modelMarkdownTests, "model/src/test/java/org/nasdanika/models/markdown/tests/MarkdownTests.java"))
				.map(Input.mapMatch(this::pomXml, "pom.xml"))
				.map(Input.mapMatch(this::readmeMd, "README.md"))
				.forEach(input -> {
					input.transferTo(output);
				});
		}		
	}
	
	//	.github/workflows/site.yml
	protected StringInput filterSiteYml(StringInput input) {
		return input.mapLines(line -> {
			return switch (line.getLineNumber()) {		
				case 27 -> line.mapLine(l -> l.replace("Set up JDK 21", "Set up JDK %s".formatted(getJavaVersion())));
				case 30 -> line.mapLine(l -> l.replace("java-version: '21'", "java-version: '%s'".formatted(getJavaVersion())));
				case 33 -> line.mapLine(l -> l.replace("2026.6.0", getCliVersion()));
				case 44 -> line.mapLine(l -> l.replace(
						"./nsd xcore ../model/src/main/resources/org/nasdanika/models/markdown/markdown.xcore doc --diagram=markdown.drawio --doc-stubs --doc-dir=../model/doc save ../model/markdown.xmi", 
						Util.interpolate(
								"./nsd xcore ../model/src/main/resources/${groupPath}/${modelName}.xcore doc --diagram=${modelName}.drawio --doc-stubs --doc-dir=../model/doc save ../model/${modelName}.xmi",
								Map.of(
										"modelName", getModelName(),
										"groupPath", getGroupId().replace('.', '/'))::get)));
				case 48 -> line.mapLine(l -> l.replace("https://markdown.nasdanika.org", getNsURI()));
				case 49 -> line.mapLine(l -> l.replace("markdown.drawio", getModelName() + ".drawio"));
				case 50 -> line.mapLine(l -> l.replace("org/nasdanika/models/markdown/markdown", getGroupId().replace('.', '/') + "/" + getModelName()));
				default -> line;
			};
		});		
	}
	
	protected StringInput handlersProject(StringInput input) {
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
	protected StringInput handlersPomXml(StringInput input) {
		return input.mapLines(line -> {
			return switch (line.getLineNumber()) {
			case 6 -> replace(
					line, 
					"org.nasdanika.models.markdown",
					getGroupId());
			case 7 -> replace(
					line, 
					"2026.6.0",
					getVersion());
			default -> line;
			};
		});
	}
	
	private String version = "0.0.1-SNAPSHOT";

	public String getVersion() {
		return version;		
	}
	
	public void setVersion(String version) {
		this.version = version;
	}

	protected Line replace(Line line, String from, String to) {
		return line.mapLine(l -> l.replace(from, to));
	}

	//	handlers/src/main/java/module-info.java
	protected StringInput handlersSrcMainJavaModuleInfoJava(StringInput input) {
		return input.mapLines(line -> {
			return switch (line.getLineNumber()) {
			case 2 -> replace(
					line, 
					"org.nasdanika.models.markdown.handlers.MarkdownToEcoreArrayResourceContentsHandlerCapabilityFactory",
					"%s.handlers.%sToEcoreArrayResourceContentsHandlerCapabilityFactory".formatted(getGroupId(), getModelJavaName()));
			case 3 -> replace(
					line, 
					"org.nasdanika.models.markdown.handlers.MarkdownToEcoreResourceContentsHandlerCapabilityFactory",
					"%s.handlers.%sToEcoreResourceContentsHandlerCapabilityFactory".formatted(getGroupId(), getModelJavaName()));
			case 5, 7, 8, 10 -> replace(
					line, 
					"org.nasdanika.models.markdown",
					getGroupId());
			case 15 -> replace(
					line, 
					"MarkdownToEcoreArrayResourceContentsHandlerCapabilityFactory",
					"%sToEcoreArrayResourceContentsHandlerCapabilityFactory".formatted(getModelJavaName()));
			case 16 -> replace(
					line, 
					"MarkdownToEcoreResourceContentsHandlerCapabilityFactory",
					"%sToEcoreResourceContentsHandlerCapabilityFactory".formatted(getModelJavaName()));
			default -> line;
			};
		});
	}

	//	handlers/src/main/java/org/nasdanika/models/markdown/handlers/MarkdownToEcoreArrayResourceContentsHandlerCapabilityFactory.java
	protected StringInput handlersMarkdownToEcoreArrayResourceContentsHandlerCapabilityFactory(StringInput input) {
		UnaryOperator<URI> uriMapper = uri -> {
			String uriStr = uri.toString();			
			String newUriStr = uriStr.replace(
					"org/nasdanika/models/markdown/handlers/MarkdownToEcoreArrayResourceContentsHandlerCapabilityFactory.java", 
					"%s/handlers/%sToEcoreArrayResourceContentsHandlerCapabilityFactory.java".formatted(getGroupId().replace('.', '/'), getModelJavaName()));
			return URI.createURI(newUriStr);
		};
		return input
				.mapURI(uriMapper)
				.mapLines(line -> {
					return switch (line.getLineNumber()) {
					case 1 -> replace(
							line, 
							"package org.nasdanika.models.markdown.handlers;",
							"package %s.handlers;".formatted(getGroupId()));
					case 16 -> replace(
							line, 
							"MarkdownToEcoreArrayResourceContentsHandlerCapabilityFactory",
							"%sToEcoreArrayResourceContentsHandlerCapabilityFactory".formatted(getModelJavaName()));
					case 57 -> replace(
							line, 
							"MarkdownToEcoreResourceContentsHandler",
							"%sToEcoreResourceContentsHandler".formatted(getModelJavaName()));
					default -> line;
					};
				});
	}

	//	handlers/src/main/java/org/nasdanika/models/markdown/handlers/MarkdownToEcoreFactory.java
	protected StringInput handlersMarkdownToEcoreFactory(StringInput input) {
		UnaryOperator<URI> uriMapper = uri -> {
			String uriStr = uri.toString();			
			String newUriStr = uriStr.replace(
					"org/nasdanika/models/markdown/handlers/MarkdownToEcoreFactory.java", 
					"%s/handlers/%sToEcoreFactory.java".formatted(getGroupId().replace('.', '/'), getModelJavaName()));
			return URI.createURI(newUriStr);
		};
		return input
				.mapURI(uriMapper)
				.mapLines(line -> {
					return switch (line.getLineNumber()) {
					case 1 -> replace(
							line, 
							"package org.nasdanika.models.markdown.handlers;",
							"package %s.handlers;".formatted(getGroupId()));
					case 26, 54 -> replace(
							line, 
							"MarkdownToEcoreFactory",
							"%sToEcoreFactory".formatted(getModelJavaName()));
					default -> line;
					};
		});
	}

	//	handlers/src/main/java/org/nasdanika/models/markdown/handlers/MarkdownToEcoreResourceContentsHandler.java
	protected StringInput handlersMarkdownToEcoreResourceContentsHandler(StringInput input) {
		UnaryOperator<URI> uriMapper = uri -> {
			String uriStr = uri.toString();			
			String newUriStr = uriStr.replace(
					"org/nasdanika/models/markdown/handlers/MarkdownToEcoreResourceContentsHandler.java", 
					"%s/handlers/%sToEcoreResourceContentsHandler.java".formatted(getGroupId().replace('.', '/'), getModelJavaName()));
			return URI.createURI(newUriStr);
		};
		return input
				.mapURI(uriMapper)
				.mapLines(line -> {
					return switch (line.getLineNumber()) {
					case 1 -> replace(
							line, 
							"package org.nasdanika.models.markdown.handlers;",
							"package %s.handlers;".formatted(getGroupId()));
					case 23, 31 -> replace(
							line, 
							"MarkdownToEcoreResourceContentsHandler",
							"%sToEcoreResourceContentsHandler".formatted(getModelJavaName()));
					default -> line;
					};
		});
	}

	//	handlers/src/main/java/org/nasdanika/models/markdown/handlers/MarkdownToEcoreResourceContentsHandlerCapabilityFactory.java
	protected StringInput handlersMarkdownToEcoreResourceContentsHandlerCapabilityFactory(StringInput input) {
		UnaryOperator<URI> uriMapper = uri -> {
			String uriStr = uri.toString();			
			String newUriStr = uriStr.replace(
					"org/nasdanika/models/markdown/handlers/MarkdownToEcoreResourceContentsHandlerCapabilityFactory.java", 
					"%s/handlers/%sToEcoreResourceContentsHandlerCapabilityFactory.java".formatted(getGroupId().replace('.', '/'), getModelJavaName()));
			return URI.createURI(newUriStr);
		};
		return input
				.mapURI(uriMapper)
				.mapLines(line -> {
					return switch (line.getLineNumber()) {
					case 1 -> replace(
							line, 
							"package org.nasdanika.models.markdown.handlers;",
							"package %s.handlers;".formatted(getGroupId()));
					case 15 -> replace(
							line, 
							"MarkdownToEcoreResourceContentsHandlerCapabilityFactory",
							"%sToEcoreResourceContentsHandlerCapabilityFactory".formatted(getModelJavaName()));
					default -> line;
					};
		});
	}

	//	handlers/src/test/java/org/nasdanika/models/markdown/tests/MarkdownContentsFilteringTests.java
	protected StringInput handlersMarkdownContentsFilteringTests(StringInput input) {
		UnaryOperator<URI> uriMapper = uri -> {
			String uriStr = uri.toString();			
			String newUriStr = uriStr.replace(
					"org/nasdanika/models/markdown/handlers/tests/MarkdownContentsFilteringTests.java", 
					"%s/handlers/tests/%sContentsFilteringTests.java".formatted(getGroupId().replace('.', '/'), getModelJavaName()));
			return URI.createURI(newUriStr);
		};
		return input
				.mapURI(uriMapper)
				.mapLines(line -> {
					return switch (line.getLineNumber()) {
					case 1 -> replace(
							line, 
							"package org.nasdanika.models.markdown.handlers.tests;",
							"package %s.handlers.tests;".formatted(getGroupId()));
					case 18, 21 -> replace(
							line, 
							"Markdown",
							getModelJavaName());
					default -> line;
					};
		});
	}

	//	model/.project
	protected StringInput modelProject(StringInput input) {
		return input.mapLines(line -> {
			return switch (line.getLineNumber()) {
			case 3 -> line.mapLine(l -> l.replace(
					"<name>org.nasdanika.models.markdown</name>", 
					"<name>%s</name>".formatted(getGroupId())));
			default -> line;
			};
		});
	}

	//	model/doc/readme.md
	protected StringInput modelDocReadmeMd(StringInput input) {
		return input.mapLines(line -> {
			return switch (line.getLineNumber()) {
			case 3 -> line.mapLine(l -> l.replace(
					"markdown", 
					getModelName()));
			default -> line;
			};
		});
	}

	//	model/model/markdown.xcore
	protected StringInput modelModelMarkdownXcore(StringInput input) {
		UnaryOperator<URI> uriMapper = uri -> {
			String uriStr = uri.toString();			
			String newUriStr = uriStr.replace(
					"org/nasdanika/models/markdown/markdown.xcore", 
					"%s/%s.xcore".formatted(getGroupId().replace('.', '/'), getModelName()));
			return URI.createURI(newUriStr);
		};
		return input
				.mapURI(uriMapper)
				.mapLines(line -> {
					return switch (line.getLineNumber()) {
					case 1 -> replace(
							line, 
							"@Ecore(nsURI=\"https://markdown.models.nasdanika.org\", nsPrefix=\"org.nasdanika.models.markdown\")",
							"@Ecore(nsURI=\"%s\", nsPrefix=\"%s\")".formatted(getNsURI(), getGroupId()));
					case 3, 11 -> replace(
							line, 
							"org.nasdanika.models.markdown",
							getGroupId());
					default -> line;
					};
		});
	}

	//	model/page-template.yml
//	protected StringInput modelPageTemplateYml(StringInput input) {
//		return input.mapLines(line -> {
//			return switch (line.getLineNumber()) {
//				default -> line;
//			};
//		});
//	}

	//	model/pom.xml
	protected StringInput modelPomXml(StringInput input) {
		return input.mapLines(line -> {
			return switch (line.getLineNumber()) {
			case 6 -> replace(
					line, 
					"org.nasdanika.models.markdown",
					getGroupId());
			case 7 -> replace(
					line, 
					"2026.6.0",
					getVersion());
			case 13 -> replace(
					line, 
					"Markdown",
					getModelJavaName());
			default -> line;
			};
		});
	}

	//	model/root-action.yml
	protected StringInput modelRootActionYml(StringInput input) {
		return input.mapLines(line -> {
			return switch (line.getLineNumber()) {
			case 6 -> replace(
					line, 
					"markdown",
					getModelName());
			case 17 -> replace(
					line, 
					"https://github.com/Nasdanika-Models/markdown",
					getSoureUrl());
				default -> line;
			};
		});
	}

	public String getSoureUrl() {
		return "https://github.com/Nasdanika-Models/" + getModelName();
	}

	//	model/src/main/java/module-info.java
	protected StringInput modelSrcMainJavaModuleInfoJava(StringInput input) {
		return input.mapLines(line -> {
			return switch (line.getLineNumber()) {
			case 2, 3, 4, 5 -> replace(
					line, 
					"org.nasdanika.models.markdown.capability.Markdown",
					"%s.capability.%s".formatted(getGroupId(), getModelJavaName()));
			case 7 -> replace(
					line, 
					"org.nasdanika.models.markdown",
					getGroupId());
			case 9, 10, 11, 12 -> replace(
					line, 
					"org.nasdanika.models.markdown",
					getGroupId());
			case 19, 20, 21, 22 -> replace(
					line, 
					"Markdown",
					getModelJavaName());
			default -> line;
			};
		});
	}

	//	model/src/main/java/org/nasdanika/models/markdown/capability/MarkdownArrayResourceContentsHandlerCapabilityFactory.java
	protected StringInput modelMarkdownArrayResourceContentsHandlerCapabilityFactory(StringInput input) {
		UnaryOperator<URI> uriMapper = uri -> {
			String uriStr = uri.toString();			
			String newUriStr = uriStr.replace(
					"org/nasdanika/models/markdown/capability/MarkdownArrayResourceContentsHandlerCapabilityFactory.java", 
					"%s/capability/%sArrayResourceContentsHandlerCapabilityFactory.java".formatted(getGroupId().replace('.', '/'), getModelJavaName()));
			return URI.createURI(newUriStr);
		};
		return input
				.mapURI(uriMapper)
				.mapLines(line -> {
					return switch (line.getLineNumber()) {
					case 1 -> replace(
							line, 
							"package org.nasdanika.models.markdown.capability;",
							"package %s.capability;".formatted(getGroupId()));
					case 14 -> replace(
							line, 
							"MarkdownArrayResourceContentsHandlerCapabilityFactory",
							"%sArrayResourceContentsHandlerCapabilityFactory".formatted(getModelJavaName()));
					case 36 -> replace(
							line, 
							"MarkdownResourceContentsHandler",
							"%sResourceContentsHandler".formatted(getModelJavaName()));
					default -> line;
					};
				});
	}

	//	model/src/main/java/org/nasdanika/models/markdown/capability/MarkdownEPackageResourceSetCapabilityFactory.java
	protected StringInput modelMarkdownEPackageResourceSetCapabilityFactory(StringInput input) {
		UnaryOperator<URI> uriMapper = uri -> {
			String uriStr = uri.toString();			
			String newUriStr = uriStr.replace(
					"org/nasdanika/models/markdown/capability/MarkdownEPackageResourceSetCapabilityFactory.java", 
					"%s/capability/%sEPackageResourceSetCapabilityFactory.java".formatted(getGroupId().replace('.', '/'), getModelJavaName()));
			return URI.createURI(newUriStr);
		};
		return input
				.mapURI(uriMapper)				
				.mapLines(line -> {
					return switch (line.getLineNumber()) {
					case 1 -> replace(
							line, 
							"package org.nasdanika.models.markdown.capability;",
							"package %s.capability;".formatted(getGroupId()));
					case 6 -> replace(
							line, 
							"import org.nasdanika.models.markdown.MarkdownPackage;",
							"import %s.%sPackage;".formatted(getGroupId(), getModelJavaName()));
					case 8, 12 -> replace(
							line, 
							"Markdown",
							getModelJavaName());
					case 17 -> replace(
							line, 
							"https://markdown.models.nasdanika.org",
							getNsURI());
					default -> line;
					};
		});
	}

	//	model/src/main/java/org/nasdanika/models/markdown/capability/MarkdownResourceContentsHandler.java
	protected StringInput modelMarkdownResourceContentsHandler(StringInput input) {
		UnaryOperator<URI> uriMapper = uri -> {
			String uriStr = uri.toString();			
			String newUriStr = uriStr.replace(
					"org/nasdanika/models/markdown/capability/MarkdownResourceContentsHandler.java", 
					"%s/capability/%sResourceContentsHandler.java".formatted(getGroupId().replace('.', '/'), getModelJavaName()));
			return URI.createURI(newUriStr);
		};
		return input
				.mapURI(uriMapper)
				.mapLines(line -> {
					return switch (line.getLineNumber()) {
					case 1 -> replace(
							line, 
							"package org.nasdanika.models.markdown.capability;",
							"package %s.capability;".formatted(getGroupId()));
					case 15 -> replace(
							line, 
							"import org.nasdanika.models.markdown.MarkdownPackage;",
							"import %s.%sPackage;".formatted(getGroupId(), getModelJavaName()));
					case 23 -> replace(
							line, 
							"Markdown",
							getModelJavaName());
					default -> line;
					};
		});
	}

	//	model/src/main/java/org/nasdanika/models/markdown/capability/MarkdownResourceContentsHandlerCapabilityFactory.java
	protected StringInput modelMarkdownResourceContentsHandlerCapabilityFactory(StringInput input) {
		UnaryOperator<URI> uriMapper = uri -> {
			String uriStr = uri.toString();			
			String newUriStr = uriStr.replace(
					"org/nasdanika/models/markdown/capability/MarkdownResourceContentsHandlerCapabilityFactory.java", 
					"%s/capability/%sResourceContentsHandlerCapabilityFactory.java".formatted(getGroupId().replace('.', '/'), getModelJavaName()));
			return URI.createURI(newUriStr);
		};
		return input
				.mapURI(uriMapper)				
				.mapLines(line -> {
					return switch (line.getLineNumber()) {
					case 1 -> replace(
							line, 
							"package org.nasdanika.models.markdown.capability;",
							"package %s.capability;".formatted(getGroupId()));
					case 14, 36 -> replace(
							line, 
							"Markdown",
							getModelJavaName());
					default -> line;
					};
		});
	}

	//	model/src/main/java/org/nasdanika/models/markdown/capability/MarkdownResourceFactoryCapabilityFactory.java
	protected StringInput modelMarkdownResourceFactoryCapabilityFactory(StringInput input) {
		UnaryOperator<URI> uriMapper = uri -> {
			String uriStr = uri.toString();			
			String newUriStr = uriStr.replace(
					"org/nasdanika/models/markdown/capability/MarkdownResourceFactoryCapabilityFactory.java", 
					"%s/capability/%sResourceFactoryCapabilityFactory.java".formatted(getGroupId().replace('.', '/'), getModelJavaName()));
			return URI.createURI(newUriStr);
		};
		return input
				.mapURI(uriMapper)				
				.mapLines(line -> {
					return switch (line.getLineNumber()) {
					case 1 -> replace(
							line, 
							"package org.nasdanika.models.markdown.capability;",
							"package %s.capability;".formatted(getGroupId()));
					case 9 -> replace(
							line, 
							"Markdown",
							getModelJavaName());
					case 22 -> replace(
							line, 
							"md",
							getExtension());
					default -> line;
					};
		});
	}

	public String getExtension() {
		return getModelName();
	}

	//	model/src/main/java/org/nasdanika/models/markdown/Icon.java
	private StringInput modelIcon(StringInput input) {
		UnaryOperator<URI> uriMapper = uri -> {
			String uriStr = uri.toString();			
			String newUriStr = uriStr.replace(
					"org/nasdanika/models/markdown", 
					getGroupId().replace('.', '/'));
			return URI.createURI(newUriStr);
		};
		return input
				.mapURI(uriMapper)				
				.mapLines(line -> {
					return switch (line.getLineNumber()) {
					case 1 -> replace(
							line, 
							"org.nasdanika.models.markdown",
							getGroupId());
					case 8 -> replace(
							line, 
							"DOCUMENT(MarkdownPackage.Literals.DOCUMENT, \"markdown.svg\");",
							"DOCUMENT(%sPackage.Literals.DOCUMENT, \"%s.svg\");".formatted(getModelJavaName(), getModelName()));
					case 10 -> replace(
							line, 
							"https://markdown.models.nasdanika.org",
							getNsURI());
					default -> line;
					};
		});
	}

	//	model/src/test/java/org/nasdanika/models/markdown/tests/MarkdownTests.java
	private StringInput modelMarkdownTests(StringInput input) {
		UnaryOperator<URI> uriMapper = uri -> {
			String uriStr = uri.toString();			
			String newUriStr = uriStr.replace(
					"org/nasdanika/models/markdown/tests/MarkdownTests.java", 
					"%s/tests/%sTests.java".formatted(getGroupId().replace('.', '/'), getModelJavaName()));
			return URI.createURI(newUriStr);
		};
		return input
				.mapURI(uriMapper)				
				.mapLines(line -> {
					return switch (line.getLineNumber()) {
					case 1 -> replace(
							line, 
							"org.nasdanika.models.markdown",
							getGroupId());
					case 22, 25 -> replace(
							line, 
							"Markdown",
							getModelJavaName());
					default -> line;
					};
		});
	}

	//	pom.xml
	private StringInput pomXml(StringInput input) {
		return input.mapLines(line -> {
			return switch (line.getLineNumber()) {
			case 4 -> line.mapLine(l -> l.replace(
					"org.nasdanika.models.markdown", 
					getGroupId()));
			case 6 -> replace(
					line, 
					"2026.6.0",
					getVersion());
			case 8, 9 -> replace(
					line, 
					"Markdown",
					getModelJavaName());
			case 10 -> replace(
					line, 
					"https://markdown.models.nasdanika.org",
					getNsURI());
			case 21 -> replace(
					line, 
					"https://github.com/Nasdanika-Models/markdown",
					getSoureUrl());
			default -> line;
			};
		});

	}

	//	README.md
	private StringInput readmeMd(StringInput input) {
		return input.mapLines(line -> {
			return switch (line.getLineNumber()) {
			case 1 -> replace(
					line, 
					"Markdown",
					getModelJavaName());
			case 3 -> replace(
					line, 
					"https://markdown.models.nasdanika.org",
					getNsURI());
				default -> line;
			};
		});

	}

}
