import org.nasdanika.capability.CapabilityFactory;
import org.nasdanika.models.markdown.handlers.MarkdownToEcoreArrayResourceContentsHandlerCapabilityFactory;
import org.nasdanika.models.markdown.handlers.MarkdownToEcoreResourceContentsHandlerCapabilityFactory;

module org.nasdanika.models.markdown.handlers {
	
	exports org.nasdanika.models.markdown.handlers;
	opens org.nasdanika.models.markdown.handlers to org.nasdanika.common; // For transformer
	
	
	requires transitive org.nasdanika.models.markdown;
	requires org.apache.commons.lang3;
	requires org.eclipse.emf.ecore;
	
	provides CapabilityFactory with 
		MarkdownToEcoreArrayResourceContentsHandlerCapabilityFactory,
		MarkdownToEcoreResourceContentsHandlerCapabilityFactory;
	
}