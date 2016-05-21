package ru.bmstu.rk9.rao.lib.database;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.google.gson.JsonObject;

import ru.bmstu.rk9.rao.lib.database.Database.ResultType;
import ru.bmstu.rk9.rao.lib.json.JSONObject;
import ru.bmstu.rk9.rao.lib.naming.NamingHelper;

public class CollectedDataNode {
	public enum IndexType {
		RESOURCE_TYPE, RESOURCE, RESOURCE_PARAMETER, RESULT, PATTERN, EVENT, SEARCH, DECISION_POINT
	}

	public static class Index {
		public Index(int number, IndexType type) {
			this.number = number;
			this.type = type;
		}

		public List<Integer> getEntryNumbers() {
			return entryNumbers;
		}

		public final void addEntry(Integer entry) {
			entryNumbers.add(entry);
		}

		public final int getNumber() {
			return number;
		}

		public final boolean isEmpty() {
			return entryNumbers.isEmpty();
		}

		public final IndexType getType() {
			return type;
		}

		protected final List<Integer> entryNumbers = new ArrayList<Integer>();
		protected final int number;
		private final IndexType type;
	}

	public static class SearchIndex extends Index {
		public static class SearchInfo {
			private int begin = -1;
			private int end = -1;
			private int decision = -1;

			public int beginEntry() {
				return getBegin();
			}

			public int endEntry() {
				return getEnd();
			}

			public int decisionStart() {
				return getDecision();
			}

			public int size() {
				return getEnd() - getBegin();
			}

			public int getBegin() {
				return begin;
			}

			public void setBegin(int begin) {
				this.begin = begin;
			}

			public int getDecision() {
				return decision;
			}

			public void setDecision(int decision) {
				this.decision = decision;
			}

			public int getEnd() {
				return end;
			}

			public void setEnd(int end) {
				this.end = end;
			}
		}

		public SearchIndex(int number) {
			super(number, IndexType.SEARCH);
		}

		public List<SearchInfo> getSearches() {
			return searches;
		}

		public void setSearches(List<SearchInfo> searches) {
			this.searches = searches;
		}

		private List<SearchInfo> searches = new ArrayList<SearchInfo>();
	}

	public static class ResourceIndex extends Index {
		public ResourceIndex(int number) {
			super(number, IndexType.RESOURCE);
		}

		public boolean isErased() {
			return erased;
		}

		public void setErased(boolean erased) {
			this.erased = erased;
		}

		private boolean erased = false;
	}

	public static class PatternIndex extends Index {
		public PatternIndex(int number) {
			super(number, IndexType.PATTERN);
		}

		public int incrementTimesExecuted() {
			return timesExecuted++;
		}

		int timesExecuted = 0;

	}

	public static class EventIndex extends Index {
		public EventIndex(int number, JsonObject event) {
			super(number, IndexType.EVENT);
			this.structure = event;
		}

		public int incrementTimesExecuted() {
			return timesExecuted++;
		}

		JsonObject structure;
		int timesExecuted = 0;
	}

	public static class ResourceTypeIndex extends Index {
		public ResourceTypeIndex(int number) {
			super(number, IndexType.RESOURCE_TYPE);
		}
	}

	public static class ResultIndex extends Index {
		public ResultIndex(int number, ResultType type) {
			super(number, IndexType.RESULT);
			this.type = type;
		}

		public final ResultType getResultType() {
			return type;
		}

		private final ResultType type;
	}

	public static class LogicIndex extends Index {
		public LogicIndex(int number) {
			super(number, IndexType.DECISION_POINT);
		}
	}

	public static class ResourceParameterIndex extends Index {
		public ResourceParameterIndex(int number) {
			super(number, IndexType.RESOURCE_PARAMETER);
		}
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

	public final void setIndex(Index index) {
		this.index = index;
	}

	public final Index getIndex() {
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
		return NamingHelper.getRelativeElementName(name);
	}

	private Index index = null;
	private final String name;
	private final CollectedDataNode parent;
	private final Map<String, CollectedDataNode> children = new TreeMap<String, CollectedDataNode>();
}