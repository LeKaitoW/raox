package ru.bmstu.rk9.rdo.lib;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.TreeSet;

import ru.bmstu.rk9.rdo.lib.Database.TypeSize;
import ru.bmstu.rk9.rdo.lib.json.JSONArray;
import ru.bmstu.rk9.rdo.lib.json.JSONObject;

public class LegacyTracer extends Tracer
{
	LegacyTracer()
	{
		super();

		legacyResourceIds =
			new HashMap<Integer, HashMap<Integer, Integer>>();
		takenResourceIds =
			new TreeSet<Integer>();
		vacantActionNumbers =
			new PriorityQueue<Integer>();
		legacyActionNumbers =
			new HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>();

		initializeTypes();
		initializeActivities();
	}

	private final HashMap<Integer, HashMap<Integer, Integer>> legacyResourceIds;
	private final TreeSet<Integer> takenResourceIds;
	private final PriorityQueue<Integer> vacantActionNumbers;
	private final HashMap<Integer, HashMap<
		Integer, HashMap<Integer, Integer>>> legacyActionNumbers;

	static private final String delimiter = " ";

	private boolean simulationStarted = false;
	//TODO probably not the best solution
	//TODO Integer and null instead of int and -1?
	private int currentDptNumber = -1;

  /*――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――/
 /                          PARSING SYSTEM ENTRIES                           /
/――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――*/

	@Override
	protected TraceOutput parseSystemEntry(final Database.Entry entry)
	{
		final ByteBuffer header = prepareBufferForReading(entry.header);

		final TraceType traceType = TraceType.SYSTEM;

		skipPart(header, TypeSize.BYTE);
		final double time = header.getDouble();
		final Database.SystemEntryType type =
			Database.SystemEntryType.values()[header.get()];

		if (type == Database.SystemEntryType.SIM_START)
			simulationStarted = true;

		final String headerLine =
			new StringBuilder(delimiter)
			.add(traceType.toString())
			.add(checkIntegerValuedReal((time)))
			.add(String.valueOf(type.ordinal() + 1))
			.getString();

		return new TraceOutput(traceType, headerLine);
	}

  /*――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――/
 /                          PARSING RESOURCE ENTRIES                         /
/――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――*/

	@Override
	protected final TraceOutput parseResourceEntry(final Database.Entry entry)
	{
		final ByteBuffer header = prepareBufferForReading(entry.header);
		final ByteBuffer data = prepareBufferForReading(entry.data);

		skipPart(header, TypeSize.BYTE);
		final double time = header.getDouble();
		final TraceType traceType;
		final Database.ResourceEntryType entryType =
			Database.ResourceEntryType.values()[header.get()];
		final int typeNum = header.getInt();
		final int resNum = header.getInt();

		int legacyId;
		switch(entryType)
		{
		case CREATED:
			traceType = simulationStarted ?
				TraceType.RESOURCE_CREATE : TraceType.RESOURCE_KEEP;

			if (legacyResourceIds.get(typeNum).get(resNum) == null)
				legacyId = getNewResourceId(typeNum, resNum);
			else
				legacyId = legacyResourceIds.get(typeNum).get(resNum);

			break;
		case ERASED:
			traceType = TraceType.RESOURCE_ERASE;
			legacyId = legacyResourceIds.get(typeNum).get(resNum);
			freeResourceId(typeNum, resNum);
			break;
		case ALTERED:
			traceType = TraceType.RESOURCE_KEEP;
			legacyId = legacyResourceIds.get(typeNum).get(resNum);
			break;
		//TODO how to know if resource was created or erased?
		case SEARCH:
		case SOLUTION:
			traceType = TraceType.SEARCH_RESOURCE_KEEP;
			legacyId = legacyResourceIds.get(typeNum).get(resNum);
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

		final ResourceTypeInfo typeInfo = resourceTypesInfo.get(typeNum);

		return
			new TraceOutput(
				traceType,
				new StringBuilder(delimiter)
					.add(headerLine)
					.add(parseResourceParameters(data, typeInfo))
					.getString()
			);
	}

	@Override
	protected final String parseResourceParameters(
		final ByteBuffer data,
		final ResourceTypeInfo typeInfo
	)
	{
		final StringBuilder stringBuilder = new StringBuilder(delimiter);

		for (int paramNum = 0; paramNum < typeInfo.numberOfParameters; paramNum++)
		{
			//TODO trace arrays when they are implemented
			switch(typeInfo.paramTypes.get(paramNum).type)
			{
			case INTEGER:
				stringBuilder.add(String.valueOf(data.getInt()));
				break;
			case REAL:
				stringBuilder.add(checkIntegerValuedReal(data.getDouble()));
				break;
			case BOOLEAN:
				stringBuilder.add(legacyBooleanString(data.get() != 0));
				break;
			case ENUM:
				stringBuilder.add(String.valueOf(data.getShort()));
				break;
			case STRING:
				final int index = typeInfo.indexList.get(paramNum);
				final int stringPosition = data.getInt(
					typeInfo.finalOffset + (index - 1) * TypeSize.RDO.INTEGER);
				final int length = data.getInt(stringPosition);

				byte rawString[] = new byte[length];
				for (int i = 0; i < length; i++)
				{
					rawString[i] = data.get(
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
					.add(checkIntegerValuedReal(time))
					.add(parsePatternData(data, traceType))
					.getString()
			);
	}

	@Override
	protected final String parsePatternData(
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
			patternNumber = data.getInt();
			skipPart(data, TypeSize.INTEGER);
			stringBuilder
				.add(String.valueOf(patternNumber + 1))
				.add(String.valueOf(patternNumber + 1));
			break;
		}
		case RULE:
		{
			int dptNumber = data.getInt();
			int activityNumber = data.getInt();
			skipPart(data, TypeSize.INTEGER);
			patternNumber = decisionPointsInfo.get(dptNumber)
				.activitiesInfo.get(activityNumber).patternNumber;
			stringBuilder
				.add(String.valueOf(1))
				.add(String.valueOf(activityNumber + 1))
				.add(String.valueOf(patternNumber + 1));
			break;
		}
		case OPERATION_BEGIN:
		{
			int dptNumber = data.getInt();
			int activityNumber = data.getInt();
			int actionNumber = data.getInt();

			HashMap<Integer, Integer> activityActions =
				legacyActionNumbers
				.get(dptNumber)
				.get(activityNumber);

			int legacyNumber;
			if (vacantActionNumbers.isEmpty())
				legacyNumber = activityActions.size();
			else
				legacyNumber = vacantActionNumbers.poll();

			activityActions.put(actionNumber, legacyNumber);

			patternNumber = decisionPointsInfo.get(dptNumber)
					.activitiesInfo.get(activityNumber).patternNumber;
			stringBuilder
				.add(String.valueOf(legacyNumber + 1))
				.add(String.valueOf(activityNumber + 1))
				.add(String.valueOf(patternNumber + 1));
			break;
		}
		case OPERATION_END:
		{
			int dptNumber = data.getInt();
			int activityNumber = data.getInt();
			int actionNumber = data.getInt();

			HashMap<Integer, Integer> activityActions =
					legacyActionNumbers
					.get(dptNumber)
					.get(activityNumber);
			int legacyNumber = activityActions.remove(actionNumber);
			vacantActionNumbers.add(legacyNumber);

			patternNumber = decisionPointsInfo.get(dptNumber)
					.activitiesInfo.get(activityNumber).patternNumber;
			stringBuilder
				.add(String.valueOf(legacyNumber + 1))
				.add(String.valueOf(activityNumber + 1))
				.add(String.valueOf(patternNumber + 1));
		}
			break;
		default:
			return null;
		}

		int numberOfRelevantResources = data.getInt();
		stringBuilder.add(String.valueOf(numberOfRelevantResources));
		stringBuilder.add("");
		for(int num = 0; num < numberOfRelevantResources; num++)
		{
			final int typeNum =
				patternsInfo.get(patternNumber).relResTypes.get(num);
			final int resNum = data.getInt();
			if (legacyResourceIds.get(typeNum).get(resNum) == null)
			{
				stringBuilder.add(
					String.valueOf(getNewResourceId(typeNum, resNum)));
			}
			else
			{
				final int legacyId =
					legacyResourceIds.get(typeNum).get(resNum);
				stringBuilder.add(String.valueOf(legacyId));
			}
		}

		return stringBuilder.getString();
	}

  /*――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――/
 /                              SEARCH ENTRIES                               /
/――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――*/

	//TODO in old RDO traceStart and Simulation are simultaneous
	//when we deal with DecisionPointSearch, so no initial resource
	//values are not printed.
	//How to check that?

	@Override
	protected TraceOutput parseSearchEntry(final Database.Entry entry)
	{
		final ByteBuffer header = prepareBufferForReading(entry.header);
		final ByteBuffer data = prepareBufferForReading(entry.data);

		final StringBuilder stringBuilder =
			new StringBuilder(delimiter);

		final TraceType traceType;
		skipPart(header, TypeSize.BYTE);
		final Database.SearchEntryType entryType =
				Database.SearchEntryType.values()[header.get()];

		switch(entryType)
		{
		case BEGIN:
		{
			traceType = TraceType.SEARCH_BEGIN;
			final double time = data.getDouble();
			final int number = data.getInt();
			currentDptNumber = number;
			skipPart(data, TypeSize.INTEGER);
			stringBuilder
				.add(traceType.toString())
				.add(checkIntegerValuedReal(time))
				.add(String.valueOf(number + 1));
			break;
		}
		case END:
		{
			currentDptNumber = -1;
			//TODO switch over enum when it is made public
			final byte endStatus = data.get();
			switch(endStatus)
			{
			case 0:
				traceType = TraceType.SEARCH_END_ABORTED;
				break;
			case 1:
				traceType = TraceType.SEARCH_END_CONDITION;
				break;
			case 2:
				traceType = TraceType.SEARCH_END_SUCCESS;
				break;
			case 3:
				traceType = TraceType.SEARCH_END_FAIL;
				break;
			default:
				//TODO throw exception
				return null;
			}

			final double time = data.getDouble();
			final long timeMillis = data.getLong();
			final long mem = data.getLong();
			final double finalCost = data.getDouble();
			final int totalOpened = data.getInt();
			final int totalNodes = data.getInt();
			final int totalAdded = data.getInt();
			final int totalSpawned = data.getInt();
			stringBuilder
				.add(traceType.toString())
				.add(checkIntegerValuedReal(time))
				.add(String.valueOf(timeMillis))
				.add(String.valueOf(mem))
				.add(checkIntegerValuedReal(finalCost))
				.add(String.valueOf(totalOpened))
				.add(String.valueOf(totalNodes))
				.add(String.valueOf(totalAdded))
				.add(String.valueOf(totalSpawned));
			break;
		}
		case OPEN:
		{
			traceType = TraceType.SEARCH_OPEN;
			final int currentNumber = data.getInt();
			final int parentNumber = data.getInt();
			final double g = data.getDouble();
			final double h = data.getDouble();
			stringBuilder
				.add(traceType.toString())
				.add(String.valueOf(currentNumber + 1))
				.add(String.valueOf(parentNumber + 1))
				.add(checkIntegerValuedReal(g))
				.add(checkIntegerValuedReal(g + h));
			break;
		}
		case SPAWN:
		{
			final DecisionPointSearch.SpawnStatus spawnStatus =
					DecisionPointSearch.SpawnStatus.values()[data.get()];
			switch(spawnStatus)
			{
			case NEW:
				traceType = TraceType.SEARCH_SPAWN_NEW;
				break;
			case WORSE:
				traceType = TraceType.SEARCH_SPAWN_WORSE;
				break;
			case BETTER:
				traceType = TraceType.SEARCH_SPAWN_BETTER;
				break;
			default:
				//TODO throw exception
				return null;
			}
			final int childNumber = data.getInt();
			final int parentNumber = data.getInt();
			final double g = data.getDouble();
			final double h = data.getDouble();
			final int ruleNumber = data.getInt();
			final int patternNumber = decisionPointsInfo.get(currentDptNumber)
				.activitiesInfo.get(ruleNumber).patternNumber;
			final double ruleCost = data.getDouble();
			final int numberOfRelevantResources =
				patternsInfo.get(patternNumber).relResTypes.size();

			stringBuilder
				.add(traceType.toString())
				.add(String.valueOf(childNumber + 1))
				.add(String.valueOf(parentNumber + 1))
				.add(checkIntegerValuedReal(g))
				.add(checkIntegerValuedReal(g + h))
				.add(String.valueOf(ruleNumber + 1))
				.add(String.valueOf(patternNumber + 1))
				.add(checkIntegerValuedReal(ruleCost))
				.add(String.valueOf(numberOfRelevantResources))
				.add("");

			for (int num = 0; num < numberOfRelevantResources; num++)
			{
				final int typeNum =
					patternsInfo.get(patternNumber).relResTypes.get(num);
				final int resNum = data.getInt();
				final int legacyNum =
					legacyResourceIds.get(typeNum).get(resNum);
				stringBuilder.add(String.valueOf(legacyNum));
			}
			break;
		}
		case DECISION:
		{
			traceType = TraceType.SEARCH_DECISION;
			final int number = data.getInt();
			final int activityNumber = data.getInt();
			stringBuilder
				.add(traceType.toString())
				.add(String.valueOf(number))
				.add(String.valueOf(activityNumber));
			break;
		}
		default:
			return null;
		}

		return
			new TraceOutput(
				traceType,
				stringBuilder.getString()
			);
	}

  /*――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――/
 /                           PARSING RESULT ENTRIES                          /
/――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――*/

	@Override
	protected final TraceOutput parseResultEntry(final Database.Entry entry)
	{
		final ByteBuffer header = prepareBufferForReading(entry.header);
		final ByteBuffer data = prepareBufferForReading(entry.data);

		skipPart(header, TypeSize.BYTE);
		final double time = header.getDouble();
		final int resultNum = header.getInt();

		final ModelStructureHelper.ValueType valueType =
			resultsInfo.get(resultNum).valueType;

		return
			new TraceOutput(
			TraceType.RESULT,
			new StringBuilder(delimiter)
				.add(TraceType.RESULT.toString())
				.add(checkIntegerValuedReal(time))
				.add(String.valueOf(resultNum + 1))
				.add(parseResultParameter(data, valueType))
				.getString()
			);
	}

	@Override
	protected final String parseResultParameter(
		final ByteBuffer resultData,
		final ModelStructureHelper.ValueType valueType
	)
	{
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

	private final void initializeTypes()
	{
		//TODO get it from JSON?
		for (Map.Entry<String, Database.ResourceTypeIndex> type :
			Simulator.getDatabase().resourceIndex.entrySet())
		{
			int typeNum = type.getValue().number;
			legacyResourceIds.put(
				typeNum,
				new HashMap<Integer, Integer>()
			);
		}
	}

	private final void initializeActivities()
	{
		JSONArray decisionPoints =
			Simulator
			.getDatabase()
			.getModelStructure()
			.getJSONArray("decision_points");

		for(int dptNum = 0; dptNum < decisionPoints.length(); dptNum++)
		{
			JSONObject decisionPoint = decisionPoints.getJSONObject(dptNum);
			String type = decisionPoint.getString("type");
			switch(type)
			{
				case "some":
				case "prior":
					HashMap<Integer, HashMap<Integer, Integer>> activities =
						new HashMap<Integer, HashMap<Integer, Integer>>();
					JSONArray jActivities =
						decisionPoint.getJSONArray("activities");
					for(int actNum = 0; actNum < jActivities.length(); actNum++)
					{
						HashMap<Integer, Integer> activity =
							new HashMap<Integer, Integer>();
						activities.put(actNum, activity);
					}
					legacyActionNumbers.put(dptNum, activities);
					break;
				default:
					break;
			}
		}
	}

	private final void freeResourceId(int typeNum, int resNum)
	{
		int legacyId = legacyResourceIds.get(typeNum).get(resNum);
		legacyResourceIds.get(typeNum).remove(resNum);
		takenResourceIds.remove(legacyId);
	}

	private final int getNewResourceId(int typeNum, int resNum)
	{
		int current;
		int legacyId = 1;
		Iterator<Integer> it = takenResourceIds.iterator();
		while (it.hasNext())
		{
			current = it.next();
			if (current != legacyId)
				break;
			legacyId++;
		}
		legacyResourceIds.get(typeNum).put(resNum, legacyId);
		takenResourceIds.add(legacyId);
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
