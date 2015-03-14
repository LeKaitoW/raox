package ru.bmstu.rk9.rdo.lib;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ru.bmstu.rk9.rdo.lib.json.JSONObject;

public class CollectedDataNode {
	public static interface AbstractIndex {
		public List<Integer> getEntries();

		public void addEntry(Integer entry);

		public int getNumber();

		public boolean isEmpty();
	}

	public static class Index implements AbstractIndex {
		public Index(int number) {
			this.number = number;
		}

		public final List<Integer> getEntries() {
			return entries;
		}

		public final void addEntry(Integer entry) {
			entries.add(entry);
		}

		public final int getNumber() {
			return number;
		}

		public final boolean isEmpty() {
			return entries.isEmpty();
		}

		protected final List<Integer> entries = new ArrayList<Integer>();
		protected final int number;
	}

	public static class SearchIndex extends Index {
		public static class SearchInfo {
			int begin = -1;
			int end = -1;
			int decision = -1;

			public int beginEntry() {
				return begin;
			}

			public int endEntry() {
				return end;
			}

			public int decisionStart() {
				return decision;
			}

			public int size() {
				return end - begin;
			}
		}

		SearchIndex(int number) {
			super(number);
		}

		List<SearchInfo> searches = new ArrayList<SearchInfo>();
	}

	public static class PatternIndex extends Index {
		public PatternIndex(int number, JSONObject structure) {
			super(number);
			this.structure = structure;
		}

		JSONObject structure;
		int timesExecuted = 0;
	}

	public static class ResourceTypeIndex extends Index {
		ResourceTypeIndex(int number, JSONObject structure) {
			super(number);
			this.structure = structure;
		}

		public JSONObject getStructure() {
			return structure;
		}

		private final JSONObject structure;
	}

	public CollectedDataNode(String name, CollectedDataNode parent) {
		this.name = name;
		this.parent = parent;
	}

	public final CollectedDataNode addChild(String name) {
		CollectedDataNode child = new CollectedDataNode(name, this);
		children.put(name, child);
		return child;
	}

	public final void setIndex(AbstractIndex index) {
		this.index = index;
	}

	public final AbstractIndex getIndex() {
		return index;
	}

	public final void addEntry(Integer entry) {
		this.index.addEntry(entry);
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

	public final String getFullName() {
		return name;
	}

	public final String getName() {
		return SerializationConfig.getRelativeElementName(name);
	}

	private AbstractIndex index = null;
	private final String name;
	private final CollectedDataNode parent;
	private final Map<String, CollectedDataNode> children =
			new TreeMap<String, CollectedDataNode>();
}