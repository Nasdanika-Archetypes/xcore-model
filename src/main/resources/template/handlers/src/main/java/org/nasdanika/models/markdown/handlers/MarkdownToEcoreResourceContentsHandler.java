package org.nasdanika.models.markdown.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EModelElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.nasdanika.capability.emf.ResourceContentsHandler;
import org.nasdanika.common.LoggerProgressMonitor;
import org.nasdanika.common.ProgressMonitor;
import org.nasdanika.common.Transformer;
import org.nasdanika.models.markdown.Document;
import org.nasdanika.models.markdown.Heading;
import org.nasdanika.models.markdown.HeadingLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MarkdownToEcoreResourceContentsHandler implements ResourceContentsHandler<EPackage[]> {

	private static final Logger LOGGER = LoggerFactory.getLogger(MarkdownToEcoreResourceContentsHandler.class);

	private ResourceContentsHandler<Document> documentHandler;

	private Resource resource;	
	
	public MarkdownToEcoreResourceContentsHandler(Resource resource, ResourceContentsHandler<Document> docuentHandler) {
		this.resource = resource;
		this.documentHandler = docuentHandler;
	}
	
	private EPackage[] map(Document document) {		
		return document.getChildren()
			.stream()
			.map(e -> {
				if (e instanceof Heading heading && heading.getLevel() == HeadingLevel.H1) {
					return mapHeadingToEpackage(document.getContent(), heading);
				}
				// TODO - add an error or a warning to the resource that only H1 headings are supported
				return e;
			})
			.toArray(EPackage[]::new);
	}

	private EPackage mapHeadingToEpackage(String markdown, Heading heading) {
		Transformer<Object,EModelElement> markdownTransformer = new Transformer<>(new MarkdownToEcoreFactory(resource, markdown));				
		try (ProgressMonitor progressMonitor = new LoggerProgressMonitor(LOGGER)) {
			return (EPackage) markdownTransformer.transform(List.of(heading), false, progressMonitor).get(heading);
		}		
	}

	@Override
	public Order getOrder() {
		return documentHandler.getOrder().add(0);
	}

	@Override
	public EPackage[] load(InputStream inputStream, Map<?, ?> options) throws IOException {
		return map(documentHandler.load(inputStream, options));
	}
	
	public ResourceContentsHandler<EObject[]> adaptToEObjectArrayContentsHandler() {
		return adapt(
				pa -> Arrays.stream(pa).toArray(EObject[]::new),
				ea -> Arrays.stream(ea).toArray(EPackage[]::new));
	}		

}