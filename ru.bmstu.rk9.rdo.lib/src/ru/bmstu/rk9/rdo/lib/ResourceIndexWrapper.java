package ru.bmstu.rk9.rdo.lib;

import java.nio.ByteBuffer;

import java.util.ArrayList;

public abstract class ResourceIndexWrapper<T>
{
	private ResourceIndexWrapper(ArrayList<ByteBuffer> index)
	{
		this.index = index;
	}

	public static ResourceIndexWrapper<?> createWrapper(String type, String resource, String parameter)
	{
		return createWrapper(type, resource, parameter, Simulator.getDatabase());
	}

	public static ResourceIndexWrapper<?> createWrapper(String type, String resource, String parameter, Database database)
	{
		Database.PermanentResourceTypeIndex typeIndex = database.permanentResourceIndex.get(type);
		ResourceStructure structure = typeIndex == null ? null : typeIndex.structure;
		Database.ResourceIndex index = typeIndex == null ? null : typeIndex.resources.get(resource);

		if(index == null)
		{
			typeIndex = database.temporaryResourceIndex.get(type);
			structure = typeIndex == null ? null : typeIndex.structure;
			index = typeIndex == null ? null : typeIndex.resources.get(resource);
		}

		if(index == null)
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
				final int offset = rsparameter.offset + Database.EntryType.RESOURCE.HEADER_SIZE;
				return 
					new ResourceIndexWrapper<Boolean>(index.entries)
					{
						public Boolean getValue(int number)
						{
							byte val = this.index.get(number).get(offset);
							return val == (byte)1 ? true : false;
						}
					};
			}
			case INTEGER:
			{
				final int offset = rsparameter.offset + Database.EntryType.RESOURCE.HEADER_SIZE;
				return 
					new ResourceIndexWrapper<Integer>(index.entries)
					{
						public Integer getValue(int number)
						{
							return this.index.get(number).getInt(offset);
						}
					};
			}
			case REAL:
			{
				final int offset = rsparameter.offset + Database.EntryType.RESOURCE.HEADER_SIZE;
				return 
					new ResourceIndexWrapper<Double>(index.entries)
					{
						public Double getValue(int number)
						{
							return this.index.get(number).getDouble(offset);
						}
					};
			}
			case ENUM:
			{
				final int offset = rsparameter.offset + Database.EntryType.RESOURCE.HEADER_SIZE;
				final Enum<?>[] enums = structure.getEnumStructure(rsparameter);
				return 
					new ResourceIndexWrapper<Enum<?>>(index.entries)
					{
						public Enum<?> getValue(int number)
						{
							return enums[this.index.get(number).getShort(offset)];
						}
					};
			}
			case STRING:
			{
				final int chunkstart = structure.chunkstart;
				final int chunkpos = structure.getChunkInfo(rsparameter).offset;
				return 
					new ResourceIndexWrapper<String>(index.entries)
					{
						public String getValue(int number)
						{
							ByteBuffer buffer = index.get(number);
							int position = buffer.getInt(chunkstart + 4 * chunkpos);
							int size = buffer.getInt(position);
							byte[] source = new byte[size];
							index.get(number).get(source, position + 4, size);
							return new String(source);
						}
					};
			}
			default:
				return null;
		}
	}

	ArrayList<ByteBuffer> index;

	public int getIndexSize()
	{
		return index.size();
	}

	public double getTime(int number)
	{
		return index.get(number).getDouble(0);
	}

	public abstract T getValue(int number);
}
