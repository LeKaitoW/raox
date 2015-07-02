package rdo.game5;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
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

		Group solvableGroup = new Group(parent, SWT.SHADOW_IN);
		solvableGroup.setText("Solvable:");
		solvableGroup.setLayout(gridLayout);

		Button solvable = new Button(solvableGroup, SWT.CHECK);
		solvable.setText("Solvable only");

		Button inOrder = new Button(parent, SWT.PUSH);
		inOrder.setText("In order");

		Group ruleCost = new Group(parent, SWT.SHADOW_IN);
		ruleCost.setText("Rule cost:");
		GridLayout ruleCostLayout = new GridLayout(4, false);
		ruleCost.setLayout(ruleCostLayout);
		GridData ruleCostData = new GridData();
		ruleCostData.verticalSpan = 3;
		ruleCost.setLayoutData(ruleCostData);

		Label moveLabel = new Label(ruleCost, SWT.NONE);
		moveLabel.setText("Move");

		Label computeLabel = new Label(ruleCost, SWT.NONE);
		computeLabel.setText("Compute");

		Label costLabel = new Label(ruleCost, SWT.NONE);
		costLabel.setText("Cost");
		GridData costLabelData = new GridData(SWT.FILL, SWT.BEGINNING, true,
				false);
		costLabelData.horizontalSpan = 2;
		costLabel.setLayoutData(costLabelData);

		Label leftLabel = new Label(ruleCost, SWT.NONE);
		leftLabel.setText("Left");

		Combo leftCombo = new Combo(ruleCost, SWT.BORDER | SWT.DROP_DOWN
				| SWT.V_SCROLL | SWT.READ_ONLY);
		leftCombo.add("after");
		leftCombo.add("before");
		leftCombo.select(0);

		Button leftButton = new Button(ruleCost, SWT.CHECK);
		Text leftCost = new Text(ruleCost, SWT.BORDER);

		Label rightLabel = new Label(ruleCost, SWT.NONE);
		rightLabel.setText("Right");

		Combo rightCombo = new Combo(ruleCost, SWT.BORDER | SWT.DROP_DOWN
				| SWT.V_SCROLL | SWT.READ_ONLY);
		rightCombo.add("after");
		rightCombo.add("before");
		rightCombo.select(0);

		Button rightButton = new Button(ruleCost, SWT.CHECK);
		Text rightCost = new Text(ruleCost, SWT.BORDER);

		Label upLabel = new Label(ruleCost, SWT.NONE);
		upLabel.setText("Up");

		Combo upCombo = new Combo(ruleCost, SWT.BORDER | SWT.DROP_DOWN
				| SWT.V_SCROLL | SWT.READ_ONLY);
		upCombo.add("after");
		upCombo.add("before");
		upCombo.select(0);

		Button upButton = new Button(ruleCost, SWT.CHECK);
		Text upCost = new Text(ruleCost, SWT.BORDER);

		Label downLabel = new Label(ruleCost, SWT.NONE);
		downLabel.setText("Down");

		Combo downCombo = new Combo(ruleCost, SWT.BORDER | SWT.DROP_DOWN
				| SWT.V_SCROLL | SWT.READ_ONLY);
		downCombo.add("after");
		downCombo.add("before");
		downCombo.select(0);

		Group traverseGraph = new Group(parent, SWT.SHADOW_IN);
		traverseGraph.setText("Traverse graph:");
		traverseGraph.setLayout(gridLayout);

		Button compareTops = new Button(traverseGraph, SWT.CHECK);
		compareTops.setText("Compare tops");

		Button shuffle = new Button(parent, SWT.PUSH);
		shuffle.setText("Shuffle");

		Button downButton = new Button(ruleCost, SWT.CHECK);
		Text downCost = new Text(ruleCost, SWT.BORDER);

		Group heuristicSelection = new Group(parent, SWT.SHADOW_IN);
		heuristicSelection.setText("Select heuristic:");
		heuristicSelection.setLayout(gridLayout);
		Combo heuristicList = new Combo(heuristicSelection, SWT.BORDER
				| SWT.DROP_DOWN | SWT.V_SCROLL);

		Button setSituation = new Button(parent, SWT.PUSH);
		setSituation.setText("Set...");

		Group heuristicCodeGroup = new Group(parent, SWT.SHADOW_IN);
		heuristicCodeGroup.setText("Heuristic code:");
		heuristicCodeGroup.setLayout(gridLayout);
		Text heuristicCode = new Text(heuristicCodeGroup, SWT.MULTI
				| SWT.V_SCROLL);
		GridData heuristicCodeData = new GridData(SWT.FILL, SWT.FILL, true,
				true, 1, 1);
		heuristicCodeData.horizontalSpan = 3;
		heuristicCodeGroup.setLayoutData(heuristicCodeData);
		heuristicCode.setLayoutData(heuristicCodeData);

	}

	@Override
	public void setFocus() {

	}

}
