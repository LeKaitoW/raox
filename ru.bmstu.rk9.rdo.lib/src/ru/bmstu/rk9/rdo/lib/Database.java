package ru.bmstu.rk9.rdo.lib;

import java.nio.ByteBuffer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

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
			public static final int TIME_SIZE = TypeSize.DOUBLE;
			public static final int TIME_OFFSET = 0;

			public static final int ENTRY_TYPE_SIZE = TypeSize.BYTE;
			public static final int ENTRY_TYPE_OFFSET = TIME_OFFSET + TIME_SIZE;
		}
	}

  /*――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――/
 /                                  GENERAL                                  /
/――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――*/

	Database(JSONObject modelStructure)
	{
		this.modelStructure = modelStructure;

		JSONArray resourceTypes = modelStructure.getJSONArray("resource_types");
		for(int i = 0; i < resourceTypes.length(); i++)
		{
			JSONObject resourceType = resourceTypes.getJSONObject(i);

			String name = resourceType.getString("name");
			ResourceTypeIndex index =
				new ResourceTypeIndex(i, resourceType.getJSONObject("structure"));
			resourceIndex.put(name, index);
		}

		JSONArray results = modelStructure.getJSONArray("results");
		for(int i = 0; i < results.length(); i++)
		{
			JSONObject result = results.getJSONObject(i);
			resultIndex.put(result.getString("name"), new Index(i));
		}

		JSONArray patterns = modelStructure.getJSONArray("patterns");
		HashMap<String, JSONObject> patternsByName = new HashMap<String, JSONObject>();
		for(int i = 0, count = 0; i < patterns.length(); i++)
		{
			JSONObject patternStructure = patterns.getJSONObject(i);
			String name = patternStructure.getString("name");
			String type = patternStructure.getString("type");
			if(type == "event")
				eventIndex.put(name, new PatternIndex(count++, patternStructure));
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
					DecisionPointIndex dptIndex = new DecisionPointIndex(i);
					decisionPointIndex.put(decisionPoint.getString("name"), dptIndex);

					JSONArray activities = decisionPoint.getJSONArray("activities");
					for(int j = 0; j < activities.length(); j++)
					{
						JSONObject activity = activities.getJSONObject(j);
						String name = activity.getString("name");
						dptIndex.activities.put
						(
							name,
							new PatternIndex(j, patternsByName.get(
								activity.getString("pattern")))
						);
					}
				break;
				case "search":
					SearchIndex index = new SearchIndex(i);
					this.searchIndex.put(decisionPoint.getString("name"), index);
				break;
			}
		}

		addSystemEntry(SystemEntryType.TRACE_START);
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
		ByteBuffer header;
		ByteBuffer data;

		Entry(ByteBuffer header, ByteBuffer data)
		{
			this.header = header;
			this.data = data;
		}
	}

	public static class Index
	{
		final int number;

		ArrayList<Integer> entries = new ArrayList<Integer>();

		private Index(int number)
		{
			this.number = number;
		}
	}

	public static enum EntryType
	{
		SYSTEM(10), RESOURCE(18), PATTERN(10), SEARCH(2), RESULT(13);

		public final int HEADER_SIZE;

		private EntryType(int HEADER_SIZE)
		{
			this.HEADER_SIZE = HEADER_SIZE;
		}
	}

	ArrayList<Entry> allEntries = new ArrayList<Entry>();

  /*――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――/
 /                              SYSTEM ENTRIES                               /
/――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――*/

	static enum SystemEntryType
	{
		TRACE_START, TRACE_END, SIM_START, SIM_FINISH, SIM_ABORT 
	}

	void addSystemEntry(SystemEntryType type)
	{
		ByteBuffer header = ByteBuffer.allocate(EntryType.SYSTEM.HEADER_SIZE);

		header
			.put((byte)EntryType.SYSTEM.ordinal())
			.putDouble(Simulator.getTime())
			.put((byte)type.ordinal());

		allEntries.add(new Entry(header, null));
	}

  /*――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――/
 /                           RESOURCE STATE ENTRIES                          /
/――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――*/

	class ResourceTypeIndex
	{
		final int number;

		private ResourceTypeIndex(int number, JSONObject structure)
		{
			resources = new ArrayList<Index>();
			this.structure = structure;
			this.number = number;
		}

		final JSONObject structure;

		final ArrayList<Index> resources;
	}
	
	HashMap<String, ResourceTypeIndex> resourceIndex =
		new HashMap<String, ResourceTypeIndex>();

	public static enum ResourceEntryType
	{
		CREATED, ERASED, ALTERED, SEARCH, SOLUTION
	}

	public void addResourceEntry(ResourceEntryType status, Resource resource, String sender)
	{
		String typeName = resource.getTypeName();

		ResourceTypeIndex resourceTypeIndex =
			resourceIndex.get(typeName);

		Index resourceIndex;

		String name = resource.getName();
		if(name != null)
			if(!sensitivityList.contains(name))
				return;
		else
		{
			name = typeName + "_" + String.valueOf(resource.getNumber());
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

		switch (status)
		{
			case CREATED:
				resourceIndex = new Index(resource.getNumber());

				while(resourceTypeIndex.resources.size() < resource.getNumber() + 1)
					resourceTypeIndex.resources.add(null);

				resourceTypeIndex.resources.set(resource.getNumber(), resourceIndex);
			break;
			case ERASED:
			case ALTERED:
			case SOLUTION:
				resourceIndex = resourceTypeIndex.resources.get(resource.getNumber());
			break;
			case SEARCH:
			default:
				resourceIndex = null;
			break;
		}

		ByteBuffer header = ByteBuffer.allocate(EntryType.RESOURCE.HEADER_SIZE);
		header
			.putDouble(Simulator.getTime())
			.put((byte)EntryType.RESOURCE.ordinal())
			.put((byte)status.ordinal())
			.putInt(resourceTypeIndex.number)
			.putInt(resourceIndex.number);

		ByteBuffer data = resource.serialize();

		Entry entry = new Entry(header, data);

		allEntries.add(entry);

		if(resourceIndex != null)
			resourceIndex.entries.add(allEntries.size() - 1);
	}
	
  /*――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――/
 /                              PATTERN ENTRIES                              /
/――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――*/

	public static enum PatternType
	{
		EVENT, RULE, OPERATION_BEGIN, OPERATION_END
	}

	public static class DecisionPointIndex
	{
		final int number;

		private DecisionPointIndex(int number)
		{
			this.number = number;
		}

		HashMap<String, PatternIndex> activities = new HashMap<String, PatternIndex>();
	}

	HashMap<String, DecisionPointIndex> decisionPointIndex =
		new HashMap<String, DecisionPointIndex>();

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

	private HashMap<Pattern, PatternPoolEntry> patternPool
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
			.putDouble(Simulator.getTime())
			.put((byte)EntryType.PATTERN.ordinal())
			.put((byte)type.ordinal());

		DecisionPointIndex dptIndex = decisionPointIndex.get(dptName);
		PatternIndex index = dptIndex.activities.get(activity.getName());

		int[] relevantResources = pattern.getRelevantInfo();

		ByteBuffer data = ByteBuffer.allocate(TypeSize.INTEGER * (relevantResources.length +
			(type == PatternType.OPERATION_BEGIN ? 4 : 3)));
		data
			.putInt(dptIndex.number)
			.putInt(index.number);

		if(type == PatternType.OPERATION_BEGIN)
		{
			int number = index.timesExecuted++;

			patternPool.put(pattern, new PatternPoolEntry(dpt, activity, number));
			data.putInt(number);
		}

		fillRelevantResources(data, relevantResources);

		Entry entry = new Entry(header, data);

		allEntries.add(entry);
		index.entries.add(allEntries.size() - 1);
	}

	public static class PatternIndex extends Index
	{
		JSONObject structure;

		private PatternIndex(int number, JSONObject structure)
		{
			super(number);
			this.structure = structure;
		}

		private int timesExecuted = 0;
	}

	HashMap<String, PatternIndex> eventIndex = new HashMap<String, PatternIndex>();

	public void addEventEntry(PatternType type, Pattern pattern)
	{
		String name = pattern.getName();

		PatternIndex index;

		PatternPoolEntry poolEntry = null;
		DecisionPointIndex dptIndex = null;

		if(type == PatternType.OPERATION_END)
		{
			poolEntry = patternPool.remove(pattern);
			if(poolEntry == null)
				return;
			dptIndex = decisionPointIndex.get(poolEntry.dpt.getName());
			index = dptIndex.activities.get(poolEntry.activity.getName());
		}
		else
		{
			if(!sensitivityList.contains(name))
				return;
			index = eventIndex.get(pattern.getName());
		}

		ByteBuffer header = ByteBuffer.allocate(EntryType.PATTERN.HEADER_SIZE);
		header
			.putDouble(Simulator.getTime())
			.put((byte)EntryType.PATTERN.ordinal())
			.put((byte)type.ordinal());

		int[] relevantResources = pattern.getRelevantInfo();

		ByteBuffer data = ByteBuffer.allocate(TypeSize.INTEGER * (relevantResources.length +
			(type == PatternType.OPERATION_END ? 4 : 2)));

		if(type == PatternType.OPERATION_END)
			data
				.putInt(dptIndex.number)
				.putInt(index.number)
				.putInt(poolEntry.number);
		else
			data.putInt(index.number);

		fillRelevantResources(data, relevantResources);

		Entry entry = new Entry(header, data);

		allEntries.add(entry);
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

	public static class SearchInfo
	{
		private int begin = -1;
		private int end = -1;
		private int decision = -1;

		public int beginEntry()
		{
			return begin;
		}

		public int endEntry()
		{
			return end;
		}

		public int decisionStart()
		{
			return decision;
		}

		public int size()
		{
			return end - begin;
		}
	}

	public static class SearchIndex
	{		
		final int number;

		private SearchIndex(int number)
		{
			this.number = number;
		}

		ArrayList<SearchInfo> searches = new ArrayList<SearchInfo>();
	}

	HashMap<String, SearchIndex> searchIndex =
		new HashMap<String, SearchIndex>();

	public static enum SearchEntryType
	{
		BEGIN, END, OPEN, SPAWN, DECISION;
	}

	void addSearchEntry(DecisionPointSearch<?> dpt, SearchEntryType type, ByteBuffer data)
	{
		SearchIndex index = searchIndex.get(dpt.getName());
		SearchInfo info = null;

		ByteBuffer header = ByteBuffer.allocate(EntryType.SEARCH.HEADER_SIZE);
		header
			.put((byte)EntryType.SEARCH.ordinal())
			.put((byte)type.ordinal());

		switch(type)
		{
			case BEGIN:
				data = ByteBuffer.allocate(TypeSize.DOUBLE + TypeSize.INTEGER * 2);
				data
					.putDouble(Simulator.getTime())
					.putInt(index.number)
					.putInt(index.searches.size());

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

		allEntries.add(entry);
	}

  /*――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――/
 /                              RESULT ENTRIES                               /
/――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――*/

	HashMap<String, Index> resultIndex = new HashMap<String, Index>();

	public void addResultEntry(Result result)
	{
		String name = result.getName();
		if(!sensitivityList.contains(name))
			return;

		Index index = resultIndex.get(name);

		ByteBuffer data = result.serialize();
		if (index.entries.size() > 0)
		{
			ByteBuffer lastResultValue =
				allEntries.get(index.entries.get(index.entries.size() -1)).data;
			ByteBuffer currentResultValue = data.duplicate();
			currentResultValue.rewind();
			if (currentResultValue.compareTo(lastResultValue) == 0)
				return;
		}

		ByteBuffer header = ByteBuffer.allocate(EntryType.RESULT.HEADER_SIZE);
		header
			.putDouble(Simulator.getTime())
			.put((byte)EntryType.RESULT.ordinal())
			.putInt(index.number);

		Entry entry = new Entry(header, data);

		allEntries.add(entry);
		index.entries.add(allEntries.size() - 1);
	}
}
