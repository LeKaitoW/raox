package ru.bmstu.rk9.rdo.lib;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

import ru.bmstu.rk9.rdo.lib.Database.TypeSize;
public class Tracer implements Subscriber
{
	public static enum TraceType
	{
		RESOURCE_CREATE("RC"),
		RESOURCE_KEEP("RK"),
		RESOURCE_ERASE("RE"),
		SYSTEM("ES"),
		OPERATION_BEGIN("EB"),
		OPERATION_END("EF"),
		EVENT("EI"),
		RULE("ER"),
		RESULT("V "),
		SEARCH_BEGIN("SB"),
		SEARCH_OPEN("SO"),
		SEARCH_SPAWN_NEW("STN"),
		SEARCH_SPAWN_WORSE("STD"),
		SEARCH_SPAWN_BETTER("STR"),
		SEARCH_RESOURCE_CREATE("SRC"),
		SEARCH_RESOURCE_KEEP("SRK"),
		SEARCH_RESOURCE_ERASE("SRE"),
		SEARCH_DECISION("SD"),
		SEARCH_END_ABORTED("SEA"),
		SEARCH_END_CONDITION("SEC"),
		SEARCH_END_SUCCESS("SES"),
		SEARCH_END_FAIL("SEN");

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

	Tracer()
	{
		ModelStructureHelper.fillResourceNames(resourceNames);
		ModelStructureHelper.fillResourceTypesInfo(resourceTypesInfo);
		ModelStructureHelper.fillResultsInfo(resultsInfo);
		ModelStructureHelper.fillPatternsInfo(patternsInfo);
		ModelStructureHelper.fillDecisionPointsInfo(decisionPointsInfo);
	}

	static private final String delimiter = " ";

	protected final HashMap<Integer, HashMap<Integer, String>> resourceNames =
		new HashMap<Integer, HashMap<Integer, String>>();
	protected final ArrayList<ResourceTypeInfo> resourceTypesInfo =
		new ArrayList<ResourceTypeInfo>();
	protected final ArrayList<ResultInfo> resultsInfo =
		new ArrayList<ResultInfo>();
	protected final ArrayList<PatternInfo> patternsInfo =
		new ArrayList<PatternInfo>();
	protected final ArrayList<DecisionPointInfo> decisionPointsInfo =
		new ArrayList<DecisionPointInfo>();

	//TODO choose the proper container for traceList
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
		case SYSTEM:
			return parseSystemEntry(entry);
		case RESOURCE:
			return parseResourceEntry(entry);
		case PATTERN:
			return parsePatternEntry(entry);
		case SEARCH:
			return parseSearchEntry(entry);
		case RESULT:
			return parseResultEntry(entry);
		default:
			return null;
		}
	}

  /*――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――/
 /                          PARSING SYSTEM ENTRIES                           /
/――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――*/

	protected TraceOutput parseSystemEntry(final Database.Entry entry)
	{
		final ByteBuffer header = prepareBufferForReading(entry.header);

		final TraceType traceType = TraceType.SYSTEM;

		skipPart(header, TypeSize.BYTE);
		final double time = header.getDouble();
		final Database.SystemEntryType type =
			Database.SystemEntryType.values()[header.get()];

		final String headerLine =
			new StringBuilder(delimiter)
			.add(traceType.toString())
			.add(time)
			.add(type.ordinal())
			.getString();

		return new TraceOutput(traceType, headerLine);
	}

  /*――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――/
 /                          PARSING RESOURCE ENTRIES                         /
/――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――*/

	protected TraceOutput parseResourceEntry(final Database.Entry entry)
	{
		final ByteBuffer header = prepareBufferForReading(entry.header);
		final ByteBuffer data = prepareBufferForReading(entry.data);

		skipPart(header, TypeSize.BYTE);
		final double time = header.getDouble();
		final TraceType traceType;

		final Database.ResourceEntryType entryType =
			Database.ResourceEntryType.values()[header.get()];
		switch(entryType)
		{
		case CREATED:
			traceType = TraceType.RESOURCE_CREATE;
			break;
		case ERASED:
			traceType = TraceType.RESOURCE_ERASE;
			break;
		case ALTERED:
			traceType = TraceType.RESOURCE_KEEP;
			break;
		default:
			return null;
		}

		final int typeNum = header.getInt();
		final ResourceTypeInfo typeInfo =
			resourceTypesInfo.get(typeNum);
		final int resNum = header.getInt();
		final String name = resourceNames.get(typeNum).get(resNum);

		final String resourceName =
			name != null ?
				name :
				typeInfo.name + encloseIndex(resNum);

		final String headerLine =
			new StringBuilder(delimiter)
				.add(traceType.toString())
				.add(time)
				.add(resourceName)
				.getString();

		return
			new TraceOutput(
				traceType,
				new StringBuilder(delimiter)
					.add(headerLine)
					.add(parseResourceParameters(data, typeInfo))
					.getString()
			);
	}

	protected String parseResourceParameters(
		final ByteBuffer data,
		final ResourceTypeInfo typeInfo
	)
	{
		final StringBuilder stringBuilder =
			new StringBuilder(", ", "= {", "}");

		for (int paramNum = 0; paramNum < typeInfo.numberOfParameters; paramNum++)
		{
			ValueInfo valueInfo = typeInfo.paramTypes.get(paramNum);
			//TODO trace arrays when they are implemented
			switch(valueInfo.type)
			{
			case INTEGER:
				stringBuilder.add(data.getInt());
				break;
			case REAL:
				stringBuilder.add(data.getDouble());
				break;
			case BOOLEAN:
				stringBuilder.add(data.get() != 0);
				break;
			case ENUM:
				stringBuilder.add(
					valueInfo.enumNames.get((int) data.getShort())
				);
				break;
			case STRING:
				final int index = typeInfo.indexList.get(paramNum);
				final int stringPosition =
					data.getInt(typeInfo.finalOffset +
						(index - 1) * TypeSize.RDO.INTEGER);
				final int length = data.getInt(stringPosition);

				byte rawString[] = new byte[length];
				for (int i = 0; i < length; i++)
					rawString[i] = data.get(stringPosition +
						TypeSize.RDO.INTEGER + i);
				stringBuilder.add(
					"\"" +
					new String(rawString,StandardCharsets.UTF_8) +
					"\""
				);
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

	protected TraceOutput parsePatternEntry(final Database.Entry entry)
	{
		final ByteBuffer header = prepareBufferForReading(entry.header);
		final ByteBuffer data = prepareBufferForReading(entry.data);

		skipPart(header, TypeSize.BYTE);
		final double time = header.getDouble();
		final TraceType traceType;

		final Database.PatternType entryType =
			Database.PatternType.values()[header.get()];
		switch(entryType)
		{
		case EVENT:
			traceType = TraceType.EVENT;
			break;
		case RULE:
			traceType = TraceType.RULE;
			break;
		case OPERATION_BEGIN:
			traceType = TraceType.OPERATION_BEGIN;
			break;
		case OPERATION_END:
			traceType = TraceType.OPERATION_END;
			break;
		default:
			return null;
		}

		return
			new TraceOutput(
				traceType,
				new StringBuilder(delimiter)
					.add(traceType.toString())
					.add(time)
					.add(parsePatternData(data, traceType))
					.getString()
			);
	}

	protected String parsePatternData(
		final ByteBuffer data,
		final TraceType patternType
	)
	{
		final StringBuilder stringBuilder = new StringBuilder(delimiter);

		int patternNumber;

		switch(patternType)
		{
		case EVENT:
		{
			int eventNumber = data.getInt();
			int actionNumber = data.getInt();
			patternNumber = eventNumber;
			stringBuilder
				.add(patternsInfo.get(eventNumber).name
					+ encloseIndex(actionNumber));
			break;
		}
		case RULE:
		case OPERATION_BEGIN:
		case OPERATION_END:
		{
			int dptNumber = data.getInt();
			int activityNumber = data.getInt();
			int actionNumber = data.getInt();
			ActivityInfo activity = decisionPointsInfo.get(dptNumber)
				.activitiesInfo.get(activityNumber);
			patternNumber = activity.patternNumber;
			stringBuilder
				.add(activity.name + encloseIndex(actionNumber));
			break;
		}
		default:
			return null;
		}

		final StringBuilder resResStringBuilder =
				new StringBuilder(", ", "(", ")");

		int numberOfRelevantResources = data.getInt();
		for(int num = 0; num < numberOfRelevantResources; num++)
		{
			final int resNum = data.getInt();
			final int typeNum =
				patternsInfo.get(patternNumber).relResTypes.get(num);
			final String typeName =
				resourceTypesInfo.get(typeNum).name;

			final String name = resourceNames.get(typeNum).get(resNum);
			final String resourceName =
				name != null ?
					name :
					typeName + encloseIndex(resNum);

			resResStringBuilder.add(resourceName);
		}

		return stringBuilder.getString() + resResStringBuilder.getString();
	}

  /*――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――/
 /                              SEARCH ENTRIES                               /
/――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――*/

	protected TraceOutput parseSearchEntry(final Database.Entry entry)
	{
		return null;
	}

  /*――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――/
 /                           PARSING RESULT ENTRIES                          /
/――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――*/

	protected TraceOutput parseResultEntry(final Database.Entry entry)
	{
		final ByteBuffer header = prepareBufferForReading(entry.header);
		final ByteBuffer data = prepareBufferForReading(entry.data);

		skipPart(header, TypeSize.BYTE);
		final double time = header.getDouble();
		final int resultNum = header.getInt();
		final ResultInfo resultInfo = resultsInfo.get(resultNum);

		return
			new TraceOutput(
			TraceType.RESULT,
			new StringBuilder(delimiter)
				.add(TraceType.RESULT.toString())
				.add(time)
				.add(resultInfo.name)
				.add("=")
				.add(parseResultParameter(data, resultInfo.valueType))
				.getString()
			);
	}

	protected String parseResultParameter(
		final ByteBuffer data,
		final ModelStructureHelper.ValueType valueType
	)
	{
		switch(valueType)
		{
		case INTEGER:
			return String.valueOf(data.getInt());
		case REAL:
			return String.valueOf(data.getDouble());
		case BOOLEAN:
			return String.valueOf(data.get() != 0);
		case ENUM:
			return String.valueOf(data.getShort());
		case STRING:
			final ByteArrayOutputStream rawString = new ByteArrayOutputStream();
			while (data.hasRemaining())
			{
				rawString.write(data.get());
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

	final static void skipPart(final ByteBuffer buffer, final int size)
	{
		for (int i = 0; i < size; i++)
			buffer.get();
	}

	final static String encloseIndex(final int index)
	{
		return "[" + String.valueOf(index) + "]";
	}

	final static ByteBuffer prepareBufferForReading(final ByteBuffer buffer)
	{
		return (ByteBuffer) buffer.duplicate().rewind();
	}

	@Override
	public void fireChange() {}
}

class StringBuilder
{
	private final String delimiter;
	private final String prefix;
	private final String postfix;

	private String current = null;

	public final String getString()
	{
		return prefix + current + postfix;
	}

	//TODO StringBuilder constructors from some custom enum?
	StringBuilder(String delimeter)
	{
		this.delimiter = delimeter;
		this.prefix = "";
		this.postfix = "";
	}

	StringBuilder(String delimeter, String prefix, String postfix)
	{
		this.delimiter = delimeter;
		this.prefix = prefix;
		this.postfix = postfix;
	}

	public final StringBuilder add(final String toAppend)
	{
		if (current == null)
			current = new String(toAppend);
		else
			current += delimiter + toAppend;
		return this;
	}

	public final StringBuilder add(final int toAppend)
	{
		return add(String.valueOf(toAppend));
	}

	public final StringBuilder add(final double toAppend)
	{
		return add(String.valueOf(toAppend));
	}

	public final StringBuilder add(final boolean toAppend)
	{
		return add(String.valueOf(toAppend));
	}
}
