package org.nasdanika.models.markdown.capability;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import org.nasdanika.capability.emf.ResourceEObjectContentsHandler;
import org.nasdanika.models.markdown.Document;
import org.nasdanika.models.markdown.MarkdownFactory;
import org.nasdanika.models.markdown.loader.MarkdownVisitor;

import com.vladsch.flexmark.ext.attributes.AttributesExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;

public class MarkdownResourceContentsHandler implements ResourceEObjectContentsHandler<Document> {

	@Override
	public Order getOrder() {
		return Order.of(0);
	}

	@Override
	public Document load(InputStream inputStream, Map<?, ?> options) throws IOException {
		try (Reader reader = new InputStreamReader(inputStream)) {
	        MutableDataSet parserOptions = new MutableDataSet();
	        parserOptions.set(Parser.EXTENSIONS, List.of(AttributesExtension.create(), TablesExtension.create()));

	        Parser parser = Parser.builder(parserOptions).build();

	        com.vladsch.flexmark.util.ast.Document document = parser.parseReader(reader);
	        Document ecoreDoc = MarkdownFactory.eINSTANCE.createDocument();
	        new MarkdownVisitor(ecoreDoc).visit(document);
			return ecoreDoc;			
		}
	}	
	
	@Override
	public void save(Document document, OutputStream outputStream, Map<?, ?> options) throws IOException {
		try (Writer writer = new OutputStreamWriter(outputStream)) {
			writer.write(document.getContent()); // Simple writing back.
		}
	}

}
