package ru.bmstu.rk9.rdo.lib;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import ru.bmstu.rk9.rdo.lib.json.*;

public class Database
{
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

	class Index
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
		SYSTEM(10), RESOURCE(18), PATTERN(0), DECISION(0), RESULT(13);

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



  /*――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――/
 /                          DECISION POINT ENTRIES                           /
/――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――*/



  /*――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――/
 /                              RESULT ENTRIES                               /
/――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――*/

	private int currentResultNumber = 0;

	HashMap<String, Index> resultIndex = new HashMap<String, Index>();

	public void registerResult(Result result)
	{
		resultIndex.put(result.getName(), new Index(currentResultNumber++));
	}

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
