package ru.bmstu.rk9.rao.ui.serialization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import ru.bmstu.rk9.rao.lib.database.SerializationObjectsNames;
import ru.bmstu.rk9.rao.lib.naming.NamingHelper;

public class SerializationConfig {
	public static class SerializationNode {
		public SerializationNode(final String name,
				final SerializationNode parent) {
			this(name, parent, false, false);
		}

		public SerializationNode(final String name,
				final SerializationNode parent, boolean serializationState) {
			this(name, parent, serializationState, false);
		}

		public SerializationNode(final String name,
				final SerializationNode parent, boolean serializationState,
				boolean isModel) {
			this.name = name;
			this.parent = parent;
			this.isSerialized = serializationState;
			this.isModel = isModel;
		}

		public SerializationNode(final SerializationNode another) {
			this.name = another.name;
			this.parent = another.parent;
			this.isSerialized = another.isSerialized;
			this.isModel = another.isModel;
			this.showFullName = another.showFullName;
			this.isVisible = another.isVisible;
			for (SerializationNode child : another.children) {
				this.children.add(new SerializationNode(child));
			}
		}

		public final SerializationNode addChild(final String name) {
			return addChild(name, false, false);
		}

		public final SerializationNode addChild(final String name,
				boolean serializationState) {
			return addChild(name, serializationState, false);
		}

		public final SerializationNode addChild(final String name,
				boolean serializationState, boolean isModel) {
			final int number = findName(name);
			if (number != -1) {
				SerializationNode child = children.get(number);
				child.isVisible = true;
				return child;
			}

			SerializationNode child = new SerializationNode(name, this,
					serializationState, isModel);
			children.add(child);
			return child;
		}

		private final int findName(final String name) {
			for (int i = 0; i < children.size(); i++) {
				SerializationNode ch = children.get(i);
				if (!ch.isVisible)
					if (ch.name.equals(name))
						return i;
			}
			return -1;
		}

		public boolean hasChildren() {
			return !children.isEmpty();
		}

		public final String getFullName() {
			return name;
		}

		public final String getName() {
			if (!isModel)
				return NamingHelper.getRelativeElementName(name);
			else if (!showFullName)
				return NamingHelper.getRelativeModelName(name);
			return name;
		}

		public final String getModelName() {
			if (isModel)
				return NamingHelper.getRelativeModelName(name);
			return NamingHelper.getNameOfElementModel(name);
		}

		public final boolean isSerialized() {
			return isSerialized;
		}

		public final void setSerializationState(boolean serializationState) {
			this.isSerialized = serializationState;
		}

		public final void setSerializeVisibleChildren(boolean serializationState) {
			for (SerializationNode ch : getVisibleChildren()) {
				if (ch.isVisible) {
					ch.setSerializeVisibleChildren(serializationState);
					ch.isSerialized = serializationState;
				}
			}
		}

		public final List<SerializationNode> getVisibleChildren() {
			List<SerializationNode> visibleChildren = new ArrayList<SerializationNode>();
			for (SerializationNode ch : children)
				if (ch.isVisible)
					visibleChildren.add(ch);
			return Collections.unmodifiableList(visibleChildren);
		}

		public final void removeHiddenChildren() {
			Iterator<SerializationNode> it = children.iterator();
			while (it.hasNext()) {
				SerializationNode child = it.next();
				if (!child.isVisible) {
					child.children.clear();
					it.remove();
				} else {
					child.removeHiddenChildren();
				}
			}
		}

		public final void toFinalModelTree(final String modelName) {
			Iterator<SerializationNode> it = children.iterator();
			while (it.hasNext()) {
				SerializationNode child = it.next();
				if (!child.getModelName().equals(modelName)
						|| (!child.isSerialized && !child.isModel)) {
					child.children.clear();
					it.remove();
				} else {
					child.toFinalModelTree(modelName);
				}
			}
		}

		public final void hideChildren() {
			for (SerializationNode child : children) {
				child.hideChildren();
				child.isVisible = false;
			}
		}

		public final SerializationNode getParent() {
			return parent;
		}

		public final void mustShowFullName(boolean showFullName) {
			this.showFullName = showFullName;
		}

		public final boolean usesFullName() {
			return showFullName;
		}

		private final SerializationNode parent;
		private final String name;
		private boolean isVisible = true;
		private boolean isSerialized = false;
		private final List<SerializationNode> children = new ArrayList<SerializationNode>();
		private boolean showFullName = false;
		private boolean isModel = false;
	}

	private final SerializationNode root = new SerializationNode("root", null);

	public final SerializationNode getRoot() {
		return root;
	}

	public final void clear() {
		root.children.clear();
	}

	public final List<SerializationNode> findModelsWithSameName(
			final String modelName) {
		List<SerializationNode> models = new ArrayList<SerializationNode>();
		for (SerializationNode c : root.getVisibleChildren())
			if (NamingHelper.getRelativeModelName(c.getFullName()).equals(
					NamingHelper.getRelativeModelName(modelName)))
				models.add(c);
		return models;
	}

	public final SerializationNode findModel(final String modelName) {
		for (SerializationNode c : root.getVisibleChildren())
			if (c.getFullName().equals(modelName))
				return c;
		throw new SerializationException("Model name \"" + modelName
				+ "\" not found");
	}

	public final void removeModel(final SerializationNode modelNode) {
		root.children.remove(modelNode);
	}

	public final void saveTreeAsNamesList() {
		List<String> names = new ArrayList<String>();
		for (SerializationNode category : root.getVisibleChildren())
			fillNames(category, names);
		SerializationObjectsNames.set(names);
	}

	private final void fillNames(final SerializationNode node,
			List<String> names) {
		for (SerializationNode child : node.getVisibleChildren()) {
			if (child.isSerialized())
				names.add(child.getFullName());
			fillNames(child, names);
		}
	}
}
