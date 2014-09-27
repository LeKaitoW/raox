package ru.bmstu.rk9.rdo.lib;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

public class Tracer
{
	Tracer()
	{
		addSystemEntry(SystemTraceType.TRACE_START);
	}

	public static enum TraceType
	{
		SYSTEM(10), RESOURCE(18), PATTERN(0), DECISION(0), RESULT(0);

		public final int HEADER_SIZE;

		private TraceType(int HEADER_SIZE)
		{
			this.HEADER_SIZE = HEADER_SIZE;
		}
	}

	ArrayList<ByteBuffer> trace = new ArrayList<ByteBuffer>();

  /*――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――/
 /                            SYSTEM EVENT TRACER                            /
/――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――*/

	static enum SystemTraceType
	{
		TRACE_START, TRACE_END, SIM_START, SIM_FINISH, SIM_ABORT 
	}

	void addSystemEntry(SystemTraceType type)
	{
		ByteBuffer entry = ByteBuffer.allocateDirect(TraceType.SYSTEM.HEADER_SIZE);

		entry.putDouble(Simulator.getTime());
		entry.put((byte)TraceType.SYSTEM.ordinal());
		entry.put((byte)type.ordinal());

		trace.add(entry);
	}

  /*――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――/
 /                           RESOURCE STATE TRACER                           /
/――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――*/

	private int currentResourceTypeNumber = 0;

	private HashMap<PermanentResource, Boolean> resourceTraceStateList =
		new HashMap<PermanentResource, Boolean>();

	public void setTraceState(PermanentResource resource, boolean traceValue)
	{
		this.resourceTraceStateList.put(resource, traceValue);
	}

	public boolean isBeingTraced(PermanentResource resource)
	{
		return this.resourceTraceStateList.get(resource);
	}

	class ResourceEvents
	{
		final int number;

		ArrayList<ByteBuffer> events = new ArrayList<ByteBuffer>();

		private ResourceEvents(int number)
		{
			this.number = number;
		}
	}

	class PermanentResourceTypeEvents
	{
		final int number;

		protected int currentResourceNumber = 1;

		private PermanentResourceTypeEvents(int number, ResourceStructure structure)
		{
			resources = new HashMap<String, ResourceEvents>();
			this.structure = structure;
			this.number = number;
		}

		final ResourceStructure structure;
		final HashMap<String, ResourceEvents> resources;
	}
	
	HashMap<String, PermanentResourceTypeEvents> permanentResourceEvents =
		new HashMap<String, PermanentResourceTypeEvents>();

	class TemporaryResourceTypeEvents extends PermanentResourceTypeEvents
	{
		ArrayList<ResourceEvents> all   = new ArrayList<ResourceEvents>();
		ArrayList<ResourceEvents> alive = new ArrayList<ResourceEvents>();

		private TemporaryResourceTypeEvents(int number, ResourceStructure structure)
		{
			super(number, structure);
		}
	}

	HashMap<String, TemporaryResourceTypeEvents> temporaryResourceEvents =
		new HashMap<String, TemporaryResourceTypeEvents>();	

	public void registerResourceType(String name, ResourceStructure structure, boolean temporary)
	{
		if(temporary)
			temporaryResourceEvents.put
			(
				name,
				new TemporaryResourceTypeEvents(currentResourceTypeNumber++, structure)
			);
		else
			permanentResourceEvents.put
			(
				name,
				new PermanentResourceTypeEvents(currentResourceTypeNumber++, structure)
			);
	}

	public void registerResource(PermanentResource resource)
	{
		PermanentResourceTypeEvents resourceTypeEvents = 
			permanentResourceEvents.get(resource.getTypeName());
		resourceTypeEvents.resources.put
		(
			resource.getName(),
			new ResourceEvents(resourceTypeEvents.currentResourceNumber++)
		);
		this.setTraceState(resource, false);
	}

	public void registerResource(TemporaryResource resource)
	{
		TemporaryResourceTypeEvents resourceTypeEvents = 
			temporaryResourceEvents.get(resource.getTypeName());
		resourceTypeEvents.resources.put
		(
			resource.getName(),
			new ResourceEvents(resourceTypeEvents.currentResourceNumber++)
		);
		this.setTraceState(resource, false);
	}

	public static enum ResourceTraceType
	{
		CREATED, ERASED, ALTERED
	}

	public void addResourceEntry(ResourceTraceType status, PermanentResource resource)
	{
		ByteBuffer entry = resource.createTracerEntry(TraceType.RESOURCE.HEADER_SIZE);
		entry.rewind();

		PermanentResourceTypeEvents resourceTypeEvents =
			permanentResourceEvents.get(resource.getTypeName());
		ResourceEvents resourceEvents =
			resourceTypeEvents.resources.get(resource.getName());

		entry
			.putDouble(Simulator.getTime())
			.put((byte)TraceType.RESOURCE.ordinal())
			.put((byte)status.ordinal())
			.putInt(resourceTypeEvents.number)
			.putInt(resourceEvents.number);

		trace.add(entry);
		resourceEvents.events.add(entry);
	}

	public void addResourceEntry(ResourceTraceType status, TemporaryResource resource)
	{
		ByteBuffer entry;
		if(status == ResourceTraceType.ERASED)
			entry = ByteBuffer.allocateDirect(TraceType.RESOURCE.HEADER_SIZE);
		else
		{
			entry = resource.createTracerEntry(TraceType.RESOURCE.HEADER_SIZE);
			entry.rewind();
		}

		TemporaryResourceTypeEvents resourceTypeEvents =
			temporaryResourceEvents.get(resource.getTypeName());
		ResourceEvents resourceEvents = null;
		if(resource.getName() != null)
		{
			resourceEvents = resourceTypeEvents.resources.get(resource.getName());
		}
		else switch (status)
		{
			case CREATED:
				resourceEvents = new ResourceEvents
				(
					resource.getNumber() + resourceTypeEvents.currentResourceNumber
				);
				resourceTypeEvents.all.add(resourceEvents);

				while(resourceTypeEvents.alive.size() < resource.getNumber() + 1)
					resourceTypeEvents.alive.add(null);

				resourceTypeEvents.alive.set(resource.getNumber(), resourceEvents);
			break;

			case ERASED:
				resourceEvents = resourceTypeEvents.alive.get(resource.getNumber()); 
				resourceTypeEvents.alive.set(resource.getNumber(), null);
			break;

			case ALTERED:
				resourceEvents = resourceTypeEvents.alive.get(resource.getNumber());
			break;
		}

		entry
			.putDouble(Simulator.getTime())
			.put((byte)TraceType.RESOURCE.ordinal())
			.put((byte)status.ordinal())
			.putInt(resourceTypeEvents.number)
			.putInt(resourceEvents.number);

		trace.add(entry);
		resourceEvents.events.add(entry);
	}

  /*――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――/
 /                              PATTERN TRACER                               /
/――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――*/



  /*――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――/
 /                           DECISION POINT TRACER                           /
/――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――*/



  /*――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――/
 /                              RESULT TRACER                                /
/――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――*/



}
