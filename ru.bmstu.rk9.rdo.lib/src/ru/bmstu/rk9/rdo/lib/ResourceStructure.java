package ru.bmstu.rk9.rdo.lib;

import java.util.Collections;

import java.util.List;
import java.util.ArrayList;

import java.util.HashMap;

public class ResourceStructure
{
	public ResourceStructure
	(
		ArrayList<Parameter> parameters,
		int chunkstart,
		HashMap<String, ChunkParameter> chunks,
		HashMap<String, Enum<?>[]> enuminfo
	)
	{
		this.parameters = parameters;
		this.chunkstart = chunkstart;
		this.chunks = chunks;
		this.enuminfo = enuminfo;
	}

	public static enum DataType
	{
		INTEGER, REAL, BOOL, ENUM, STRING, ARRAY
	}

	public static class Parameter
	{
		public final String name;
		public final DataType type;
		public final int offset;

		public Parameter(String name, DataType type, int offset)
		{
			this.name = name;
			this.type = type;
			this.offset = offset;
		}
	}

	public static class ChunkParameter extends Parameter
	{
		public final int dimension;

		public final DataType inner;

		public ChunkParameter(String name, DataType type, int position, int dimension)
		{
			super(name, dimension > 0 ? DataType.ARRAY : type, position);
			this.dimension = dimension;
			this.inner = dimension > 0 ? type : null;
		}
	}

	private ArrayList<Parameter> parameters;

	public final int chunkstart; // i.e. total length of parameters with known size

	private HashMap<String, ChunkParameter> chunks;

	private HashMap<String, Enum<?>[]> enuminfo;

	public List<Parameter> getAllParameters()
	{
		return Collections.unmodifiableList(parameters);
	}

	public ChunkParameter getChunkInfo(Parameter parameter)
	{
		return chunks.get(parameter.name);
	}

	public Enum<?>[] getEnumStructure(Parameter parameter)
	{
		return enuminfo.get(parameter.name);
	}
}
