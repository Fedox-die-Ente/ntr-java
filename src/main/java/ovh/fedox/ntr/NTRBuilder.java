package ovh.fedox.ntr;


import ovh.fedox.ntr.model.NTRNode;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Builder for creating NTR data structures programmatically.
 * <p>
 * This class provides a fluent API for building NTR data structures.
 * </p>
 */
public class NTRBuilder {
	private final Map<String, NTRNode> rootNodes = new HashMap<>();
	private NTRNode currentNode = null;
	private final Stack<NTRNode> nodeStack = new Stack<>();

	/**
	 * Creates a new NTR builder.
	 */
	public NTRBuilder() {}

	/**
	 * Adds a root node.
	 *
	 * @param key The key of the node
	 * @return this builder instance for method chaining
	 */
	public NTRBuilder addRoot(String key) {
		return addRoot(key, "");
	}

	/**
	 * Adds a root node with a value.
	 *
	 * @param key The key of the node
	 * @param value The value of the node
	 * @return this builder instance for method chaining
	 */
	public NTRBuilder addRoot(String key, String value) {
		NTRNode node = new NTRNode(key, value);
		rootNodes.put(key, node);
		currentNode = node;
		nodeStack.clear();
		nodeStack.push(node);
		return this;
	}

	/**
	 * Adds a child node to the current node.
	 *
	 * @param key The key of the node
	 * @return this builder instance for method chaining
	 */
	public NTRBuilder addChild(String key) {
		return addChild(key, "");
	}

	/**
	 * Adds a child node with a value to the current node.
	 *
	 * @param key The key of the node
	 * @param value The value of the node
	 * @return this builder instance for method chaining
	 */
	public NTRBuilder addChild(String key, String value) {
		if (currentNode == null) {
			throw new IllegalStateException("No current node. Call addRoot() first.");
		}

		NTRNode node = new NTRNode(key, value);
		currentNode.addChild(node);
		nodeStack.push(node);
		currentNode = node;
		return this;
	}

	/**
	 * Navigates up to the parent node.
	 *
	 * @return this builder instance for method chaining
	 */
	public NTRBuilder parent() {
		if (nodeStack.size() <= 1) {
			return this;
		}

		nodeStack.pop();
		currentNode = nodeStack.peek();
		return this;
	}

	/**
	 * Navigates to a sibling node by adding it at the same level as the current node.
	 *
	 * @param key The key of the sibling node
	 * @param value The value of the sibling node
	 * @return this builder instance for method chaining
	 */
	public NTRBuilder sibling(String key, String value) {
		if (nodeStack.size() <= 1) {
			return addRoot(key, value);
		}

		NTRNode parent = nodeStack.get(nodeStack.size() - 2);

		NTRNode node = new NTRNode(key, value);
		parent.addChild(node);

		nodeStack.pop();
		nodeStack.push(node);
		currentNode = node;

		return this;
	}

	/**
	 * Navigates to a sibling node.
	 *
	 * @param key The key of the sibling node
	 * @return this builder instance for method chaining
	 */
	public NTRBuilder sibling(String key) {
		return sibling(key, "");
	}

	/**
	 * Navigates to the root node with the given key.
	 *
	 * @param key The key of the root node
	 * @return this builder instance for method chaining
	 */
	public NTRBuilder navigateToRoot(String key) {
		NTRNode node = rootNodes.get(key);
		if (node == null) {
			throw new IllegalArgumentException("No root node with key: " + key);
		}

		nodeStack.clear();
		nodeStack.push(node);
		currentNode = node;
		return this;
	}

	/**
	 * Builds the NTR data structure.
	 *
	 * @return Map of root nodes
	 */
	public Map<String, NTRNode> build() {
		return new HashMap<>(rootNodes);
	}

	/**
	 * Creates a new NTR writer for the built data structure.
	 *
	 * @return NTR writer
	 */
	public NTRWriter createWriter() {
		return new NTRWriter(build());
	}
}