package rdo.game5;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

public class Game5View extends EditorPart {
	public static final String ID = "rdo-game5.Game5View";

	@Override
	public void doSave(IProgressMonitor arg0) {
		
	}

	@Override
	public void doSaveAs() {
		
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setSite(site);
		setInput(input);
		
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void createPartControl(Composite parent) {
		
		GridLayout gridLayout = new GridLayout(3, false);
		parent.setLayout(gridLayout);
		gridLayout.verticalSpacing = 100;
		gridLayout.horizontalSpacing = 50;
		
		Label heuristicSelection = new Label(parent, SWT.NONE);
		heuristicSelection.setText("Select heuristic:");
		Label traverseGraph = new Label(parent, SWT.NONE);
		traverseGraph.setText("Traverse graph:");
		Label compareTops = new Label(parent, SWT.NONE);
		compareTops.setText("Compare tops");
		Label ruleCost = new Label(parent, SWT.NONE);
		ruleCost.setText("Rule cost:");
		
		Combo heuristicList = new Combo(parent, SWT.BORDER | SWT.DROP_DOWN | SWT.V_SCROLL);
		
		Text heuristicCode = new Text(parent, SWT.BORDER | SWT.V_SCROLL);
		
	}

	@Override
	public void setFocus() {
		
	}

	
}
