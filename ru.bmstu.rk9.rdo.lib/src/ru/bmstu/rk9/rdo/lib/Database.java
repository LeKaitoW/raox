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

	public static enum EntryType
	{
		SYSTEM(10), RESOURCE(18), PATTERN(0), DECISION(0), RESULT(0);

		public final int HEADER_SIZE;

		private EntryType(int HEADER_SIZE)
		{
			this.HEADER_SIZE = HEADER_SIZE;
		}
	}

	ArrayList<ByteBuffer> allEntries = new ArrayList<ByteBuffer>();

  /*――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――/
 /                              SYSTEM ENTRIES                               /
/――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――*/

	static enum SystemEntryType
	{
		TRACE_START, TRACE_END, SIM_START, SIM_FINISH, SIM_ABORT 
	}

	void addSystemEntry(SystemEntryType type)
	{
		ByteBuffer entry = ByteBuffer.allocateDirect(EntryType.SYSTEM.HEADER_SIZE);

		entry.putDouble(Simulator.getTime());
		entry.put((byte)EntryType.SYSTEM.ordinal());
		entry.put((byte)type.ordinal());

		allEntries.add(entry);
	}

  /*――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――/
 /                           RESOURCE STATE ENTRIES                          /
/――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――*/

	private int currentResourceTypeNumber = 0;

	class ResourceIndex
	{
		final int number;

		ArrayList<ByteBuffer> entries = new ArrayList<ByteBuffer>();

		private ResourceIndex(int number)
		{
			this.number = number;
		}
	}

	class PermanentResourceTypeIndex
	{
		final int number;

		protected int currentResourceNumber = 1;

		private PermanentResourceTypeIndex(int number, ResourceStructure structure)
		{
			resources = new HashMap<String, ResourceIndex>();
			this.structure = structure;
			this.number = number;
		}

		final ResourceStructure structure;
		final HashMap<String, ResourceIndex> resources;
	}
	
	HashMap<String, PermanentResourceTypeIndex> permanentResourceIndex =
		new HashMap<String, PermanentResourceTypeIndex>();

	class TemporaryResourceTypeIndex extends PermanentResourceTypeIndex
	{
		ArrayList<ResourceIndex> all   = new ArrayList<ResourceIndex>();
		ArrayList<ResourceIndex> alive = new ArrayList<ResourceIndex>();

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
			new ResourceIndex(resourceTypeIndex.currentResourceNumber++)
		);
	}

	public void registerResource(TemporaryResource resource)
	{
		TemporaryResourceTypeIndex resourceTypeIndex = 
			temporaryResourceIndex.get(resource.getTypeName());
		resourceTypeIndex.resources.put
		(
			resource.getName(),
			new ResourceIndex(resourceTypeIndex.currentResourceNumber++)
		);
	}

	public static enum ResourceEntryType
	{
		CREATED, ERASED, ALTERED
	}

	public void addResourceEntry(ResourceEntryType status, PermanentResource resource)
	{
		ByteBuffer entry = resource.serialize(EntryType.RESOURCE.HEADER_SIZE);
		entry.rewind();

		PermanentResourceTypeIndex resourceTypeIndex =
			permanentResourceIndex.get(resource.getTypeName());
		ResourceIndex resourceIndex =
			resourceTypeIndex.resources.get(resource.getName());

		entry
			.putDouble(Simulator.getTime())
			.put((byte)EntryType.RESOURCE.ordinal())
			.put((byte)status.ordinal())
			.putInt(resourceTypeIndex.number)
			.putInt(resourceIndex.number);

		allEntries.add(entry);
		resourceIndex.entries.add(entry);
	}

	public void addResourceEntry(ResourceEntryType status, TemporaryResource resource)
	{
		ByteBuffer entry;
		if(status == ResourceEntryType.ERASED)
			entry = ByteBuffer.allocateDirect(EntryType.RESOURCE.HEADER_SIZE);
		else
		{
			entry = resource.serialize(EntryType.RESOURCE.HEADER_SIZE);
			entry.rewind();
		}

		TemporaryResourceTypeIndex resourceTypeIndex =
			temporaryResourceIndex.get(resource.getTypeName());
		ResourceIndex resourceIndex = null;
		if(resource.getName() != null)
		{
			resourceIndex = resourceTypeIndex.resources.get(resource.getName());
		}
		else switch (status)
		{
			case CREATED:
				resourceIndex = new ResourceIndex
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

		entry
			.putDouble(Simulator.getTime())
			.put((byte)EntryType.RESOURCE.ordinal())
			.put((byte)status.ordinal())
			.putInt(resourceTypeIndex.number)
			.putInt(resourceIndex.number);

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



}
