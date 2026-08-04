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

public class MarkdownTests {
	
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
