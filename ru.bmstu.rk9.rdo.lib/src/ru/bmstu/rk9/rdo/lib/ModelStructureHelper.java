package ru.bmstu.rk9.rdo.lib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ru.bmstu.rk9.rdo.lib.json.JSONArray;
import ru.bmstu.rk9.rdo.lib.json.JSONObject;

class ModelStructureHelper
{
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

	final static void fillResourceNames(
		final HashMap<Integer, HashMap<Integer, String>> resourceNames)
	{
		final JSONArray resourceTypes =
			Simulator
			.getDatabase()
			.getModelStructure()
			.getJSONArray("resource_types");

		for (int typeNum = 0; typeNum < resourceTypes.length(); typeNum++)
		{
			JSONObject type = resourceTypes.getJSONObject(typeNum);
			HashMap<Integer, String> resources =
				new HashMap<Integer, String>();

			JSONArray jResources = type.getJSONArray("resources");
			for (int resNum = 0; resNum < jResources.length(); resNum++)
			{
				resources.put(resNum, jResources.getString(resNum));
			}

			resourceNames.put(
				typeNum,
				resources
			);
		}
	}

	//TODO 4 similar methods should be merged into one
	final static void fillResourceTypesInfo(
		final ArrayList<ResourceTypeInfo> resourceTypesInfo)
	{
		final JSONArray resourceTypes =
			Simulator
			.getDatabase()
			.getModelStructure()
			.getJSONArray("resource_types");

		for (int num = 0; num < resourceTypes.length(); num++)
			resourceTypesInfo.add(
				new ResourceTypeInfo(resourceTypes.getJSONObject(num))
			);
	}

	final static void fillPatternsInfo(
		final ArrayList<PatternInfo> patternsInfo)
	{
		final JSONArray patterns =
			Simulator
			.getDatabase()
			.getModelStructure()
			.getJSONArray("patterns");

		for (int num = 0; num < patterns.length(); num++)
			patternsInfo.add(
				new PatternInfo(patterns.getJSONObject(num))
			);
	}

	final static void fillDecisionPointsInfo(
		final ArrayList<DecisionPointInfo> decisionPointsInfo)
	{
		final JSONArray decisionPoints =
			Simulator
			.getDatabase()
			.getModelStructure()
			.getJSONArray("decision_points");

		for (int num = 0; num < decisionPoints.length(); num++)
			decisionPointsInfo.add(
				new DecisionPointInfo(decisionPoints.getJSONObject(num))
			);
	}

	final static void fillResultsInfo(
		final ArrayList<ResultInfo> resultsInfo)
	{
		final JSONArray results =
			Simulator
			.getDatabase()
			.getModelStructure()
			.getJSONArray("results");

		for (int num = 0; num < results.length(); num++)
			resultsInfo.add(
				new ResultInfo(results.getJSONObject(num))
			);
	}

	final static String getRelativeName(final String fullName)
	{
		return fullName.substring(fullName.lastIndexOf(".") + 1);
	}
}

class ValueInfo
{
	ValueInfo(final JSONObject param)
	{
		type = ModelStructureHelper.ValueType.get(param.getString("type"));
		if (type == ModelStructureHelper.ValueType.ENUM)
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

				JSONArray originParams =
					originType.getJSONObject(
						"structure").getJSONArray("parameters");
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

	public final ModelStructureHelper.ValueType type;
	public final HashMap<Integer, String> enumNames;
}

class ResourceTypeInfo
{
	ResourceTypeInfo(final JSONObject resourceType)
	{
		name = ModelStructureHelper.getRelativeName(
			resourceType.getString("name"));
		temporary = resourceType.getBoolean("temporary");

		JSONObject structure = resourceType.getJSONObject("structure");
		JSONArray parameters = structure.getJSONArray("parameters");
		numberOfParameters = parameters.length();

		paramTypes = new HashMap<Integer, ValueInfo>();
		indexList = new HashMap<Integer, Integer>();
		for (int num = 0; num < numberOfParameters; num++)
		{
			final JSONObject currentParameter = parameters.getJSONObject(num);
			ModelStructureHelper.ValueType type =
				ModelStructureHelper.ValueType.get(
					currentParameter.getString("type"));
			paramTypes.put(
				num,
				new ValueInfo(currentParameter)
			);
			if (type == ModelStructureHelper.ValueType.STRING)
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
		name = ModelStructureHelper.getRelativeName(pattern.getString("name"));
		relResTypes = new HashMap<Integer, Integer>();
		JSONArray relevantResources =
			pattern.getJSONArray("relevant_resources");
		for (int num = 0; num < relevantResources.length(); num++)
		{
			String typeName =
				relevantResources.getJSONObject(num).getString("type");

			JSONArray resTypes = Simulator.getDatabase().getModelStructure()
				.getJSONArray("resource_types");
			int typeNum = -1;
			//TODO throw exception if not found
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
		name = ModelStructureHelper.getRelativeName(dpt.getString("name"));

		activitiesInfo = new HashMap<Integer, ActivityInfo>();
		JSONArray activities = dpt.getJSONArray("activities");
		for (int num = 0; num < activities.length(); num++)
			activitiesInfo.put(
				num,
				new ActivityInfo(activities.getJSONObject(num))
			);
	}

	public final String name;
	public final HashMap<Integer, ActivityInfo> activitiesInfo;
}

class ActivityInfo
{
	ActivityInfo(final JSONObject activity)
	{
		name = ModelStructureHelper.getRelativeName(activity.getString("name"));

		final String patternName = activity.getString("pattern");
		JSONArray patterns = Simulator.getDatabase().getModelStructure()
				.getJSONArray("patterns");
		//TODO throw exception if not found
		for (int num = 0; num < patterns.length(); num++)
			if (patternName.equals(
					patterns.getJSONObject(num).getString("name")))
			{
				patternNumber = num;
				break;
			}
	}

	public final String name;
	public int patternNumber = -1;
}

class ResultInfo
{
	ResultInfo(final JSONObject result)
	{
		name = ModelStructureHelper.getRelativeName(result.getString("name"));
		valueType = ModelStructureHelper.ValueType.get
			(result.getString("value_type"));
	}

	public final String name;
	public final ModelStructureHelper.ValueType valueType;
}
