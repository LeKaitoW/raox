package ru.bmstu.rk9.rdo.lib;

import java.nio.ByteBuffer;

import java.util.PriorityQueue;
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
			HashMap<String, Index> resources;

			String name = resourceType.getString("name");
			if(resourceType.getBoolean("temporary"))
			{
				TemporaryResourceTypeIndex index =
					new TemporaryResourceTypeIndex(i, resourceType.getJSONObject("structure"));
				resources = index.resources;
				temporaryResourceIndex.put(name, index);
			}
			else
			{
				PermanentResourceTypeIndex index
					= new PermanentResourceTypeIndex(i, resourceType.getJSONObject("structure"));
				resources = index.resources;
				permanentResourceIndex.put(name, index);
			}

			JSONArray modelResources = resourceType.getJSONArray("resources");
			for(int j = 0; j < modelResources.length(); j++)
			{
				resources.put(modelResources.getString(j), new Index(j));
			}
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
					new PatternIndex(j,	patternsByName.get(
						activity.getString("pattern")))
				);
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
		SYSTEM(10), RESOURCE(18), PATTERN(10), SEARCH(0), RESULT(13);

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

		header.putDouble(Simulator.getTime());
		header.put((byte)EntryType.SYSTEM.ordinal());
		header.put((byte)type.ordinal());

		allEntries.add(new Entry(header, null));
	}

  /*――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――/
 /                           RESOURCE STATE ENTRIES                          /
/――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――*/

	class PermanentResourceTypeIndex
	{
		final int number;

		private PermanentResourceTypeIndex(int number, JSONObject structure)
		{
			resources = new HashMap<String, Index>();
			this.structure = structure;
			this.number = number;
		}

		final JSONObject structure;
		final HashMap<String, Index> resources;
	}
	
	HashMap<String, PermanentResourceTypeIndex> permanentResourceIndex =
		new HashMap<String, PermanentResourceTypeIndex>();

	class TemporaryResourceTypeIndex extends PermanentResourceTypeIndex
	{
		ArrayList<Index> temporary = new ArrayList<Index>();

		private TemporaryResourceTypeIndex(int number, JSONObject structure)
		{
			super(number, structure);
		}
	}

	HashMap<String, TemporaryResourceTypeIndex> temporaryResourceIndex =
		new HashMap<String, TemporaryResourceTypeIndex>();	

	public static enum ResourceEntryType
	{
		CREATED, ERASED, ALTERED
	}

	public void addResourceEntry(ResourceEntryType status, PermanentResource resource, String sender)
	{
		String name = resource.getName();
		if(!sensitivityList.contains(name))
			return;

		PermanentResourceTypeIndex resourceTypeIndex =
			permanentResourceIndex.get(resource.getTypeName());
		Index resourceIndex =
			resourceTypeIndex.resources.get(resource.getName());

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
		resourceIndex.entries.add(allEntries.size() - 1);
	}

	public void addResourceEntry(ResourceEntryType status, TemporaryResource resource, String sender)
	{
		TemporaryResourceTypeIndex resourceTypeIndex =
			temporaryResourceIndex.get(resource.getTypeName());
		Index resourceIndex = null;

		String permanentName = resource.getName();
		if(permanentName != null)
		{
			if(!sensitivityList.contains(permanentName))
				return;
			resourceIndex = resourceTypeIndex.resources.get(resource.getName());
		}
		else
		{
			String typeName = resource.getTypeName();
			String temporaryName = typeName + "_" + String.valueOf(resource.getNumber());
			
			switch (status)
			{
				case CREATED:
					if(sensitivityList.contains(sender))
						sensitivityList.add(temporaryName);
					else
						return;

					resourceIndex = new Index
					(
						resource.getNumber() + resourceTypeIndex.resources.size()
					);

					while(resourceTypeIndex.temporary.size() < resource.getNumber() + 1)
						resourceTypeIndex.temporary.add(null);
	
					resourceTypeIndex.temporary.set(resource.getNumber(), resourceIndex);
				break;
	
				case ERASED:
					if(!sensitivityList.remove(temporaryName))
						return;
					resourceIndex = resourceTypeIndex.temporary.get(resource.getNumber()); 
				break;
	
				case ALTERED:
					if(!sensitivityList.contains(temporaryName))
						return;
					resourceIndex = resourceTypeIndex.temporary.get(resource.getNumber());
				break;
			}
		}

		ByteBuffer header = ByteBuffer.allocate(EntryType.RESOURCE.HEADER_SIZE);
		header
			.putDouble(Simulator.getTime())
			.put((byte)EntryType.RESOURCE.ordinal())
			.put((byte)status.ordinal())
			.putInt(resourceTypeIndex.number)
			.putInt(resourceIndex.number);

		ByteBuffer data = (status == ResourceEntryType.ERASED)? null : resource.serialize();

		Entry entry = new Entry(header, data);

		allEntries.add(entry);
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

	private PriorityQueue<Integer> vacantPoolNumbers
		= new PriorityQueue<Integer>();

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

		JSONArray relevantResources = pattern.getRelevantInfo();

		ByteBuffer data = ByteBuffer.allocate(TypeSize.INTEGER * (relevantResources.length() +
			(type == PatternType.OPERATION_BEGIN ? 4 : 3)));
		data
			.putInt(dptIndex.number)
			.putInt(index.number);

		if(type == PatternType.OPERATION_BEGIN)
		{
			int number;
			if(vacantPoolNumbers.isEmpty())
				number = patternPool.size();
			else
				number = vacantPoolNumbers.poll();

			patternPool.put(pattern, new PatternPoolEntry(dpt, activity, number));
			data.putInt(number);
		}

		fillRelevantResources(data, index.structure, relevantResources);

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
			vacantPoolNumbers.add(poolEntry.number);
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

		JSONArray relevantResources = pattern.getRelevantInfo();

		ByteBuffer data = ByteBuffer.allocate(TypeSize.INTEGER * (relevantResources.length() +
			(type == PatternType.OPERATION_END ? 4 : 2)));

		if(type == PatternType.OPERATION_END)
			data
				.putInt(dptIndex.number)
				.putInt(index.number)
				.putInt(poolEntry.number);
		else
			data.putInt(index.number);

		fillRelevantResources(data, index.structure, relevantResources);

		Entry entry = new Entry(header, data);

		allEntries.add(entry);
		index.entries.add(allEntries.size() - 1);
	}

	private void fillRelevantResources
	(
		ByteBuffer data,
		JSONObject structure,
		JSONArray relevantResources
	)
	{
		data.putInt(relevantResources.length());
		JSONArray resourceTypes = structure.getJSONArray("relevant_resources");
		for(int i = 0; i < relevantResources.length(); i++)
		{
			String typeName = resourceTypes.getJSONObject(i).getString("type");
			Object value = relevantResources.get(i);
			if(value instanceof Integer)
				data.putInt(temporaryResourceIndex.get(typeName)
					.resources.size() + (Integer)value);
			else
				if(permanentResourceIndex.containsKey(typeName))
					data.putInt(permanentResourceIndex.get(typeName)
							.resources.get((String)value).number);
				else
					data.putInt(temporaryResourceIndex.get(typeName)
							.resources.get((String)value).number);
		}
	}

  /*――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――/
 /                          DECISION POINT ENTRIES                           /
/――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――*/



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

		ByteBuffer header = ByteBuffer.allocate(EntryType.RESULT.HEADER_SIZE);
		header
			.putDouble(Simulator.getTime())
			.put((byte)EntryType.RESULT.ordinal())
			.putInt(index.number);

		ByteBuffer data = result.serialize();

		Entry entry = new Entry(header, data);

		allEntries.add(entry);
		index.entries.add(allEntries.size() - 1);
	}
}
