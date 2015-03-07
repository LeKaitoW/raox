package ru.bmstu.rk9.rdo.lib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.bmstu.rk9.rdo.lib.Database.Entry;

public class CollectedDataNode {
	public CollectedDataNode(String name, CollectedDataNode parent) {
		this.name = name;
		this.parent = parent;
	}

	public final CollectedDataNode addChild(String name) {
		CollectedDataNode child = new CollectedDataNode(name, this);
		children.put(name, child);
		return child;
	}

	public final void addEntry(Entry entry) {
		entries.add(entry);
	}

	public final CollectedDataNode getParent() {
		return parent;
	}

	public final boolean hasChildren() {
		return !children.isEmpty();
	}

	public final Map<String, CollectedDataNode> getChildren() {
		return children;
	}

	public final String getName() {
		return name;
	}

	private final List<Entry> entries = new ArrayList<Entry>();
	private final String name;
	private final CollectedDataNode parent;
	private final Map<String, CollectedDataNode> children =
			new HashMap<String, CollectedDataNode>();
}
