package rdo.game5;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
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

		final GridLayout gridLayout = new GridLayout(3, false);
		parent.setLayout(gridLayout);

		final Group solvableGroup = new Group(parent, SWT.SHADOW_IN);
		solvableGroup.setText("Solvable:");
		solvableGroup.setLayout(gridLayout);

		final Button solvable = new Button(solvableGroup, SWT.CHECK);
		solvable.setText("Solvable only");

		final Button inOrder = new Button(parent, SWT.PUSH);
		inOrder.setText("In order");

		final Group ruleCost = new Group(parent, SWT.SHADOW_IN);
		ruleCost.setText("Rule cost:");
		final GridLayout ruleCostLayout = new GridLayout(4, false);
		ruleCost.setLayout(ruleCostLayout);
		final GridData ruleCostData = new GridData();
		ruleCostData.verticalSpan = 3;
		ruleCost.setLayoutData(ruleCostData);

		final Label moveLabel = new Label(ruleCost, SWT.NONE);
		moveLabel.setText("Move");

		final Label computeLabel = new Label(ruleCost, SWT.NONE);
		computeLabel.setText("Compute");

		final Label costLabel = new Label(ruleCost, SWT.NONE);
		costLabel.setText("Cost");
		final GridData costLabelData = new GridData(SWT.FILL, SWT.BEGINNING,
				true, false);
		costLabelData.horizontalSpan = 2;
		costLabel.setLayoutData(costLabelData);

		final Label leftLabel = new Label(ruleCost, SWT.NONE);
		leftLabel.setText("Left");

		final Combo leftCombo = new Combo(ruleCost, SWT.BORDER | SWT.DROP_DOWN
				| SWT.V_SCROLL | SWT.READ_ONLY);
		leftCombo.add("after");
		leftCombo.add("before");
		leftCombo.select(0);

		final Button leftButton = new Button(ruleCost, SWT.CHECK);
		final Text leftCost = new Text(ruleCost, SWT.BORDER);
		leftCost.setEnabled(false);

		leftButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (leftButton.getSelection()) {
					leftCost.setEnabled(true);
				} else {
					leftCost.setEnabled(false);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		final Label rightLabel = new Label(ruleCost, SWT.NONE);
		rightLabel.setText("Right");

		final Combo rightCombo = new Combo(ruleCost, SWT.BORDER | SWT.DROP_DOWN
				| SWT.V_SCROLL | SWT.READ_ONLY);
		rightCombo.add("after");
		rightCombo.add("before");
		rightCombo.select(0);

		final Button rightButton = new Button(ruleCost, SWT.CHECK);
		final Text rightCost = new Text(ruleCost, SWT.BORDER);
		rightCost.setEnabled(false);

		rightButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (rightButton.getSelection()) {
					rightCost.setEnabled(true);
				} else {
					rightCost.setEnabled(false);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		final Label upLabel = new Label(ruleCost, SWT.NONE);
		upLabel.setText("Up");

		final Combo upCombo = new Combo(ruleCost, SWT.BORDER | SWT.DROP_DOWN
				| SWT.V_SCROLL | SWT.READ_ONLY);
		upCombo.add("after");
		upCombo.add("before");
		upCombo.select(0);

		final Button upButton = new Button(ruleCost, SWT.CHECK);
		final Text upCost = new Text(ruleCost, SWT.BORDER);
		upCost.setEnabled(false);

		upButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (upButton.getSelection()) {
					upCost.setEnabled(true);
				} else {
					upCost.setEnabled(false);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		final Label downLabel = new Label(ruleCost, SWT.NONE);
		downLabel.setText("Down");

		final Combo downCombo = new Combo(ruleCost, SWT.BORDER | SWT.DROP_DOWN
				| SWT.V_SCROLL | SWT.READ_ONLY);
		downCombo.add("after");
		downCombo.add("before");
		downCombo.select(0);

		final Group traverseGraph = new Group(parent, SWT.SHADOW_IN);
		traverseGraph.setText("Traverse graph:");
		traverseGraph.setLayout(gridLayout);

		final Button compareTops = new Button(traverseGraph, SWT.CHECK);
		compareTops.setText("Compare tops");

		final Button shuffle = new Button(parent, SWT.PUSH);
		shuffle.setText("Shuffle");

		final Button downButton = new Button(ruleCost, SWT.CHECK);
		final Text downCost = new Text(ruleCost, SWT.BORDER);
		downCost.setEnabled(false);

		downButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (downButton.getSelection()) {
					downCost.setEnabled(true);
				} else
					downCost.setEnabled(false);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		final Group heuristicSelection = new Group(parent, SWT.SHADOW_IN);
		heuristicSelection.setText("Select heuristic:");
		heuristicSelection.setLayout(gridLayout);
		final Combo heuristicList = new Combo(heuristicSelection, SWT.BORDER
				| SWT.DROP_DOWN | SWT.V_SCROLL);

		final Button setSituation = new Button(parent, SWT.PUSH);
		setSituation.setText("Set...");

		setSituation.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				final Display display = PlatformUI.getWorkbench().getDisplay();
				final Shell shell = new Shell(display);
				shell.setText("Set situation");
				shell.setLayout(new FormLayout());
				shell.setLayout(gridLayout);

				final Text setText = new Text(shell, SWT.BORDER);
				shell.setSize(180, 80);
				shell.open();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		final Group heuristicCodeGroup = new Group(parent, SWT.SHADOW_IN);
		heuristicCodeGroup.setText("Heuristic code:");
		heuristicCodeGroup.setLayout(gridLayout);
		final Text heuristicCode = new Text(heuristicCodeGroup, SWT.MULTI
				| SWT.V_SCROLL);
		final GridData heuristicCodeData = new GridData(SWT.FILL, SWT.FILL,
				true, true, 1, 1);
		heuristicCodeData.horizontalSpan = 3;
		heuristicCodeGroup.setLayoutData(heuristicCodeData);
		heuristicCode.setLayoutData(heuristicCodeData);
	}

	@Override
	public void setFocus() {
	}
}
