package org.nasdanika.models.markdown.tests;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.BiConsumer;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.nasdanika.capability.CapabilityLoader;
import org.nasdanika.capability.ServiceCapabilityFactory;
import org.nasdanika.capability.ServiceCapabilityFactory.Requirement;
import org.nasdanika.capability.emf.ResourceSetRequirement;
import org.nasdanika.common.PrintStreamProgressMonitor;
import org.nasdanika.common.ProgressMonitor;
import org.nasdanika.models.markdown.Document;
import org.nasdanika.models.markdown.MarkdownFactory;
import org.nasdanika.models.markdown.loader.MarkdownVisitor;

import com.vladsch.flexmark.ext.attributes.AttributesExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.ast.NodeVisitor;
import com.vladsch.flexmark.util.ast.Visitor;
import com.vladsch.flexmark.util.data.MutableDataSet;

public class MarkdownTests {
	
	@Test
	public void testVisitMarkdown() throws Exception {
        MutableDataSet options = new MutableDataSet();
        options.set(Parser.EXTENSIONS, List.of(AttributesExtension.create(), TablesExtension.create()));

        Parser parser = Parser.builder(options).build();

        String source = Files.readString(Path.of("src/test/resources/product-domain.md")); 
        Node document = parser.parse(source);
        
        NodeVisitor visitor = new NodeVisitor() {
        	
        	@Override
        	protected void processNode(
        			@NotNull Node node, 
        			boolean withChildren,
        			@NotNull BiConsumer<Node, Visitor<Node>> processor) {
        		super.processNode(node, withChildren, processor);
        		
        		System.out.println("Processing node: " + node.getClass().getSimpleName() + "@" + node.getStartLineNumber() + " with text: " + node.getChars());
        		System.out.println("\tSegments: ");	        		
        		System.out.println("\tParent: " + node.getParent());
        	}
        	        	
        };
		
        visitor.visit(document);
	}
		
	@Test
	public void testMarkdownVisitor() throws Exception {
        // ── 1. Configure parser with the Attributes extension ─────────────────
        MutableDataSet options = new MutableDataSet();
        options.set(Parser.EXTENSIONS, List.of(AttributesExtension.create()));

        Parser parser = Parser.builder(options).build();

        // ── 2. Parse source ───────────────────────────────────────────────────
        String source = """        		
                # Heading {id="intro" class="title"}

                A paragraph with **bold** text. {lang="en"}

                - item one {.highlight}
                - item two
                
                ## Heading 2
                
                My important info
                
                ### Heading 3  {id="intro 3" class="title 3"}
                
                Even more important info
                
                ## Heading 2 again
                
                purum param {#id .class key=value}
                
                # Heading 1 again
                
                zusu
                
                ########## Heading 10 {#id .class key=value}

                
                """;

        com.vladsch.flexmark.util.ast.@NotNull Document document = parser.parse(source);

        // ── 3. Bootstrap the Ecore root ───────────────────────────────────────
        //  Replace MarkdownFactory / MarkdownPackage with your generated classes.
        Document ecoreDoc = MarkdownFactory.eINSTANCE.createDocument();

        // ── 4. Traverse the AST and populate the model ────────────────────────
        new MarkdownVisitor(ecoreDoc).visit(document);

        System.out.println("Ecore model populated: " + ecoreDoc);
        
		CapabilityLoader capabilityLoader = new CapabilityLoader();
		ProgressMonitor progressMonitor = new PrintStreamProgressMonitor();
		Requirement<ResourceSetRequirement, ResourceSet> requirement = ServiceCapabilityFactory.createRequirement(ResourceSet.class);		
		ResourceSet resourceSet = capabilityLoader.loadOne(requirement, progressMonitor);
        
		File markdownFile = new File("target/markdown.yaml").getCanonicalFile();
		Resource markdownResource = resourceSet.createResource(URI.createFileURI(markdownFile.getAbsolutePath()));
		markdownResource.getContents().add(ecoreDoc);
		markdownResource.save(null);		
	}
	
	@Test
	public void testMarkdownResource() throws Exception {
		CapabilityLoader capabilityLoader = new CapabilityLoader();
		ProgressMonitor progressMonitor = new PrintStreamProgressMonitor();
		Requirement<ResourceSetRequirement, ResourceSet> requirement = ServiceCapabilityFactory.createRequirement(ResourceSet.class);		
		ResourceSet resourceSet = capabilityLoader.loadOne(requirement, progressMonitor);
        
		File markdownFile = new File("src/test/resources/product-domain.md").getCanonicalFile();
		Resource markdownResource = resourceSet.getResource(URI.createFileURI(markdownFile.getAbsolutePath()), true);		
		Document document = (Document) markdownResource.getContents().get(0);
		
		File xmlFile = new File("target/product-domain.xml").getCanonicalFile();
		Resource xmlResource = resourceSet.createResource(URI.createFileURI(xmlFile.getAbsolutePath()));
		xmlResource.getContents().add(EcoreUtil.copy(document));
		xmlResource.save(null);		
	}	

}
