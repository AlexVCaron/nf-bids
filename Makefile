# Build the plugin
assemble:
	./gradlew assemble

clean:
	rm -rf .nextflow*
	rm -rf work
	rm -rf build
	./gradlew clean

# Run plugin unit tests
test:
	./gradlew test

# Install the plugin into local nextflow plugins dir
install:
	./gradlew install

# Publish the plugin
release:
	./gradlew releasePlugin

# --- Documentation ---------------------------------------------------------

# Build the full documentation site (API reference + Antora site + diagrams)
docs:
	./gradlew docs

# Render only the PlantUML diagrams to SVG
docs-diagrams:
	./gradlew renderDiagrams

# Serve the built site locally at http://localhost:5050
docs-serve:
	npm --prefix documentation run serve
