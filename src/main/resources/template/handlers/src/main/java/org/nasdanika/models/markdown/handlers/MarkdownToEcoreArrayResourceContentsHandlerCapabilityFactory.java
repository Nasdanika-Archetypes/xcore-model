package org.nasdanika.models.markdown.handlers;

import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import org.apache.commons.lang3.stream.Streams;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.nasdanika.capability.CapabilityProvider;
import org.nasdanika.capability.ServiceCapabilityFactory;
import org.nasdanika.capability.emf.ResourceContentsHandler;
import org.nasdanika.common.ProgressMonitor;
import org.nasdanika.models.markdown.Document;

public class MarkdownToEcoreArrayResourceContentsHandlerCapabilityFactory extends ServiceCapabilityFactory<org.nasdanika.capability.emf.ResourceContentsHandler.Requirement, ResourceContentsHandler<EObject[]>> {

	@Override
	public boolean isFor(Class<?> type, Object serviceRequirement) {
		return ResourceContentsHandler.class.equals(type) 
				&& serviceRequirement instanceof ResourceContentsHandler.Requirement handlerRequirement
				&& EObject[].class.equals(handlerRequirement.getContentsType())
				&& "ecore".equalsIgnoreCase(handlerRequirement.getQualifiers()[handlerRequirement.getQualifierIndex()])
				&& handlerRequirement.getQualifiers().length > 0;
	}

	@Override
	protected CompletionStage<Iterable<CapabilityProvider<ResourceContentsHandler<EObject[]>>>> createService(
			Class<ResourceContentsHandler<EObject[]>> serviceType, 
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
	
	private Iterable<CapabilityProvider<ResourceContentsHandler<EObject[]>>> createHandler(
			Iterable<CapabilityProvider<ResourceContentsHandler<Document>>> providers,
			Resource resource) {
		
		Function<CapabilityProvider<ResourceContentsHandler<Document>>, CapabilityProvider<ResourceContentsHandler<EObject[]>>> mapper = provider -> provider.map(documentHandler -> createHandler(resource, documentHandler));
		return Streams.of(providers).map(mapper).toList();
	}
	

	protected ResourceContentsHandler<EObject[]> createHandler(Resource resource, ResourceContentsHandler<Document> dh) {
		return new MarkdownToEcoreResourceContentsHandler(resource, dh).adaptToEObjectArrayContentsHandler();
	}
	
}

