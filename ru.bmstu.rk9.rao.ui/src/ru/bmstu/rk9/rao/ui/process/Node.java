package ru.bmstu.rk9.rao.ui.process;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.geometry.Rectangle;

public class Node {

	private String name;
	private Rectangle layout;
	private List<Node> children;
	private Node parent;

	public Node() {
		this.name = "Unknown";
		this.layout = new Rectangle(10, 10, 100, 100);
		this.children = new ArrayList<Node>();
		this.parent = null;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public void setLayout(Rectangle layout) {
		this.layout = layout;
	}

	public Rectangle getLayout() {
		return this.layout;
	}

	public boolean addChild(Node child) {
		child.setParent(this);
		return this.children.add(child);
	}

	public boolean removeChild(Node child) {
		return this.children.remove(child);
	}

	public List<Node> getChildrenArray() {
		return this.children;
	}

	public void setParent(Node parent) {
		this.parent = parent;
	}

	public Node getParent() {
		return this.parent;
	}
}
