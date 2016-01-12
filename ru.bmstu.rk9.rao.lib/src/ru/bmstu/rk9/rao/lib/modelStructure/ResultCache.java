package ru.bmstu.rk9.rao.lib.modelStructure;

import java.util.ArrayList;
import java.util.List;

import ru.bmstu.rk9.rao.lib.json.JSONArray;
import ru.bmstu.rk9.rao.lib.json.JSONObject;

public class ResultCache {
	ResultCache(final JSONObject result) {
		name = ModelStructureCache.getRelativeName(result.getString("name"));
		String valueTypeRaw = result.getString("value_type");
		if (valueTypeRaw.contains("_enum")) {
			valueType = ModelStructureCache.ValueType.ENUM;
			enumNames = new ArrayList<String>();
			String enumOriginName = valueTypeRaw.substring(0, valueTypeRaw.lastIndexOf("_"));
			JSONObject originParam = ModelStructureCache.getEnumOrigin(enumOriginName);

			JSONArray enums = originParam.getJSONArray("enums");
			for (int num = 0; num < enums.length(); num++)
				getEnumNames().add(enums.getString(num));
		} else {
			valueType = ModelStructureCache.ValueType.get(result.getString("value_type"));
			enumNames = null;
		}
	}

	public ModelStructureCache.ValueType getValueType() {
		return valueType;
	}

	public List<String> getEnumNames() {
		return enumNames;
	}

	public String getName() {
		return name;
	}

	private final String name;
	private final ModelStructureCache.ValueType valueType;
	private final List<String> enumNames;
}