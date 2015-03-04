package ru.bmstu.rk9.rdo.ui.contributions;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import ru.bmstu.rk9.rdo.lib.SerializationConfig.SerializationNode;

public class RDOSerializedObjectsView extends ViewPart {

	private static TreeViewer serializedObjectsTreeViewer;

	@Override
	public void createPartControl(Composite parent) {
		serializedObjectsTreeViewer = new TreeViewer(parent);
		Tree serializedObjectsTree = serializedObjectsTreeViewer.getTree();
		serializedObjectsTree.setLayoutData(new GridLayout());
		serializedObjectsTree.setLinesVisible(true);

		serializedObjectsTreeViewer.setContentProvider(
				new RDOSerializedObjectsContentProvider());
		serializedObjectsTreeViewer.setLabelProvider(
				new RDOSerializedObjectsLabelProvider());
	}

	public static void setTree(SerializationNode root, String modelName) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				SerializationNode newRoot = new SerializationNode(root);
				newRoot.toFinalModelTree(modelName);
				serializedObjectsTreeViewer.setInput(newRoot);
			}
		});
	}

	@Override
	public void setFocus() {
	}
}

class RDOSerializedObjectsContentProvider implements ITreeContentProvider {

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	@Override
	public Object[] getElements(Object inputElement) {
		SerializationNode root = (SerializationNode) inputElement;
		if (!root.hasChildren())
			return new Object[]{};
		return root.getVisibleChildren().toArray();
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		SerializationNode parent = (SerializationNode) parentElement;
		if (!parent.hasChildren())
			return new Object[]{};
		return parent.getVisibleChildren().toArray();
	}

	@Override
	public Object getParent(Object element) {
		SerializationNode serializationNode = (SerializationNode) element;
		return serializationNode.getParent();
	}

	@Override
	public boolean hasChildren(Object element) {
		SerializationNode serializationNode = (SerializationNode) element;
		return serializationNode.hasChildren();
	}
}

class RDOSerializedObjectsLabelProvider implements ILabelProvider {

	@Override
	public void addListener(ILabelProviderListener listener) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
	}

	@Override
	public Image getImage(Object element) {
		return null;
	}

	@Override
	public String getText(Object element) {
		SerializationNode serializationNode = (SerializationNode) element;
		return serializationNode.getRelativeName();
	}
}