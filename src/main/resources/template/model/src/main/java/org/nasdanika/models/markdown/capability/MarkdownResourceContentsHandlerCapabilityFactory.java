package org.nasdanika.models.markdown.capability;

import java.util.concurrent.CompletionStage;

import org.nasdanika.capability.CapabilityProvider;
import org.nasdanika.capability.ServiceCapabilityFactory;
import org.nasdanika.capability.emf.ResourceContentsHandler;
import org.nasdanika.common.ProgressMonitor;
import org.nasdanika.models.markdown.Document;

/**
 * Provides an {@link OpenAIClient} instance.  
 */
public class MarkdownResourceContentsHandlerCapabilityFactory extends ServiceCapabilityFactory<org.nasdanika.capability.emf.ResourceContentsHandler.Requirement, ResourceContentsHandler<Document>> {

	@Override
	public boolean isFor(Class<?> type, Object serviceRequirement) {
		return ResourceContentsHandler.class.equals(type) 
				&& serviceRequirement instanceof ResourceContentsHandler.Requirement handlerRequirement
				&& match(handlerRequirement);
	}

	private boolean match(org.nasdanika.capability.emf.ResourceContentsHandler.Requirement handlerRequirement) {
		return Document.class.equals(handlerRequirement.getContentsType())
				&& handlerRequirement.getQualifierIndex() == 0
				&& "md".equalsIgnoreCase(handlerRequirement.getQualifiers()[0]);
	}

	@Override
	protected CompletionStage<Iterable<CapabilityProvider<ResourceContentsHandler<Document>>>> createService(
			Class<ResourceContentsHandler<Document>> serviceType, 
			org.nasdanika.capability.emf.ResourceContentsHandler.Requirement serviceRequirement, 
			final Loader loader,
			ProgressMonitor progressMonitor) {
		
		return wrap(new MarkdownResourceContentsHandler());
	}

}
