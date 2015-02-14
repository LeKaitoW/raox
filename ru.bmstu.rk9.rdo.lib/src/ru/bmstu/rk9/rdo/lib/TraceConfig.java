package ru.bmstu.rk9.rdo.lib;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class TraceConfig
{
	public class TraceNode
	{
		public TraceNode(String name, TraceNode parent) {
			this(name, parent, false, false);
		}

		public TraceNode(String name, TraceNode parent, boolean traceState) {
			this(name, parent, traceState, false);
		}

		public TraceNode(String name, TraceNode parent, boolean traceState,
				boolean isModel) {
			this.name = name;
			this.parent = parent;
			this.traceState = traceState;
			this.isModel = isModel;
		}

		public final TraceNode addChild(String name) {
			return addChild(name, false, false);
		}

		public final TraceNode addChild(String name, boolean traceState) {
			return addChild(name, traceState, false);
		}

		public final TraceNode addChild(String name, boolean traceState,
				boolean isModel) {
			final int number = findName(name);
			if (number != -1) {
				TraceNode child = children.get(number);
				child.isVisible = true;
				return child;
			}

			TraceNode child = new TraceNode(name, this, traceState, isModel);
			children.add(child);
			return child;
		}

		private final int findName(String name)
		{
			for(int i = 0; i < children.size(); i++)
			{
				TraceNode ch = children.get(i);
				if(!ch.isVisible)
					if(ch.name.equals(name))
						return i;
			}
			return -1;
		}

		public boolean hasChildren()
		{
			return !children.isEmpty();
		}

		public final String getName() {
			return name;
		}

		public final String getRelativeName() {
			if (isModel && !showFullName)
				return TraceConfig.getRelativeModelName(name);
			return name;
		}

		public final boolean isTraced()
		{
			return traceState;
		}

		public final void setTraceState(boolean traceState)
		{
			this.traceState = traceState;
		}

		public final void traceVisibleChildren(boolean traceState)
		{
			for(TraceNode ch : getVisibleChildren())
			{
				if(ch.isVisible)
				{
					ch.traceVisibleChildren(traceState);
					ch.traceState = traceState;
				}
			}
		}

		public final List<TraceNode> getVisibleChildren()
		{
			List<TraceNode> visibleChildren = new ArrayList<TraceNode>();
			for(TraceNode ch : children)
				if(ch.isVisible)
					visibleChildren.add(ch);
			return Collections.unmodifiableList(visibleChildren);
		}

		public final void removeHiddenChildren()
		{
			Iterator<TraceNode> it = children.iterator();
			while(it.hasNext())
			{
				TraceNode child = it.next();
				if(!child.isVisible)
				{
					child.children.clear();
					it.remove();
				}
				else
				{
					child.removeHiddenChildren();
				}
			}
		}

		public final void hideChildren()
		{
			for(TraceNode child : children)
			{
				child.hideChildren();
				child.isVisible = false;
			}
		}

		public final TraceNode getParent()
		{
			return parent;
		}

		public final void mustShowFullName(boolean showFullName) {
			this.showFullName = showFullName;
		}

		public final boolean usesFullName() {
			return showFullName;
		}

		private final TraceNode parent;
		private final String name;
		private boolean isVisible = true;
		private boolean traceState = false;
		private final List<TraceNode> children =
			new ArrayList<TraceNode>();
		private boolean showFullName = false;
		private boolean isModel = false;
	}

	private final TraceNode root = new TraceNode("root", null);

	public final TraceNode getRoot()
	{
		return root;
	}

	public final List<TraceNode> findModelsWithSameName(String modelName) {
		List<TraceNode> models = new ArrayList<TraceNode>();
		for (TraceNode c : root.getVisibleChildren())
			if (getRelativeModelName(c.getName())
					.equals(getRelativeModelName(modelName)))
				models.add(c);
		return models;
	}

	public final TraceNode findModel(String modelName) {
		for (TraceNode c : root.getVisibleChildren())
			if (c.getName().equals(modelName))
				return c;
		return null;
	}

	public final void removeModel(TraceNode modelNode) {
		root.children.remove(modelNode);
	}

	private static List<String> names = new ArrayList<String>();

	public static final List<String> getNames() {
		return Collections.unmodifiableList(names);
	}

	public final void initNames() {
		names.clear();
		for (TraceNode category : root.getVisibleChildren())
			fillNames(category);
	}

	private final void fillNames(final TraceNode node) {
		for (TraceNode child : node.getVisibleChildren()) {
			if (child.isTraced())
				names.add(child.getName());
			fillNames(child);
		}
	}

	private static final String getRelativeModelName(final String name) {
		return name.substring(name.lastIndexOf('/') + 1);
	}
}
