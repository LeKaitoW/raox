package ru.bmstu.rk9.rao.lib.database;

import ru.bmstu.rk9.rao.lib.database.Database.SerializationCategory;

public class IndexHelper {
	public final CollectedDataNode getTree() {
		return root;
	}

	public final CollectedDataNode initializeModel(String name) {
		if (root.hasChildren()) {
			root.getChildren().clear();
		}

		modelName = name;
		CollectedDataNode modelIndex = root.addChild(name);
		for (SerializationCategory value : SerializationCategory.values()) {
			modelIndex.addChild(value.getName());
		}
		return modelIndex;
	}

	public final CollectedDataNode getModel() {
		return root.getChildren().get(modelName);
	}

	private final CollectedDataNode getCategory(SerializationCategory category) {
		return getModel().getChildren().get(category.getName());
	}

	public final CollectedDataNode addResourceType(String name) {
		return getCategory(SerializationCategory.RESOURCE).addChild(name);
	}

	public final CollectedDataNode getResourceType(String name) {
		return getCategory(SerializationCategory.RESOURCE).getChildren().get(name);
	}

	public final CollectedDataNode addResult(String name) {
		return getCategory(SerializationCategory.RESULT).addChild(name);
	}

	public final CollectedDataNode getResult(String name) {
		return getCategory(SerializationCategory.RESULT).getChildren().get(name);
	}

	public final CollectedDataNode addEvent(String name) {
		return getCategory(SerializationCategory.EVENT).addChild(name);
	}

	public final CollectedDataNode getEvent(String name) {
		return getCategory(SerializationCategory.EVENT).getChildren().get(name);
	}

	public final CollectedDataNode addLogic(String name) {
		return getCategory(SerializationCategory.LOGIC).addChild(name);
	}

	public final CollectedDataNode getLogic(String name) {
		return getCategory(SerializationCategory.LOGIC).getChildren().get(name);
	}

	public final CollectedDataNode addSearch(String name) {
		return getCategory(SerializationCategory.SEARCH).addChild(name);
	}

	public final CollectedDataNode getSearch(String name) {
		return getCategory(SerializationCategory.SEARCH).getChildren().get(name);
	}

	public final CollectedDataNode addPattern(String name) {
		return getCategory(SerializationCategory.PATTERN).addChild(name);
	}

	public final CollectedDataNode getPattern(String name) {
		return getCategory(SerializationCategory.PATTERN).getChildren().get(name);
	}

	private String modelName;
	private final CollectedDataNode root = new CollectedDataNode("root", null);
}
