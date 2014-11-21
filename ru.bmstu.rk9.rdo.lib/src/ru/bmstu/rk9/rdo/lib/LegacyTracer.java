package ru.bmstu.rk9.rdo.lib;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import ru.bmstu.rk9.rdo.lib.Database.TypeSize;

public class LegacyTracer extends Tracer
{
	LegacyTracer()
	{
		super();

		legacyResourceIndexes =
			new HashMap<Integer, HashMap<Integer, Integer>>();
		takenIds = new TreeSet<Integer>();
		initializeTypes();
	}

	private final HashMap<Integer, HashMap<Integer, Integer>> legacyResourceIndexes;
	private final TreeSet<Integer> takenIds;

	static private final String delimiter = " ";

  /*――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――/
 /                          PARSING RESOURCE ENTRIES                         /
/――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――*/

	@Override
	protected final TraceOutput parseResourceEntry(final Database.Entry entry)
	{
		final ByteBuffer resourceHeader = entry.header;

		prepareBufferForReading(resourceHeader);

		final double time = resourceHeader.getDouble();
		skipPart(resourceHeader, TypeSize.BYTE);
		final TraceType traceType;
		byte entryType = resourceHeader.get();
		final int typeNum = resourceHeader.getInt();
		final int resNum = resourceHeader.getInt();

		int legacyId;
		switch(entryType)
		{
		case 0:
			//TODO resources created before model start should have
			//RK converter status instead of RC
			traceType = TraceType.RESOURCE_CREATE;
			if (legacyResourceIndexes.get(typeNum).get(resNum) == null)
				legacyId = getNewId(typeNum, resNum);
			else
				legacyId = legacyResourceIndexes.get(typeNum).get(resNum);
			break;
		case 1:
			traceType = TraceType.RESOURCE_ERASE;
			legacyId = legacyResourceIndexes.get(typeNum).get(resNum);
			freeId(typeNum, resNum);
			break;
		case 2:
			traceType = TraceType.RESOURCE_KEEP;
			legacyId = legacyResourceIndexes.get(typeNum).get(resNum);
			break;
		default:
			return null;
		}

		final String headerLine =
			new StringBuilder(delimiter)
			.add(traceType.toString())
			.add(checkIntegerValuedReal((time)))
			.add(String.valueOf(typeNum + 1))
			.add(String.valueOf(legacyId))
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
				new StringBuilder(delimiter)
					.add(headerLine)
					.add(parseResourceParameters(entry.data, typeInfo))
					.getString()
			);
	}

	@Override
	protected final String parseResourceParameters(
		final ByteBuffer resourceData,
		final ResourceTypeInfo typeInfo
	)
	{
		final StringBuilder stringBuilder = new StringBuilder(delimiter);

		prepareBufferForReading(resourceData);

		for (int paramNum = 0; paramNum < typeInfo.numberOfParameters; paramNum++)
		{
			//TODO trace arrays when they are implemented
			switch(typeInfo.paramTypes.get(paramNum).type)
			{
			case INTEGER:
				stringBuilder.add(String.valueOf(resourceData.getInt()));
				break;
			case REAL:
				stringBuilder.add(checkIntegerValuedReal(resourceData.getDouble()));
				break;
			case BOOLEAN:
				stringBuilder.add(legacyBooleanString(resourceData.get() != 0));
				break;
			case ENUM:
				stringBuilder.add(String.valueOf(resourceData.getShort()));
				break;
			case STRING:
				final int index = typeInfo.indexList.get(paramNum);
				final int stringPosition = resourceData.getInt(
					typeInfo.finalOffset + (index - 1) * TypeSize.RDO.INTEGER);
				final int length = resourceData.getInt(stringPosition);

				byte rawString[] = new byte[length];
				for (int i = 0; i < length; i++)
				{
					rawString[i] = resourceData.get(
						stringPosition + TypeSize.RDO.INTEGER + i);
				}
				stringBuilder.add(new String(rawString, StandardCharsets.UTF_8));
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

	@Override
	protected final TraceOutput parsePatternEntry(final Database.Entry entry)
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
					.add(checkIntegerValuedReal(time))
					.add(parsePatternData(entry.data, traceType))
					.getString()
			);
	}

	@Override
	protected final String parsePatternData(
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
			patternNumber = patternData.getInt();
			stringBuilder
				.add(String.valueOf(patternNumber + 1))
				.add(String.valueOf(patternNumber + 1));
			break;
		}
		case RULE:
		{
			int dptNumber = patternData.getInt();
			int activityNumber = patternData.getInt();
			patternNumber = decisionPointsInfo.get(dptNumber)
				.activitiesInfo.get(activityNumber).patternNumber;
			stringBuilder
				.add(String.valueOf(1))
				.add(String.valueOf(activityNumber + 1))
				.add(String.valueOf(patternNumber + 1));
			break;
		}
		case OPERATION_BEGIN:
		case OPERATION_END:
		{
			int dptNumber = patternData.getInt();
			int activityNumber = patternData.getInt();
			int actionNumber = patternData.getInt();
			patternNumber = decisionPointsInfo.get(dptNumber)
					.activitiesInfo.get(activityNumber).patternNumber;
			stringBuilder
				.add(String.valueOf(actionNumber + 1))
				.add(String.valueOf(activityNumber + 1))
				.add(String.valueOf(patternNumber + 1));
		}
			break;
		default:
			return null;
		}

		int numberOfRelevantResources = patternData.getInt();
		stringBuilder.add(String.valueOf(numberOfRelevantResources));
		stringBuilder.add("");
		for(int num = 0; num < numberOfRelevantResources; num++)
		{
			final int typeNum =
				patternsInfo.get(patternNumber).relResTypes.get(num);
			final int resNum = patternData.getInt();
			if (legacyResourceIndexes.get(typeNum).get(resNum) == null)
			{
				stringBuilder.add(String.valueOf(getNewId(typeNum, resNum)));
			}
			else
			{
				final int legacyId =
					legacyResourceIndexes.get(typeNum).get(resNum);
				stringBuilder.add(String.valueOf(legacyId));
			}
		}

		return stringBuilder.getString();
	}

  /*――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――/
 /                           PARSING RESULT ENTRIES                          /
/――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――*/

	@Override
	protected final TraceOutput parseResultEntry(final Database.Entry entry)
	{
		final ByteBuffer resultHeader = entry.header;

		prepareBufferForReading(resultHeader);

		final double time = resultHeader.getDouble();
		skipPart(resultHeader, TypeSize.BYTE);
		final int resultNum = resultHeader.getInt();

		final ModelStructureHelper.ValueType valueType = resultsInfo.get(resultNum).valueType;

		return
			new TraceOutput(
			TraceType.RESULT,
			new StringBuilder(delimiter)
				.add(TraceType.RESULT.toString())
				.add(checkIntegerValuedReal(time))
				.add(String.valueOf(resultNum + 1))
				.add(parseResultParameter(entry.data, valueType))
				.getString()
			);
	}

	@Override
	protected final String parseResultParameter(
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
			return checkIntegerValuedReal(resultData.getDouble());
		case BOOLEAN:
			return legacyBooleanString(resultData.get() != 0);
		case ENUM:
			return String.valueOf(resultData.getShort());
		case STRING:
			final ByteArrayOutputStream rawString = new ByteArrayOutputStream();
			while (resultData.hasRemaining())
			{
				rawString.write(resultData.get());
			}
			return rawString.toString();
		default:
			break;
		}

		return null;
	}

  /*――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――/
 /                               HELPER METHODS                              /
/――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――*/

	final private void initializeTypes()
	{
		for (Map.Entry<String, Database.PermanentResourceTypeIndex> type :
			Simulator.getDatabase().permanentResourceIndex.entrySet())
		{
			int typeNum = type.getValue().number;
			legacyResourceIndexes.put(
				typeNum,
				new HashMap<Integer, Integer>()
			);
		}

		for (Map.Entry<String, Database.TemporaryResourceTypeIndex> type :
				Simulator.getDatabase().temporaryResourceIndex.entrySet())
		{
			int typeNum = type.getValue().number;
			legacyResourceIndexes.put(
				typeNum,
				new HashMap<Integer, Integer>()
			);
		}
	}

	private final void freeId(int typeNum, int resNum)
	{
		int legacyId = legacyResourceIndexes.get(typeNum).get(resNum);
		legacyResourceIndexes.get(typeNum).remove(resNum);
		takenIds.remove(legacyId);
	}

	private final int getNewId(int typeNum, int resNum)
	{
		int current;
		int legacyId = 1;
		Iterator<Integer> it = takenIds.iterator();
		while (it.hasNext())
		{
			current = it.next();
			if (current != legacyId)
				break;
			legacyId++;
		}
		legacyResourceIndexes.get(typeNum).put(resNum, legacyId);
		takenIds.add(legacyId);
		return legacyId;
	}

	private final String legacyBooleanString(boolean value)
	{
		return value ? "TRUE" : "FALSE";
	}

	private final String checkIntegerValuedReal(double value)
	{
		return (int) value == value ?
			String.valueOf((int) value) : String.valueOf(value);
	}
}
