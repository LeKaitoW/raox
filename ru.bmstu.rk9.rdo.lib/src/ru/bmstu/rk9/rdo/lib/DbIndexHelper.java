package ru.bmstu.rk9.rdo.lib;

import ru.bmstu.rk9.rdo.lib.Database.SerializationCategory;

public class DbIndexHelper implements Subscriber {
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
		return getCategory(SerializationCategory.RESOURCES).addChild(name);
	}

	public final CollectedDataNode getResourceType(String name) {
		return getCategory(SerializationCategory.RESOURCES).getChildren().get(
				name);
	}

	public final CollectedDataNode addResult(String name) {
		return getCategory(SerializationCategory.RESULTS).addChild(name);
	}

	public final CollectedDataNode getResult(String name) {
		return getCategory(SerializationCategory.RESULTS).getChildren().get(
				name);
	}

	public final CollectedDataNode addPattern(String name) {
		return getCategory(SerializationCategory.PATTERNS).addChild(name);
	}

	public final CollectedDataNode getPattern(String name) {
		return getCategory(SerializationCategory.PATTERNS).getChildren().get(
				name);
	}

	public final CollectedDataNode addDecisionPoint(String name) {
		return getCategory(SerializationCategory.DECISION_POINTS)
				.addChild(name);
	}

	public final CollectedDataNode getDecisionPoint(String name) {
		return getCategory(SerializationCategory.DECISION_POINTS).getChildren()
				.get(name);
	}

	public final CollectedDataNode addSearch(String name) {
		return getCategory(SerializationCategory.SEARCH).addChild(name);
	}

	public final CollectedDataNode getSearch(String name) {
		return getCategory(SerializationCategory.SEARCH).getChildren()
				.get(name);
	}

	private String modelName;
	private final CollectedDataNode root = new CollectedDataNode("root", null);

	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //
	// -----------------------NOTIFICATION SYSTEM -------------------------- //
	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //

	// TODO unify with Tracer notification system

	private boolean paused = true;

	public final synchronized void setPaused(boolean paused) {
		if (this.paused == paused)
			return;

		this.paused = paused;
		fireChange();
	}

	private Subscriber realTimeSubscriber = null;

	public final void setRealTimeSubscriber(Subscriber subscriber) {
		this.realTimeSubscriber = subscriber;
	}

	private final void notifyRealTimeSubscriber() {
		if (realTimeSubscriber != null)
			realTimeSubscriber.fireChange();
	}

	private Subscriber commonSubscriber = null;

	public final void setCommonSubscriber(Subscriber subscriber) {
		this.commonSubscriber = subscriber;
	}

	public final void notifyCommonSubscriber() {
		if (commonSubscriber != null)
			commonSubscriber.fireChange();
	}

	@Override
	public void fireChange() {
		if (paused)
			return;

		notifyRealTimeSubscriber();
	}
}
