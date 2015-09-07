package ru.bmstu.rk9.rao.lib.modelStructure;

import java.util.ArrayList;
import java.util.List;

import ru.bmstu.rk9.rao.lib.json.JSONArray;
import ru.bmstu.rk9.rao.lib.json.JSONObject;

public class ValueCache {
	public ValueCache(final JSONObject param) {
		type = ModelStructureCache.ValueType.get(param.getString("type"));
		if (getType() == ModelStructureCache.ValueType.ENUM) {
			enumNames = new ArrayList<String>();
			String enumOriginName = param.getString("enum_origin");
			JSONObject enumOrigin = ModelStructureCache.getEnumOrigin(enumOriginName);

			JSONArray enums = enumOrigin.getJSONObject("structure")
					.getJSONArray("enums");
			for (int num = 0; num < enums.length(); num++)
				getEnumNames().add(enums.getString(num));
		} else
			enumNames = null;
	}

	public ModelStructureCache.ValueType getType() {
		return type;
	}

	public List<String> getEnumNames() {
		return enumNames;
	}

	private final ModelStructureCache.ValueType type;
	private final List<String> enumNames;
}