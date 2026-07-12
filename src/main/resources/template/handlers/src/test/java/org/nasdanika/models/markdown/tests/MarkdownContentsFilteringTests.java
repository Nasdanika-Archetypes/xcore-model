package org.nasdanika.models.markdown.tests;

import java.io.File;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.junit.jupiter.api.Test;
import org.nasdanika.capability.CapabilityLoader;
import org.nasdanika.capability.ServiceCapabilityFactory;
import org.nasdanika.capability.ServiceCapabilityFactory.Requirement;
import org.nasdanika.capability.emf.ResourceSetRequirement;
import org.nasdanika.common.PrintStreamProgressMonitor;
import org.nasdanika.common.ProgressMonitor;

public class MarkdownContentsFilteringTests {
		
	@Test
	public void testFamilyEcoreMarkdownResource() throws Exception {
		CapabilityLoader capabilityLoader = new CapabilityLoader();
		ProgressMonitor progressMonitor = new PrintStreamProgressMonitor();
		Requirement<ResourceSetRequirement, ResourceSet> requirement = ServiceCapabilityFactory.createRequirement(ResourceSet.class);		
		ResourceSet resourceSet = capabilityLoader.loadOne(requirement, progressMonitor);
        
		File markdownFile = new File("src/test/resources/family.ecore.md").getCanonicalFile();
		Resource markdownResource = resourceSet.getResource(URI.createFileURI(markdownFile.getAbsolutePath()), true);		
		EPackage ePackage = (EPackage) markdownResource.getContents().get(0);
		
		File ecoreFile = new File("target/family.ecore").getCanonicalFile();
		Resource ecoreResource = resourceSet.createResource(URI.createFileURI(ecoreFile.getAbsolutePath()));
		ecoreResource.getContents().add(EcoreUtil.copy(ePackage));
		ecoreResource.save(null);
	}	

}
