package ru.bmstu.rk9.rao.lib.database;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.bmstu.rk9.rao.lib.database.CollectedDataNode.LogicIndex;
import ru.bmstu.rk9.rao.lib.database.CollectedDataNode.EventIndex;
import ru.bmstu.rk9.rao.lib.database.CollectedDataNode.Index;
import ru.bmstu.rk9.rao.lib.database.CollectedDataNode.PatternIndex;
import ru.bmstu.rk9.rao.lib.database.CollectedDataNode.ResourceIndex;
import ru.bmstu.rk9.rao.lib.database.CollectedDataNode.ResourceParameterIndex;
import ru.bmstu.rk9.rao.lib.database.CollectedDataNode.ResourceTypeIndex;
import ru.bmstu.rk9.rao.lib.database.CollectedDataNode.ResultIndex;
import ru.bmstu.rk9.rao.lib.database.CollectedDataNode.SearchIndex;
import ru.bmstu.rk9.rao.lib.database.CollectedDataNode.SearchIndex.SearchInfo;
import ru.bmstu.rk9.rao.lib.dpt.AbstractDecisionPoint;
import ru.bmstu.rk9.rao.lib.dpt.Search;
import ru.bmstu.rk9.rao.lib.dpt.AbstractActivity;
import ru.bmstu.rk9.rao.lib.event.Event;
import ru.bmstu.rk9.rao.lib.json.JSONArray;
import ru.bmstu.rk9.rao.lib.json.JSONObject;
import ru.bmstu.rk9.rao.lib.modelStructure.ModelStructureCache.ValueType;
import ru.bmstu.rk9.rao.lib.modelStructure.ValueCache;
import ru.bmstu.rk9.rao.lib.notification.Notifier;
import ru.bmstu.rk9.rao.lib.pattern.Rule;
import ru.bmstu.rk9.rao.lib.resource.Resource;
import ru.bmstu.rk9.rao.lib.result.Result;
import ru.bmstu.rk9.rao.lib.simulator.Simulator;

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

		public static class Rao {
			public static final int INTEGER = TypeSize.INTEGER;
			public static final int REAL = TypeSize.DOUBLE;
			public static final int ENUM = TypeSize.SHORT;
			public static final int BOOLEAN = TypeSize.BYTE;
		}

		public static class Internal {
			public static final int ENTRY_TYPE_SIZE = TypeSize.BYTE;
			public static final int ENTRY_TYPE_OFFSET = 0;

			public static final int TIME_SIZE = TypeSize.DOUBLE;
			public static final int TIME_OFFSET = ENTRY_TYPE_SIZE + ENTRY_TYPE_OFFSET;
		}
	}

	public enum SerializationCategory {
		RESOURCES("Resources"), PATTERNS("Patterns"), EVENTS("Events"), DECISION_POINTS("Decision points"), RESULTS(
				"Results"), SEARCH("Search");

		SerializationCategory(final String name) {
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

	public Database(final JSONObject modelStructure) {
		this.modelStructure = modelStructure;
		String modelName = modelStructure.getString("name");
		indexHelper.initializeModel(modelName);

		final JSONArray resourceTypes = modelStructure.getJSONArray("resource_types");
		for (int i = 0; i < resourceTypes.length(); i++) {
			final JSONObject resourceType = resourceTypes.getJSONObject(i);

			final String name = modelName + "." + resourceType.getString("name");
			final CollectedDataNode typeNode = indexHelper.addResourceType(name);
			final ResourceTypeIndex resourceTypeIndex = new ResourceTypeIndex(i,
					resourceType.getJSONObject("structure"));
			typeNode.setIndex(resourceTypeIndex);
		}

		final JSONArray results = modelStructure.getJSONArray("results");
		for (int i = 0; i < results.length(); i++) {
			final JSONObject result = results.getJSONObject(i);
			final ResultType type = ResultType.getByString(result.getString("type"));
			final String name = modelName + "." + result.getString("name");
			indexHelper.addResult(name).setIndex(new ResultIndex(i, type));
		}

		final JSONArray patterns = modelStructure.getJSONArray("patterns");
		final Map<String, JSONObject> patternsByName = new HashMap<String, JSONObject>();
		for (int i = 0; i < patterns.length(); i++) {
			final JSONObject pattern = patterns.getJSONObject(i);
			final String name = modelName + "." + pattern.getString("name");
			patternsByName.put(name, pattern);
		}

		final JSONArray events = modelStructure.getJSONArray("events");
		for (int i = 0; i < events.length(); i++) {
			final JSONObject event = events.getJSONObject(i);
			final String name = modelName + "." + event.getString("name");
			final CollectedDataNode eventNode = indexHelper.addEvent(name);
			eventNode.setIndex(new EventIndex(i, event));
		}

		final JSONArray decisionPoints = modelStructure.getJSONArray("decision_points");
		for (int i = 0; i < decisionPoints.length(); i++) {
			final JSONObject decisionPoint = decisionPoints.getJSONObject(i);
			final String type = decisionPoint.getString("type");
			final String dptName = modelName + "." + decisionPoint.getString("name");
			switch (type) {
			case "some":
			case "prior":
				final CollectedDataNode dptNode = indexHelper.addLogic(dptName);
				dptNode.setIndex(new LogicIndex(i));

				final JSONArray activities = decisionPoint.getJSONArray("activities");
				for (int j = 0; j < activities.length(); j++) {
					final JSONObject activity = activities.getJSONObject(j);
					final String name = activity.getString("name");
					dptNode.addChild(name)
							.setIndex(new PatternIndex(j, patternsByName.get(activity.getString("pattern"))));
				}
				break;
			case "search":
				indexHelper.addSearch(dptName).setIndex(new SearchIndex(i));
				break;
			}
		}

		addSystemEntry(SystemEntryType.TRACE_START);

		for (final String traceName : SerializationObjectsNames.get())
			addSensitivity(traceName);
	}

	final JSONObject modelStructure;

	public final JSONObject getModelStructure() {
		return modelStructure;
	}

	private final HashSet<String> sensitivityList = new HashSet<String>();

	public final void addSensitivity(final String name) {
		sensitivityList.add(name);
	}

	public final boolean removeSensitivity(final String name) {
		return sensitivityList.remove(name);
	}

	public final boolean sensitiveTo(final String name) {
		return sensitivityList.contains(name);
	}

	public static class Entry {

		final ByteBuffer header;
		final ByteBuffer data;

		Entry(final ByteBuffer header, final ByteBuffer data) {
			this.header = header != null ? header.asReadOnlyBuffer() : null;
			this.data = data != null ? data.asReadOnlyBuffer() : null;
		}

		public final ByteBuffer getHeader() {
			return header;
		}

		public final ByteBuffer getData() {
			return data;
		}
	}

	public static enum EntryType {
		SYSTEM(TypeSize.BYTE * 2 + TypeSize.DOUBLE, 0), RESOURCE(
				TypeSize.BYTE * 2 + TypeSize.INTEGER * 2 + TypeSize.DOUBLE + TypeSize.INTEGER,
				0), PATTERN(TypeSize.BYTE * 2 + TypeSize.DOUBLE, TypeSize.INTEGER * 4), EVENT(
						TypeSize.BYTE * 2 + TypeSize.DOUBLE,
						TypeSize.INTEGER * 2), SEARCH(TypeSize.BYTE * 2 + TypeSize.INTEGER * 2 + TypeSize.DOUBLE,
								0), RESULT(TypeSize.BYTE + TypeSize.INTEGER + TypeSize.DOUBLE, 0);

		final int HEADER_SIZE;
		final int METADATA_SIZE;

		private EntryType(final int HEADER_SIZE, final int METADATA_SIZE) {
			this.HEADER_SIZE = HEADER_SIZE;
			this.METADATA_SIZE = METADATA_SIZE;
		}
	}

	private final List<Entry> allEntries = new ArrayList<Entry>();

	public final List<Entry> getAllEntries() {
		return Collections.unmodifiableList(allEntries);
	}

	private final void addEntry(final Entry entry) {
		allEntries.add(entry);
		notifyChange(NotificationCategory.ENTRY_ADDED);
	}

	public enum NotificationCategory {
		ENTRY_ADDED
	};

	private final Notifier<NotificationCategory> notifier = new Notifier<NotificationCategory>(
			NotificationCategory.class);

	public final Notifier<NotificationCategory> getNotifier() {
		return notifier;
	}

	private final void notifyChange(final NotificationCategory category) {
		notifier.notifySubscribers(category);
	}

	private final DbIndexHelper indexHelper = new DbIndexHelper();

	public final DbIndexHelper getIndexHelper() {
		return indexHelper;
	}

	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //
	// ------------------------- SYSTEM ENTRIES ---------------------------- //
	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //

	public static enum SystemEntryType {
		TRACE_START("Tracing started"), SIM_START("Simulation started"), NORMAL_TERMINATION(
				"Simulation finished: terminate condition"), NO_MORE_EVENTS(
						"Simulation finished: no more events"), ABORT(
								"Simulation finished: user interrupt"), RUN_TIME_ERROR(
										"Simulation finished: runtime error");

		SystemEntryType(final String description) {
			this.description = description;
		}

		private final String description;

		public final String getDescription() {
			return description;
		}
	}

	public final void addSystemEntry(final SystemEntryType type) {
		final ByteBuffer header = ByteBuffer.allocate(EntryType.SYSTEM.HEADER_SIZE);

		header.put((byte) EntryType.SYSTEM.ordinal()).putDouble(Simulator.getTime()).put((byte) type.ordinal());

		addEntry(new Entry(header, null));
	}

	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //
	// ---------------------- RESOURCE STATE ENTRIES ----------------------- //
	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //

	public static enum ResourceEntryType {
		CREATED, ERASED, ALTERED, SEARCH, SOLUTION
	}

	public class ResourceUniqueEntry {
		public ResourceUniqueEntry(final Resource resource, final ResourceEntryType status) {
			this.resource = resource;
			this.status = status;
		}

		private final Resource resource;
		private final ResourceEntryType status;

		@Override
		public boolean equals(final Object object) {
			if (object == null || !(object instanceof ResourceUniqueEntry))
				return false;

			final ResourceUniqueEntry other = (ResourceUniqueEntry) object;
			if (this.resource.getTypeName().equals(other.resource.getTypeName())
					&& this.resource.getNumber() == other.resource.getNumber() && this.status == other.status) {
				return true;
			}

			return false;
		}

		@Override
		public int hashCode() {
			final int primeMagic = 31;
			int result = 17;

			result = result * primeMagic + status.ordinal();
			result = result * primeMagic + resource.getNumber();
			result = result * primeMagic + resource.getTypeName().hashCode();

			return result;
		}
	}

	private final Set<ResourceUniqueEntry> memorizedResourceEntries = new LinkedHashSet<ResourceUniqueEntry>();

	public final void memorizeResourceEntry(final Resource resource, final ResourceEntryType updateType) {
		final ResourceUniqueEntry entry = new ResourceUniqueEntry(resource, updateType);
		memorizedResourceEntries.remove(entry);
		memorizedResourceEntries.add(entry);
	}

	public final void addMemorizedResourceEntries(final String sender, final Rule.ExecutedFrom executedFrom,
			String dptName) {
		for (final ResourceUniqueEntry entry : memorizedResourceEntries) {
			final Resource resource = entry.resource;
			final ResourceEntryType status = entry.status;
			ResourceEntryType actualStatus;

			if (executedFrom != null && status == ResourceEntryType.ALTERED)
				actualStatus = executedFrom.resourceSpecialStatus;
			else
				actualStatus = status;

			Simulator.getDatabase().addResourceEntry(resource, actualStatus, sender, dptName);
		}

		memorizedResourceEntries.clear();
	}

	private final void addResourceEntry(final Resource resource, final ResourceEntryType status, final String sender,
			final String dptName) {
		final String typeName = resource.getTypeName();

		final CollectedDataNode resourceTypeNode = indexHelper.getResourceType(typeName);

		final ResourceTypeIndex resourceTypeIndex = (ResourceTypeIndex) resourceTypeNode.getIndex();

		ResourceIndex resourceIndex;

		String name = resource.getName();
		if (name != null) {
			if (!sensitivityList.contains(name))
				return;
		} else {
			name = typeName + "[" + String.valueOf(resource.getNumber()) + "]";
			if (!sensitivityList.contains(name)) {
				if (status == ResourceEntryType.CREATED) {
					if (sensitivityList.contains(sender))
						sensitivityList.add(name);
					else
						return;
				} else {
					return;
				}
			}
		}

		boolean shouldSerializeToIndex = true;
		int dptNumber = -1;

		switch (status) {
		case CREATED:
			final CollectedDataNode resourceNode = resourceTypeNode.addChild(name);

			resourceIndex = new ResourceIndex(resource.getNumber());
			resourceNode.setIndex(resourceIndex);

			final JSONArray parameters = resourceTypeIndex.getStructure().getJSONArray("parameters");
			for (int paramNum = 0; paramNum < parameters.length(); paramNum++) {
				final JSONObject param = parameters.getJSONObject(paramNum);
				final ValueCache paramType = new ValueCache(param);

				final int offset = (paramType.getType() != ValueType.STRING) ? param.getInt("offset") : -1;

				resourceNode.addChild(parameters.getJSONObject(paramNum).getString("name"))
						.setIndex(new ResourceParameterIndex(paramNum, paramType, offset));
			}

			break;
		case SEARCH:
			shouldSerializeToIndex = false;
		case SOLUTION:
			dptNumber = indexHelper.getSearch(dptName).getIndex().getNumber();
		case ALTERED:
			resourceIndex = (ResourceIndex) resourceTypeNode.getChildren().get(name).getIndex();

			break;
		case ERASED:
			resourceIndex = (ResourceIndex) resourceTypeNode.getChildren().get(name).getIndex();
			resourceIndex.setErased(true);
			break;
		default:
			resourceIndex = null;
			break;
		}

		final ByteBuffer header = ByteBuffer.allocate(EntryType.RESOURCE.HEADER_SIZE);
		header.put((byte) EntryType.RESOURCE.ordinal()).putDouble(Simulator.getTime()).put((byte) status.ordinal())
				.putInt(resourceTypeIndex.getNumber()).putInt(resourceIndex.getNumber()).putInt(dptNumber);

		final ByteBuffer data = resource.serialize();

		final Entry entry = new Entry(header, data);

		addEntry(entry);

		if (shouldSerializeToIndex)
			resourceIndex.getEntryNumbers().add(allEntries.size() - 1);
	}

	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //
	// ------------------------ PATTERN ENTRIES ---------------------------- //
	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //

	public static enum PatternType {
		RULE, OPERATION_BEGIN, OPERATION_END
	}

	private static class PatternPoolEntry {
		final AbstractDecisionPoint dpt;
		final AbstractActivity activity;
		final int number;

		PatternPoolEntry(final AbstractDecisionPoint dpt, final AbstractActivity activity, final int number) {
			this.dpt = dpt;
			this.activity = activity;
			this.number = number;
		}
	}

	private final Map<Rule, PatternPoolEntry> patternPool = new HashMap<Rule, PatternPoolEntry>();

	public final void addDecisionEntry(final AbstractDecisionPoint dpt, final AbstractActivity activity,
			final PatternType type, final Rule rule) {
		final String dptName = dpt.getName();

		if (!sensitivityList.contains(dptName) && !sensitivityList.contains(rule.getName()))
			return;

		final ByteBuffer header = ByteBuffer.allocate(EntryType.PATTERN.HEADER_SIZE);
		header.put((byte) EntryType.PATTERN.ordinal()).putDouble(Simulator.getTime()).put((byte) type.ordinal());

		final CollectedDataNode dptNode = indexHelper.getDecisionPoint(dptName);
		final LogicIndex dptIndex = (LogicIndex) dptNode.getIndex();
		final PatternIndex index = (PatternIndex) dptNode.getChildren().get(activity.getName()).getIndex();

		final int number = index.incrementTimesExecuted();
		if (type == PatternType.OPERATION_BEGIN)
			patternPool.put(rule, new PatternPoolEntry(dpt, activity, number));

		final List<Integer> relevantResources = rule.getRelevantInfo();

		final ByteBuffer data = ByteBuffer
				.allocate(EntryType.PATTERN.METADATA_SIZE + relevantResources.size() * TypeSize.INTEGER);
		data.putInt(dptIndex.getNumber()).putInt(index.getNumber()).putInt(number);

		fillRelevantResources(data, relevantResources);

		final Entry entry = new Entry(header, data);

		addEntry(entry);
		index.getEntryNumbers().add(allEntries.size() - 1);
	}

	public final void addOperationEndEntry(final Rule rule) {
		final String name = rule.getName();
		if (!sensitivityList.contains(name))
			return;

		PatternPoolEntry poolEntry = null;
		LogicIndex dptIndex = null;

		poolEntry = patternPool.remove(rule);
		if (poolEntry == null)
			return;
		final CollectedDataNode dptNode = indexHelper.getDecisionPoint(poolEntry.dpt.getName());

		dptIndex = (LogicIndex) dptNode.getIndex();
		final PatternIndex index = (PatternIndex) dptNode.getChildren().get(poolEntry.activity.getName()).getIndex();

		final ByteBuffer header = ByteBuffer.allocate(EntryType.PATTERN.HEADER_SIZE);
		header.put((byte) EntryType.PATTERN.ordinal()).putDouble(Simulator.getTime())
				.put((byte) PatternType.OPERATION_END.ordinal());

		final List<Integer> relevantResources = rule.getRelevantInfo();

		final ByteBuffer data = ByteBuffer
				.allocate(EntryType.PATTERN.METADATA_SIZE + relevantResources.size() * TypeSize.INTEGER);

		data.putInt(dptIndex.getNumber()).putInt(index.getNumber()).putInt(poolEntry.number);

		fillRelevantResources(data, relevantResources);

		final Entry entry = new Entry(header, data);

		addEntry(entry);
		index.getEntryNumbers().add(allEntries.size() - 1);
	}

	private final void fillRelevantResources(final ByteBuffer data, final List<Integer> relevantResources) {
		data.putInt(relevantResources.size());
		for (final int number : relevantResources)
			data.putInt(number);
	}

	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //
	// ------------------------- EVENT ENTRIES ----------------------------- //
	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //

	public final void addEventEntry(final Event event) {
		final String name = event.getName();

		if (!sensitivityList.contains(name))
			return;

		if (!sensitivityList.contains(name))
			return;
		final EventIndex index = (EventIndex) indexHelper.getEvent(name).getIndex();

		final ByteBuffer header = ByteBuffer.allocate(EntryType.PATTERN.HEADER_SIZE);
		header.put((byte) EntryType.EVENT.ordinal()).putDouble(Simulator.getTime());

		final ByteBuffer data = ByteBuffer.allocate(EntryType.EVENT.METADATA_SIZE);
		data.putInt(index.getNumber()).putInt(index.incrementTimesExecuted());

		final Entry entry = new Entry(header, data);

		addEntry(entry);
		index.getEntryNumbers().add(allEntries.size() - 1);
	}

	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //
	// -------------------------- SEARCH ENTRIES --------------------------- //
	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //

	public static enum SearchEntryType {
		BEGIN, END, OPEN, SPAWN, DECISION;
	}

	public final void addSearchEntry(final Search dpt, final SearchEntryType type, final ByteBuffer data) {
		final String name = dpt.getName();

		final SearchIndex index = (SearchIndex) indexHelper.getSearch(name).getIndex();
		SearchInfo info = null;

		final ByteBuffer header = ByteBuffer.allocate(EntryType.SEARCH.HEADER_SIZE);
		header.put((byte) EntryType.SEARCH.ordinal()).put((byte) type.ordinal()).putDouble(Simulator.getTime())
				.putInt(index.getNumber()).putInt(index.getSearches().size());

		switch (type) {
		case BEGIN:
			info = new SearchInfo();
			info.setBegin(allEntries.size());

			index.getSearches().add(info);
			break;
		case DECISION:
			info = index.getSearches().get(index.getSearches().size() - 1);
			if (info.decisionStart() != -1)
				info.setDecision(allEntries.size());
			break;
		case END:
			info = index.getSearches().get(index.getSearches().size() - 1);
			info.setEnd(allEntries.size());
		default:
			break;
		}

		final Entry entry = new Entry(header, data);

		addEntry(entry);
	}

	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //
	// -------------------------- RESULT ENTRIES --------------------------- //
	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //

	public static enum ResultType {
		GET_VALUE("getValue"), WATCH_PAR("watchParameter"), WATCH_QUANT("watchQuantity"), WATCH_STATE(
				"watchState"), WATCH_VALUE("watchValue");

		ResultType(final String type) {
			this.type = type;
		}

		static final ResultType getByString(final String type) {
			for (final ResultType t : values()) {
				if (t.type.equals(type))
					return t;
			}
			throw new DatabaseException("Unexpected result type: " + type);
		}

		public String getString() {
			return type;
		}

		final private String type;
	}

	public final void addResultEntry(final Result result) {
		final String name = result.getName();
		if (!sensitivityList.contains(name))
			return;

		final Index index = indexHelper.getResult(name).getIndex();

		final ByteBuffer data = result.serialize();
		if (!index.isEmpty()) {
			final ByteBuffer lastResultValue = allEntries
					.get(index.getEntryNumbers().get(index.getEntryNumbers().size() - 1)).data.duplicate();
			final ByteBuffer currentResultValue = data.duplicate();
			currentResultValue.rewind();
			lastResultValue.rewind();
			if (currentResultValue.compareTo(lastResultValue) == 0)
				return;
		}

		final ByteBuffer header = ByteBuffer.allocate(EntryType.RESULT.HEADER_SIZE);
		header.put((byte) EntryType.RESULT.ordinal()).putDouble(Simulator.getTime()).putInt(index.getNumber());

		final Entry entry = new Entry(header, data);

		addEntry(entry);
		index.getEntryNumbers().add(allEntries.size() - 1);
	}
}
