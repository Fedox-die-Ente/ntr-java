package ovh.fedox.ntr;


import ovh.fedox.ntr.exception.NTRParseException;
import ovh.fedox.ntr.model.NTRNode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main parser for NTR files.
 * <p>
 * NTR is a hierarchical key-value file format with the following features:
 * <ul>
 *   <li>Lines starting with @ are comments</li>
 *   <li>Hierarchical structure is represented by indentation</li>
 *   <li>Key-value pairs are separated by &gt;</li>
 * </ul>
 * </p>
 *
 * @author Fedox
 * @version 1.0.0
 */
public class NTRParser {
	private static final Logger LOGGER = Logger.getLogger(NTRParser.class.getName());

	private final Map<String, NTRNode> rootNodes = new HashMap<>();

	/**
	 * Creates a new NTR parser instance.
	 */
	public NTRParser() {}

	/**
	 * Parses an NTR file from the given path.
	 *
	 * @param filePath Path to the NTR file
	 * @return this parser instance for method chaining
	 * @throws NTRParseException if there is an error parsing the file
	 */
	public NTRParser parseFile(String filePath) throws NTRParseException {
		try {
			return parseFile(Paths.get(filePath));
		} catch (IOException e) {
			throw new NTRParseException("Failed to parse NTR file: " + filePath, e);
		}
	}

	/**
	 * Parses an NTR file from the given path.
	 *
	 * @param path Path to the NTR file
	 * @return this parser instance for method chaining
	 * @throws IOException if there is an error reading the file
	 * @throws NTRParseException if there is an error parsing the file
	 */
	public NTRParser parseFile(Path path) throws IOException, NTRParseException {
		LOGGER.log(Level.INFO, "Parsing NTR file: {0}", path);
		try (InputStream is = Files.newInputStream(path)) {
			return parseStream(is);
		}
	}

	/**
	 * Parses an NTR file from the given input stream.
	 *
	 * @param inputStream Input stream containing NTR content
	 * @return this parser instance for method chaining
	 * @throws NTRParseException if there is an error parsing the content
	 */
	public NTRParser parseStream(InputStream inputStream) throws NTRParseException {
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
			return parseReader(reader);
		} catch (IOException e) {
			throw new NTRParseException("Failed to parse NTR content from stream", e);
		}
	}

	/**
	 * Parses an NTR file from the given reader.
	 *
	 * @param reader Reader containing NTR content
	 * @return this parser instance for method chaining
	 * @throws NTRParseException if there is an error parsing the content
	 */
	public NTRParser parseReader(BufferedReader reader) throws NTRParseException {
		try {
			String line;
			NTRNode currentNode = null;
			List<NTRNode> nodeStack = new ArrayList<>();
			int prevIndentation = 0;
			int lineNumber = 0;

			while ((line = reader.readLine()) != null) {
				lineNumber++;
				String originalLine = line;
				line = line.trim();

				if (line.isEmpty()) {
					continue;
				}

				if (line.startsWith("@")) {
					continue;
				}

				int indentation = calculateIndentation(originalLine);

				try {
					if (line.contains(">")) {
						String[] parts = line.split(">");
						String key = parts[0].trim();
						String value = parts.length > 1 ? parts[1].trim() : "";

						currentNode = processKeyValueNode(key, value, indentation, prevIndentation,
								currentNode, nodeStack);
					} else {
						currentNode = processNode(line, indentation, prevIndentation,
								currentNode, nodeStack);
					}

					prevIndentation = indentation;
				} catch (Exception e) {
					throw new NTRParseException("Error parsing line " + lineNumber + ": " + originalLine, e);
				}
			}

			return this;
		} catch (IOException e) {
			throw new NTRParseException("Failed to parse NTR content", e);
		}
	}

	/**
	 * Parses an NTR file from the given string content.
	 *
	 * @param content String containing NTR content
	 * @return this parser instance for method chaining
	 * @throws NTRParseException if there is an error parsing the content
	 */
	public NTRParser parseString(String content) throws NTRParseException {
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(
						java.io.ByteArrayInputStream.class.getConstructor(byte[].class)
								.newInstance(content.getBytes(StandardCharsets.UTF_8)),
						StandardCharsets.UTF_8))) {
			return parseReader(reader);
		} catch (Exception e) {
			throw new NTRParseException("Failed to parse NTR content from string", e);
		}
	}

	/**
	 * Gets a value from the parsed data.
	 *
	 * @param path Path to the value, e.g., "welcome.title"
	 * @return Optional containing the value, or empty if not found
	 */
	public Optional<String> getValue(String path) {
		return getNode(path).map(NTRNode::getValue);
	}

	/**
	 * Gets a node from the parsed data.
	 *
	 * @param path Path to the node, e.g., "welcome"
	 * @return Optional containing the node, or empty if not found
	 */
	public Optional<NTRNode> getNode(String path) {
		if (path == null || path.isEmpty()) {
			return Optional.empty();
		}

		String[] parts = path.split("\\.");
		if (parts.length == 0) {
			return Optional.empty();
		}

		NTRNode node = rootNodes.get(parts[0]);
		if (node == null) {
			return Optional.empty();
		}

		for (int i = 1; i < parts.length; i++) {
			node = node.getChild(parts[i]);
			if (node == null) {
				return Optional.empty();
			}
		}

		return Optional.of(node);
	}

	/**
	 * Gets all root nodes.
	 *
	 * @return Map of root nodes
	 */
	public Map<String, NTRNode> getRootNodes() {
		return new HashMap<>(rootNodes);
	}

	/**
	 * Clears all parsed data.
	 *
	 * @return this parser instance for method chaining
	 */
	public NTRParser clear() {
		rootNodes.clear();
		return this;
	}

	/**
	 * Calculates the indentation level of a line.
	 *
	 * @param line The line to calculate indentation for
	 * @return The indentation level
	 */
	private int calculateIndentation(String line) {
		int indentation = 0;
		for (int i = 0; i < line.length(); i++) {
			if (Character.isWhitespace(line.charAt(i))) {
				indentation++;
			} else {
				break;
			}
		}
		return indentation;
	}

	/**
	 * Processes a key-value node.
	 *
	 * @param key The key of the node
	 * @param value The value of the node
	 * @param indentation The indentation level of the node
	 * @param prevIndentation The indentation level of the previous node
	 * @param currentNode The current node
	 * @param nodeStack The stack of nodes
	 * @return The new current node
	 */
	private NTRNode processKeyValueNode(String key, String value, int indentation,
										int prevIndentation, NTRNode currentNode,
										List<NTRNode> nodeStack) {
		if (indentation == 0) {
			NTRNode newNode = new NTRNode(key, value);
			rootNodes.put(key, newNode);
			nodeStack.clear();
			nodeStack.add(newNode);
			return newNode;
		} else {
			return processChildNode(key, value, indentation, prevIndentation, currentNode, nodeStack);
		}
	}

	/**
	 * Processes a node without value.
	 *
	 * @param key The key of the node
	 * @param indentation The indentation level of the node
	 * @param prevIndentation The indentation level of the previous node
	 * @param currentNode The current node
	 * @param nodeStack The stack of nodes
	 * @return The new current node
	 */
	private NTRNode processNode(String key, int indentation, int prevIndentation,
								NTRNode currentNode, List<NTRNode> nodeStack) {
		if (indentation == 0) {
			NTRNode newNode = new NTRNode(key, "");
			rootNodes.put(key, newNode);
			nodeStack.clear();
			nodeStack.add(newNode);
			return newNode;
		} else {
			return processChildNode(key, "", indentation, prevIndentation, currentNode, nodeStack);
		}
	}

	/**
	 * Processes a child node.
	 *
	 * @param key The key of the node
	 * @param value The value of the node
	 * @param indentation The indentation level of the node
	 * @param prevIndentation The indentation level of the previous node
	 * @param currentNode The current node
	 * @param nodeStack The stack of nodes
	 * @return The new current node
	 */
	private NTRNode processChildNode(String key, String value, int indentation,
									 int prevIndentation, NTRNode currentNode,
									 List<NTRNode> nodeStack) {
		if (indentation > prevIndentation) {
			if (currentNode != null) {
				NTRNode newNode = new NTRNode(key, value);
				currentNode.addChild(newNode);
				nodeStack.add(newNode);
				return newNode;
			}
		} else if (indentation == prevIndentation) {
			if (nodeStack.size() > 1) {
				NTRNode parent = nodeStack.get(nodeStack.size() - 2);
				NTRNode newNode = new NTRNode(key, value);
				parent.addChild(newNode);
				nodeStack.remove(nodeStack.size() - 1);
				nodeStack.add(newNode);
				return newNode;
			}
		} else {
			int levelsUp = (prevIndentation - indentation) / 2 + 1;
			for (int j = 0; j < levelsUp && nodeStack.size() > 1; j++) {
				nodeStack.remove(nodeStack.size() - 1);
			}

			if (!nodeStack.isEmpty()) {
				NTRNode parent = nodeStack.get(nodeStack.size() - 1);
				NTRNode newNode = new NTRNode(key, value);
				parent.addChild(newNode);
				nodeStack.add(newNode);
				return newNode;
			}
		}

		return currentNode;
	}
}