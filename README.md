# Nasdanika Xcore Model Archetype

This is an archetype repository for [Xcore](https://wiki.eclipse.org/Xcore) models based on the [Xcore Doc](https://github.com/Nasdanika-Templates/xcore-doc) template repository.
It allows you to quickly generate a project skeleton and publish Xcore model documentation.

## Generated project structure

The generator produces a multi-module Maven project with two sub-modules:

| Module | Description |
|--------|-------------|
| `model` | The Xcore model itself, along with its EMF capability factories and a documentation site driven by GitHub Pages. |
| `handlers` | Optional resource-contents handlers that convert arbitrary input (e.g. text files) into model instances. |

## How to use

1. **Copy and customise a test method** from
   [TestGenerateXcoreModelProject.java](src/test/java/org/nasdanika/archetypes/models/xcore/tests/TestGenerateXcoreModelProject.java).  
   Adjust properties to fit your model:
   - `modelName` – short lowercase name of the model (e.g. `"inventory"`).
   - `version` – Maven artifact version (e.g. `"2026.8.0"` or `"0.0.1-SNAPSHOT"`).
   - `javaVersion` – Java release target (default: `"21"`).
   - `cliVersion` – Nasdanika CLI version used in the GitHub Actions workflow (default: `"2026.2.0"`).

2. **Run the test** to generate the project skeleton into the configured output directory (e.g. `target/generated-model/<modelName>`).

3. **Populate the Xcore model** – the generated `.xcore` file is empty by default.  
   You have several options:
   - Write the classifiers manually using standard Xcore syntax.
   - Use an AI assistant: ask it to generate an Xcore model for your problem domain.  
     If you are building on top of the [Nasdanika Model Tower](https://nasdanika.com/models.html),
     ask which floor your model belongs to.  
     For example:
     - Need to track element ownership? Build on the [Role](https://role.models.nasdanika.org/) floor.
     - Building an organisational model to accelerate AI adoption or strengthen governance? Build on the "AI Org" floor.  
     
     Paste the generated classifier definitions into the `.xcore` file and adjust as needed.

4. **Publish to GitHub Pages** (optional) – follow the instructions in the
   [Xcore Doc template repository](https://github.com/Nasdanika-Templates/xcore-doc) to add a diagram and element-level documentation.

## Prerequisites

- Java 21 or later
- Maven 3.9+
- Eclipse Modeling Framework (EMF) / Xcore tooling (for editing `.xcore` files)

## Resources

* Examples: all models in the Nasdanika Model
 Tower, including the role model mentioned above, were generated using this archetype.
* [Micro-models](https://nasdanika.com/stories/2026/micro-models.html) story.