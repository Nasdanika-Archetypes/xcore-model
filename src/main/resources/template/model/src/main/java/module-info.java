import org.nasdanika.capability.CapabilityFactory;
import org.nasdanika.models.markdown.capability.MarkdownArrayResourceContentsHandlerCapabilityFactory;
import org.nasdanika.models.markdown.capability.MarkdownEPackageResourceSetCapabilityFactory;
import org.nasdanika.models.markdown.capability.MarkdownResourceContentsHandlerCapabilityFactory;
import org.nasdanika.models.markdown.capability.MarkdownResourceFactoryCapabilityFactory;

module org.nasdanika.models.markdown {
	
	exports org.nasdanika.models.markdown;
	exports org.nasdanika.models.markdown.impl;
	exports org.nasdanika.models.markdown.util;
	exports org.nasdanika.models.markdown.loader;
	
	requires transitive org.eclipse.emf.ecore;
	requires transitive org.eclipse.emf.common;
	requires transitive org.nasdanika.capability;
	
	provides CapabilityFactory with 
		MarkdownEPackageResourceSetCapabilityFactory,
		MarkdownResourceFactoryCapabilityFactory,
		MarkdownResourceContentsHandlerCapabilityFactory,
		MarkdownArrayResourceContentsHandlerCapabilityFactory;
	
}