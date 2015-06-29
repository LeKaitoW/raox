package ru.bmstu.rk9.rdo.lib;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ru.bmstu.rk9.rdo.lib.Database.ResultType;
import ru.bmstu.rk9.rdo.lib.ModelStructureCache.ValueCache;
import ru.bmstu.rk9.rdo.lib.json.JSONObject;

public class CollectedDataNode {
	public enum IndexType {
		RESOURCE_TYPE, RESOURCE, RESOURCE_PARAMETER, RESULT, PATTERN, SEARCH, DECISION_POINT
	}

	public static interface AbstractIndex {
		public List<Integer> getEntryNumbers();

		public void addEntry(Integer entry);

		public int getNumber();

		public boolean isEmpty();

		public IndexType getType();
	}

	private static class Index implements AbstractIndex {
		public Index(int number, IndexType type) {
			this.number = number;
			this.type = type;
		}

		@Override
		public List<Integer> getEntryNumbers() {
			return entryNumbers;
		}

		@Override
		public final void addEntry(Integer entry) {
			entryNumbers.add(entry);
		}

		@Override
		public final int getNumber() {
			return number;
		}

		@Override
		public final boolean isEmpty() {
			return entryNumbers.isEmpty();
		}

		@Override
		public final IndexType getType() {
			return type;
		}

		protected final List<Integer> entryNumbers = new ArrayList<Integer>();
		protected final int number;
		private final IndexType type;
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
			super(number, IndexType.SEARCH);
		}

		List<SearchInfo> searches = new ArrayList<SearchInfo>();
	}

	public static class ResourceIndex extends Index {
		public ResourceIndex(int number) {
			super(number, IndexType.RESOURCE);
		}

		public boolean isErased() {
			return erased;
		}

		boolean erased = false;
	}

	public static class PatternIndex extends Index {
		public PatternIndex(int number, JSONObject structure) {
			super(number, IndexType.PATTERN);
			this.structure = structure;
		}

		public JSONObject getStructrure() {
			return structure;
		}

		JSONObject structure;
		int timesExecuted = 0;

	}

	public static class ResourceTypeIndex extends Index {
		ResourceTypeIndex(int number, JSONObject structure) {
			super(number, IndexType.RESOURCE_TYPE);
			this.structure = structure;
		}

		public JSONObject getStructure() {
			return structure;
		}

		private final JSONObject structure;
	}

	public static class ResultIndex extends Index {
		ResultIndex(int number, ResultType type) {
			super(number, IndexType.RESULT);
			this.type = type;
		}

		public final ResultType getResultType() {
			return type;
		}

		private final ResultType type;
	}

	public static class DecisionPointIndex extends Index {
		DecisionPointIndex(int number) {
			super(number, IndexType.DECISION_POINT);
		}
	}

	public static class ResourceParameterIndex extends Index {
		ResourceParameterIndex(int number, ValueCache cache, int offset) {
			super(number, IndexType.RESOURCE_PARAMETER);
			this.cache = cache;
			this.offset = offset;
		}

		public final ValueCache getValueCache() {
			return cache;
		}

		public final int getOffset() {
			return offset;
		}

		private final ValueCache cache;
		private final int offset;
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
	private final Map<String, CollectedDataNode> children = new TreeMap<String, CollectedDataNode>();
}