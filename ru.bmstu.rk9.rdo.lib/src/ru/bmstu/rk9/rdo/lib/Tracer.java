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
		//TODO add DPT
		RESOURCE_CREATE("RC"),
		RESOURCE_KEEP("RK"),
		RESOURCE_ERASE("RE"),
		SYSTEM("ES"),
		OPERATION_BEGIN("EB"),
		OPERATION_END("EF"),
		EVENT("EI"),
		RULE("ER"),
		RESULT("V ");

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

	protected TraceOutput parseResourceEntry(final Database.Entry entry)
	{
		final ByteBuffer resourceHeader = entry.header;

		prepareBufferForReading(resourceHeader);

		final double time = resourceHeader.getDouble();
		skipPart(resourceHeader, TypeSize.BYTE);
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
		final ResourceTypeInfo typeInfo =
			resourceTypesInfo.get(typeNum);
		final int resNum = resourceHeader.getInt();
		final String name = resourceNames.get(typeNum).get(resNum);

		final String resourceName =
			name != null ?
				name :
				typeInfo.name + encloseIndex(resNum);

		final String headerLine =
			new StringBuilder(delimiter)
			.add(traceType.toString())
			.add(String.valueOf(time))
			.add(resourceName)
			.getString();

		return
			new TraceOutput(
				traceType,
				new StringBuilder(delimiter)
					.add(headerLine)
					.add(parseResourceParameters(entry.data, typeInfo))
					.getString()
			);
	}

	protected String parseResourceParameters(
		final ByteBuffer resourceData,
		final ResourceTypeInfo typeInfo
	)
	{
		final StringBuilder stringBuilder =
			new StringBuilder(", ", "= {", "}");

		prepareBufferForReading(resourceData);

		for (int paramNum = 0; paramNum < typeInfo.numberOfParameters; paramNum++)
		{
			ValueInfo valueInfo = typeInfo.paramTypes.get(paramNum);
			//TODO trace arrays when they are implemented
			switch(valueInfo.type)
			{
			case INTEGER:
				stringBuilder.add(String.valueOf(resourceData.getInt()));
				break;
			case REAL:
				stringBuilder.add(String.valueOf(resourceData.getDouble()));
				break;
			case BOOLEAN:
				stringBuilder.add(String.valueOf(resourceData.get() != 0));
				break;
			case ENUM:
				stringBuilder.add(
					valueInfo.enumNames.get((int) resourceData.getShort())
				);
				break;
			case STRING:
				final int index = typeInfo.indexList.get(paramNum);
				final int stringPosition =
					resourceData.getInt(typeInfo.finalOffset +
						(index - 1) * TypeSize.RDO.INTEGER);
				final int length = resourceData.getInt(stringPosition);

				byte rawString[] = new byte[length];
				for (int i = 0; i < length; i++)
					rawString[i] = resourceData.get(stringPosition +
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
		final ByteBuffer patternHeader = entry.header;

		prepareBufferForReading(patternHeader);

		final double time = patternHeader.getDouble();
		skipPart(patternHeader, TypeSize.BYTE);
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
				new StringBuilder(delimiter)
					.add(traceType.toString())
					.add(String.valueOf(time))
					.add(parsePatternData(entry.data, traceType))
					.getString()
			);
	}

	protected String parsePatternData(
		final ByteBuffer patternData,
		final TraceType patternType
	)
	{
		final StringBuilder stringBuilder = new StringBuilder(delimiter);

		prepareBufferForReading(patternData);

		int patternNumber;

		switch(patternType)
		{
		case EVENT:
		{
			int eventNumber = patternData.getInt();
			patternNumber = eventNumber;
			stringBuilder
				.add(patternsInfo.get(eventNumber).name);
			break;
		}
		case RULE:
		{
			int dptNumber = patternData.getInt();
			int activityNumber = patternData.getInt();
			ActivityInfo activity = decisionPointsInfo.get(dptNumber)
				.activitiesInfo.get(activityNumber);
			patternNumber = activity.patternNumber;
			stringBuilder
				.add(activity.name);
			break;
		}
		case OPERATION_BEGIN:
		case OPERATION_END:
		{
			int dptNumber = patternData.getInt();
			int activityNumber = patternData.getInt();
			int actionNumber = patternData.getInt();
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

		int numberOfRelevantResources = patternData.getInt();
		for(int num = 0; num < numberOfRelevantResources; num++)
		{
			final int resNum = patternData.getInt();
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
 /                           PARSING RESULT ENTRIES                          /
/――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――*/

	protected TraceOutput parseResultEntry(final Database.Entry entry)
	{
		final ByteBuffer resultHeader = entry.header;

		prepareBufferForReading(resultHeader);

		final double time = resultHeader.getDouble();
		skipPart(resultHeader, TypeSize.BYTE);
		final int resultNum = resultHeader.getInt();
		final ResultInfo resultInfo = resultsInfo.get(resultNum);

		return
			new TraceOutput(
			TraceType.RESULT,
			new StringBuilder(delimiter)
				.add(TraceType.RESULT.toString())
				.add(String.valueOf(time))
				.add(resultInfo.name)
				.add("=")
				.add(parseResultParameter(entry.data, resultInfo.valueType))
				.getString()
			);
	}

	protected String parseResultParameter(
		final ByteBuffer resultData,
		final ModelStructureHelper.ValueType valueType
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
			return String.valueOf(resultData.get() != 0);
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

	final static void skipPart(final ByteBuffer buffer, final int size)
	{
		for (int i = 0; i < size; i++)
			buffer.get();
	}

	final static String encloseIndex(final int index)
	{
		return "[" + String.valueOf(index) + "]";
	}

	final static void prepareBufferForReading(final ByteBuffer buffer)
	{
		buffer.duplicate();
		buffer.rewind();
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
}
