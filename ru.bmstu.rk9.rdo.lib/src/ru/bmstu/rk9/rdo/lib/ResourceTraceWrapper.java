package ru.bmstu.rk9.rdo.lib;

import java.nio.ByteBuffer;

import java.util.ArrayList;

public abstract class ResourceTraceWrapper<T>
{
	private ResourceTraceWrapper(ArrayList<ByteBuffer> events)
	{
		this.events = events;
	}

	public static ResourceTraceWrapper<?> createWrapper(String type, String resource, String parameter)
	{
		return createWrapper(type, resource, parameter, Simulator.getTracer());
	}

	public static ResourceTraceWrapper<?> createWrapper(String type, String resource, String parameter, Tracer tracer)
	{
		Tracer.PermanentResourceTypeEvents typeEvents = tracer.permanentResourceEvents.get(type);
		ResourceStructure structure = typeEvents == null ? null : typeEvents.structure;
		Tracer.ResourceEvents events = typeEvents == null ? null : typeEvents.resources.get(resource);

		if(events == null)
		{
			typeEvents = tracer.temporaryResourceEvents.get(type);
			structure = typeEvents == null ? null : typeEvents.structure;
			events = typeEvents == null ? null : typeEvents.resources.get(resource);
		}

		if(events == null)
			return null;

		ResourceStructure.Parameter rsparameter = null;
		for(ResourceStructure.Parameter p : structure.getAllParameters())
			if(p.name == parameter)
				rsparameter = p;

		if(rsparameter == null)
			return null;

		switch (rsparameter.type)
		{
			case BOOL:
			{
				final int offset = rsparameter.offset + Tracer.TraceType.RESOURCE.HEADER_SIZE;
				return 
					new ResourceTraceWrapper<Boolean>(events.events)
					{
						public Boolean getValue(int number)
						{
							byte val = this.events.get(number).get(offset);
							return val == (byte)1 ? true : false;
						}
					};
			}
			case INTEGER:
			{
				final int offset = rsparameter.offset + Tracer.TraceType.RESOURCE.HEADER_SIZE;
				return 
					new ResourceTraceWrapper<Integer>(events.events)
					{
						public Integer getValue(int number)
						{
							return this.events.get(number).getInt(offset);
						}
					};
			}
			case REAL:
			{
				final int offset = rsparameter.offset + Tracer.TraceType.RESOURCE.HEADER_SIZE;
				return 
					new ResourceTraceWrapper<Double>(events.events)
					{
						public Double getValue(int number)
						{
							return this.events.get(number).getDouble(offset);
						}
					};
			}
			case ENUM:
			{
				final int offset = rsparameter.offset + Tracer.TraceType.RESOURCE.HEADER_SIZE;
				final Enum<?>[] enums = structure.getEnumStructure(rsparameter);
				return 
					new ResourceTraceWrapper<Enum<?>>(events.events)
					{
						public Enum<?> getValue(int number)
						{
							return enums[this.events.get(number).getShort(offset)];
						}
					};
			}
			case STRING:
			{
				final int chunkstart = structure.chunkstart;
				final int chunkpos = structure.getChunkInfo(rsparameter).offset;
				return 
					new ResourceTraceWrapper<String>(events.events)
					{
						public String getValue(int number)
						{
							ByteBuffer buffer = events.get(number);
							int position = buffer.getInt(chunkstart + 4 * chunkpos);
							int size = buffer.getInt(position);
							byte[] source = new byte[size];
							events.get(number).get(source, position + 4, size);
							return new String(source);
						}
					};
			}
			default:
				return null;
		}
	}

	ArrayList<ByteBuffer> events;

	public int getTraceSize()
	{
		return events.size();
	}

	public double getTime(int number)
	{
		return events.get(number).getDouble(0);
	}

	public abstract T getValue(int number);
}
