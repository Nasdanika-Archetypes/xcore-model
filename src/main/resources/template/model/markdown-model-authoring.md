Leanpub uses markdown with extensions as one of their authoring formats. At my work design documents are also markdown-based with validation and compilation. 
I think that it would be a good idea to have markdown as one of model definition formats - it has a hierarchy, links.
I have fenced block processor and detailed configuration can be put in fenced blocks in YAML format.

I need your advice on how to implement it - parser to use, for example. 
I'm currently using flexmark.
I would like a parser where I can get an AST and each node would provide its text.

This is how I see it working - build a header tree, use some sort of qualifiers to 
identify header/section type/role in its container - it might be a reference and subsections would be elements of the reference
or it might be an element and the qualifier would specify reference and possibly type for polymorphic references.

Essentially https://docs.nasdanika.org/core/mapping/index.html applied to markdown.
Maybe I should have a markdown model similar to my Java model (https://java.models.nasdanika.org/) on top of the source engineering model (https://source-engineering.models.nasdanika.org/) and have the model transformation pipeline.

I feel it might be a valuable addition to the nasdanika capabilities and it will lower the entry barrier for product management model adoption.

Markdown resource once there is a model - everything through resourceset

=== Response ===

Yes, this is a valuable addition and the architecture you're sketching is the right one. Markdown-as-input lowers the entry barrier substantially — every PM, architect, and engineer authors Markdown daily; YAML is read-friendly but write-friendly only when you already know the schema. The hybrid (header tree from Markdown + rich config in fenced YAML blocks) gives both natural authoring and semantic precision exactly where you need each. Below: parser recommendation, qualifier strategy, the EMF-model question, and implementation order.

**Stay on flexmark.** It is the right tool for what you're describing, and you already have momentum there. Of the Java alternatives, only `commonmark-java` is comparable for AST quality, and it has fewer extensions and a smaller ecosystem. Flexmark gives you exactly what you need: a full AST where every node has `getChars()` for the source text, source-offset information for diagnostics, a clean visitor API, and a built-in extension mechanism for the specific things you'll need (attribute lists, fenced-block info strings, definition lists). No reason to switch parsers; reason to lean further into flexmark's extension surface.

**Qualifier strategy: attribute lists, with fenced YAML for richer config.** The cleanest qualifier mechanism in Markdown is the *attribute list* syntax (pandoc/kramdown convention), which flexmark supports via `flexmark-ext-attributes`. Headings carry attributes inline, so the qualifier is part of the heading without polluting the rendered prose:

```markdown
# My Product Domain {.product-domain}

## Personas {.persona-domain}

### Senior Leader {.persona #senior-leader period.start=2024-01-01}

A leader responsible for portfolio outcomes...

#### Goals {.goal-list}

- See actual coverage, not claimed coverage {#see-coverage}
- Surface duplication structurally {#surface-duplication}

#### Configuration

```yaml type=persona-config
authority: org.nasdanika
evidence:
  - source: 2024-portfolio-review.pdf
    confidence: high
```

```

This gives you four layers of authoring expressiveness, used as needed:

1. **Heading hierarchy** carries the structural relationship (persona contains goals, capability contains concerns).
2. **Heading attribute lists** (`{.persona #id}`) carry type and identity — the qualifier that tells the mapper *what kind of element this is*.
3. **Heading prose attributes** (`period.start=2024-01-01`) carry simple key-value config that doesn't need YAML.
4. **Fenced YAML blocks** with `type=` info strings carry rich nested config that doesn't fit in attribute lists.

Your existing fenced-block processor handles layer 4 already. Layers 1 and 2 are pure parser work; layer 3 is post-parsing extraction.

**On the Markdown EMF model.** Yes, build it, and yes, position it as a parallel to your Java model on top of source-engineering. The reasoning:

The flexmark AST is a fine *parser-side* representation but a poor *integration-side* representation. If the only consumer of Markdown parsing is the PM model, you'd be tempted to skip the EMF layer and map flexmark AST directly to PM model elements. That works once. It doesn't work when a second consumer arrives — a design-doc-as-code workflow, an ADR (architecture decision record) parser, a legacy-modernization narrative extractor, the book toolchain you'll eventually need. Each new consumer would re-derive the flexmark→target mapping. A canonical EMF Markdown model means the flexmark→Markdown-model conversion is done once and every downstream consumer maps from a stable, citable, identity-preserving source.

The Markdown EMF model also unlocks the same things the Java model unlocks: cross-references resolvable via URI, source position preserved for diagnostics, document fragments addressable from outside, federation across documents the same way Maven artifacts federate across modules. The work-effort design documents at your job become *citable* the same way Java classes are — that's a non-trivial property in its own right.

The model should be small. Don't try to capture every Markdown construct. Capture what's load-bearing for transformation:

- `Document` (root)
- `Section` (heading + content, with `level`, `attributes`, `id`)
- `Paragraph` (with inline content)
- `List` and `ListItem` (with attributes)
- `FencedBlock` (with info string, language, content, parsed attributes)
- `Link` (with target, label, attributes — the cross-reference primitive)
- `InlineCode`, `Emphasis`, `Strong` if you care about them
- `Reference` (link reference definitions)
- Some kind of `Block` or `Text` catch-all for the rest

Maybe 12-15 classes. Two-day exercise to define, plus the flexmark adapter on top.

**On the mapping framework.** This is exactly the shape your [mapping framework](https://docs.nasdanika.org/core/mapping/index.html) was built for. The PM-side mapping is declarative:

- Section with `.persona` attribute → `Persona` instance, id from `#id` attribute, name from heading text
- Sub-section with `.goal-list` under a Persona → populates the `goals` reference; each list item becomes a `Goal`
- Fenced block with `type=persona-config` → merged into the containing Persona's attributes via YAML parse
- Link with target `#senior-leader` → resolves to a reference to the Persona with that id, in whatever scope is currently being mapped

Each mapping rule is a small piece of code against the Markdown EMF model, identical in shape to the rules your existing pipelines use against the Java model. The mapping framework's resolve-and-validate phases do the cross-reference work.

**Implementation order (probably 3-5 focused weekends):**

1. **Markdown EMF metamodel.** Define the ~12 classes in Xcore. One sitting.

2. **flexmark → Markdown-EMF adapter.** Walk the flexmark AST, build the EMF tree. Preserve source positions. Extract attribute lists into a structured attribute representation. ~200-400 lines, one weekend.

3. **Validation: structural sanity of the produced Markdown model.** Independent of any downstream consumer. Useful in its own right for the design-doc workflow at work.

4. **PM-model mapping rules.** One mapping per element type. The first few are the most work (figure out the conventions); the rest follow the pattern. Probably 5-8 rules total: persona-domain, persona, goal-list/goal, need-list/need, pain-point-list/pain-point, capability, capability-provider, evidence.

5. **CLI command.** `nasdanika pm markdown <input.md> <output.yaml>` (or directly to the model). Self-contained validation reports for unmapped sections, unrecognized qualifiers, dangling references.

6. **Round-trip back to documentation.** The PM model already generates docs; the Markdown source feeds the same pipeline, and the generated site shows the original Markdown prose alongside the resolved structural view. Same site, two views of the same source.

**A few practical concerns worth planning for:**

*Inline content within sections.* A section has free-form prose between sub-sections. The model needs a convention for what that prose means — usually "description of the section's owning element." Either treat the first paragraph as description (simple, fragile), or require an explicit `### Description` sub-heading (more verbose, more reliable), or use attribute syntax on a paragraph (`{.description}`) for cases where the convention doesn't fit.

*Diagnostics.* Markdown's flexibility means authors will write things that don't map. Error messages must be source-position-precise (line:column of the offending heading) and instructive ("section `.persona` expected `.goals` or `.needs` sub-section; got `.user-stories`"). Source positions from flexmark survive into the EMF model if you preserve them on each element. Without precise diagnostics, the entry-barrier-lowering goal of using Markdown gets eaten by the entry-barrier of debugging silent failures.

*Reserved attribute names.* Decide upfront that certain attribute keys are framework-reserved (`type`, `id`, `period.*`, `authority`) and the rest are pass-through metadata. The pass-through stays accessible on the model but doesn't participate in mapping unless explicitly used. This avoids name collisions when authors want to add their own annotations.

*Round-tripping.* You probably don't need bidirectional Markdown ↔ model. The Markdown *is* the source; the model is derived. But document this decision deliberately, because someone will eventually ask "can I edit the model and regenerate the Markdown?" The answer is "no, edit the Markdown; the model is derived" — but only if you've designed it that way from the start. If you want round-trip later, the EMF model needs to preserve formatting metadata flexmark currently strips.

**On the entry-barrier argument specifically.** This is the strongest case for the work. Asking a product manager to learn YAML schema is a different ask than asking them to write Markdown with `{.persona}` tags they look up once. The Markdown path is a *much* lower commitment to evaluation, and evaluation is the gating event for adoption. Free-text Markdown with optional structural qualifiers also lets authors start informally and graduate to formal as the document matures — the path from "design doc" to "model artifact" becomes continuous instead of discrete. That continuity is the actual win, beyond the syntactic preference.

This is also worth a future blog post or section in the book once it's working — *"Why your design documents should compile."* The design-doc-as-code workflow at your work is the lived experience that grounds the argument; Nasdanika's Markdown model is the toolchain that generalizes it. Both halves are publishable content once the implementation is real.