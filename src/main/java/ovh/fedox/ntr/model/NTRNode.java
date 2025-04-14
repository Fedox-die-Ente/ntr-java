package ovh.fedox.ntr.model;


import java.util.*;

/**
 * Represents a node in the NTR file structure.
 * <p>
 * Each node has a key, an optional value, and can have child nodes.
 * </p>
 */
public class NTRNode {
	private final String key;
	private final String value;
	private final Map<String, NTRNode> children = new HashMap<>();
	private final List<NTRNode> childrenList = new ArrayList<>();

	/**
	 * Creates a new NTR node.
	 *
	 * @param key The key of the node
	 * @param value The value of the node
	 * @throws IllegalArgumentException if key is null
	 */
	public NTRNode(String key, String value) {
		this.key = Objects.requireNonNull(key, "Key cannot be null");
		this.value = value != null ? value : "";
	}

	/**
	 * Gets the key of the node.
	 *
	 * @return The key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Gets the value of the node.
	 *
	 * @return The value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Adds a child node.
	 *
	 * @param child The child node to add
	 * @return This node for method chaining
	 * @throws IllegalArgumentException if child is null or has a duplicate key
	 */
	public NTRNode addChild(NTRNode child) {
		Objects.requireNonNull(child, "Child node cannot be null");

		if (children.containsKey(child.getKey())) {
			throw new IllegalArgumentException("Child with key '" + child.getKey() + "' already exists");
		}

		children.put(child.getKey(), child);
		childrenList.add(child);
		return this;
	}

	/**
	 * Gets a child node by key.
	 *
	 * @param key The key of the child node
	 * @return The child node or null if not found
	 */
	public NTRNode getChild(String key) {
		return children.get(key);
	}

	/**
	 * Gets a child node by key as an Optional.
	 *
	 * @param key The key of the child node
	 * @return Optional containing the child node, or empty if not found
	 */
	public Optional<NTRNode> getChildOptional(String key) {
		return Optional.ofNullable(children.get(key));
	}

	/**
	 * Gets all children.
	 *
	 * @return Unmodifiable list of all children
	 */
	public List<NTRNode> getChildren() {
		return Collections.unmodifiableList(childrenList);
	}

	/**
	 * Gets all children as a map.
	 *
	 * @return Unmodifiable map of all children
	 */
	public Map<String, NTRNode> getChildrenMap() {
		return Collections.unmodifiableMap(children);
	}

	/**
	 * Checks if the node has children.
	 *
	 * @return True if the node has children, false otherwise
	 */
	public boolean hasChildren() {
		return !children.isEmpty();
	}

	/**
	 * Gets the number of children.
	 *
	 * @return The number of children
	 */
	public int getChildCount() {
		return children.size();
	}

	@Override
	public String toString() {
		return key + (value.isEmpty() ? "" : " > " + value) +
				" (Children: " + children.size() + ")";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		NTRNode ntrNode = (NTRNode) o;
		return Objects.equals(key, ntrNode.key) &&
				Objects.equals(value, ntrNode.value) &&
				Objects.equals(children, ntrNode.children);
	}

	@Override
	public int hashCode() {
		return Objects.hash(key, value, children);
	}
}
