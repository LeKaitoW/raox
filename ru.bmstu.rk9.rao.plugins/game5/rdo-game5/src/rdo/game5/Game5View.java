package rdo.game5;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
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
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.xtext.ui.editor.embedded.EmbeddedEditor;
import org.eclipse.xtext.ui.editor.embedded.EmbeddedEditorFactory;
import org.eclipse.xtext.ui.editor.embedded.EmbeddedEditorModelAccess;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.inject.Injector;

@SuppressWarnings("restriction")
public class Game5View extends EditorPart {

	protected static boolean dirty = false;
	public static final String ID = "rdo-game5.Game5View";
	private static EmbeddedEditorModelAccess editor;
	private static JSONObject object;

	@SuppressWarnings("unchecked")
	@Override
	public void createPartControl(Composite parent) {
		try {
			JSONParser parser = new JSONParser();
			object = (JSONObject) parser.parse(new FileReader(
					Game5ProjectConfigurator.getConfigFilePath().toString()));

		} catch (IOException | ParseException e1) {
			e1.printStackTrace();
		}

		final GridLayout gridLayout = new GridLayout(3, false);
		parent.setLayout(gridLayout);

		final Group solvableGroup = new Group(parent, SWT.SHADOW_IN);
		solvableGroup.setText("Solvable:");
		solvableGroup.setLayout(gridLayout);

		final Button solvable = new Button(solvableGroup, SWT.CHECK);
		solvable.setText("Solvable only");
		solvable.setSelection((boolean) object.get("solvable"));

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
		leftCombo.add("After");
		leftCombo.add("Before");
		leftCombo.select(0);

		final Button leftButton = new Button(ruleCost, SWT.CHECK);
		final Text leftCost = new Text(ruleCost, SWT.BORDER);
		leftCost.setText(object.get("costLeft").toString());
		leftCost.setEnabled((boolean) object.get("enableLeft"));
		leftButton.setSelection((boolean) object.get("enableLeft"));

		leftButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				setDirty(true);
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
		rightCombo.add("After");
		rightCombo.add("Before");
		rightCombo.select(0);

		final Button rightButton = new Button(ruleCost, SWT.CHECK);
		final Text rightCost = new Text(ruleCost, SWT.BORDER);
		rightCost.setText(object.get("costRight").toString());
		rightCost.setEnabled((boolean) object.get("enableRight"));
		rightButton.setSelection((boolean) object.get("enableRight"));

		rightButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				setDirty(true);
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
		upCombo.add("After");
		upCombo.add("Before");
		upCombo.select(0);

		final Button upButton = new Button(ruleCost, SWT.CHECK);
		final Text upCost = new Text(ruleCost, SWT.BORDER);
		upCost.setText(object.get("costUp").toString());
		upCost.setEnabled((boolean) object.get("enableUp"));
		upButton.setSelection((boolean) object.get("enableUp"));

		upButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				setDirty(true);
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
		downCombo.add("After");
		downCombo.add("Before");
		downCombo.select(0);

		final Group traverseGraph = new Group(parent, SWT.SHADOW_IN);
		traverseGraph.setText("Traverse graph:");
		traverseGraph.setLayout(gridLayout);

		final Button compareTops = new Button(traverseGraph, SWT.CHECK);
		compareTops.setText("Compare tops");
		compareTops.setSelection((boolean) object.get("compare"));

		final Button shuffle = new Button(solvableGroup, SWT.PUSH);
		shuffle.setText("Shuffle");

		final Button downButton = new Button(ruleCost, SWT.CHECK);
		final Text downCost = new Text(ruleCost, SWT.BORDER);
		downCost.setText(object.get("costDown").toString());
		downCost.setEnabled((boolean) object.get("enableDown"));
		downButton.setSelection((boolean) object.get("enableDown"));

		downButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				setDirty(true);
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
				setDirty(true);
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

		final Button simulationButton = new Button(parent, SWT.PUSH);
		simulationButton.setText("Simulation");

		final Group editorGroup = new Group(parent, SWT.SHADOW_IN);
		editorGroup.setText("Heuristic code:");
		editorGroup.setLayout(gridLayout);
		final GridData editorGrideData = new GridData(SWT.FILL, SWT.FILL, true,
				true, 1, 1);
		editorGrideData.horizontalSpan = 3;
		editorGroup.setLayoutData(editorGrideData);

		final Injector injector = ru.bmstu.rk9.rao.ui.RaoActivatorExtension
				.getInstance()
				.getInjector(
						ru.bmstu.rk9.rao.ui.RaoActivatorExtension.RU_BMSTU_RK9_RAO_RAO);
		final EmbeddedEditorFactory factory = injector
				.getInstance(EmbeddedEditorFactory.class);
		final EditedResourceProvider resourceProvider = injector
				.getInstance(EditedResourceProvider.class);
		final EmbeddedEditor embeddedEditor = factory
				.newEditor(resourceProvider).showErrorAndWarningAnnotations()
				.withParent(editorGroup);
		editor = embeddedEditor.createPartialEditor();

		heuristicList.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				object.put("heuristic", heuristicList.getSelectionIndex());
				setDirty(true);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});
		leftCost.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent arg0) {
				object.put("costLeft", leftCost.getText());
				setDirty(true);
			}

			@Override
			public void keyPressed(KeyEvent arg0) {
			}
		});

		leftCombo.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				object.put("computeLeft", leftCombo.getText());
				setDirty(true);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		rightCost.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent arg0) {
				object.put("costRight", rightCost.getText());
				setDirty(true);
			}

			@Override
			public void keyPressed(KeyEvent arg0) {
			}
		});

		rightCombo.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				object.put("computeRight", rightCombo.getText());
				setDirty(true);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		upCost.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent arg0) {
				object.put("costUp", upCost.getText());
				setDirty(true);
			}

			@Override
			public void keyPressed(KeyEvent arg0) {
			}
		});

		upCombo.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				object.put("computeUp", upCombo.getText());
				setDirty(true);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		downCost.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent arg0) {
				object.put("costDown", downCost.getText());
				setDirty(true);
			}

			@Override
			public void keyPressed(KeyEvent arg0) {
			}
		});

		downCombo.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				object.put("computeDown", downCombo.getText());
				setDirty(true);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		solvable.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				object.put("solvable", solvable.getSelection());
				setDirty(true);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		compareTops.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				object.put("compare", compareTops.getSelection());
				setDirty(true);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		simulationButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				try {
					Game5ProjectConfigurator.addHeuristicCode();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent event) {
			}
		});
	}

	@Override
	public void setFocus() {
	}

	@Override
	public void doSave(IProgressMonitor arg0) {
		setDirty(false);
		try {
			OutputStream outputStream = new FileOutputStream(new File(
					Game5ProjectConfigurator.getConfigFilePath().toString()));
			PrintStream printStream = new PrintStream(outputStream, true,
					StandardCharsets.UTF_8.name());
			printStream.print(object.toString());
			printStream.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setInput(input);
		setSite(site);
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	public static final EmbeddedEditorModelAccess getEditor() {
		return editor;
	}

	public static final JSONObject getConfigurations() {
		return object;
	}

	protected void setDirty(boolean value) {
		dirty = value;
		firePropertyChange(IEditorPart.PROP_DIRTY);
	}
}
