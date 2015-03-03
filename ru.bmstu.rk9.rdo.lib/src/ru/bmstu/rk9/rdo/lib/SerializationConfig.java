package ru.bmstu.rk9.rdo.lib;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class SerializationConfig {
	public class SerializationNode {
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

		public final SerializationNode copy() {
			SerializationNode node = new SerializationNode(this.name,
					this.parent, this.isSerialized, this.isModel);
			node.showFullName = this.showFullName;
			node.isVisible = this.isVisible;
			for (SerializationNode child : this.children) {
				node.children.add(child.copy());
			}
			return node;
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

		public final String getName() {
			return name;
		}

		public final String getRelativeName() {
			if (!isModel)
				return SerializationConfig.getRelativeElementName(name);
			else if (!showFullName)
				return SerializationConfig.getRelativeModelName(name);
			return name;
		}

		public final String getModelName() {
			if (isModel)
				return SerializationConfig.getRelativeModelName(name);
			return SerializationConfig.getNameOfElementModel(name);
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

		public final void toFinalModelTree(String modelName) {
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

	public final List<SerializationNode> findModelsWithSameName(String modelName) {
		List<SerializationNode> models = new ArrayList<SerializationNode>();
		for (SerializationNode c : root.getVisibleChildren())
			if (getRelativeModelName(c.getName()).equals(
					getRelativeModelName(modelName)))
				models.add(c);
		return models;
	}

	public final SerializationNode findModel(String modelName) {
		for (SerializationNode c : root.getVisibleChildren())
			if (c.getName().equals(modelName))
				return c;
		return null;
	}

	public final void removeModel(SerializationNode modelNode) {
		root.children.remove(modelNode);
	}

	private static List<String> names = new ArrayList<String>();

	public static final List<String> getNames() {
		return Collections.unmodifiableList(names);
	}

	public final void initNames() {
		names.clear();
		for (SerializationNode category : root.getVisibleChildren())
			fillNames(category);
	}

	private final void fillNames(final SerializationNode node) {
		for (SerializationNode child : node.getVisibleChildren()) {
			if (child.isSerialized())
				names.add(child.getName());
			fillNames(child);
		}
	}

	private static final String getRelativeModelName(final String name) {
		return name.substring(name.lastIndexOf('/') + 1, name.lastIndexOf('.'));
	}

	private static final String getRelativeElementName(final String name) {
		return name.substring(name.indexOf('.') + 1);
	}

	private static final String getNameOfElementModel(final String name) {
		return name.substring(0, name.indexOf('.'));
	}
}
