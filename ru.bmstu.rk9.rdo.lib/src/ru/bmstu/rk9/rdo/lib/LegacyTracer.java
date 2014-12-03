package ru.bmstu.rk9.rdo.lib;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
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

		initializeTypes();
		initializeActivities();
	}

	private final HashMap<Integer, HashMap<Integer, Integer>> legacyResourceIds =
		new HashMap<Integer, HashMap<Integer, Integer>>();
	private final TreeSet<Integer> takenResourceIds =
		new TreeSet<Integer>();
	private final PriorityQueue<Integer> vacantActionNumbers =
		new PriorityQueue<Integer>();
	private final HashMap<Integer, HashMap<
		Integer, HashMap<Integer, Integer>>> legacyActionNumbers=
			new HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>();

	static private final String delimiter = " ";

	private boolean simulationStarted = false;
	private boolean dptSearchJustStarted = false;

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
			new RDOLibStringJoiner(delimiter)
			.add(traceType.toString())
			.add(checkIntegerValuedReal((time)))
			.add(type.ordinal() + 1)
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
			new RDOLibStringJoiner(delimiter)
			.add(traceType.toString())
			.add(checkIntegerValuedReal((time)))
			.add(typeNum + 1)
			.add(legacyId)
			.getString();

		final ResourceTypeInfo typeInfo = resourceTypesInfo.get(typeNum);

		return
			new TraceOutput(
				traceType,
				new RDOLibStringJoiner(delimiter)
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
		final RDOLibStringJoiner stringJoiner =
			new RDOLibStringJoiner(delimiter);

		for (int paramNum = 0; paramNum < typeInfo.numberOfParameters; paramNum++)
		{
			//TODO trace arrays when they are implemented
			switch(typeInfo.paramTypes.get(paramNum).type)
			{
			case INTEGER:
				stringJoiner.add(data.getInt());
				break;
			case REAL:
				stringJoiner.add(checkIntegerValuedReal(data.getDouble()));
				break;
			case BOOLEAN:
				stringJoiner.add(legacyBooleanString(data.get() != 0));
				break;
			case ENUM:
				stringJoiner.add(data.getShort());
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
				stringJoiner.add(new String(rawString, StandardCharsets.UTF_8));
				break;
			default:
				return null;
			}
		}
		return stringJoiner.getString();
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
				new RDOLibStringJoiner(delimiter)
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
		final RDOLibStringJoiner stringJoiner =
			new RDOLibStringJoiner(delimiter);

		int patternNumber;

		switch(patternType)
		{
		case EVENT:
		{
			patternNumber = data.getInt();
			skipPart(data, TypeSize.INTEGER);
			stringJoiner
				.add(patternNumber + 1)
				.add(patternNumber + 1);
			break;
		}
		case RULE:
		{
			int dptNumber = data.getInt();
			int activityNumber = data.getInt();
			skipPart(data, TypeSize.INTEGER);
			patternNumber = decisionPointsInfo.get(dptNumber)
				.activitiesInfo.get(activityNumber).patternNumber;
			stringJoiner
				.add(1)
				.add(activityNumber + 1)
				.add(patternNumber + 1);
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
			stringJoiner
				.add(legacyNumber + 1)
				.add(activityNumber + 1)
				.add(patternNumber + 1);
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
			stringJoiner
				.add(legacyNumber + 1)
				.add(activityNumber + 1)
				.add(patternNumber + 1);
		}
			break;
		default:
			return null;
		}

		int numberOfRelevantResources = data.getInt();
		stringJoiner.add(numberOfRelevantResources).add("");
		for(int num = 0; num < numberOfRelevantResources; num++)
		{
			final int typeNum =
				patternsInfo.get(patternNumber).relResTypes.get(num);
			final int resNum = data.getInt();
			if (legacyResourceIds.get(typeNum).get(resNum) == null)
			{
				stringJoiner.add(getNewResourceId(typeNum, resNum));
			}
			else
			{
				final int legacyId =
					legacyResourceIds.get(typeNum).get(resNum);
				stringJoiner.add(legacyId);
			}
		}

		return stringJoiner.getString();
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
		if (dptSearchJustStarted)
		{
			addLegacySearchEntries();
			dptSearchJustStarted = false;
		}

		final ByteBuffer header = prepareBufferForReading(entry.header);
		final ByteBuffer data = prepareBufferForReading(entry.data);

		final RDOLibStringJoiner stringJoiner =
			new RDOLibStringJoiner(delimiter);

		final TraceType traceType;
		skipPart(header, TypeSize.BYTE);
		final Database.SearchEntryType entryType =
				Database.SearchEntryType.values()[header.get()];

		switch(entryType)
		{
		case BEGIN:
		{
			dptSearchJustStarted = true;
			traceType = TraceType.SEARCH_BEGIN;
			final double time = data.getDouble();
			final int number = data.getInt();
			currentDptNumber = number;
			skipPart(data, TypeSize.INTEGER);
			stringJoiner
				.add("SB")
				.add(checkIntegerValuedReal(time))
				.add(number + 1);
			break;
		}
		case END:
		{
			currentDptNumber = -1;
			//TODO switch over enum when it is made public
			final DecisionPointSearch.StopCode endStatus =
				DecisionPointSearch.StopCode.values()[data.get()];
			switch(endStatus)
			{
			case ABORTED:
				traceType = TraceType.SEARCH_END_ABORTED;
				break;
			case CONDITION:
				traceType = TraceType.SEARCH_END_CONDITION;
				break;
			case SUCCESS:
				traceType = TraceType.SEARCH_END_SUCCESS;
				break;
			case FAIL:
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
			stringJoiner
				.add(traceType.toString())
				.add(checkIntegerValuedReal(time))
				.add(timeMillis)
				.add(mem)
				.add(checkIntegerValuedReal(finalCost))
				.add(totalOpened)
				.add(totalNodes)
				.add(totalAdded)
				.add(totalSpawned);
			break;
		}
		case OPEN:
		{
			traceType = TraceType.SEARCH_OPEN;
			final int currentNumber = data.getInt();
			final int parentNumber = data.getInt();
			final double g = data.getDouble();
			final double h = data.getDouble();
			stringJoiner
				.add("SO")
				.add(currentNumber + 1)
				.add(parentNumber + 1)
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

			stringJoiner
				.add(traceType.toString())
				.add(childNumber + 1)
				.add(parentNumber + 1)
				.add(checkIntegerValuedReal(g))
				.add(checkIntegerValuedReal(g + h))
				.add(ruleNumber + 1)
				.add(patternNumber + 1)
				.add(checkIntegerValuedReal(ruleCost))
				.add(numberOfRelevantResources)
				.add("");

			for (int num = 0; num < numberOfRelevantResources; num++)
			{
				final int typeNum =
					patternsInfo.get(patternNumber).relResTypes.get(num);
				final int resNum = data.getInt();
				final int legacyNum =
					legacyResourceIds.get(typeNum).get(resNum);
				stringJoiner.add(legacyNum);
			}
			break;
		}
		case DECISION:
		{
			traceType = TraceType.SEARCH_DECISION;
			final int number = data.getInt();
			final int activityNumber = data.getInt();
			stringJoiner
				.add("SD")
				.add(number)
				.add(activityNumber);
			break;
		}
		default:
			return null;
		}

		return
			new TraceOutput(
				traceType,
				stringJoiner.getString()
			);
	}

	private final void addLegacySearchEntries()
	{
		traceList.add(
			new TraceOutput(
				TraceType.SEARCH_SPAWN_NEW,
				new RDOLibStringJoiner(delimiter)
					.add("STN")
					.add(1)
					.add(0)
					.add(0)
					.add(0)
					.add(-1)
					.add(-1)
					.add(0)
					.add(0)
					.getString()
				)
		);

		traceList.add(
				new TraceOutput(
					TraceType.SEARCH_OPEN,
					new RDOLibStringJoiner(delimiter)
						.add("SO")
						.add(1)
						.add(0)
						.add(0)
						.add(0)
						.getString()
					)
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
			new RDOLibStringJoiner(delimiter)
				.add(TraceType.RESULT.toString())
				.add(checkIntegerValuedReal(time))
				.add(resultNum + 1)
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
		final JSONArray resourceTypes =
				Simulator
				.getDatabase()
				.getModelStructure()
				.getJSONArray("resource_types");

		for (int num = 0; num < resourceTypes.length(); num++)
		{
			legacyResourceIds.put(
				num,
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
