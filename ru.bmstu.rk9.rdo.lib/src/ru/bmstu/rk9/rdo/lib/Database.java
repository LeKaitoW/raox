package ru.bmstu.rk9.rdo.lib;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import ru.bmstu.rk9.rdo.lib.CollectedDataNode.AbstractIndex;
import ru.bmstu.rk9.rdo.lib.CollectedDataNode.Index;
import ru.bmstu.rk9.rdo.lib.CollectedDataNode.SearchIndex;
import ru.bmstu.rk9.rdo.lib.CollectedDataNode.PatternIndex;
import ru.bmstu.rk9.rdo.lib.CollectedDataNode.ResourceTypeIndex;
import ru.bmstu.rk9.rdo.lib.CollectedDataNode.SearchIndex.SearchInfo;
import ru.bmstu.rk9.rdo.lib.json.*;

public class Database
{
  /*――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――/
 /                                 TYPE INFO                                 /
/――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――*/

	public static class TypeSize
	{
		public static final int INTEGER = Integer.SIZE / Byte.SIZE;
		public static final int DOUBLE = Double.SIZE / Byte.SIZE;
		public static final int SHORT = Short.SIZE / Byte.SIZE;
		public static final int LONG = Long.SIZE / Byte.SIZE;
		public static final int BYTE = 1;

		public static class RDO
		{
			public static final int INTEGER = TypeSize.INTEGER;
			public static final int REAL    = TypeSize.DOUBLE;
			public static final int ENUM    = TypeSize.SHORT;
			public static final int BOOLEAN = TypeSize.BYTE;
		}

		public static class Internal
		{
			public static final int ENTRY_TYPE_SIZE = TypeSize.BYTE;
			public static final int ENTRY_TYPE_OFFSET = 0;

			public static final int TIME_SIZE = TypeSize.DOUBLE;
			public static final int TIME_OFFSET = ENTRY_TYPE_SIZE + ENTRY_TYPE_OFFSET;
		}
	}

	public enum SerializationCategory {
		RESOURCES("Resources"), PATTERNS("Patterns"), DECISION_POINTS(
				"Decision points"), RESULTS("Results"), SEARCH("Search");

		SerializationCategory(String name) {
			this.name = name;
		}

		private final String name;

		public final String getName() {
			return name;
		}
	}

  /*――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――/
 /                                  GENERAL                                  /
/――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――*/

	Database(JSONObject modelStructure)
	{
		this.modelStructure = modelStructure;
		CollectedDataNode modelIndex =
				indexTreeRoot.addChild(modelStructure.getString("name"));
		for (SerializationCategory value: SerializationCategory.values()) {
			modelIndex.addChild(value.getName());
		}

		JSONArray resourceTypes = modelStructure.getJSONArray("resource_types");
		for(int i = 0; i < resourceTypes.length(); i++)
		{
			JSONObject resourceType = resourceTypes.getJSONObject(i);

			String name = resourceType.getString("name");
			CollectedDataNode typeNode = modelIndex.getChildren()
					.get(SerializationCategory.RESOURCES.name).addChild(name);
			ResourceTypeIndex resourceTypeIndex =
					new ResourceTypeIndex(i, resourceType.getJSONObject("structure"));
			typeNode.setIndex(resourceTypeIndex);

			JSONArray resources = resourceType.getJSONArray("resources");
			for(int j = 0; j < resources.length(); j++)
				typeNode.addChild(resources.getString(j)).setIndex(new Index(j));
		}

		JSONArray results = modelStructure.getJSONArray("results");
		for(int i = 0; i < results.length(); i++)
		{
			JSONObject result = results.getJSONObject(i);
			modelIndex.getChildren().get(SerializationCategory.RESULTS.name)
					.addChild(result.getString("name")).setIndex(new Index(i));
		}

		JSONArray patterns = modelStructure.getJSONArray("patterns");
		Map<String, JSONObject> patternsByName = new HashMap<String, JSONObject>();
		for(int i = 0; i < patterns.length(); i++)
		{
			JSONObject patternStructure = patterns.getJSONObject(i);
			String name = patternStructure.getString("name");
			String type = patternStructure.getString("type");
			CollectedDataNode patternNode = modelIndex.getChildren()
					.get(SerializationCategory.PATTERNS.name)
					.addChild(name);
			if(type.equals("event"))
				patternNode.setIndex(new PatternIndex(i, patternStructure));
			else
				patternsByName.put(name, patternStructure);
		}

		JSONArray decisionPoints = modelStructure.getJSONArray("decision_points");
		for(int i = 0; i < decisionPoints.length(); i++)
		{
			JSONObject decisionPoint = decisionPoints.getJSONObject(i);
			String type = decisionPoint.getString("type");
			switch(type)
			{
				case "some":
				case "prior":
					CollectedDataNode dptNode = modelIndex.getChildren()
							.get(SerializationCategory.DECISION_POINTS.name)
							.addChild(decisionPoint.getString("name"));
					dptNode.setIndex(new Index(i));

					JSONArray activities = decisionPoint.getJSONArray("activities");
					for(int j = 0; j < activities.length(); j++)
					{
						JSONObject activity = activities.getJSONObject(j);
						String name = activity.getString("name");
						dptNode.addChild(name).setIndex(
								new PatternIndex(j, patternsByName.get(activity
										.getString("pattern"))));
					}
				break;
				case "search":
					modelIndex.getChildren()
							.get(SerializationCategory.SEARCH.name)
							.addChild(decisionPoint.getString("name"))
							.setIndex(new SearchIndex(i));
				break;
			}
		}

		addSystemEntry(SystemEntryType.TRACE_START);

		for(String traceName : SerializationConfig.getNames())
			addSensitivity(traceName);
	}

	JSONObject modelStructure;

	public JSONObject getModelStructure()
	{
		return modelStructure;
	}

	private HashSet<String> sensitivityList = new HashSet<String>();

	public void addSensitivity(String name)
	{
		sensitivityList.add(name);
	}

	public boolean removeSensitivity(String name)
	{
		return sensitivityList.remove(name);
	}

	public boolean sensitiveTo(String name)
	{
		return sensitivityList.contains(name);
	}

	public static class Entry
	{
		final ByteBuffer header;
		final ByteBuffer data;

		Entry(final ByteBuffer header, final ByteBuffer data)
		{
			this.header = header != null ? header.asReadOnlyBuffer() : null;
			this.data = data != null ? data.asReadOnlyBuffer() : null;
		}
	}

	public static enum EntryType
	{
		SYSTEM(TypeSize.BYTE * 2 + TypeSize.DOUBLE),
		RESOURCE(TypeSize.BYTE * 2 + TypeSize.INTEGER * 2 + TypeSize.DOUBLE),
		PATTERN(TypeSize.BYTE * 2 + TypeSize.DOUBLE),
		SEARCH(TypeSize.BYTE * 2 + TypeSize.INTEGER * 2 + TypeSize.DOUBLE),
		RESULT(TypeSize.BYTE + TypeSize.INTEGER + TypeSize.DOUBLE);

		public final int HEADER_SIZE;

		private EntryType(int HEADER_SIZE)
		{
			this.HEADER_SIZE = HEADER_SIZE;
		}
	}

	final CollectedDataNode getIndexNode(SerializationCategory type) {
		return indexTreeRoot.getChildren()
				.get(modelStructure.getString("name")).getChildren()
				.get(type.name);
	}

	private List<Entry> allEntries = new ArrayList<Entry>();

	public final List<Entry> getAllEntries() {
		return Collections.unmodifiableList(allEntries);
	}

	private final void addEntry(Entry entry)
	{
		allEntries.add(entry);
		notifyChange("EntryAdded");
	}

	private NotificationManager notificationManager =
		new NotificationManager(
			new String[]
			{
				"EntryAdded"
			}
		);

	public final Notifier getNotifier()
	{
		return notificationManager;
	}

	private final void notifyChange(String category)
	{
		notificationManager.notifySubscribers(category);
	}

	private final CollectedDataNode indexTreeRoot =
			new CollectedDataNode("root", null);

	public final CollectedDataNode getIndexTree() {
		return indexTreeRoot;
	}

  /*――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――/
 /                              SYSTEM ENTRIES                               /
/――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――*/

	static enum SystemEntryType
	{
		TRACE_START ("Tracing started"),
		SIM_START ("Simulation started"),
		NORMAL_TERMINATION ("Simulation finished: terminate condition"),
		NO_MORE_EVENTS ("Simulation finished: no more events"),
		ABORT ("Simulation finished: user interrupt"),
		RUN_TIME_ERROR ("Simulation finished: runtime error");

		SystemEntryType(String description)
		{
			this.description = description;
		}

		private final String description;

		final String getDescription()
		{
			return description;
		}
	}

	void addSystemEntry(SystemEntryType type)
	{
		ByteBuffer header = ByteBuffer.allocate(EntryType.SYSTEM.HEADER_SIZE);

		header
			.put((byte)EntryType.SYSTEM.ordinal())
			.putDouble(Simulator.getTime())
			.put((byte)type.ordinal());

		addEntry(new Entry(header, null));
	}

  /*――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――/
 /                           RESOURCE STATE ENTRIES                          /
/――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――*/

	public static enum ResourceEntryType
	{
		CREATED, ERASED, ALTERED, SEARCH, SOLUTION
	}

	public void addResourceEntry(ResourceEntryType status, Resource resource, String sender)
	{
		String typeName = resource.getTypeName();

		CollectedDataNode resourceTypeNode = getIndexNode(SerializationCategory.RESOURCES)
				.getChildren().get(typeName);

		ResourceTypeIndex resourceTypeIndex =
				(ResourceTypeIndex) resourceTypeNode.getIndex();

		Index resourceIndex;

		String name = resource.getName();
		if(name != null)
		{
			if(!sensitivityList.contains(name))
				return;
		}
		else
		{
			name = typeName + "[" + String.valueOf(resource.getNumber()) + "]";
			if(!sensitivityList.contains(name))
				if(status == ResourceEntryType.CREATED)
				{
					if(sensitivityList.contains(sender))
						sensitivityList.add(name);
					else
						return;
				}
				else
					return;
		}

		boolean shouldSerializeToIndex = true;

		switch (status)
		{
			case CREATED:
				resourceIndex = new Index(resource.getNumber());

				resourceTypeNode.addChild(name).setIndex(resourceIndex);
			break;
			case SEARCH:
				shouldSerializeToIndex = false;
			case ERASED:
			case ALTERED:
			case SOLUTION:
				resourceIndex = (Index) resourceTypeNode.getChildren().get(name).getIndex();
			break;
			default:
				resourceIndex = null;
			break;
		}

		ByteBuffer header = ByteBuffer.allocate(EntryType.RESOURCE.HEADER_SIZE);
		header
			.put((byte)EntryType.RESOURCE.ordinal())
			.putDouble(Simulator.getTime())
			.put((byte)status.ordinal())
			.putInt(resourceTypeIndex.number)
			.putInt(resourceIndex.number);

		ByteBuffer data = resource.serialize();

		Entry entry = new Entry(header, data);

		addEntry(entry);

		if(shouldSerializeToIndex)
			resourceIndex.entries.add(allEntries.size() - 1);
	}

  /*――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――/
 /                              PATTERN ENTRIES                              /
/――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――*/

	public static enum PatternType
	{
		EVENT, RULE, OPERATION_BEGIN, OPERATION_END
	}

	private static class PatternPoolEntry
	{
		final DecisionPoint dpt;
		final DecisionPoint.Activity activity;
		final int number;

		PatternPoolEntry
		(
			DecisionPoint dpt,
			DecisionPoint.Activity activity,
			int number
		)
		{
			this.dpt = dpt;
			this.activity = activity;
			this.number = number;
		}
	}

	private Map<Pattern, PatternPoolEntry> patternPool
		= new HashMap<Pattern, PatternPoolEntry>();

	public void addDecisionEntry
	(
		DecisionPoint dpt,
		DecisionPoint.Activity activity,
		PatternType type,
		Pattern pattern
	)
	{
		String dptName = dpt.getName();

		if(!sensitivityList.contains(dptName) && !sensitivityList.contains(pattern.getName()))
			return;

		ByteBuffer header = ByteBuffer.allocate(EntryType.PATTERN.HEADER_SIZE);
		header
			.put((byte)EntryType.PATTERN.ordinal())
			.putDouble(Simulator.getTime())
			.put((byte)type.ordinal());

		CollectedDataNode dptNode = getIndexNode(SerializationCategory.DECISION_POINTS)
				.getChildren().get(dptName);
		Index dptIndex = (Index) dptNode.getIndex();
		PatternIndex index = (PatternIndex) dptNode.getChildren().get(
				activity.getName()).getIndex();

		int number = index.timesExecuted++;
		if(type == PatternType.OPERATION_BEGIN)
			patternPool.put(pattern, new PatternPoolEntry(dpt, activity, number));

		int[] relevantResources = pattern.getRelevantInfo();

		ByteBuffer data = ByteBuffer.allocate(
			TypeSize.INTEGER * (relevantResources.length + 4));
		data
			.putInt(dptIndex.number)
			.putInt(index.number)
			.putInt(number);

		fillRelevantResources(data, relevantResources);

		Entry entry = new Entry(header, data);

		addEntry(entry);
		index.entries.add(allEntries.size() - 1);
	}

	public void addEventEntry(PatternType type, Pattern pattern)
	{
		String name = pattern.getName();
		if(!sensitivityList.contains(name))
			return;

		PatternIndex index;

		PatternPoolEntry poolEntry = null;
		Index dptIndex = null;

		if(type == PatternType.OPERATION_END)
		{
			poolEntry = patternPool.remove(pattern);
			if(poolEntry == null)
				return;
			CollectedDataNode dptNode = getIndexNode(SerializationCategory.DECISION_POINTS)
					.getChildren().get(poolEntry.dpt.getName());
			dptIndex = (Index) dptNode.getIndex();
			index = (PatternIndex) dptNode.getChildren().get(poolEntry.activity.getName()).getIndex();
		}
		else
		{
			if(!sensitivityList.contains(name))
				return;
			index = (PatternIndex) getIndexNode(SerializationCategory.PATTERNS)
					.getChildren().get(name).getIndex();
		}

		ByteBuffer header = ByteBuffer.allocate(EntryType.PATTERN.HEADER_SIZE);
		header
			.put((byte)EntryType.PATTERN.ordinal())
			.putDouble(Simulator.getTime())
			.put((byte)type.ordinal());

		int[] relevantResources = pattern.getRelevantInfo();

		ByteBuffer data = ByteBuffer.allocate(TypeSize.INTEGER * (relevantResources.length +
			(type == PatternType.OPERATION_END ? 4 : 3)));

		if(type == PatternType.OPERATION_END)
			data
				.putInt(dptIndex.number)
				.putInt(index.number)
				.putInt(poolEntry.number);
		else
			data
				.putInt(index.number)
				.putInt(index.timesExecuted++);

		fillRelevantResources(data, relevantResources);

		Entry entry = new Entry(header, data);

		addEntry(entry);
		index.entries.add(allEntries.size() - 1);
	}

	private void fillRelevantResources
	(
		ByteBuffer data,
		int[] relevantResources
	)
	{
		data.putInt(relevantResources.length);
		for(int number : relevantResources)
			data.putInt(number);
	}

  /*――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――/
 /                              SEARCH ENTRIES                               /
/――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――*/

	public static enum SearchEntryType
	{
		BEGIN, END, OPEN, SPAWN, DECISION;
	}

	void addSearchEntry(DecisionPointSearch<?> dpt, SearchEntryType type, ByteBuffer data)
	{
		String name = dpt.getName();

		SearchIndex index = (SearchIndex) getIndexNode(SerializationCategory.SEARCH)
				.getChildren().get(name).getIndex();
		SearchInfo info = null;

		ByteBuffer header = ByteBuffer.allocate(EntryType.SEARCH.HEADER_SIZE);
		header
			.put((byte)EntryType.SEARCH.ordinal())
			.put((byte)type.ordinal())
			.putDouble(Simulator.getTime())
			.putInt(index.getNumber())
			.putInt(index.searches.size());

		switch(type)
		{
			case BEGIN:
				info = new SearchInfo();
				info.begin = allEntries.size();

				index.searches.add(info);
			break;
			case DECISION:
				info = index.searches.get(index.searches.size() - 1);
				if(info.decision != -1)
					info.decision = allEntries.size();
			break;
			case END:
				info = index.searches.get(index.searches.size() - 1);
				info.end = allEntries.size();
			default: break;
		}

		Entry entry = new Entry(header, data);

		addEntry(entry);
	}

  /*――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――/
 /                              RESULT ENTRIES                               /
/――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――*/

	public void addResultEntry(Result result)
	{
		String name = result.getName();
		if(!sensitivityList.contains(name))
			return;

		AbstractIndex index = getIndexNode(SerializationCategory.RESULTS)
				.getChildren().get(name).getIndex();

		ByteBuffer data = result.serialize();
		if(!index.isEmpty())
		{
			ByteBuffer lastResultValue =
				allEntries.get(
					index.getEntries().get(index.getEntries().size() -1)
				).data.duplicate();
			ByteBuffer currentResultValue = data.duplicate();
			currentResultValue.rewind();
			lastResultValue.rewind();
			if(currentResultValue.compareTo(lastResultValue) == 0)
				return;
		}

		ByteBuffer header = ByteBuffer.allocate(EntryType.RESULT.HEADER_SIZE);
		header
			.put((byte)EntryType.RESULT.ordinal())
			.putDouble(Simulator.getTime())
			.putInt(index.getNumber());

		Entry entry = new Entry(header, data);

		addEntry(entry);
		index.getEntries().add(allEntries.size() - 1);
	}
}
