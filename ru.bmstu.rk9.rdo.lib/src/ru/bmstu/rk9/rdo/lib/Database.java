package ru.bmstu.rk9.rdo.lib;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import ru.bmstu.rk9.rdo.lib.CollectedDataNode.Index;
import ru.bmstu.rk9.rdo.lib.CollectedDataNode.EventIndex;
import ru.bmstu.rk9.rdo.lib.CollectedDataNode.ResourceIndex;
import ru.bmstu.rk9.rdo.lib.CollectedDataNode.SearchIndex;
import ru.bmstu.rk9.rdo.lib.CollectedDataNode.PatternIndex;
import ru.bmstu.rk9.rdo.lib.CollectedDataNode.ResultIndex;
import ru.bmstu.rk9.rdo.lib.CollectedDataNode.DecisionPointIndex;
import ru.bmstu.rk9.rdo.lib.CollectedDataNode.ResourceTypeIndex;
import ru.bmstu.rk9.rdo.lib.CollectedDataNode.ResourceParameterIndex;
import ru.bmstu.rk9.rdo.lib.CollectedDataNode.SearchIndex.SearchInfo;
import ru.bmstu.rk9.rdo.lib.ModelStructureCache.ValueType;
import ru.bmstu.rk9.rdo.lib.json.*;

public class Database {
	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //
	// ---------------------------- TYPE INFO ------------------------------ //
	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //

	public static class TypeSize {
		public static final int INTEGER = Integer.SIZE / Byte.SIZE;
		public static final int DOUBLE = Double.SIZE / Byte.SIZE;
		public static final int SHORT = Short.SIZE / Byte.SIZE;
		public static final int LONG = Long.SIZE / Byte.SIZE;
		public static final int BYTE = 1;

		public static class RDO {
			public static final int INTEGER = TypeSize.INTEGER;
			public static final int REAL = TypeSize.DOUBLE;
			public static final int ENUM = TypeSize.SHORT;
			public static final int BOOLEAN = TypeSize.BYTE;
		}

		public static class Internal {
			public static final int ENTRY_TYPE_SIZE = TypeSize.BYTE;
			public static final int ENTRY_TYPE_OFFSET = 0;

			public static final int TIME_SIZE = TypeSize.DOUBLE;
			public static final int TIME_OFFSET = ENTRY_TYPE_SIZE
					+ ENTRY_TYPE_OFFSET;
		}
	}

	public enum SerializationCategory {
		RESOURCES("Resources"), PATTERNS("Patterns"), EVENTS("Events"), DECISION_POINTS(
				"Decision points"), RESULTS("Results"), SEARCH("Search");

		SerializationCategory(String name) {
			this.name = name;
		}

		private final String name;

		public final String getName() {
			return name;
		}
	}

	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //
	// ------------------------------ GENERAL ------------------------------ //
	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //

	Database(JSONObject modelStructure) {
		this.modelStructure = modelStructure;
		indexHelper.initializeModel(modelStructure.getString("name"));

		JSONArray resourceTypes = modelStructure.getJSONArray("resource_types");
		for (int i = 0; i < resourceTypes.length(); i++) {
			JSONObject resourceType = resourceTypes.getJSONObject(i);

			String name = resourceType.getString("name");
			CollectedDataNode typeNode = indexHelper.addResourceType(name);
			ResourceTypeIndex resourceTypeIndex = new ResourceTypeIndex(i,
					resourceType.getJSONObject("structure"));
			typeNode.setIndex(resourceTypeIndex);
		}

		JSONArray results = modelStructure.getJSONArray("results");
		for (int i = 0; i < results.length(); i++) {
			JSONObject result = results.getJSONObject(i);
			ResultType type = ResultType.get(result.getString("type"));
			indexHelper.addResult(result.getString("name")).setIndex(
					new ResultIndex(i, type));
		}

		JSONArray patterns = modelStructure.getJSONArray("patterns");
		Map<String, JSONObject> patternsByName = new HashMap<String, JSONObject>();
		for (int i = 0; i < patterns.length(); i++) {
			JSONObject pattern = patterns.getJSONObject(i);
			String name = pattern.getString("name");
			patternsByName.put(name, pattern);
		}

		JSONArray events = modelStructure.getJSONArray("events");
		for (int i = 0; i < events.length(); i++) {
			JSONObject event = events.getJSONObject(i);
			String name = event.getString("name");
			CollectedDataNode eventNode = indexHelper.addEvent(name);
			eventNode.setIndex(new EventIndex(i, event));
		}

		JSONArray decisionPoints = modelStructure
				.getJSONArray("decision_points");
		for (int i = 0; i < decisionPoints.length(); i++) {
			JSONObject decisionPoint = decisionPoints.getJSONObject(i);
			String type = decisionPoint.getString("type");
			switch (type) {
			case "some":
			case "prior":
				CollectedDataNode dptNode = indexHelper
						.addDecisionPoint(decisionPoint.getString("name"));
				dptNode.setIndex(new DecisionPointIndex(i));

				JSONArray activities = decisionPoint.getJSONArray("activities");
				for (int j = 0; j < activities.length(); j++) {
					JSONObject activity = activities.getJSONObject(j);
					String name = activity.getString("name");
					dptNode.addChild(name).setIndex(
							new PatternIndex(j, patternsByName.get(activity
									.getString("pattern"))));
				}
				break;
			case "search":
				indexHelper.addSearch(decisionPoint.getString("name"))
						.setIndex(new SearchIndex(i));
				break;
			}
		}

		addSystemEntry(SystemEntryType.TRACE_START);

		for (String traceName : SerializationConfig.getNames())
			addSensitivity(traceName);
	}

	JSONObject modelStructure;

	public JSONObject getModelStructure() {
		return modelStructure;
	}

	private HashSet<String> sensitivityList = new HashSet<String>();

	public void addSensitivity(String name) {
		sensitivityList.add(name);
	}

	public boolean removeSensitivity(String name) {
		return sensitivityList.remove(name);
	}

	public boolean sensitiveTo(String name) {
		return sensitivityList.contains(name);
	}

	public static class Entry {
		final ByteBuffer header;
		final ByteBuffer data;

		Entry(final ByteBuffer header, final ByteBuffer data) {
			this.header = header != null ? header.asReadOnlyBuffer() : null;
			this.data = data != null ? data.asReadOnlyBuffer() : null;
		}
	}

	public static enum EntryType {
		SYSTEM(TypeSize.BYTE * 2 + TypeSize.DOUBLE), RESOURCE(TypeSize.BYTE * 2
				+ TypeSize.INTEGER * 2 + TypeSize.DOUBLE), PATTERN(
				TypeSize.BYTE * 2 + TypeSize.DOUBLE), SEARCH(TypeSize.BYTE * 2
				+ TypeSize.INTEGER * 2 + TypeSize.DOUBLE), RESULT(TypeSize.BYTE
				+ TypeSize.INTEGER + TypeSize.DOUBLE);

		public final int HEADER_SIZE;

		private EntryType(int HEADER_SIZE) {
			this.HEADER_SIZE = HEADER_SIZE;
		}
	}

	private List<Entry> allEntries = new ArrayList<Entry>();

	public final List<Entry> getAllEntries() {
		return Collections.unmodifiableList(allEntries);
	}

	private final void addEntry(Entry entry) {
		allEntries.add(entry);
		notifyChange("EntryAdded");
	}

	private NotificationManager notificationManager = new NotificationManager(
			new String[] { "EntryAdded" });

	public final Notifier getNotifier() {
		return notificationManager;
	}

	private final void notifyChange(String category) {
		notificationManager.notifySubscribers(category);
	}

	private final DbIndexHelper indexHelper = new DbIndexHelper();

	public final DbIndexHelper getIndexHelper() {
		return indexHelper;
	}

	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //
	// ------------------------- SYSTEM ENTRIES ---------------------------- //
	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //

	static enum SystemEntryType {
		TRACE_START("Tracing started"), SIM_START("Simulation started"), NORMAL_TERMINATION(
				"Simulation finished: terminate condition"), NO_MORE_EVENTS(
				"Simulation finished: no more events"), ABORT(
				"Simulation finished: user interrupt"), RUN_TIME_ERROR(
				"Simulation finished: runtime error");

		SystemEntryType(String description) {
			this.description = description;
		}

		private final String description;

		final String getDescription() {
			return description;
		}
	}

	void addSystemEntry(SystemEntryType type) {
		ByteBuffer header = ByteBuffer.allocate(EntryType.SYSTEM.HEADER_SIZE);

		header.put((byte) EntryType.SYSTEM.ordinal())
				.putDouble(Simulator.getTime()).put((byte) type.ordinal());

		addEntry(new Entry(header, null));
	}

	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //
	// ---------------------- RESOURCE STATE ENTRIES ----------------------- //
	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //

	public static enum ResourceEntryType {
		CREATED, ERASED, ALTERED, SEARCH, SOLUTION
	}

	public void addResourceEntry(ResourceEntryType status, Resource resource,
			String sender) {
		String typeName = resource.getTypeName();

		CollectedDataNode resourceTypeNode = indexHelper
				.getResourceType(typeName);

		ResourceTypeIndex resourceTypeIndex = (ResourceTypeIndex) resourceTypeNode
				.getIndex();

		ResourceIndex resourceIndex;

		String name = resource.getName();
		if (name != null) {
			if (!sensitivityList.contains(name))
				return;
		} else {
			name = typeName + "[" + String.valueOf(resource.getNumber()) + "]";
			if (!sensitivityList.contains(name))
				if (status == ResourceEntryType.CREATED) {
					if (sensitivityList.contains(sender))
						sensitivityList.add(name);
					else
						return;
				} else
					return;
		}

		boolean shouldSerializeToIndex = true;

		switch (status) {
		case CREATED:
			CollectedDataNode resourceNode = resourceTypeNode.addChild(name);

			resourceIndex = new ResourceIndex(resource.getNumber());
			resourceNode.setIndex(resourceIndex);

			JSONArray parameters = resourceTypeIndex.getStructure()
					.getJSONArray("parameters");
			for (int paramNum = 0; paramNum < parameters.length(); paramNum++) {
				JSONObject param = parameters.getJSONObject(paramNum);
				ValueType paramType = ValueType.get(param.getString("type"));

				int offset = (paramType != ValueType.STRING) ? param
						.getInt("offset") : -1;

				resourceNode.addChild(
						parameters.getJSONObject(paramNum).getString("name"))
						.setIndex(
								new ResourceParameterIndex(paramNum, paramType,
										offset));
			}

			break;
		case SEARCH:
			shouldSerializeToIndex = false;
		case ALTERED:
		case SOLUTION:
			resourceIndex = (ResourceIndex) resourceTypeNode.getChildren()
					.get(name).getIndex();
			break;
		case ERASED:
			resourceIndex = (ResourceIndex) resourceTypeNode.getChildren()
					.get(name).getIndex();
			resourceIndex.erased = true;
			break;
		default:
			resourceIndex = null;
			break;
		}

		ByteBuffer header = ByteBuffer.allocate(EntryType.RESOURCE.HEADER_SIZE);
		header.put((byte) EntryType.RESOURCE.ordinal())
				.putDouble(Simulator.getTime()).put((byte) status.ordinal())
				.putInt(resourceTypeIndex.number).putInt(resourceIndex.number);

		ByteBuffer data = resource.serialize();

		Entry entry = new Entry(header, data);

		addEntry(entry);

		if (shouldSerializeToIndex)
			resourceIndex.entryNumbers.add(allEntries.size() - 1);
	}

	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //
	// ------------------------ PATTERN ENTRIES ---------------------------- //
	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //

	public static enum PatternType {
		EVENT, RULE, OPERATION_BEGIN, OPERATION_END
	}

	private static class PatternPoolEntry {
		final DecisionPoint dpt;
		final DecisionPoint.Activity activity;
		final int number;

		PatternPoolEntry(DecisionPoint dpt, DecisionPoint.Activity activity,
				int number) {
			this.dpt = dpt;
			this.activity = activity;
			this.number = number;
		}
	}

	private Map<Pattern, PatternPoolEntry> patternPool = new HashMap<Pattern, PatternPoolEntry>();

	public void addDecisionEntry(DecisionPoint dpt,
			DecisionPoint.Activity activity, PatternType type, Pattern pattern) {
		String dptName = dpt.getName();

		if (!sensitivityList.contains(dptName)
				&& !sensitivityList.contains(pattern.getName()))
			return;

		ByteBuffer header = ByteBuffer.allocate(EntryType.PATTERN.HEADER_SIZE);
		header.put((byte) EntryType.PATTERN.ordinal())
				.putDouble(Simulator.getTime()).put((byte) type.ordinal());

		CollectedDataNode dptNode = indexHelper.getDecisionPoint(dptName);
		DecisionPointIndex dptIndex = (DecisionPointIndex) dptNode.getIndex();
		PatternIndex index = (PatternIndex) dptNode.getChildren()
				.get(activity.getName()).getIndex();

		int number = index.timesExecuted++;
		if (type == PatternType.OPERATION_BEGIN)
			patternPool.put(pattern,
					new PatternPoolEntry(dpt, activity, number));

		int[] relevantResources = pattern.getRelevantInfo();

		ByteBuffer data = ByteBuffer.allocate(TypeSize.INTEGER
				* (relevantResources.length + 4));
		data.putInt(dptIndex.number).putInt(index.number).putInt(number);

		fillRelevantResources(data, relevantResources);

		Entry entry = new Entry(header, data);

		addEntry(entry);
		index.entryNumbers.add(allEntries.size() - 1);
	}

	public void addOperationEndEntry(Pattern pattern) {
		String name = pattern.getName();
		if (!sensitivityList.contains(name))
			return;

		PatternPoolEntry poolEntry = null;
		DecisionPointIndex dptIndex = null;

		poolEntry = patternPool.remove(pattern);
		if (poolEntry == null)
			return;
		CollectedDataNode dptNode = indexHelper.getDecisionPoint(poolEntry.dpt
				.getName());

		dptIndex = (DecisionPointIndex) dptNode.getIndex();
		PatternIndex index = (PatternIndex) dptNode.getChildren()
				.get(poolEntry.activity.getName()).getIndex();

		ByteBuffer header = ByteBuffer.allocate(EntryType.PATTERN.HEADER_SIZE);
		header.put((byte) EntryType.PATTERN.ordinal())
				.putDouble(Simulator.getTime())
				.put((byte) PatternType.OPERATION_END.ordinal());

		int[] relevantResources = pattern.getRelevantInfo();

		ByteBuffer data = ByteBuffer.allocate(TypeSize.INTEGER
				* (relevantResources.length + 4));

		data.putInt(dptIndex.number).putInt(index.number)
				.putInt(poolEntry.number);

		fillRelevantResources(data, relevantResources);

		Entry entry = new Entry(header, data);

		addEntry(entry);
		index.entryNumbers.add(allEntries.size() - 1);
	}

	private void fillRelevantResources(ByteBuffer data, int[] relevantResources) {
		data.putInt(relevantResources.length);
		for (int number : relevantResources)
			data.putInt(number);
	}

	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //
	// ------------------------ PATTERN ENTRIES ---------------------------- //
	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //

	public void addEventEntry(Event event) {
		String name = event.getName();
		if (!sensitivityList.contains(name))
			return;

		if (!sensitivityList.contains(name))
			return;
		EventIndex index = (EventIndex) indexHelper.getEvent(name).getIndex();

		ByteBuffer header = ByteBuffer.allocate(EntryType.PATTERN.HEADER_SIZE);
		header.put((byte) EntryType.PATTERN.ordinal())
				.putDouble(Simulator.getTime())
				.put((byte) PatternType.EVENT.ordinal());

		ByteBuffer data = ByteBuffer.allocate(TypeSize.INTEGER * 2);
		data.putInt(index.number).putInt(index.timesExecuted++);

		Entry entry = new Entry(header, data);

		addEntry(entry);
		index.entryNumbers.add(allEntries.size() - 1);
	}

	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //
	// -------------------------- SEARCH ENTRIES --------------------------- //
	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //

	public static enum SearchEntryType {
		BEGIN, END, OPEN, SPAWN, DECISION;
	}

	void addSearchEntry(DecisionPointSearch<?> dpt, SearchEntryType type,
			ByteBuffer data) {
		String name = dpt.getName();

		SearchIndex index = (SearchIndex) indexHelper.getSearch(name)
				.getIndex();
		SearchInfo info = null;

		ByteBuffer header = ByteBuffer.allocate(EntryType.SEARCH.HEADER_SIZE);
		header.put((byte) EntryType.SEARCH.ordinal())
				.put((byte) type.ordinal()).putDouble(Simulator.getTime())
				.putInt(index.getNumber()).putInt(index.searches.size());

		switch (type) {
		case BEGIN:
			info = new SearchInfo();
			info.begin = allEntries.size();

			index.searches.add(info);
			break;
		case DECISION:
			info = index.searches.get(index.searches.size() - 1);
			if (info.decision != -1)
				info.decision = allEntries.size();
			break;
		case END:
			info = index.searches.get(index.searches.size() - 1);
			info.end = allEntries.size();
		default:
			break;
		}

		Entry entry = new Entry(header, data);

		addEntry(entry);
	}

	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //
	// -------------------------- RESULT ENTRIES --------------------------- //
	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //

	public static enum ResultType {
		GET_VALUE("get_value"), WATCH_PAR("watch_par"), WATCH_QUANT(
				"watch_quant"), WATCH_STATE("watch_state"), WATCH_VALUE(
				"watch_value");

		ResultType(String type) {
			this.type = type;
		}

		static final ResultType get(final String type) {
			for (ResultType t : values()) {
				if (t.type.equals(type))
					return t;
			}
			return null;
		}

		public String getString() {
			return type;
		}

		final private String type;
	}

	public void addResultEntry(Result result) {
		String name = result.getName();
		if (!sensitivityList.contains(name))
			return;

		Index index = indexHelper.getResult(name).getIndex();

		ByteBuffer data = result.serialize();
		if (!index.isEmpty()) {
			ByteBuffer lastResultValue = allEntries.get(index.getEntryNumbers()
					.get(index.getEntryNumbers().size() - 1)).data.duplicate();
			ByteBuffer currentResultValue = data.duplicate();
			currentResultValue.rewind();
			lastResultValue.rewind();
			if (currentResultValue.compareTo(lastResultValue) == 0)
				return;
		}

		ByteBuffer header = ByteBuffer.allocate(EntryType.RESULT.HEADER_SIZE);
		header.put((byte) EntryType.RESULT.ordinal())
				.putDouble(Simulator.getTime()).putInt(index.getNumber());

		Entry entry = new Entry(header, data);

		addEntry(entry);
		index.getEntryNumbers().add(allEntries.size() - 1);
	}
}
