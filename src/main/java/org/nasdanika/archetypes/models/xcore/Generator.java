package org.nasdanika.archetypes.models.xcore;

import java.io.File;
import java.io.OutputStream;
import java.util.stream.Stream;

import org.eclipse.emf.common.util.URI;
import org.nasdanika.common.Input;
import org.nasdanika.common.Output;
import org.nasdanika.common.StreamInput;
import org.nasdanika.common.StreamOutput;
import org.nasdanika.common.StringInput;
import org.nasdanika.common.Util;

public class Generator {
	
	public void generate(File outputDir) {
		outputDir.mkdirs();
		URI outputBase = URI.createFileURI(outputDir.getAbsolutePath()).appendSegment("");						
		Output<OutputStream> output = StreamOutput.INSTANCE.base(outputBase);
		try (Stream<StreamInput> inputs = StreamInput.of(Generator.class)) {
			inputs
				.filter(i -> !Util.isBlank(i.getURI().lastSegment()))
				.flatMap(Input.subpath("template/**"))
				.map(StringInput::ofStreamInput)
				.map(Input.mapMatch(
						si -> {
							System.out.println(">>>> File " + si.getURI());
							return si;
						}, 
						"tests/TestStreams.java"))
				.forEach(si -> {
					System.out.println("File " + si.getURI());
					System.out.println("Writing " + si.getURI() + " to " + si.getURI().resolve(outputBase));
//					si.lines().forEach(line -> {
//						System.out.println("\t" + line.getLineNumber() + ":\t" + line.getLine());
//					});
				});
				
//				.map(StreamInput::of)
//				.forEach(input -> {
////					try {
////						input.transferTo(output);
////					} catch (IOException e) {
////						throw new NasdanikaException(e);
////					}
//				});
		}

		// Verify that some files were written to target/descrs
		String[] written = outputDir.list();
		System.out.println("Written to " + outputDir.getAbsolutePath() + ":");
		if (written != null) {
			for (String name : written) {
				System.out.println("  " + name);
			}
		}
		
		
	}
	
}
