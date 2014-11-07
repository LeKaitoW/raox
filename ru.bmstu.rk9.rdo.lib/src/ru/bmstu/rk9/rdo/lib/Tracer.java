package ru.bmstu.rk9.rdo.lib;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ru.bmstu.rk9.rdo.lib.Database.TypeSize;
import ru.bmstu.rk9.rdo.lib.json.JSONArray;
import ru.bmstu.rk9.rdo.lib.json.JSONObject;

public final class Tracer implements Subscriber
{
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
		resourceNames = new HashMap<Integer, HashMap<Integer, String>>();
		fillResourceNames(resourceNames);

		resourceTypesInfo = new HashMap<Integer, ResourceTypeInfo>();
		fillResourceTypesInfo(resourceTypesInfo);
		resultsInfo = new HashMap<Integer, ResultInfo>();
		fillResultsInfo(resultsInfo);
		patternsInfo = new HashMap<Integer, PatternInfo>();
		fillPatternsInfo(patternsInfo);
		decisionPointsInfo = new HashMap<Integer, DecisionPointInfo>();
		fillDecisionPointsInfo(decisionPointsInfo);
	}

	static private final String delimiter = " ";
	static private final String numberDelimiter = ":";

	//TODO change all HashMaps with continuous keys to ArrayLists?
	private final HashMap<Integer, HashMap<Integer, String>> resourceNames;
	private final HashMap<Integer, ResourceTypeInfo> resourceTypesInfo;
	private final HashMap<Integer, ResultInfo> resultsInfo;
	private final HashMap<Integer, PatternInfo> patternsInfo;
	private final HashMap<Integer, DecisionPointInfo> decisionPointsInfo;

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

	private final TraceOutput parseResourceEntry(final Database.Entry entry)
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
				typeInfo.name + numberDelimiter + resNum;

		final String headerLine =
			new StringJoin(delimiter)
			.add(traceType.toString())
			.add(String.valueOf(time))
			.add(resourceName)
			.getString();

		//TODO fix when resource parameters are also serialized on erase
		if (traceType == TraceType.RESOURCE_ERASE)
		{
			return new TraceOutput(traceType, headerLine);
		}

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

	private final TraceOutput parsePatternEntry(final Database.Entry entry)
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
				.add(activity.name + numberDelimiter + actionNumber);
			break;
		}
		default:
			return null;
		}

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
					typeName + numberDelimiter + resNum;

			stringBuilder.add(resourceName);
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
		skipPart(resultHeader, TypeSize.BYTE);
		final int resultNum = resultHeader.getInt();
		final ResultInfo resultInfo = resultsInfo.get(resultNum);

		return
			new TraceOutput(
			TraceType.RESULT,
			new StringJoin(delimiter)
				.add(TraceType.RESULT.toString())
				.add(String.valueOf(time))
				.add(resultInfo.name)
				.add(parseResultParameter(entry.data, resultInfo.valueType))
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

	final static void fillResourceNames(
		final HashMap<Integer, HashMap<Integer, String>> resourceNames
	)
	{
		for (Map.Entry<String, Database.PermanentResourceTypeIndex> type :
				Simulator.getDatabase().permanentResourceIndex.entrySet())
		{
			int typeNum = type.getValue().number;
			resourceNames.put(
				typeNum,
				new HashMap<Integer, String>()
			);
			HashMap<Integer, String> resources = resourceNames.get(typeNum);
			for (Map.Entry<String, Database.Index> res :
					type.getValue().resources.entrySet())
				resources.put(
					res.getValue().number,
					getRelativeName(res.getKey())
				);
		}

		for (Map.Entry<String, Database.TemporaryResourceTypeIndex> type :
				Simulator.getDatabase().temporaryResourceIndex.entrySet())
		{
			int typeNum = type.getValue().number;
			resourceNames.put(
				typeNum,
				new HashMap<Integer, String>()
			);
			HashMap<Integer, String> resources = resourceNames.get(typeNum);
			for (Map.Entry<String, Database.Index> res :
					type.getValue().resources.entrySet())
				resources.put(
					res.getValue().number,
					getRelativeName(res.getKey())
				);
		}
	}

	//TODO 4 similar methods should be merged into one
	final static void fillResourceTypesInfo(
		final HashMap<Integer, ResourceTypeInfo> resourceTypesInfo
	)
	{
		final JSONArray resourceTypes =
			Simulator
			.getDatabase()
			.getModelStructure()
			.getJSONArray("resource_types");

		for (int num = 0; num < resourceTypes.length(); num++)
			resourceTypesInfo.put(
				num,
				new ResourceTypeInfo(resourceTypes.getJSONObject(num))
			);
	}

	final static void fillPatternsInfo(
		final HashMap<Integer, PatternInfo> patternsInfo
	)
	{
		final JSONArray patterns =
			Simulator
			.getDatabase()
			.getModelStructure()
			.getJSONArray("patterns");

		for (int num = 0; num < patterns.length(); num++)
			patternsInfo.put(
				num,
				new PatternInfo(patterns.getJSONObject(num))
			);
	}

	final static void fillDecisionPointsInfo(
		final HashMap<Integer, DecisionPointInfo> decisionPointsInfo
	)
	{
		final JSONArray decisionPoints =
			Simulator
			.getDatabase()
			.getModelStructure()
			.getJSONArray("decision_points");

		for (int num = 0; num < decisionPoints.length(); num++)
			decisionPointsInfo.put(
				num,
				new DecisionPointInfo(decisionPoints.getJSONObject(num))
			);
	}

	final static void fillResultsInfo(
			final HashMap<Integer, ResultInfo> resultsInfo
		)
	{
		final JSONArray results =
			Simulator
			.getDatabase()
			.getModelStructure()
			.getJSONArray("results");

		for (int num = 0; num < results.length(); num++)
			resultsInfo.put(
				num,
				new ResultInfo(results.getJSONObject(num))
			);
	}

	final static void skipPart(final ByteBuffer buffer, final int size)
	{
		for (int i = 0; i < size; i++)
			buffer.get();
	}

	final static void prepareBufferForReading(final ByteBuffer buffer)
	{
		buffer.duplicate();
		buffer.rewind();
	}

	final static String getRelativeName(final String fullName)
	{
		return fullName.substring(fullName.lastIndexOf(".") + 1);
	}

	@Override
	public void fireChange() {}
}

class ValueInfo
{
	ValueInfo(final JSONObject param)
	{
		type = Tracer.ValueType.get(param.getString("type"));
		if (type == Tracer.ValueType.ENUM)
		{
			enumNames = new HashMap<Integer, String>();
			JSONObject originParam = null;
			if (param.has("enums"))
			{
				originParam = param;
			}
			else
			{
				String enumOrigin = param.getString("enum_origin");
				String typeName =
					enumOrigin.substring(0, enumOrigin.lastIndexOf("."));
				String paramName =
					enumOrigin.substring(enumOrigin.lastIndexOf(".") + 1);

				//TODO simpler solution than parsing resourceTypes?
				// maybe storing enumOrigin type number?
				JSONArray resourceTypes =
					Simulator.getDatabase().getModelStructure()
					.getJSONArray("resource_types");
				JSONObject originType = null;
				for (int num = 0; num < resourceTypes.length(); num++)
				{
					JSONObject curType = resourceTypes.getJSONObject(num);
					if (typeName.equals(curType.getString("name")))
					{
						originType = curType;
						break;
					}
				}

				//TODO simpler solution than parsing all parameters?
				// maybe storing parameter number?
				JSONArray originParams =
					originType.getJSONObject("structure").getJSONArray("parameters");
				for (int num = 0; num < originParams.length(); num++)
				{
					JSONObject curParam = originParams.getJSONObject(num);
					if (paramName.equals(curParam.getString("name")))
					{
						originParam = curParam;
						break;
					}
				}
			}

			JSONArray enums = originParam.getJSONArray("enums");
			for (int num = 0; num < enums.length(); num++)
				enumNames.put(num, enums.getString(num));
		}
		else
			enumNames = null;
	}
	public final Tracer.ValueType type;
	public final HashMap<Integer, String> enumNames;
}

class ResourceTypeInfo
{
	ResourceTypeInfo(final JSONObject resourceType)
	{
		name = Tracer.getRelativeName(resourceType.getString("name"));
		temporary = resourceType.getBoolean("temporary");

		JSONObject structure = resourceType.getJSONObject("structure");
		JSONArray parameters = structure.getJSONArray("parameters");
		numberOfParameters = parameters.length();

		paramTypes = new HashMap<Integer, ValueInfo>();
		indexList = new HashMap<Integer, Integer>();
		for (int num = 0; num < numberOfParameters; num++)
		{
			final JSONObject currentParameter = parameters.getJSONObject(num);
			Tracer.ValueType type = Tracer.ValueType.get(currentParameter.getString("type"));
			paramTypes.put(
				num,
				new ValueInfo(currentParameter)
			);
			if (type == Tracer.ValueType.STRING)
			{
				indexList.put(num, currentParameter.getInt("index"));
			}
		}
		finalOffset = structure.getInt("last_offset");
	}

	public final String name;
	public final boolean temporary;
	public final int numberOfParameters;
	public final HashMap<Integer, ValueInfo> paramTypes;
	public final HashMap<Integer, Integer> indexList;
	public final int finalOffset;
}

class PatternInfo
{
	PatternInfo(final JSONObject pattern)
	{
		name = Tracer.getRelativeName(pattern.getString("name"));
		relResTypes = new HashMap<Integer, Integer>();
		JSONArray relevantResources =
			pattern.getJSONArray("relevant_resources");
		for (int num = 0; num < relevantResources.length(); num++)
		{
			String typeName =
				relevantResources.getJSONObject(num).getString("type");

			//TODO instead of searching this should be serialized
			JSONArray resTypes = Simulator.getDatabase().getModelStructure()
				.getJSONArray("resource_types");
			Integer typeNum = null;
			for (int i = 0; i < resTypes.length(); i++)
				if (typeName.equals(
						resTypes.getJSONObject(i).getString("name")))
				{
					typeNum = i;
					break;
				}
			relResTypes.put(
				num,
				typeNum
			);
		}
	}

	public final String name;
	public final HashMap<Integer, Integer> relResTypes;
}

class DecisionPointInfo
{
	DecisionPointInfo(final JSONObject dpt)
	{
		name = Tracer.getRelativeName(dpt.getString("name"));
		activitiesInfo = new HashMap<Integer, ActivityInfo>();
		JSONArray activities = dpt.getJSONArray("activities");
		for (int num = 0; num < activities.length(); num++)
		{
			activitiesInfo.put(
				num,
				new ActivityInfo(activities.getJSONObject(num))
			);
		}
	}

	public final String name;
	public final HashMap<Integer, ActivityInfo> activitiesInfo;
}

class ActivityInfo
{
	ActivityInfo(final JSONObject activity)
	{
		name = Tracer.getRelativeName(activity.getString("name"));
		final String patternName = activity.getString("pattern");
		//TODO instead of searching this should be serialized
		JSONArray patterns = Simulator.getDatabase().getModelStructure()
				.getJSONArray("patterns");
		for (int num = 0; num < patterns.length(); num++)
			if (patternName.equals(
					patterns.getJSONObject(num).getString("name")))
			{
				patternNumber = num;
				break;
			}
	}

	public final String name;
	//TODO change to final int when fixed TODO above
	public Integer patternNumber = null;
}

class ResultInfo
{
	ResultInfo(final JSONObject result)
	{
		name = Tracer.getRelativeName(result.getString("name"));
		valueType = Tracer.ValueType.get(result.getString("value_type"));
	}

	public final String name;
	public final Tracer.ValueType valueType;
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
