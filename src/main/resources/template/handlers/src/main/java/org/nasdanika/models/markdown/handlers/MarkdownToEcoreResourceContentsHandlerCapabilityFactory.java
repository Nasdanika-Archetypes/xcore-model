package org.nasdanika.models.markdown.handlers;

import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import org.apache.commons.lang3.stream.Streams;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.nasdanika.capability.CapabilityProvider;
import org.nasdanika.capability.ServiceCapabilityFactory;
import org.nasdanika.capability.emf.ResourceContentsHandler;
import org.nasdanika.common.ProgressMonitor;
import org.nasdanika.models.markdown.Document;

public class MarkdownToEcoreResourceContentsHandlerCapabilityFactory extends ServiceCapabilityFactory<org.nasdanika.capability.emf.ResourceContentsHandler.Requirement, ResourceContentsHandler<EPackage[]>> {

	@Override
	public boolean isFor(Class<?> type, Object serviceRequirement) {
		return ResourceContentsHandler.class.equals(type) 
				&& serviceRequirement instanceof ResourceContentsHandler.Requirement handlerRequirement
				&& EPackage[].class.equals(handlerRequirement.getContentsType())
				&& "ecore".equalsIgnoreCase(handlerRequirement.getQualifiers()[handlerRequirement.getQualifierIndex()])
				&& handlerRequirement.getQualifiers().length > 0;
	}

	@Override
	protected CompletionStage<Iterable<CapabilityProvider<ResourceContentsHandler<EPackage[]>>>> createService(
			Class<ResourceContentsHandler<EPackage[]>> serviceType, 
			org.nasdanika.capability.emf.ResourceContentsHandler.Requirement serviceRequirement, 
			final Loader loader,
			ProgressMonitor progressMonitor) {

		ResourceContentsHandler.Requirement handlerRequirement = ResourceContentsHandler.createRequirement(
				serviceRequirement.getResource(),				
				Document.class, 
				serviceRequirement.getQualifiers(),
				serviceRequirement.getQualifierIndex() - 1);
		
		@SuppressWarnings("rawtypes")
		ServiceCapabilityFactory.Requirement<org.nasdanika.capability.emf.ResourceContentsHandler.Requirement, ResourceContentsHandler> markdownServiceRequirement = ServiceCapabilityFactory.createRequirement(ResourceContentsHandler.class, null, handlerRequirement);		
		CompletionStage<Iterable<CapabilityProvider<ResourceContentsHandler<Document>>>> documentHandlerCS = loader.load(markdownServiceRequirement, progressMonitor);
		
		return documentHandlerCS.thenApply(providers -> createHandler(providers, serviceRequirement.getResource()));
	}	
	
	private Iterable<CapabilityProvider<ResourceContentsHandler<EPackage[]>>> createHandler(
			Iterable<CapabilityProvider<ResourceContentsHandler<Document>>> providers,
			Resource resource) {
		
		Function<CapabilityProvider<ResourceContentsHandler<Document>>, CapabilityProvider<ResourceContentsHandler<EPackage[]>>> mapper = provider -> provider.map(documentHandler -> createHandler(resource, documentHandler));
		return Streams.of(providers).map(mapper).toList();
	}
	

	protected MarkdownToEcoreResourceContentsHandler createHandler(Resource resource, ResourceContentsHandler<Document> dh) {
		return new MarkdownToEcoreResourceContentsHandler(resource, dh);
	}
	
}

