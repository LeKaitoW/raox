package ru.bmstu.rk9.rdo.lib;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

import ru.bmstu.rk9.rdo.lib.Database.TypeSize;
import ru.bmstu.rk9.rdo.lib.json.JSONArray;
import ru.bmstu.rk9.rdo.lib.json.JSONObject;

public final class Tracer implements Subscriber
{
	Tracer()
	{
		fillResourceTypeStructure();
		fillResultValueTypes();
	}

	//TODO Shouldn't it be moved to Database?
	public static enum ValueType
	{
		INTEGER("integer"),
		REAL("real"),
		BOOLEAN("boolean"),
		ENUM("enum"),
		STRING("string");

		private final String type;

		ValueType(String type)
		{
			this.type = type;
		}

		public static final ValueType get(final String type)
		{
			for (ValueType t : values())
			{
				if (t.type.equals(type))
					return t;
			}
			return null;
		}
	}

	public static enum TraceType
	{
		//TODO add DPT
		//TODO it duplicates Database enums in a way, what to do?
		RESOURCE_CREATE("RC"),
		RESOURCE_KEEP("RK"),
		RESOURCE_ERASE("RE"),
		SYSTEM("ES"),
		OPERATION_BEGIN("EB"),
		OPERATION_END("EF"),
		EVENT("EI"),
		RULE("ER"),
		RESULT("V");

		private String traceCode;
		private TraceType(String traceCode)
		{
			this.traceCode = traceCode;
		}

		@Override
		public String toString()
		{
			return traceCode;
		}
	}

	//TODO proper place of nested class inside a Tracer class?s
	public final static class TraceOutput
	{
		TraceOutput(TraceType type, String content)
		{
			this.type = type;
			this.content = content;
		}

		private TraceType type;
		private String content;

		public final TraceType type()
		{
			return type;
		}

		public final String content()
		{
			return content;
		}
	}

	static private final String delimiter = " ";

	private HashMap<Integer, ResourceTypeInfo> resourceTypesInfo =
		new HashMap<Integer, ResourceTypeInfo>();
	private HashMap<Integer, ValueType> resultValueTypes =
		new HashMap<Integer, ValueType>();

	private final void fillResourceTypeStructure()
	{
		final JSONArray jsonResourceTypes =
			Simulator
			.getDatabase()
			.getModelStructure()
			.getJSONArray("resource_types");

		for (int typeNum = 0; typeNum < jsonResourceTypes.length(); typeNum++)
		{
			resourceTypesInfo.put(
				typeNum,
				new ResourceTypeInfo(
					jsonResourceTypes
					.getJSONObject(typeNum)
					.getJSONObject("structure")
				)
			);
		}
	}

	private final void fillResultValueTypes()
	{
		final JSONArray results =
			Simulator
			.getDatabase()
			.getModelStructure()
			.getJSONArray("results");

		for (int resultNum = 0; resultNum < results.length(); resultNum++)
		{
			resultValueTypes.put(
				resultNum,
				ValueType.get(
					results.getJSONObject(resultNum).getString("value_type"))
			);
		}
	}

	//TODO choose the proper container for traceList
	//TODO besides string it should contain type identifier for future
	//coloring in UI
	private ArrayList<TraceOutput> traceList = new ArrayList<TraceOutput>();

	public final ArrayList<TraceOutput> getTraceList()
	{
		//TODO make unmodifiable
		return traceList;
	}

	public final void saveTraceData()
	{
		final ArrayList<Database.Entry> entries = Simulator.getDatabase().allEntries;

		for (Database.Entry entry : entries)
		{
			final TraceOutput traceOutput = parseSerializedData(entry);
			if (traceOutput != null)
				traceList.add(traceOutput);
		}
	}

	private final TraceOutput parseSerializedData(final Database.Entry entry)
	{
		final Database.EntryType type =
			Database.EntryType.values()[entry.header.get(
				TypeSize.Internal.ENTRY_TYPE_OFFSET)];
		switch(type)
		{
		//TODO implement the rest of EntryTypes
		case RESOURCE:
			return parseResourceEntry(entry);
		case RESULT:
			return parseResultEntry(entry);
		case PATTERN:
			return parsePatternEntry(entry);
		default:
			return null;
		}
	}

  /*――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――/
 /                          PARSING RESOURCE ENTRIES                         /
/――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――*/

	private final TraceOutput parseResourceEntry(final Database.Entry entry)
	{
		final ByteBuffer resourceHeader = entry.header;

		prepareBufferForReading(resourceHeader);

		final double time = resourceHeader.getDouble();
		skipEntryType(resourceHeader);
		final TraceType traceType;
		switch(resourceHeader.get())
		{
		case 0:
			traceType = TraceType.RESOURCE_CREATE;
			break;
		case 1:
			traceType = TraceType.RESOURCE_ERASE;
			break;
		case 2:
			traceType = TraceType.RESOURCE_KEEP;
			break;
		default:
			return null;
		}
		final int typeNum = resourceHeader.getInt();
		final int resNum = resourceHeader.getInt();

		final String headerLine =
			new StringJoin(delimiter)
			.add(traceType.toString())
			.add(String.valueOf(time))
			.add(String.valueOf(typeNum))
			.add(String.valueOf(resNum))
			.getString();

		//TODO fix when resource parameters are also serialized on erase
		if (traceType == TraceType.RESOURCE_ERASE)
		{
			return new TraceOutput(traceType, headerLine);
		}

		final ResourceTypeInfo typeInfo = resourceTypesInfo.get(typeNum);

		return
			new TraceOutput(
				traceType,
				new StringJoin(delimiter)
					.add(headerLine)
					.add(parseResourceParameters(entry.data, typeInfo))
					.getString()
			);
	}

	private final String parseResourceParameters(
		final ByteBuffer resourceData,
		final ResourceTypeInfo typeInfo
	)
	{
		final StringJoin stringBuilder = new StringJoin(delimiter);

		prepareBufferForReading(resourceData);

		for (int paramNum = 0; paramNum < typeInfo.numberOfParameters; paramNum++)
		{
			//TODO trace arrays when they are implemented
			switch(typeInfo.paramTypes.get(paramNum))
			{
			case INTEGER:
				stringBuilder.add(String.valueOf(resourceData.getInt()));
				break;
			case REAL:
				stringBuilder.add(String.valueOf(resourceData.getDouble()));
				break;
			case BOOLEAN:
				stringBuilder.add(String.valueOf(new Byte(resourceData.get())));
				break;
			case ENUM:
				stringBuilder.add(String.valueOf(resourceData.getShort()));
				break;
			case STRING:
				//TODO macro-like variable sizeofInt should be
				//moved somewhere on upper level or discarded
				final int index = typeInfo.indexList.get(paramNum);
				final int stringPosition =
					resourceData.getInt(typeInfo.finalOffset + (index - 1) * TypeSize.RDO.INTEGER);
				final int length = resourceData.getInt(stringPosition);

				byte rawString[] = new byte[length];
				for (int i = 0; i < length; i++)
				{
					rawString[i] = resourceData.get(stringPosition + TypeSize.RDO.INTEGER + i);
				}
				stringBuilder.add("\"" + new String(rawString, StandardCharsets.UTF_8) + "\"");
				break;
			default:
				return null;
			}
		}
		return stringBuilder.getString();
	}

  /*――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――/
 /                          PARSING PATTERN ENTRIES                          /
/――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――*/

	private final TraceOutput parsePatternEntry(final Database.Entry entry)
	{
		final ByteBuffer patternHeader = entry.header;

		prepareBufferForReading(patternHeader);

		final double time = patternHeader.getDouble();
		skipEntryType(patternHeader);
		final TraceType traceType;

		//TODO trace system events when implemented
		switch(patternHeader.get())
		{
		case 0:
			traceType = TraceType.EVENT;
			break;
		case 1:
			traceType = TraceType.RULE;
			break;
		case 2:
			traceType = TraceType.OPERATION_BEGIN;
			break;
		case 3:
			traceType = TraceType.OPERATION_END;
			break;
		default:
			return null;
		}

		return
			new TraceOutput(
				traceType,
				new StringJoin(delimiter)
					.add(traceType.toString())
					.add(String.valueOf(time))
					.add(parsePatternData(entry.data, traceType))
					.getString()
			);
	}

	private final String parsePatternData(
		final ByteBuffer patternData,
		final TraceType patternType
	)
	{
		final StringJoin stringBuilder = new StringJoin(delimiter);

		prepareBufferForReading(patternData);

		int dptNumber;
		int patternNumber;
		int actionNumber;

		switch(patternType)
		{
		case EVENT:
			patternNumber = patternData.getInt();
			stringBuilder
				.add(String.valueOf(patternNumber));
			break;
		case RULE:
			dptNumber = patternData.getInt();
			patternNumber = patternData.getInt();
			stringBuilder
				.add(String.valueOf(dptNumber))
				.add(String.valueOf(patternNumber));
			break;
		case OPERATION_BEGIN:
		case OPERATION_END:
			dptNumber = patternData.getInt();
			patternNumber = patternData.getInt();
			actionNumber = patternData.getInt();
			stringBuilder
				.add(String.valueOf(actionNumber))
				.add(String.valueOf(dptNumber))
				.add(String.valueOf(patternNumber));
			break;
		default:
			return null;
		}

		int numberOfRelevantResources = patternData.getInt();
		stringBuilder.add(String.valueOf(numberOfRelevantResources));
		for(int i = 0; i < numberOfRelevantResources; i++)
		{
			stringBuilder.add(String.valueOf(patternData.getInt()));
		}

		return stringBuilder.getString();
	}

  /*――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――/
 /                           PARSING RESULT ENTRIES                          /
/――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――*/

	private final TraceOutput parseResultEntry(final Database.Entry entry)
	{
		final ByteBuffer resultHeader = entry.header;

		prepareBufferForReading(resultHeader);

		final double time = resultHeader.getDouble();
		skipEntryType(resultHeader);
		final int resultNum = resultHeader.getInt();

		final ValueType valueType = resultValueTypes.get(resultNum);

		return
			new TraceOutput(
			TraceType.RULE,
			new StringJoin(delimiter)
				.add(TraceType.RULE.toString())
				.add(String.valueOf(time))
				.add(String.valueOf(resultNum))
				.add(parseResultParameter(entry.data, valueType))
				.getString()
			);
	}

	private final String parseResultParameter(
		final ByteBuffer resultData,
		final ValueType valueType
	)
	{
		prepareBufferForReading(resultData);

		switch(valueType)
		{
		case INTEGER:
			return String.valueOf(resultData.getInt());
		case REAL:
			return String.valueOf(resultData.getDouble());
		case BOOLEAN:
			return String.valueOf(new Byte(resultData.get()));
		case ENUM:
			return String.valueOf(resultData.getShort());
		case STRING:
			final ByteArrayOutputStream rawString = new ByteArrayOutputStream();
			while (resultData.hasRemaining())
			{
				rawString.write(resultData.get());
			}
			return "\"" + rawString.toString() + "\"";
		default:
			break;
		}

		return null;
	}

  /*――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――/
 /                               HELPER METHODS                              /
/――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――*/

	private final void skipEntryType(final ByteBuffer buffer)
	{
		buffer.get();
	}

	private final void prepareBufferForReading(final ByteBuffer buffer)
	{
		buffer.duplicate();
		buffer.rewind();
	}

	@Override
	public void fireChange() {}
}

//TODO make private and move inside of Tracer?
class ResourceTypeInfo
{
	ResourceTypeInfo(final JSONObject structure)
	{
		JSONArray parameters = structure.getJSONArray("parameters");
		numberOfParameters = parameters.length();
		for (int paramNum = 0; paramNum < numberOfParameters; paramNum++)
		{
			final JSONObject currentParameter = parameters.getJSONObject(paramNum);
			Tracer.ValueType type = Tracer.ValueType.get(currentParameter.getString("type"));
			paramTypes.put(paramNum, type);
			if (type == Tracer.ValueType.STRING)
			{
				indexList.put(paramNum, currentParameter.getInt("index"));
			}
		}
		finalOffset = structure.getInt("last_offset");
	}

	public HashMap<Integer, Tracer.ValueType> paramTypes =
		new HashMap<Integer, Tracer.ValueType>();
	public HashMap<Integer, Integer> indexList = new HashMap<Integer, Integer>();
	public final int finalOffset;
	public final int numberOfParameters;
}

//TODO use standard class when switched to Java8
class StringJoin
{
	private final String delimiter;

	private String current = null;

	public final String getString()
	{
		return current;
	}

	StringJoin(String delimiter)
	{
		this.delimiter = delimiter;
	}

	public final StringJoin add(final String toAppend)
	{
		if (current == null)
		{
			current = new String(toAppend);
		}
		else
		{
			current += delimiter + toAppend;
		}
		return this;
	}
}
