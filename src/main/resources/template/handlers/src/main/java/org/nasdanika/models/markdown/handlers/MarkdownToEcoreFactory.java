package org.nasdanika.models.markdown.handlers;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EModelElement;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.ETypedElement;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.nasdanika.common.ProgressMonitor;
import org.nasdanika.common.Util;
import org.nasdanika.models.markdown.Attribute;
import org.nasdanika.models.markdown.Heading;
import org.nasdanika.models.markdown.Node;

public class MarkdownToEcoreFactory {
	
	private static final String DERIVED_ATTR = "derived";
	private static final String TYPE_ATTR = "type";
	private static final String REFERS_ATTR = "refers";
	private static final String CONTAINS_ATTR = "contains";
	private static final String SUPER_TYPES_ATTR = "superTypes";
	private static final String OPPOSITE_ATTR = "opposite";
	private static final String MULTIPLICITY_ATTR = "multiplicity";
	private static final String NAME_ATTR = "name";

	/**
	 * Request for creating an {@link EAttribute} from a {@link Heading} with level 4.
	 */
	public record AttributeRequest(Heading heading) {
		
	}
	
	/**
	 * Request for creating an {@link EReference} from a {@link Heading} with level 4.
	 */
	public record ReferenceRequest(Heading heading) {
		
	}
	
	private Resource resource;
	private String markdown;

	public MarkdownToEcoreFactory(Resource resource, String markdown) {
		this.resource = resource;
		this.markdown = markdown;
	}
	
	private String labelToName(String label) {
		String[] cca = label.split("\\s+");
		for (int i=0; i<cca.length; ++i) {
			cca[i] = StringUtils.capitalize(cca[i]);
		}
		return String.join("", cca);
	}
	
	private String getName(Heading heading) {
		int attributesLength = heading.getAttributesEndOffset() - heading.getAttributesStartOffset();
		String name = heading.getText();
		return name.substring(0, name.length() - attributesLength).trim();
	}
		
	@org.nasdanika.common.Transformer.Factory("level.value == 1")
	public EPackage createEPackage(
		Heading heading,
		boolean parallel,
		BiConsumer<Object, BiConsumer<Object,ProgressMonitor>> elementProvider, 
		Consumer<BiConsumer<Map<Object, EModelElement>,ProgressMonitor>> registryProvider,
		ProgressMonitor progressMonitor) {
		
		EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();

		ePackage.setName(StringUtils.uncapitalize(labelToName(getName(heading))));
		for (Attribute attr: heading.getAttributes()) {
			switch (attr.getKey()) {
			case "uri":
				ePackage.setNsURI(attr.getValue());
				break;
			case "prefix":
				ePackage.setNsPrefix(attr.getValue());
				break;
			case NAME_ATTR:
				ePackage.setName(attr.getValue());
				break;
			default:
				// TODO - add an error or a warning to the resource that only uri, prefix and name attributes are supported
			}
		}
		
		int ownContentStartOffset = Math.max(heading.getEndOffset(), heading.getAttributesEndOffset());
		int ownContentEndOffset = heading.getContentEndOffset();		
		
		for (Node child: heading.getChildren()) {
			if (child instanceof Heading h2	&& h2.getLevel().getValue() == 2) {
				ownContentEndOffset = Math.min(ownContentEndOffset, h2.getStartOffset());
				elementProvider.accept(h2, (eClassifier, pm) -> ePackage.getEClassifiers().add((EClassifier) eClassifier));
			}
		}
		
		String documentation = markdown.substring(ownContentStartOffset, ownContentEndOffset).trim();
		if (!Util.isBlank(documentation)) {
			EcoreUtil.setDocumentation(ePackage, documentation.trim());
		}
		
		return ePackage;	
	}
	
	@org.nasdanika.common.Transformer.Factory("level.value == 2")
	public EClassifier createEClassifier(
		Heading heading,
		boolean parallel,
		BiConsumer<Object, BiConsumer<Object,ProgressMonitor>> elementProvider, 
		Consumer<BiConsumer<Map<Object, EModelElement>,ProgressMonitor>> registryProvider,
		ProgressMonitor progressMonitor) {
		
		// TODO - support of EDataType and EEnum. For EClass - abstract, interface 
			
		EClass eClass = EcoreFactory.eINSTANCE.createEClass();
	
		eClass.setName(labelToName(getName(heading)));
		for (Attribute attr: heading.getAttributes()) {
			switch (attr.getKey()) {
			case NAME_ATTR:
				eClass.setName(attr.getValue());
				break;
			default:
				// TODO - add an error or a warning to the resource that only uri, prefix and name attributes are supported
			}
		}
		
		int ownContentStartOffset = Math.max(heading.getEndOffset(), heading.getAttributesEndOffset());
		int ownContentEndOffset = heading.getContentEndOffset();		
		
		for (Node child: heading.getChildren()) {
			if (child instanceof Heading h3	&& h3.getLevel().getValue() == 3) {
				if ("Attributes".equals(h3.getText())) {
					ownContentEndOffset = Math.min(ownContentEndOffset, h3.getStartOffset());
					for (Node attrNode: h3.getChildren()) {
						if (attrNode instanceof Heading h4	&& h4.getLevel().getValue() == 4) {
							elementProvider.accept(new AttributeRequest(h4), (eAttribute, pm) -> eClass.getEStructuralFeatures().add((EStructuralFeature) eAttribute));
						}
					}
				} else if ("References".equals(h3.getText())) {
					ownContentEndOffset = Math.min(ownContentEndOffset, h3.getStartOffset());					
					for (Node refNode: h3.getChildren()) {
						if (refNode instanceof Heading h4	&& h4.getLevel().getValue() == 4) {
							elementProvider.accept(new ReferenceRequest(h4), (eAttribute, pm) -> eClass.getEStructuralFeatures().add((EStructuralFeature) eAttribute));
						}
					}
				}
			}
		}
		
		String documentation = markdown.substring(ownContentStartOffset, ownContentEndOffset).trim();
		if (!Util.isBlank(documentation)) {
			EcoreUtil.setDocumentation(eClass, documentation.trim());
		}
		
		return eClass;	
	}
		
	@org.nasdanika.common.Transformer.Factory
	public EAttribute createEAttribute(
		AttributeRequest attributeRequest,
		boolean parallel,
		BiConsumer<Object, BiConsumer<Object,ProgressMonitor>> configProvider, 
		Consumer<BiConsumer<Map<Object, EModelElement>,ProgressMonitor>> registryProvider,
		ProgressMonitor progressMonitor) {
			
		EAttribute eAttribute = EcoreFactory.eINSTANCE.createEAttribute();
	
		eAttribute.setName(StringUtils.uncapitalize(labelToName(getName(attributeRequest.heading()))));
		for (Attribute attr: attributeRequest.heading().getAttributes()) {
			switch (attr.getKey()) {
			case NAME_ATTR:
				eAttribute.setName(attr.getValue());
				break;
			case MULTIPLICITY_ATTR:
				setMultiplicity(eAttribute, attr.getValue());
				break;
			case DERIVED_ATTR:
				eAttribute.setDerived(true);
				break;	
			default:
				// TODO - add an error or a warning to the resource that only uri, prefix and name attributes are supported
			}
		}
		
		int ownContentStartOffset = Math.max(attributeRequest.heading().getEndOffset(), attributeRequest.heading().getAttributesEndOffset());
		int ownContentEndOffset = attributeRequest.heading().getContentEndOffset();		
		
		String documentation = markdown.substring(ownContentStartOffset, ownContentEndOffset).trim();
		if (!Util.isBlank(documentation)) {
			EcoreUtil.setDocumentation(eAttribute, documentation);
		}
		
		return eAttribute;	
	}
	
	@org.nasdanika.common.Transformer.Factory
	public EReference createEReference(
		ReferenceRequest referenceRequest,
		boolean parallel,
		BiConsumer<Object, BiConsumer<Object,ProgressMonitor>> configProvider, 
		Consumer<BiConsumer<Map<Object, EModelElement>,ProgressMonitor>> registryProvider,
		ProgressMonitor progressMonitor) {
			
		EReference eReference = EcoreFactory.eINSTANCE.createEReference();
	
		eReference.setName(StringUtils.uncapitalize(labelToName(getName(referenceRequest.heading()))));
		for (Attribute attr: referenceRequest.heading().getAttributes()) {
			switch (attr.getKey()) {
			case NAME_ATTR:
				eReference.setName(attr.getValue());
				break;
			case MULTIPLICITY_ATTR:
				setMultiplicity(eReference, attr.getValue());
				break;
			case DERIVED_ATTR:
				eReference.setDerived(true);
				break;	
			default:
				// TODO - add an error or a warning to the resource that only uri, prefix and name attributes are supported
			}
		}
		
		int ownContentStartOffset = Math.max(referenceRequest.heading().getEndOffset(), referenceRequest.heading().getAttributesEndOffset());
		int ownContentEndOffset = referenceRequest.heading().getContentEndOffset();		
		
		String documentation = markdown.substring(ownContentStartOffset, ownContentEndOffset).trim();
		if (!Util.isBlank(documentation)) {
			EcoreUtil.setDocumentation(eReference, documentation);
		}
		
		return eReference;	
	}
	
	// Wire - extends, reference type, opposite
	
	@org.nasdanika.common.Transformer.Wire
	public void wireSuperTypes(
			Heading heading,
			EClass eClass,
			Map<Object, EModelElement> registry,
			int pass,
			ProgressMonitor progressMonitor) {
		
		for (Attribute attr: heading.getAttributes()) {
			if (SUPER_TYPES_ATTR.equals(attr.getKey())) {
				String superTypesStr[] = attr.getValue().split(",");
				for (String superTypeStr: superTypesStr) {
					registry.entrySet().stream()
						.filter(e -> e.getKey() instanceof Heading h && getName(h).equals(superTypeStr.trim()) && e.getValue() instanceof EClass)
						.map(Map.Entry::getValue)
						.map(EClass.class::cast)
						.findFirst()
						.ifPresent(eClass.getESuperTypes()::add);
				}
			}
		}
		
	}
	
	@org.nasdanika.common.Transformer.Wire
	public void wireAttributeType(
			AttributeRequest attributeRequest,
			EAttribute eAttribute,
			Map<Object, EModelElement> registry,
			int pass,
			ProgressMonitor progressMonitor) {
				
		for (Attribute attr: attributeRequest.heading().getAttributes()) {
			if (TYPE_ATTR.equals(attr.getKey())) {
				String typeStr = attr.getValue().trim();
				if (!Util.isBlank(typeStr)) {
					EcoreFactory.eINSTANCE
						.getEPackage()
						.getEClassifiers()
						.stream()
						.filter(e -> e.getName().equals(typeStr))
						.forEach(eAttribute::setEType);
				}
			}
		}
		
		if (eAttribute.getEType() == null) {
			eAttribute.setEType(EcorePackage.Literals.ESTRING); // Default type is String
		}
	}
	
	@org.nasdanika.common.Transformer.Wire
	public void wireReferenceType(
			ReferenceRequest referenceRequest,
			EReference eReference,
			Map<Object, EModelElement> registry,
			int pass,
			ProgressMonitor progressMonitor) {
				
		for (Attribute attr: referenceRequest.heading().getAttributes()) {
			boolean contains = CONTAINS_ATTR.equals(attr.getKey());
			if (contains || REFERS_ATTR.equals(attr.getKey())) {
				eReference.setContainment(contains);
				String typeStr = attr.getValue().trim();
				if (!Util.isBlank(typeStr)) {
					registry.entrySet().stream()
						.filter(e -> e.getKey() instanceof Heading h && getName(h).equals(typeStr.trim()) && e.getValue() instanceof EClass)
						.map(Map.Entry::getValue)
						.map(EClass.class::cast)
						.findFirst()
						.ifPresent(eReference::setEType);
				}
			} 
		}
	}
	
	@org.nasdanika.common.Transformer.Wire(phase = 1)
	public void wireReferenceOpposite(
			ReferenceRequest referenceRequest,
			EReference eReference,
			Map<Object, EModelElement> registry,
			int pass,
			ProgressMonitor progressMonitor) {
		
		for (Attribute attr: referenceRequest.heading().getAttributes()) {
			if (OPPOSITE_ATTR.equals(attr.getKey())) {
				String oppositeStr = attr.getValue().trim();
				EClass eRefType = eReference.getEReferenceType();
				if (!Util.isBlank(oppositeStr) && eRefType != null) {
					registry.entrySet().stream()
						.filter(e -> e.getKey() instanceof ReferenceRequest refReq && getName(refReq.heading()).equals(oppositeStr.trim()) && e.getValue() instanceof EReference eRef && eRef.getEContainingClass() == eRefType)
						.map(Map.Entry::getValue)
						.map(EReference.class::cast)
						.findFirst()
						.ifPresent(eReference::setEOpposite);
				}
			}
		}		
	}

    /**
     * Parses a UML-style multiplicity string and applies the resulting
     * lower/upper bounds to the given typed element.
     *
     * Supported forms:
     *   "*"      -> [0..*]  (unbounded)
     *   "n"      -> [n..n]  (exactly n)
     *   "n..*"   -> [n..*]  (n or more)
     *   "n..m"   -> [n..m]  (between n and m)
     *
     * Unbounded upper is ETypedElement.UNBOUNDED_MULTIPLICITY (-1).
     */
    public static void setMultiplicity(ETypedElement element, String multiplicity) {
        if (element == null) {
            throw new IllegalArgumentException("element must not be null");
        }
        if (multiplicity == null) {
            throw new IllegalArgumentException("multiplicity must not be null");
        }

        String spec = multiplicity.trim();
        if (spec.isEmpty()) {
            throw new IllegalArgumentException("multiplicity must not be empty");
        }

        int lower;
        int upper;

        if ("*".equals(spec)) {
            lower = 0;
            upper = ETypedElement.UNBOUNDED_MULTIPLICITY;
        } else {
            int dots = spec.indexOf("..");
            if (dots < 0) {
                lower = parseBound(spec, false);   // single number -> exactly n
                upper = lower;
            } else {
                lower = parseBound(spec.substring(0, dots).trim(), false);
                upper = parseBound(spec.substring(dots + 2).trim(), true);
                if (upper != ETypedElement.UNBOUNDED_MULTIPLICITY && upper < lower) {
                    throw new IllegalArgumentException(
                        "upper bound (" + upper + ") < lower bound (" + lower
                            + ") in multiplicity \"" + multiplicity + "\"");
                }
            }
        }

        element.setLowerBound(lower);
        element.setUpperBound(upper);
    }

    private static int parseBound(String token, boolean allowStar) {
        if (allowStar && "*".equals(token)) {
            return ETypedElement.UNBOUNDED_MULTIPLICITY;
        }
        final int value;
        try {
            value = Integer.parseInt(token);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("invalid multiplicity bound: \"" + token + "\"", e);
        }
        if (value < 0) {
            throw new IllegalArgumentException("multiplicity bound must be >= 0: " + value);
        }
        return value;
    }
    
}
