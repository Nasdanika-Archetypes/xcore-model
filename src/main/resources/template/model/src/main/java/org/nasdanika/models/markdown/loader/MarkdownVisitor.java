package org.nasdanika.models.markdown.loader;

import java.util.IdentityHashMap;
import java.util.Map;

import org.nasdanika.models.markdown.Attributable;
import org.nasdanika.models.markdown.Block;
import org.nasdanika.models.markdown.FencedDiv;
import org.nasdanika.models.markdown.MarkdownFactory;
import org.nasdanika.models.markdown.TableCellAlignment;

import com.vladsch.flexmark.ast.BulletList;
import com.vladsch.flexmark.ast.BulletListItem;
import com.vladsch.flexmark.ast.FencedCodeBlock;
import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ast.InlineLinkNode;
import com.vladsch.flexmark.ast.Link;
import com.vladsch.flexmark.ast.LinkNode;
import com.vladsch.flexmark.ast.LinkNodeBase;
import com.vladsch.flexmark.ast.OrderedList;
import com.vladsch.flexmark.ast.OrderedListItem;
import com.vladsch.flexmark.ast.Paragraph;
import com.vladsch.flexmark.ext.attributes.AttributeNode;
import com.vladsch.flexmark.ext.attributes.AttributesNode;
import com.vladsch.flexmark.ext.definition.DefinitionItem;
import com.vladsch.flexmark.ext.definition.DefinitionList;
import com.vladsch.flexmark.ext.definition.DefinitionTerm;
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListItem;
import com.vladsch.flexmark.ext.tables.TableBlock;
import com.vladsch.flexmark.ext.tables.TableBody;
import com.vladsch.flexmark.ext.tables.TableCaption;
import com.vladsch.flexmark.ext.tables.TableCell;
import com.vladsch.flexmark.ext.tables.TableCell.Alignment;
import com.vladsch.flexmark.ext.tables.TableHead;
import com.vladsch.flexmark.ext.tables.TableRow;
import com.vladsch.flexmark.ext.tables.TableSeparator;
import com.vladsch.flexmark.util.ast.ContentNode;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.ast.NodeVisitor;
import com.vladsch.flexmark.util.ast.VisitHandler;

public class MarkdownVisitor {
	
    private final org.nasdanika.models.markdown.Document root;          // your Ecore root
    
    private Map<Node, org.nasdanika.models.markdown.Node> nodeMap = new IdentityHashMap<>();  // to keep track of created Ecore nodes for each visited AST node
    
    private org.nasdanika.models.markdown.Block currentContainerBlock;  
    
    private static final String FENCED_DIV_PREFIX = ":::";

    public MarkdownVisitor(org.nasdanika.models.markdown.Document root) {
        this.root = root;
        this.currentContainerBlock = root;   // start with root as the current container
    }

    // ── visitor wiring ────────────────────────────────────────────────────────
    private final NodeVisitor visitor = new NodeVisitor(
        new VisitHandler<>(Heading.class, this::visitHeading),
        new VisitHandler<>(Paragraph.class, this::visitParagraph),
        
        // Lists
        new VisitHandler<>(OrderedList.class, this::visitOrderedList),
        new VisitHandler<>(BulletList.class, this::visitBulletList),
        new VisitHandler<>(DefinitionList.class, this::visitDefinitionList),
        new VisitHandler<>(BulletListItem.class, this::visitBulletListItem),
        new VisitHandler<>(DefinitionItem.class, this::visitDefinitionItem),
        new VisitHandler<>(DefinitionTerm.class, this::visitDefinitionTerm),
        new VisitHandler<>(OrderedListItem.class, this::visitOrderedListItem),
        new VisitHandler<>(TaskListItem.class, this::visitTaskListItem),
        
        // Tables
        new VisitHandler<>(TableBlock.class, this::visitTableBlock),
        new VisitHandler<>(TableHead.class, this::visitTableHead),
        new VisitHandler<>(TableSeparator.class, this::visitTableSeparator),
        new VisitHandler<>(TableBody.class, this::visitTableBody),
        new VisitHandler<>(TableRow.class, this::visitTableRow),
        new VisitHandler<>(TableCell.class, this::visitTableCell),
        new VisitHandler<>(TableCaption.class, this::visitTableCaption),
        
        new VisitHandler<>(Link.class, this::visitLink),
        
        new VisitHandler<>(FencedCodeBlock.class, this::visitCode),
        new VisitHandler<>(AttributesNode.class, this::visitAttributes)
    );

    public void visit(Document document) {
    	populateContentNode(document, root);
        visitor.visit(document);
        root.setContentEndOffset(root.getEndOffset());
        root.setContent(root.getChars());
        setChildrenContent(root.getChars(), root);        
    }
    
//    private int getInnerEndOffset(org.nasdanika.models.markdown.Node node) {
//    	return node instanceof FencedDiv fencedDiv && fencedDiv.getClosingStartOffset() > 0 ? fencedDiv.getClosingStartOffset() : node.getEndOffset();
//	}
//    
//    private int getOuterEndOffset(org.nasdanika.models.markdown.Node node) {
//    	return node instanceof FencedDiv fencedDiv && fencedDiv.getClosingEndOffset() > 0 ? fencedDiv.getClosingEndOffset() : node.getEndOffset();
//	}
    
    private void setChildrenContent(String docContent, org.nasdanika.models.markdown.Node node) {		
		if (node instanceof FencedDiv fencedDiv && fencedDiv.getClosingStartOffset() > 0) {
			fencedDiv.setContent(docContent.substring(fencedDiv.getStartOffset(), fencedDiv.getClosingStartOffset()));
			fencedDiv.setContentEndOffset(fencedDiv.getClosingStartOffset());
		}
		
    	org.nasdanika.models.markdown.Node childNode = null;
    	for (org.nasdanika.models.markdown.Node cn: node.getChildren()) {
    		if (childNode != null) {
    			childNode.setContent(docContent.substring(childNode.getStartOffset(), cn.getStartOffset()));
    			childNode.setContentEndOffset(cn.getStartOffset());
			}
    		childNode = cn;
		}
		if (childNode != null) {
			childNode.setContent(docContent.substring(childNode.getStartOffset(), node.getContentEndOffset()));
			childNode.setContentEndOffset(node.getContentEndOffset());
		}    	
		
		// Second pass with content end offsets set
    	for (org.nasdanika.models.markdown.Node cn: node.getChildren()) {
    		setChildrenContent(docContent, cn);    		
		}		
    }    
    
    private void populateNode(Node node, org.nasdanika.models.markdown.Node ecoreNode) {
    	ecoreNode.setStartOffset(node.getStartOffset());
    	ecoreNode.setStartLineNumber(node.getStartLineNumber());
    	
    	ecoreNode.setEndOffset(node.getEndOffset());
    	ecoreNode.setEndLineNumber(node.getEndLineNumber());
    	
    	ecoreNode.setChars(node.getChars().toString());
	}
    
    private void populateContentNode(ContentNode node, org.nasdanika.models.markdown.ContentNode ecoreNode) {
    	populateNode(node, ecoreNode);
	}

    // ── handler implementations ───────────────────────────────────────────────

    private void visitHeading(Heading node) {
    	org.nasdanika.models.markdown.Heading heading = getFactory().createHeading();
    	populateContentNode(node, heading);
        int nodeLevel = node.getLevel();
		heading.setLevel(org.nasdanika.models.markdown.HeadingLevel.get(nodeLevel));
        heading.setText(node.getText().toString());
        while (currentContainerBlock instanceof org.nasdanika.models.markdown.Heading currentHeading
				&& currentHeading.getLevel().getValue() >= heading.getLevel().getValue()) {
			currentContainerBlock = (org.nasdanika.models.markdown.Block) currentContainerBlock.eContainer();
		}
        currentContainerBlock.getChildren().add(heading);
        nodeMap.put(node, heading);
        currentContainerBlock = heading; 

        visitor.visitChildren(node);   // recurse so AttributesNode is caught
    }

	protected MarkdownFactory getFactory() {
		return MarkdownFactory.eINSTANCE;
	}
	
	private FencedDiv matchOpeningFencedDiv(int fencedDivLevel) {
		for (Block containerBlock = currentContainerBlock; containerBlock != null || containerBlock != root; containerBlock = (org.nasdanika.models.markdown.Block) containerBlock.eContainer()) {
			if (containerBlock instanceof FencedDiv currentFencedDiv && currentFencedDiv.getLevel() <= fencedDivLevel) {
				return currentFencedDiv;
			}
		}
		return null;
	}
		
	private static int getFencedDivLevel(String text) {
		int level = 0;
		while (text.startsWith(FENCED_DIV_PREFIX, level)) {
			++level;
		}
		return level;		
	}	

    private void visitParagraph(Paragraph node) {    	
    	String text = node.getChars().toString();
		int fencedDivLevel = getFencedDivLevel(text);
    	org.nasdanika.models.markdown.Paragraph para = fencedDivLevel == 0 ? getFactory().createParagraph() : getFactory().createFencedDiv();    	    	
		boolean setAsContainer = false;
    	if (fencedDivLevel > 0) {
    		FencedDiv fencedDiv = (FencedDiv) para;
			fencedDiv.setLevel(fencedDivLevel);
			fencedDiv.setText(node.getChars().toString().substring(fencedDivLevel + 2).trim());

    		if (text.trim().length() == FENCED_DIV_PREFIX.length() + fencedDivLevel - 1) { // Closing fenced div marker should have only the colons and optional whitespace
				FencedDiv openingFencedDiv = matchOpeningFencedDiv(fencedDivLevel);
				if (openingFencedDiv != null) {
					openingFencedDiv.setClosingStartLineNumber(node.getStartLineNumber());
					openingFencedDiv.setClosingStartOffset(node.getStartOffset());
					openingFencedDiv.setClosingEndLineNumber(node.getEndLineNumber());
					openingFencedDiv.setClosingEndOffset(node.getEndOffset());
	    			currentContainerBlock = (org.nasdanika.models.markdown.Block) openingFencedDiv.eContainer();	
					return;
				}
			} else {
				setAsContainer = true;					
			}
		} 
    	
    	populateContentNode(node, para);
//      getLineIndent(int)
//      getLineIndents()
//      getSegments()
//      hasTableSeparator()
//      isTrailingBlankLine()
    	
    	addToParent(node, para);
    	if (setAsContainer) {
    		currentContainerBlock = para;
    	}
        
        visitor.visitChildren(node);
    }
    
//    private void visitNode(Node node) {    	
//    	System.out.println("Visiting node: " + node.getClass().getSimpleName() + "@" + node.getStartLineNumber() + " with text: " + node.getChars());
//    	visitor.visitChildren(node);    	
//    }
    
    private void addToParent(Node node, org.nasdanika.models.markdown.Node ecoreNode) {
    	for (Node parent = node.getParent(); parent != null; parent = parent.getParent()) {
			if (nodeMap.get(parent) instanceof org.nasdanika.models.markdown.Node parentNode) {
				parentNode.getChildren().add(ecoreNode);
				break;
			}
		}
    	
    	if (ecoreNode.eContainer() == null || ecoreNode.eContainer() == root) {
            currentContainerBlock.getChildren().add(ecoreNode);
		}
    	
		nodeMap.put(node, ecoreNode);
	}
    
    // Lists
    private void visitOrderedList(OrderedList node) {
    	org.nasdanika.models.markdown.OrderedList orderedList = getFactory().createOrderedList();
    	populateContentNode(node, orderedList);
    	addToParent(node, orderedList);
    	orderedList.setTight(node.isTight());
    	orderedList.setStartNumber(node.getStartNumber());
    	orderedList.setDelimiter(String.valueOf(node.getDelimiter()));
        visitor.visitChildren(node);
    }

    private void visitBulletList(BulletList node) {
    	org.nasdanika.models.markdown.BulletList bulletList = getFactory().createBulletList();
    	populateContentNode(node, bulletList);
    	addToParent(node, bulletList);
    	bulletList.setTight(node.isTight());
    	bulletList.setOpeningMarker(String.valueOf(node.getOpeningMarker()));
        visitor.visitChildren(node);
    }

    private void visitDefinitionList(DefinitionList node) {
    	org.nasdanika.models.markdown.DefinitionList definitionList = getFactory().createDefinitionList();
    	populateContentNode(node, definitionList);
    	addToParent(node, definitionList);
    	definitionList.setTight(node.isTight());
        visitor.visitChildren(node);
    }

    private void visitBulletListItem(BulletListItem node) {
    	org.nasdanika.models.markdown.DefinitionList definitionList = getFactory().createDefinitionList();
    	populateContentNode(node, definitionList);
    	addToParent(node, definitionList);
        visitor.visitChildren(node);
    }

    private void visitDefinitionItem(DefinitionItem node) {
    	org.nasdanika.models.markdown.DefinitionItem definitionItem = getFactory().createDefinitionItem();
    	populateContentNode(node, definitionItem);
    	addToParent(node, definitionItem);
        visitor.visitChildren(node);
    }

    private void visitDefinitionTerm(DefinitionTerm node) {
    	org.nasdanika.models.markdown.DefinitionTerm definitionTerm = getFactory().createDefinitionTerm();
    	populateContentNode(node, definitionTerm);
    	addToParent(node, definitionTerm);
        visitor.visitChildren(node);
    }

    private void visitOrderedListItem(OrderedListItem node) {
    	org.nasdanika.models.markdown.OrderedListItem orderedListItem = getFactory().createOrderedListItem();
    	populateContentNode(node, orderedListItem);
    	addToParent(node, orderedListItem);
        visitor.visitChildren(node);
    }

    private void visitTaskListItem(TaskListItem node) {
    	org.nasdanika.models.markdown.TaskListItem taskListItem = getFactory().createTaskListItem();
    	populateContentNode(node, taskListItem);
    	addToParent(node, taskListItem);
        visitor.visitChildren(node);
    }
    
    // Tables
    private void visitTableBlock(TableBlock node) {
    	org.nasdanika.models.markdown.TableBlock tableBlock = getFactory().createTableBlock();
    	populateContentNode(node, tableBlock);
    	addToParent(node, tableBlock);
        visitor.visitChildren(node);
    }

    private void visitTableHead(TableHead node) {
    	org.nasdanika.models.markdown.TableHead tableHead = getFactory().createTableHead();
    	populateNode(node, tableHead);
    	addToParent(node, tableHead);
        visitor.visitChildren(node);
    }

    private void visitTableSeparator(TableSeparator node) {
    	org.nasdanika.models.markdown.TableSeparator tableSeparator = getFactory().createTableSeparator();
    	populateNode(node, tableSeparator);
    	addToParent(node, tableSeparator);
        visitor.visitChildren(node);
    }

    private void visitTableBody(TableBody node) {
    	org.nasdanika.models.markdown.TableBody tableBody = getFactory().createTableBody();
    	populateNode(node, tableBody);
    	addToParent(node, tableBody);
        visitor.visitChildren(node);
    }

    private void visitTableRow(TableRow node) {
    	org.nasdanika.models.markdown.TableRow tableRow = getFactory().createTableRow();
    	populateNode(node, tableRow);
    	addToParent(node, tableRow);
        visitor.visitChildren(node);
    }

    private void visitTableCaption(TableCaption node) {
    	org.nasdanika.models.markdown.TableCaption tableCaption = getFactory().createTableCaption();
    	populateNode(node, tableCaption);    	
    	addToParent(node, tableCaption);
    	tableCaption.setText(node.getText().toString());
    	tableCaption.setOpeningMarker(node.getOpeningMarker().toString());
    	tableCaption.setClosingMarker(node.getClosingMarker().toString());
        visitor.visitChildren(node);
    }

    private void visitTableCell(TableCell node) {
    	org.nasdanika.models.markdown.TableCell tableCell = getFactory().createTableCell();
    	populateNode(node, tableCell);    	
    	addToParent(node, tableCell);
    	tableCell.setText(node.getText().toString());
    	tableCell.setOpeningMarker(node.getOpeningMarker().toString());
    	tableCell.setClosingMarker(node.getClosingMarker().toString());
    	Alignment alignment = node.getAlignment();
    	if (alignment != null) {
    		tableCell.setAlignment(TableCellAlignment.valueOf(alignment.name()));
    	}
        visitor.visitChildren(node);
    }
    
    private void populateLinkNodeBase(LinkNodeBase node, org.nasdanika.models.markdown.LinkNodeBase ecoreNode) {
    	populateNode(node, ecoreNode);
    	ecoreNode.setAnchorMarker(node.getAnchorMarker().toString());
    	ecoreNode.setAnchorRef(node.getAnchorRef().toString());
    	ecoreNode.setPageRef(node.getPageRef().toString());
    	ecoreNode.setTitle(node.getTitle().toString());
    	ecoreNode.setTitleClosingMarker(node.getTitleClosingMarker().toString());
    	ecoreNode.setTitleOpeningMarker(node.getTitleOpeningMarker().toString());
    	ecoreNode.setUrl(node.getUrl().toString());
    	ecoreNode.setUrlClosingMarker(node.getUrlClosingMarker().toString());
    	ecoreNode.setUrlOpeningMarker(node.getUrlOpeningMarker().toString());    	
    }    
    
    private void populateLinkNode(LinkNode node, org.nasdanika.models.markdown.LinkNode ecoreNode) {
    	populateLinkNodeBase(node, ecoreNode);    	
    }
    
    private void populateInlineLinkNode(InlineLinkNode node, org.nasdanika.models.markdown.InlineLinkNode ecoreNode) {
    	populateLinkNode(node, ecoreNode);
    	ecoreNode.setLinkOpeningMarker(node.getLinkOpeningMarker().toString());
    	ecoreNode.setLinkClosingMarker(node.getLinkClosingMarker().toString());
    	ecoreNode.setText(node.getText().toString());
    	ecoreNode.setTextOpeningMarker(node.getTextOpeningMarker().toString());
    	ecoreNode.setTextClosingMarker(node.getTextClosingMarker().toString());    	
    }

    private void visitLink(Link node) {
    	org.nasdanika.models.markdown.Link link = getFactory().createLink();
    	populateInlineLinkNode(node, link);
    	addToParent(node, link);
        visitor.visitChildren(node);
    }

    private void visitCode(FencedCodeBlock node) {
    	org.nasdanika.models.markdown.FencedCodeBlock code = getFactory().createFencedCodeBlock();
    	populateContentNode(node, code);
        code.setInfo(node.getInfo().toString());
        code.setContent(node.getContentChars().toString());
        code.setClosingFence(node.getClosingFence().toString());
        code.setClosingMarker(node.getClosingMarker().toString());
        code.setFenceIndent(node.getFenceIndent());
        code.setFenceLength(node.getFenceLength());
        code.setOpeningFence(node.getOpeningFence().toString());
        code.setOpeningMarker(node.getOpeningMarker().toString());
        
    	addToParent(node, code);

        visitor.visitChildren(node);
    }

    /**
     * Called for every { attr="value" } block.
     * The AttributesNode is a sibling of the element it annotates,
     * so currentBlock is already set when this fires.
     */
    private void visitAttributes(AttributesNode node) {
        if (nodeMap.get(node.getParent()) instanceof Attributable attributable) {
        	attributable.setAttributesStartOffset(node.getStartOffset());
        	attributable.setAttributesEndOffset(node.getEndOffset());
	        for (Node child : node.getChildren()) {
	            if (child instanceof AttributeNode attr) {
	                String key   = attr.getName().toString();
	                String value = attr.getValue().toString();
	
	                org.nasdanika.models.markdown.Attribute ecoreAttr = getFactory().createAttribute();
	                ecoreAttr.setKey(key);
	                ecoreAttr.setValue(value);
	                attributable.getAttributes().add(ecoreAttr);
	            }
	        }
        }
    }
	
}