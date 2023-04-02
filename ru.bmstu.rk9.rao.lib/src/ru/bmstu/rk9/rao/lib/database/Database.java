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
import java.util.function.Function;

import ru.bmstu.rk9.rao.lib.database.CollectedDataNode.EventIndex;
import ru.bmstu.rk9.rao.lib.database.CollectedDataNode.LogicIndex;
import ru.bmstu.rk9.rao.lib.database.CollectedDataNode.PatternIndex;
import ru.bmstu.rk9.rao.lib.database.CollectedDataNode.ResourceIndex;
import ru.bmstu.rk9.rao.lib.database.CollectedDataNode.ResourceParameterIndex;
import ru.bmstu.rk9.rao.lib.database.CollectedDataNode.ResourceTypeIndex;
import ru.bmstu.rk9.rao.lib.database.CollectedDataNode.ResultIndex;
import ru.bmstu.rk9.rao.lib.database.CollectedDataNode.SearchIndex;
import ru.bmstu.rk9.rao.lib.database.CollectedDataNode.SearchIndex.SearchInfo;
import ru.bmstu.rk9.rao.lib.dpt.AbstractActivity;
import ru.bmstu.rk9.rao.lib.dpt.AbstractDecisionPoint;
import ru.bmstu.rk9.rao.lib.dpt.Search;
import ru.bmstu.rk9.rao.lib.event.Event;
import ru.bmstu.rk9.rao.lib.json.JSONArray;
import ru.bmstu.rk9.rao.lib.json.JSONObject;
import ru.bmstu.rk9.rao.lib.modeldata.ModelStructureConstants;
import ru.bmstu.rk9.rao.lib.notification.Notifier;
import ru.bmstu.rk9.rao.lib.pattern.Operation;
import ru.bmstu.rk9.rao.lib.pattern.Pattern;
import ru.bmstu.rk9.rao.lib.pattern.Rule;
import ru.bmstu.rk9.rao.lib.resource.Resource;
import ru.bmstu.rk9.rao.lib.result.AbstractResult;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorInitializationInfo;

public class Database {
	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //
	// ---------------------------- TYPE INFO ------------------------------ //
	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //

	public static class TypeSize {
		public static final int INT = Integer.SIZE / Byte.SIZE;
		public static final int DOUBLE = Double.SIZE / Byte.SIZE;
		public static final int SHORT = Short.SIZE / Byte.SIZE;
		public static final int LONG = Long.SIZE / Byte.SIZE;
		public static final int BYTE = 1;

		public static class Internal {
			public static final int ENTRY_TYPE_SIZE = TypeSize.BYTE;
			public static final int ENTRY_TYPE_OFFSET = 0;

			public static final int TIME_SIZE = TypeSize.DOUBLE;
			public static final int TIME_OFFSET = ENTRY_TYPE_SIZE + ENTRY_TYPE_OFFSET;
		}
	}

	public enum DataType {
		INT(TypeSize.INT, "int", "Integer"), DOUBLE(TypeSize.DOUBLE, "double", "Double"),
		BOOLEAN(TypeSize.BYTE, "boolean", "Boolean"), OTHER(0, "", "");

		DataType(int size, String namePrivitive, String nameClass) {
			this.size = size;
			this.namePrimitive = namePrivitive;
			this.nameClass = nameClass;
		}

		public final int getSize() {
			return size;
		}

		public static final DataType getByName(String name) {
			for (final DataType t : values()) {
				if (t.namePrimitive.equals(name) || t.nameClass.equals(name))
					return t;
			}

			return DataType.OTHER;
		}

		private final String namePrimitive;
		private final String nameClass;
		private final int size;
	}

	public enum SerializationCategory {
		RESOURCE("Resources"), PATTERN("Patterns"), EVENT("Events"), LOGIC("Logic"), RESULT("Results"),
		SEARCH("Search");

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
		String modelName = modelStructure.getString(ModelStructureConstants.NAME);
		indexHelper.initializeModel(modelName);

		final JSONArray resourceTypes = modelStructure.getJSONArray(ModelStructureConstants.RESOURCE_TYPES);
		for (int i = 0; i < resourceTypes.length(); i++) {
			final JSONObject resourceType = resourceTypes.getJSONObject(i);

			final String name = resourceType.getString(ModelStructureConstants.NAME);
			final CollectedDataNode typeNode = indexHelper.addResourceType(name);
			final ResourceTypeIndex resourceTypeIndex = new ResourceTypeIndex(i);
			typeNode.setIndex(resourceTypeIndex);
		}

		final JSONArray results = modelStructure.getJSONArray(ModelStructureConstants.RESULTS);
		for (int i = 0; i < results.length(); i++) {
			final JSONObject result = results.getJSONObject(i);
			final String name = result.getString(ModelStructureConstants.NAME);
			final CollectedDataNode resultNode = indexHelper.addResult(name);
			resultNode.setIndex(new ResultIndex(i));
		}

		final JSONArray patterns = modelStructure.getJSONArray(ModelStructureConstants.PATTERNS);
		for (int i = 0; i < patterns.length(); i++) {
			final JSONObject pattern = patterns.getJSONObject(i);
			final String name = pattern.getString(ModelStructureConstants.NAME);
			final CollectedDataNode patternNode = indexHelper.addPattern(name);
			patternNode.setIndex(new PatternIndex(i));
		}

		final JSONArray events = modelStructure.getJSONArray(ModelStructureConstants.EVENTS);
		for (int i = 0; i < events.length(); i++) {
			final JSONObject event = events.getJSONObject(i);
			final String name = event.getString(ModelStructureConstants.NAME);
			final CollectedDataNode eventNode = indexHelper.addEvent(name);
			eventNode.setIndex(new EventIndex(i, event));
		}

		final JSONArray logics = modelStructure.getJSONArray(ModelStructureConstants.LOGICS);
		for (int i = 0; i < logics.length(); i++) {
			final JSONObject logic = logics.getJSONObject(i);
			final String dptName = logic.getString(ModelStructureConstants.NAME);
			final CollectedDataNode dptNode = indexHelper.addLogic(dptName);
			dptNode.setIndex(new LogicIndex(i));
		}

		final JSONArray searches = modelStructure.getJSONArray(ModelStructureConstants.SEARCHES);
		for (int i = 0; i < searches.length(); i++) {
			final JSONObject search = searches.getJSONObject(i);
			final String dptName = search.getString(ModelStructureConstants.NAME);
			indexHelper.addSearch(dptName).setIndex(new SearchIndex(i));
		}

		addSystemEntry(SystemEntryType.TRACE_START);

		for (final String traceName : SerializationObjectsNames.get())
			addSensitivity(traceName);
	}

	private Function<Double, String> timeFormatter = SimulatorInitializationInfo.DEFAULT_TIME_FORMATTER;

	public Function<Double, String> getTimeFormatter() {
		return timeFormatter;
	}

	public void setTimeFormatter(Function<Double, String> timeFormatter) {
		this.timeFormatter = timeFormatter;
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

		public Entry(final ByteBuffer header, final ByteBuffer data) {
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
		SYSTEM(TypeSize.BYTE * 2 + TypeSize.DOUBLE, 0),
		RESOURCE(TypeSize.BYTE * 2 + TypeSize.INT * 2 + TypeSize.DOUBLE + TypeSize.INT, 0),
		PATTERN(TypeSize.BYTE * 2 + TypeSize.DOUBLE, TypeSize.INT * 5),
		EVENT(TypeSize.BYTE * 2 + TypeSize.DOUBLE, TypeSize.INT * 2),
		SEARCH(TypeSize.BYTE * 2 + TypeSize.INT * 2 + TypeSize.DOUBLE, 0),
		RESULT(TypeSize.BYTE + TypeSize.INT + TypeSize.DOUBLE + TypeSize.BYTE, 0),
		PROCESS(TypeSize.BYTE + TypeSize.DOUBLE + TypeSize.BYTE + TypeSize.INT, 0);

		public final int HEADER_SIZE;
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

	private final IndexHelper indexHelper = new IndexHelper();

	public final IndexHelper getIndexHelper() {
		return indexHelper;
	}

	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //
	// ------------------------- SYSTEM ENTRIES ---------------------------- //
	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //

	public static enum SystemEntryType {
		TRACE_START("Tracing started"), SIM_START("Simulation started"),
		NORMAL_TERMINATION("Simulation finished: terminate condition"),
		NO_MORE_EVENTS("Simulation finished: no more events"), ABORT("Simulation finished: user interrupt"),
		RUN_TIME_ERROR("Simulation finished: runtime error");

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

		header.put((byte) EntryType.SYSTEM.ordinal()).putDouble(CurrentSimulator.getTime()).put((byte) type.ordinal());

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

			CurrentSimulator.getDatabase().addResourceEntry(resource, actualStatus, sender, dptName);
		}

		memorizedResourceEntries.clear();
	}

	private final void addResourceEntry(final Resource resource, final ResourceEntryType status, final String sender,
			final String dptName) {
		final String typeName = resource.getTypeName();
		final CollectedDataNode resourceTypeNode = indexHelper.getResourceType(typeName);
		int typeNumber = CurrentSimulator.getStaticModelData().getResourceTypeNumber(typeName);

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

		ResourceIndex resourceIndex;
		boolean shouldSerializeToIndex = true;
		int dptNumber = -1;

		switch (status) {
		case CREATED:
			final CollectedDataNode resourceNode = resourceTypeNode.addChild(name);

			resourceIndex = new ResourceIndex(resource.getNumber());
			resourceNode.setIndex(resourceIndex);

			final int numberOfParameters = CurrentSimulator.getStaticModelData()
					.getNumberOfResourceTypeParameters(typeNumber);
			for (int paramNum = 0; paramNum < numberOfParameters; paramNum++) {
				final JSONObject parameter = CurrentSimulator.getStaticModelData().getResourceTypeParameter(typeNumber,
						paramNum);
				resourceNode.addChild(parameter.getString(ModelStructureConstants.NAME))
						.setIndex(new ResourceParameterIndex(paramNum));
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
		header.put((byte) EntryType.RESOURCE.ordinal()).putDouble(CurrentSimulator.getTime())
				.put((byte) status.ordinal()).putInt(typeNumber).putInt(resourceIndex.getNumber()).putInt(dptNumber);

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

	private final Map<Pattern, PatternPoolEntry> patternPool = new HashMap<>();

	public final void addDecisionEntry(final AbstractDecisionPoint dpt, final AbstractActivity activity) {
		final String dptName = dpt.getTypeName();
		final Pattern pattern = activity.getPattern();
		final PatternType patternType = pattern instanceof Rule ? PatternType.RULE : PatternType.OPERATION_BEGIN;

		if (!sensitivityList.contains(dptName) && !sensitivityList.contains(pattern.getTypeName()))
			return;

		final ByteBuffer header = ByteBuffer.allocate(EntryType.PATTERN.HEADER_SIZE);
		header.put((byte) EntryType.PATTERN.ordinal()).putDouble(CurrentSimulator.getTime())
				.put((byte) patternType.ordinal());

		final CollectedDataNode dptNode = indexHelper.getLogic(dptName);
		final LogicIndex dptIndex = (LogicIndex) dptNode.getIndex();
		final CollectedDataNode patternNode = indexHelper.getPattern(pattern.getTypeName());
		final PatternIndex index = (PatternIndex) patternNode.getIndex();

		final int number = index.incrementTimesExecuted();
		if (patternType == PatternType.OPERATION_BEGIN)
			patternPool.put(pattern, new PatternPoolEntry(dpt, activity, number));

		final List<Integer> relevantResources = pattern.getRelevantResourcesNumbers();

		final ByteBuffer data = ByteBuffer
				.allocate(EntryType.PATTERN.METADATA_SIZE + relevantResources.size() * TypeSize.INT);
		data.putInt(dptIndex.getNumber()).putInt(activity.getNumber()).putInt(index.getNumber()).putInt(number);

		fillRelevantResources(data, relevantResources);

		final Entry entry = new Entry(header, data);

		addEntry(entry);
		index.getEntryNumbers().add(allEntries.size() - 1);
	}

	public final void addOperationEndEntry(final Operation operation) {
		final String name = operation.getTypeName();
		if (!sensitivityList.contains(name))
			return;

		PatternPoolEntry poolEntry = null;
		LogicIndex dptIndex = null;

		poolEntry = patternPool.remove(operation);
		if (poolEntry == null)
			return;

		final CollectedDataNode dptNode = indexHelper.getLogic(poolEntry.dpt.getTypeName());
		dptIndex = (LogicIndex) dptNode.getIndex();

		final CollectedDataNode patternNode = indexHelper.getPattern(operation.getTypeName());
		final PatternIndex index = (PatternIndex) patternNode.getIndex();

		final ByteBuffer header = ByteBuffer.allocate(EntryType.PATTERN.HEADER_SIZE);
		header.put((byte) EntryType.PATTERN.ordinal()).putDouble(CurrentSimulator.getTime())
				.put((byte) PatternType.OPERATION_END.ordinal());

		final List<Integer> relevantResources = operation.getRelevantResourcesNumbers();

		final ByteBuffer data = ByteBuffer
				.allocate(EntryType.PATTERN.METADATA_SIZE + relevantResources.size() * TypeSize.INT);

		data.putInt(dptIndex.getNumber()).putInt(poolEntry.activity.getNumber()).putInt(index.getNumber())
				.putInt(poolEntry.number);

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

		final EventIndex index = (EventIndex) indexHelper.getEvent(name).getIndex();

		final ByteBuffer header = ByteBuffer.allocate(EntryType.PATTERN.HEADER_SIZE);
		header.put((byte) EntryType.EVENT.ordinal()).putDouble(CurrentSimulator.getTime());

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
		final String name = dpt.getTypeName();

		final SearchIndex index = (SearchIndex) indexHelper.getSearch(name).getIndex();
		SearchInfo info = null;

		final ByteBuffer header = ByteBuffer.allocate(EntryType.SEARCH.HEADER_SIZE);
		header.put((byte) EntryType.SEARCH.ordinal()).put((byte) type.ordinal()).putDouble(CurrentSimulator.getTime())
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
		NUMBER, OTHER;
	}

	public final <T> void addResultEntry(final AbstractResult<T> result, T value) {
		final String name = result.getName();
		if (!sensitivityList.contains(name))
			return;

		final ResultIndex index = (ResultIndex) indexHelper.getResult(name).getIndex();

		final ByteBuffer header = ByteBuffer.allocate(EntryType.RESULT.HEADER_SIZE);
		header.put((byte) EntryType.RESULT.ordinal()).putInt(index.getNumber()).putDouble(CurrentSimulator.getTime());

		ByteBuffer data;
		if (value instanceof Number) {
			header.put((byte) ResultType.NUMBER.ordinal());
			data = ByteBuffer.allocate(TypeSize.DOUBLE);
			data.putDouble(((Number) value).doubleValue());
		} else {
			header.put((byte) ResultType.OTHER.ordinal());
			String valueStr = value.toString();
			byte[] valueBytes = valueStr.getBytes(java.nio.charset.StandardCharsets.UTF_8);
			int valueBytesLen = valueBytes.length;
			data = ByteBuffer.allocate(TypeSize.INT + TypeSize.BYTE * valueBytesLen);

			data.putInt(valueBytesLen);
			data.put(valueBytes);
		}

		final Entry entry = new Entry(header, data);

		addEntry(entry);
		index.getEntryNumbers().add(allEntries.size() - 1);
	}

	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //
	// ------------------------- PROCESS ENTRIES --------------------------- //
	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //

	public static enum ProcessEntryType {
		GENERATE("Generate"), TERMINATE("Terminate"), HOLD("Hold"), SEIZE("Seize"), RELEASE("Release"), QUEUE("Queue"),
		SELECT_PATH("SelectPath");

		private ProcessEntryType(final String block) {
			this.block = block;
		}

		public String getString() {
			return block;
		}

		final private String block;
	}

	public final void addProcessEntry(final ProcessEntryType processEntryType, final int index, ByteBuffer data) {
		final ByteBuffer header = ByteBuffer.allocate(EntryType.PROCESS.HEADER_SIZE);
		header.put((byte) EntryType.PROCESS.ordinal()).putDouble(CurrentSimulator.getTime())
				.put((byte) processEntryType.ordinal()).putInt(index);

		Entry entry = new Entry(header, data);
		addEntry(entry);
	}
}
