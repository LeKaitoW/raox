package ru.bmstu.rk9.rdo.lib;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

public class Database
{
	Database()
	{
		addSystemEntry(SystemEntryType.TRACE_START);
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

		ArrayList<Entry> entries = new ArrayList<Entry>();

		private Index(int number)
		{
			this.number = number;
		}
	}

	public static enum EntryType
	{
		SYSTEM(10), RESOURCE(18), PATTERN(0), DECISION(0), RESULT(0);

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

	private int currentResourceTypeNumber = 0;

	class PermanentResourceTypeIndex
	{
		final int number;

		protected int currentResourceNumber = 1;

		private PermanentResourceTypeIndex(int number, ResourceStructure structure)
		{
			resources = new HashMap<String, Index>();
			this.structure = structure;
			this.number = number;
		}

		final ResourceStructure structure;
		final HashMap<String, Index> resources;
	}
	
	HashMap<String, PermanentResourceTypeIndex> permanentResourceIndex =
		new HashMap<String, PermanentResourceTypeIndex>();

	class TemporaryResourceTypeIndex extends PermanentResourceTypeIndex
	{
		ArrayList<Index> all   = new ArrayList<Index>();
		ArrayList<Index> alive = new ArrayList<Index>();

		private TemporaryResourceTypeIndex(int number, ResourceStructure structure)
		{
			super(number, structure);
		}
	}

	HashMap<String, TemporaryResourceTypeIndex> temporaryResourceIndex =
		new HashMap<String, TemporaryResourceTypeIndex>();	

	public void registerResourceType(String name, ResourceStructure structure, boolean temporary)
	{
		if(temporary)
			temporaryResourceIndex.put
			(
				name,
				new TemporaryResourceTypeIndex(currentResourceTypeNumber++, structure)
			);
		else
			permanentResourceIndex.put
			(
				name,
				new PermanentResourceTypeIndex(currentResourceTypeNumber++, structure)
			);
	}

	public void registerResource(PermanentResource resource)
	{
		PermanentResourceTypeIndex resourceTypeIndex = 
			permanentResourceIndex.get(resource.getTypeName());
		resourceTypeIndex.resources.put
		(
			resource.getName(),
			new Index(resourceTypeIndex.currentResourceNumber++)
		);
	}

	public void registerResource(TemporaryResource resource)
	{
		TemporaryResourceTypeIndex resourceTypeIndex = 
			temporaryResourceIndex.get(resource.getTypeName());
		resourceTypeIndex.resources.put
		(
			resource.getName(),
			new Index(resourceTypeIndex.currentResourceNumber++)
		);
	}

	public static enum ResourceEntryType
	{
		CREATED, ERASED, ALTERED
	}

	public void addResourceEntry(ResourceEntryType status, PermanentResource resource)
	{
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
		resourceIndex.entries.add(entry);
	}

	public void addResourceEntry(ResourceEntryType status, TemporaryResource resource)
	{
		TemporaryResourceTypeIndex resourceTypeIndex =
			temporaryResourceIndex.get(resource.getTypeName());
		Index resourceIndex = null;
		if(resource.getName() != null)
		{
			resourceIndex = resourceTypeIndex.resources.get(resource.getName());
		}
		else switch (status)
		{
			case CREATED:
				resourceIndex = new Index
				(
					resource.getNumber() + resourceTypeIndex.currentResourceNumber
				);
				resourceTypeIndex.all.add(resourceIndex);

				while(resourceTypeIndex.alive.size() < resource.getNumber() + 1)
					resourceTypeIndex.alive.add(null);

				resourceTypeIndex.alive.set(resource.getNumber(), resourceIndex);
			break;

			case ERASED:
				resourceIndex = resourceTypeIndex.alive.get(resource.getNumber()); 
				resourceTypeIndex.alive.set(resource.getNumber(), null);
			break;

			case ALTERED:
				resourceIndex = resourceTypeIndex.alive.get(resource.getNumber());
			break;
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
		resourceIndex.entries.add(entry);
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
		Index index = resultIndex.get(result.getName());

		ByteBuffer header = ByteBuffer.allocate(EntryType.RESULT.HEADER_SIZE);
		header
			.putDouble(Simulator.getTime())
			.putInt(index.number);

		ByteBuffer data = result.serialize();

		Entry entry = new Entry(header, data);

		allEntries.add(entry);
		index.entries.add(entry);
	}
}
