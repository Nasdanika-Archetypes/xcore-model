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
	
	requires flexmark;
	requires flexmark.profile.pegdown;
	requires transitive flexmark.util.ast;
	requires flexmark.util.collection;
	requires flexmark.util.visitor;
	requires flexmark.util.data;
	requires flexmark.util.misc;
	requires flexmark.util.sequence;
	requires flexmark.util.builder;
	requires flexmark.ext.attributes;
	requires flexmark.ext.tables;
	requires flexmark.ext.definition;
	requires flexmark.ext.gfm.tasklist; 	
	
	provides CapabilityFactory with 
		MarkdownEPackageResourceSetCapabilityFactory,
		MarkdownResourceFactoryCapabilityFactory,
		MarkdownResourceContentsHandlerCapabilityFactory,
		MarkdownArrayResourceContentsHandlerCapabilityFactory;
	
}