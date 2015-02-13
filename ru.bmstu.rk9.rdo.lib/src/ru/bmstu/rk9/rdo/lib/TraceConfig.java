package ru.bmstu.rk9.rdo.lib;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class TraceConfig
{
	public class TraceNode
	{
		public TraceNode(String name, TraceNode parent)
		{
			this(name, parent, false);
		}

		public TraceNode(String name, TraceNode parent, boolean traceState)
		{
			this.name = name;
			this.parent = parent;
			this.traceState = traceState;
		}

		public final TraceNode addChild(String name)
		{
			return addChild(name, false);
		}

		public final TraceNode addChild(String name, boolean traceState)
		{
			final int number = findName(name);
			if(number != -1)
			{
				TraceNode child = children.get(number);
				child.isVisible = true;
				return child;
			}

			TraceNode child = new TraceNode(name, this, traceState);
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

		public final String getName()
		{
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

		private final TraceNode parent;
		private final String name;
		private boolean isVisible = true;
		private boolean traceState = false;
		private final List<TraceNode> children =
			new ArrayList<TraceNode>();
	}

	private final TraceNode root = new TraceNode("root", null);

	public final TraceNode getRoot()
	{
		return root;
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

	private final void fillNames(TraceNode node) {
		for (TraceNode child : node.getVisibleChildren()) {
			if (child.isTraced())
				names.add(child.getName());
			fillNames(child);
		}
	}
}
