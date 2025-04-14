package ovh.fedox.ntr.example;


import ovh.fedox.ntr.NTRBuilder;
import ovh.fedox.ntr.NTRParser;
import ovh.fedox.ntr.NTRWriter;
import ovh.fedox.ntr.exception.NTRParseException;
import ovh.fedox.ntr.exception.NTRWriteException;
import ovh.fedox.ntr.model.NTRNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Example usage of the NTR parser library.
 */
public class Example {

	public static void main(String[] args) {
		try {
			// Create a sample .ntr file
			String sampleContent = "@Willkommenstexte\n" +
					"welcome\n" +
					"  title>Willkommen auf unserer Seite\n" +
					"  message>Schön, dass du hier bist!\n" +
					"\n" +
					"@Fehlermeldungen\n" +
					"error\n" +
					"  404\n" +
					"    title>Seite nicht gefunden\n" +
					"    message>Die angeforderte Seite konnte nicht gefunden werden.\n" +
					"  500\n" +
					"    title>Serverfehler\n" +
					"    message>Es gab einen internen Serverfehler.\n" +
					"\n" +
					"@Benutzerinformationen\n" +
					"user\n" +
					"  profile\n" +
					"    name>Name\n" +
					"    age>Alter\n" +
					"\n" +
					"@Mehrsprachigkeit\n" +
					"language\n" +
					"  en>Englisch\n" +
					"  de>Deutsch\n" +
					"  es>Spanisch\n" +
					"  fr>Französisch";

			Path tempFile = Files.createTempFile("sample", ".ntr");
			Files.writeString(tempFile, sampleContent);

			// Parse the file
			NTRParser parser = new NTRParser();
			parser.parseFile(tempFile);

			// Demonstrate usage
			System.out.println("=== Parsed NTR File ===");

			// Get specific values
			System.out.println("\n=== Specific Values ===");
			System.out.println("Welcome Title: " + parser.getValue("welcome.title").orElse("Not found"));
			System.out.println("Error 404 Message: " + parser.getValue("error.404.message").orElse("Not found"));
			System.out.println("User Profile Age: " + parser.getValue("user.profile.age").orElse("Not found"));
			System.out.println("Language DE: " + parser.getValue("language.de").orElse("Not found"));

			// Print the entire structure
			System.out.println("\n=== Complete Structure ===");
			Map<String, NTRNode> rootNodes = parser.getRootNodes();
			for (Map.Entry<String, NTRNode> entry : rootNodes.entrySet()) {
				printNode(entry.getValue(), 0);
			}

			// Demonstrate building NTR data programmatically
			System.out.println("\n=== Building NTR Data ===");
			NTRBuilder builder = new NTRBuilder();
			builder.addRoot("config")
					.addChild("database")
					.addChild("url", "jdbc:mysql://localhost:3306/mydb")
					.parent() // Go back to database
					.addChild("username", "user")
					.parent() // Go back to database
					.addChild("password", "pass")
					.parent() // Go back to database
					.addChild("pool")
					.addChild("min", "5")
					.parent() // Go back to pool
					.addChild("max", "20")
					.navigateToRoot("config") // Go back to config
					.addChild("app")
					.addChild("name", "My App")
					.parent() // Go back to app
					.addChild("version", "1.0.0");

			// Write the built data to a string
			NTRWriter writer = builder.createWriter();
			writer.setComment("Configuration File");
			String builtContent = writer.writeToString();
			System.out.println(builtContent);

			// Clean up
			Files.delete(tempFile);

		} catch (IOException | NTRParseException | NTRWriteException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Prints a node and its children recursively.
	 *
	 * @param node The node to print
	 * @param level The indentation level
	 */
	private static void printNode(NTRNode node, int level) {
		StringBuilder indent = new StringBuilder();
		for (int i = 0; i < level; i++) {
			indent.append("  ");
		}

		System.out.println(indent + node.getKey() +
				(node.getValue().isEmpty() ? "" : " > " + node.getValue()));

		for (NTRNode child : node.getChildren()) {
			printNode(child, level + 1);
		}
	}
}