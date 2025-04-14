package ovh.fedox.ntr;


import ovh.fedox.ntr.exception.NTRWriteException;
import ovh.fedox.ntr.model.NTRNode;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Writer for NTR files.
 * <p>
 * This class provides methods to write NTR data to files, streams, or strings.
 * </p>
 */
public class NTRWriter {
	private static final Logger LOGGER = Logger.getLogger(NTRWriter.class.getName());
	private static final String INDENT = "  ";

	private final Map<String, NTRNode> rootNodes;
	private String comment;

	/**
	 * Creates a new NTR writer.
	 *
	 * @param rootNodes The root nodes to write
	 */
	public NTRWriter(Map<String, NTRNode> rootNodes) {
		this.rootNodes = rootNodes;
	}

	/**
	 * Sets a comment to be written at the top of the file.
	 *
	 * @param comment The comment
	 * @return this writer instance for method chaining
	 */
	public NTRWriter setComment(String comment) {
		this.comment = comment;
		return this;
	}

	/**
	 * Writes the NTR data to a file.
	 *
	 * @param filePath Path to the file
	 * @throws NTRWriteException if there is an error writing the file
	 */
	public void writeToFile(String filePath) throws NTRWriteException {
		try {
			writeToFile(Paths.get(filePath));
		} catch (IOException e) {
			throw new NTRWriteException("Failed to write NTR file: " + filePath, e);
		}
	}

	/**
	 * Writes the NTR data to a file.
	 *
	 * @param path Path to the file
	 * @throws IOException if there is an error writing the file
	 * @throws NTRWriteException if there is an error writing the NTR data
	 */
	public void writeToFile(Path path) throws IOException, NTRWriteException {
		LOGGER.log(Level.INFO, "Writing NTR file: {0}", path);
		try (OutputStream os = Files.newOutputStream(path)) {
			writeToStream(os);
		}
	}

	/**
	 * Writes the NTR data to an output stream.
	 *
	 * @param outputStream The output stream
	 * @throws NTRWriteException if there is an error writing the NTR data
	 */
	public void writeToStream(OutputStream outputStream) throws NTRWriteException {
		try (BufferedWriter writer = new BufferedWriter(
				new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))) {
			writeToWriter(writer);
		} catch (IOException e) {
			throw new NTRWriteException("Failed to write NTR data to stream", e);
		}
	}

	/**
	 * Writes the NTR data to a writer.
	 *
	 * @param writer The writer
	 * @throws IOException if there is an error writing to the writer
	 */
	public void writeToWriter(BufferedWriter writer) throws IOException {
		if (comment != null && !comment.isEmpty()) {
			writer.write("@" + comment);
			writer.newLine();
			writer.newLine();
		}

		boolean first = true;
		for (NTRNode node : rootNodes.values()) {
			if (!first) {
				writer.newLine();
			}
			writeNode(writer, node, 0);
			first = false;
		}
	}

	/**
	 * Writes the NTR data to a string.
	 *
	 * @return The NTR data as a string
	 * @throws NTRWriteException if there is an error writing the NTR data
	 */
	public String writeToString() throws NTRWriteException {
		try {
			java.io.StringWriter stringWriter = new java.io.StringWriter();
			try (BufferedWriter writer = new BufferedWriter(stringWriter)) {
				writeToWriter(writer);
			}
			return stringWriter.toString();
		} catch (IOException e) {
			throw new NTRWriteException("Failed to write NTR data to string", e);
		}
	}

	/**
	 * Writes a node and its children recursively.
	 *
	 * @param writer The writer
	 * @param node The node to write
	 * @param level The indentation level
	 * @throws IOException if there is an error writing to the writer
	 */
	private void writeNode(BufferedWriter writer, NTRNode node, int level) throws IOException {
		StringBuilder indent = new StringBuilder();
		for (int i = 0; i < level; i++) {
			indent.append(INDENT);
		}

		writer.write(indent.toString());
		writer.write(node.getKey());
		if (!node.getValue().isEmpty()) {
			writer.write(">");
			writer.write(node.getValue());
		}
		writer.newLine();

		for (NTRNode child : node.getChildren()) {
			writeNode(writer, child, level + 1);
		}
	}
}