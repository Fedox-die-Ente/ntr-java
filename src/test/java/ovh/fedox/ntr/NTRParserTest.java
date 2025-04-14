package ovh.fedox.ntr;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ovh.fedox.ntr.exception.NTRParseException;
import ovh.fedox.ntr.model.NTRNode;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class NTRParserTest {
	private NTRParser parser;
	private String sampleContent;

	public static void main(String[] args) {}

	@BeforeEach
	void setUp() {
		parser = new NTRParser();
		sampleContent = "@Willkommenstexte\n" +
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
	}

	@Test
	void testParseString() throws NTRParseException {
		parser.parseString(sampleContent);

		// Test root nodes
		Map<String, NTRNode> rootNodes = parser.getRootNodes();
		assertEquals(4, rootNodes.size());
		assertTrue(rootNodes.containsKey("welcome"));
		assertTrue(rootNodes.containsKey("error"));
		assertTrue(rootNodes.containsKey("user"));
		assertTrue(rootNodes.containsKey("language"));

		// Test specific values
		assertEquals("Willkommen auf unserer Seite", parser.getValue("welcome.title").orElse(null));
		assertEquals("Schön, dass du hier bist!", parser.getValue("welcome.message").orElse(null));
		assertEquals("Seite nicht gefunden", parser.getValue("error.404.title").orElse(null));
		assertEquals("Die angeforderte Seite konnte nicht gefunden werden.", parser.getValue("error.404.message").orElse(null));
		assertEquals("Serverfehler", parser.getValue("error.500.title").orElse(null));
		assertEquals("Es gab einen internen Serverfehler.", parser.getValue("error.500.message").orElse(null));
		assertEquals("Name", parser.getValue("user.profile.name").orElse(null));
		assertEquals("Alter", parser.getValue("user.profile.age").orElse(null));
		assertEquals("Englisch", parser.getValue("language.en").orElse(null));
		assertEquals("Deutsch", parser.getValue("language.de").orElse(null));
		assertEquals("Spanisch", parser.getValue("language.es").orElse(null));
		assertEquals("Französisch", parser.getValue("language.fr").orElse(null));

		// Test non-existent values
		assertFalse(parser.getValue("nonexistent").isPresent());
		assertFalse(parser.getValue("welcome.nonexistent").isPresent());
		assertFalse(parser.getValue("error.404.nonexistent").isPresent());
	}

	@Test
	void testGetNode() throws NTRParseException {
		parser.parseString(sampleContent);

		// Test getting nodes
		Optional<NTRNode> welcomeNode = parser.getNode("welcome");
		assertTrue(welcomeNode.isPresent());
		assertEquals("welcome", welcomeNode.get().getKey());
		assertEquals("", welcomeNode.get().getValue());
		assertEquals(2, welcomeNode.get().getChildCount());

		Optional<NTRNode> errorNode = parser.getNode("error");
		assertTrue(errorNode.isPresent());
		assertEquals(2, errorNode.get().getChildCount());

		Optional<NTRNode> error404Node = parser.getNode("error.404");
		assertTrue(error404Node.isPresent());
		assertEquals("404", error404Node.get().getKey());
		assertEquals(2, error404Node.get().getChildCount());

		// Test non-existent nodes
		assertFalse(parser.getNode("nonexistent").isPresent());
	}

	@Test
	void testClear() throws NTRParseException {
		parser.parseString(sampleContent);
		assertFalse(parser.getRootNodes().isEmpty());

		parser.clear();
		assertTrue(parser.getRootNodes().isEmpty());
	}
}