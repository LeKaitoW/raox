package rdo.game5;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.services.IServiceLocator;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.embedded.EmbeddedEditor;
import org.eclipse.xtext.ui.editor.embedded.EmbeddedEditorFactory;
import org.eclipse.xtext.ui.editor.embedded.EmbeddedEditorModelAccess;
import org.eclipse.xtext.ui.editor.model.IXtextDocument;
import org.eclipse.xtext.ui.editor.model.IXtextModelListener;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import ru.bmstu.rk9.rao.lib.notification.Subscriber;
import ru.bmstu.rk9.rao.lib.simulator.Simulator.ExecutionState;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorSubscriberManager;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorSubscriberManager.SimulatorSubscriberInfo;
import ru.bmstu.rk9.rao.ui.graph.GraphControl;
import ru.bmstu.rk9.rao.ui.graph.GraphControl.FrameInfo;
import com.google.inject.Injector;

@SuppressWarnings("restriction")
public class Game5View extends EditorPart {

	protected static boolean dirty = false;
	public static final String ID = "rdo-game5.Game5View";
	private static EmbeddedEditorModelAccess editor;
	private static JSONObject object;
	private final List<TileButton> tiles = new ArrayList<>();
	private final SimulatorSubscriberManager simulatorSubscriberManager = new SimulatorSubscriberManager();

	@SuppressWarnings("unchecked")
	@Override
	public void createPartControl(Composite parent) {
		try {
			final JSONParser parser = new JSONParser();
			final IPath workspacePath = ResourcesPlugin.getWorkspace()
					.getRoot().getLocation();
			final IFileEditorInput input = (IFileEditorInput) this
					.getEditorInput();
			final IFile configIFile = input.getFile();
			setPartName(configIFile.getProject().getName());
			object = (JSONObject) parser.parse(new FileReader(workspacePath
					.append(configIFile.getFullPath()).toString()));
		} catch (IOException | ParseException e1) {
			e1.printStackTrace();
		}

		final GridLayout gridLayout = new GridLayout(5, false);
		parent.setLayout(gridLayout);

		final Group boardGroup = new Group(parent, SWT.NONE);
		final GridLayout boardLayout = new GridLayout(3, false);
		final GridData boardData = new GridData();
		boardData.verticalSpan = 2;
		boardGroup.setLayoutData(boardData);
		boardGroup.setLayout(boardLayout);
		boardGroup.setText("Board:");
		JSONArray places = (JSONArray) object.get("places");
		for (int i = 1; i < 7; i++) {
			tiles.add(new TileButton(boardGroup, SWT.NONE, String
					.valueOf(places.indexOf(String.valueOf(i)) + 1)));
		}

		final Group solvableGroup = new Group(parent, SWT.SHADOW_IN);
		solvableGroup.setText("Solvable:");
		final GridData solvableData = new GridData();
		solvableData.verticalSpan = 2;
		solvableGroup.setLayoutData(solvableData);
		solvableGroup.setLayout(new GridLayout(1, false));
		final Button solvableOnly = new Button(solvableGroup, SWT.RADIO);
		solvableOnly.setText("Solvable only");
		final Button unsolvableOnly = new Button(solvableGroup, SWT.RADIO);
		unsolvableOnly.setText("Unsolvable only");
		final Button allSituations = new Button(solvableGroup, SWT.RADIO);
		allSituations.setText("All situations");

		if (object.get("solvable").equals("true")) {
			solvableOnly.setSelection(true);
		} else if (object.get("solvable").equals("false")) {
			unsolvableOnly.setSelection(true);
		} else {
			allSituations.setSelection(true);
		}

		final Button shuffle = new Button(solvableGroup, SWT.PUSH);
		shuffle.setText("Shuffle");

		final Group setOrderGroup = new Group(parent, SWT.NONE);
		setOrderGroup.setText("Set order:");
		setOrderGroup.setLayout(new FormLayout());
		final Button setOrderButton = new Button(setOrderGroup, SWT.TOGGLE);
		setOrderButton.setText("Set...");
		final Label setOrderError = new Label(setOrderGroup, SWT.NONE);
		setOrderError.setText("Invalid order");
		final Color red = new Color(PlatformUI.getWorkbench().getDisplay(),
				0x9B, 0x11, 0x1E);
		setOrderError.setForeground(red);
		setOrderError.setVisible(false);
		final Text setOrderText = new Text(setOrderGroup, SWT.BORDER);
		setOrderText.setText("1 2 3 4 5 6");
		setOrderText.setEnabled(false);
		final Button setOrderOkButton = new Button(setOrderGroup, SWT.PUSH);
		setOrderOkButton.setText("Ok");
		setOrderOkButton.setEnabled(false);
		FormData setOrderErrorData = new FormData();
		setOrderErrorData.left = new FormAttachment(setOrderButton, 2);
		setOrderErrorData.top = new FormAttachment(0, 5);
		setOrderError.setLayoutData(setOrderErrorData);
		FormData setOrderTextData = new FormData();
		setOrderTextData.top = new FormAttachment(setOrderButton, 2);
		setOrderText.setLayoutData(setOrderTextData);
		FormData setOrderOkButtonData = new FormData();
		setOrderOkButtonData.left = new FormAttachment(setOrderText, 2);
		setOrderOkButtonData.right = new FormAttachment(100, -2);
		setOrderOkButtonData.top = new FormAttachment(setOrderButton, 1);
		setOrderOkButton.setLayoutData(setOrderOkButtonData);

		final Group traverseGraph = new Group(parent, SWT.SHADOW_IN);
		traverseGraph.setText("Traverse graph:");
		traverseGraph.setLayout(gridLayout);
		final Button compareTops = new Button(traverseGraph, SWT.CHECK);
		compareTops.setText("Compare tops");
		compareTops.setSelection((boolean) object.get("compare"));

		final Group ruleCost = new Group(parent, SWT.SHADOW_IN);
		ruleCost.setText("Rule cost:");
		final GridLayout ruleCostLayout = new GridLayout(4, false);
		ruleCost.setLayout(ruleCostLayout);
		final GridData ruleCostData = new GridData();
		ruleCostData.verticalSpan = 2;
		ruleCost.setLayoutData(ruleCostData);

		final Button inOrder = new Button(parent, SWT.PUSH);
		inOrder.setText("In order");

		final Group heuristicSelection = new Group(parent, SWT.SHADOW_IN);
		heuristicSelection.setText("Select heuristic:");
		heuristicSelection.setLayout(gridLayout);
		final Combo heuristicList = new Combo(heuristicSelection, SWT.BORDER
				| SWT.DROP_DOWN | SWT.V_SCROLL);
		final String zeroHeuristic = "Поиск_в_ширину()";
		heuristicList.add(zeroHeuristic);
		final String tilesHeuristic = "Кол_во_фишек_не_на_месте()";
		heuristicList.add(tilesHeuristic);
		final String manhattanDistanceHeuristic = "Расстояния_фишек_до_мест()";
		heuristicList.add(manhattanDistanceHeuristic);
		heuristicList.setText((String) object.get("heuristic"));

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
		leftCombo.select(object.get("computeLeft").equals("After") ? 0 : 1);

		final Button leftButton = new Button(ruleCost, SWT.CHECK);
		final Text leftCost = new Text(ruleCost, SWT.BORDER);
		leftCost.setText(object.get("costLeft").toString());
		leftCost.setEnabled((boolean) object.get("enableLeft"));
		leftButton.setSelection((boolean) object.get("enableLeft"));
		leftButton.addSelectionListener(new CostButtonListener("enableLeft",
				leftButton, leftCost));

		final Label rightLabel = new Label(ruleCost, SWT.NONE);
		rightLabel.setText("Right");

		final Combo rightCombo = new Combo(ruleCost, SWT.BORDER | SWT.DROP_DOWN
				| SWT.V_SCROLL | SWT.READ_ONLY);
		rightCombo.add("After");
		rightCombo.add("Before");
		rightCombo.select(object.get("computeRight").equals("After") ? 0 : 1);

		final Button rightButton = new Button(ruleCost, SWT.CHECK);
		final Text rightCost = new Text(ruleCost, SWT.BORDER);
		rightCost.setText(object.get("costRight").toString());
		rightCost.setEnabled((boolean) object.get("enableRight"));
		rightButton.setSelection((boolean) object.get("enableRight"));
		rightButton.addSelectionListener(new CostButtonListener("enableRight",
				rightButton, rightCost));

		final Label upLabel = new Label(ruleCost, SWT.NONE);
		upLabel.setText("Up");

		final Combo upCombo = new Combo(ruleCost, SWT.BORDER | SWT.DROP_DOWN
				| SWT.V_SCROLL | SWT.READ_ONLY);
		upCombo.add("After");
		upCombo.add("Before");
		upCombo.select(object.get("computeUp").equals("After") ? 0 : 1);

		final Button upButton = new Button(ruleCost, SWT.CHECK);
		final Text upCost = new Text(ruleCost, SWT.BORDER);
		upCost.setText(object.get("costUp").toString());
		upCost.setEnabled((boolean) object.get("enableUp"));
		upButton.setSelection((boolean) object.get("enableUp"));
		upButton.addSelectionListener(new CostButtonListener("enableUp",
				upButton, upCost));

		final Label downLabel = new Label(ruleCost, SWT.NONE);
		downLabel.setText("Down");

		final Combo downCombo = new Combo(ruleCost, SWT.BORDER | SWT.DROP_DOWN
				| SWT.V_SCROLL | SWT.READ_ONLY);
		downCombo.add("After");
		downCombo.add("Before");
		downCombo.select(object.get("computeDown").equals("After") ? 0 : 1);

		final Button downButton = new Button(ruleCost, SWT.CHECK);
		final Text downCost = new Text(ruleCost, SWT.BORDER);
		downCost.setText(object.get("costDown").toString());
		downCost.setEnabled((boolean) object.get("enableDown"));
		downButton.setSelection((boolean) object.get("enableDown"));
		downButton.addSelectionListener(new CostButtonListener("enableDown",
				downButton, downCost));

		setOrderButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (setOrderButton.getSelection()) {
					setOrderText.setEnabled(true);
					setOrderOkButton.setEnabled(true);
					JSONArray places = (JSONArray) object.get("places");
					String order = OrderConfigurator
							.convertPlacesToString(places);
					setOrderText.setText(order);
				} else {
					setOrderText.setEnabled(false);
					setOrderOkButton.setEnabled(false);
					setOrderError.setVisible(false);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		setOrderText.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent arg0) {
			}

			@Override
			public void focusGained(FocusEvent arg0) {
				setOrderError.setVisible(false);
			}
		});

		setOrderOkButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				JSONArray places = OrderConfigurator
						.convertStringToPlaces(setOrderText.getText());
				if (places != null) {
					object.put("places", places);
					updateTiles();
					setDirty(true);
				} else {
					setOrderError.setVisible(true);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		final Group editorGroup = new Group(parent, SWT.SHADOW_IN);
		editorGroup.setText("Heuristic code:");
		editorGroup.setLayout(gridLayout);
		final GridData editorGridData = new GridData(SWT.FILL, SWT.FILL, true,
				true, 1, 1);
		editorGridData.horizontalSpan = 5;
		editorGroup.setLayoutData(editorGridData);

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
		editor = embeddedEditor.createPartialEditor("", object.get("code")
				.toString(), "", false);

		final Button simulationButton = new Button(parent, SWT.PUSH);
		simulationButton.setText("Simulation");
		final Button graphButton = new Button(parent, SWT.PUSH);
		graphButton.setText("Show graph");

		IXtextDocument document = embeddedEditor.getDocument();
		document.addModelListener(new IXtextModelListener() {
			@Override
			public void modelChanged(XtextResource resource) {
				setDirty(true);
			}
		});

		heuristicList.addSelectionListener(new ComboConfigurationListener(
				"heuristic", heuristicList));

		leftCost.addKeyListener(new CostKeyListener("costLeft", leftCost));
		leftCombo.addSelectionListener(new ComboConfigurationListener(
				"computeLeft", leftCombo));

		rightCost.addKeyListener(new CostKeyListener("costRight", rightCost));
		rightCombo.addSelectionListener(new ComboConfigurationListener(
				"computeRight", rightCombo));

		upCost.addKeyListener(new CostKeyListener("costUp", upCost));
		upCombo.addSelectionListener(new ComboConfigurationListener(
				"computeUp", upCombo));

		downCost.addKeyListener(new CostKeyListener("costDown", downCost));
		downCombo.addSelectionListener(new ComboConfigurationListener(
				"computeDown", downCombo));

		solvableOnly.addSelectionListener(new RadioConfigurationListener(
				"solvable", "true"));
		unsolvableOnly.addSelectionListener(new RadioConfigurationListener(
				"solvable", "false"));
		allSituations.addSelectionListener(new RadioConfigurationListener(
				"solvable", "all"));

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
				// need to check serialization objects tree
				IServiceLocator serviceLocator = PlatformUI.getWorkbench();
				doSave((IProgressMonitor) serviceLocator
						.getService(IProgressMonitor.class));
				ICommandService commandService = (ICommandService) serviceLocator
						.getService(ICommandService.class);
				Command command = commandService
						.getCommand("ru.bmstu.rk9.rao.ui.runtime.execute");
				try {
					command.executeWithChecks(new ExecutionEvent());
				} catch (ExecutionException | NotDefinedException
						| NotEnabledException | NotHandledException e) {
					e.printStackTrace();
				}
				simulatorSubscriberManager.initialize(Arrays
						.asList(new SimulatorSubscriberInfo(
								showGraphSubscriber,
								ExecutionState.EXECUTION_COMPLETED)));
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent event) {
			}
		});

		graphButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				GraphControl.openFrameWindow(new FrameInfo(0, "game5"));
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		shuffle.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				setOrderButton.setSelection(false);
				setOrderText.setEnabled(false);
				setOrderOkButton.setEnabled(false);
				JSONArray places = (JSONArray) object.get("places");
				object.put(
						"places",
						OrderConfigurator.shuffle(places,
								(String) object.get("solvable")));
				updateTiles();
				setDirty(true);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		inOrder.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				setOrderButton.setSelection(false);
				setOrderText.setEnabled(false);
				setOrderOkButton.setEnabled(false);
				OrderConfigurator.setInOrder(object);
				updateTiles();
				setDirty(true);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});
	}

	@Override
	public void setFocus() {
	}

	@SuppressWarnings("unchecked")
	@Override
	public void doSave(IProgressMonitor arg0) {
		object.put("code", editor.getEditablePart());
		try {
			IFile configIFile = (IFile) PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage()
					.getActiveEditor().getEditorInput().getAdapter(IFile.class);
			OutputStream outputStream = new FileOutputStream(configIFile
					.getRawLocation().toString());
			PrintStream printStream = new PrintStream(outputStream, true,
					StandardCharsets.UTF_8.name());
			printStream.print(object.toString());
			printStream.close();
			fillModelFile(configIFile);
			configIFile.getProject().refreshLocal(IResource.DEPTH_INFINITE,
					null);
		} catch (IOException | CoreException e) {
			e.printStackTrace();
		}
		setDirty(false);
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

	private static final void fillModelFile(IFile configIFile)
			throws IOException {
		final String modelTemplatePath = "/model_template/game_5.rao";
		final InputStream inputStream = Game5ProjectConfigurator.class
				.getClassLoader().getResourceAsStream(modelTemplatePath);
		OutputStream outputStream = null;

		try {
			InputStreamReader inputStreamReader = new InputStreamReader(
					inputStream, StandardCharsets.UTF_8.name());
			BufferedReader bufferedReader = new BufferedReader(
					inputStreamReader);
			outputStream = new FileOutputStream(configIFile.getLocation()
					.removeLastSegments(1).append("/game5.rao").toString());
			PrintStream printStream = new PrintStream(outputStream, true,
					StandardCharsets.UTF_8.name());

			String modelTemplateCode = bufferedReader.readLine();
			while (modelTemplateCode != null) {
				printStream.println(modelTemplateCode);
				modelTemplateCode = bufferedReader.readLine();
			}

			final String configuration = ConfigurationParser
					.parseConfig(configIFile);
			printStream.print(configuration);
			printStream.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		} finally {
			inputStream.close();
			if (outputStream != null) {
				outputStream.flush();
				outputStream.close();
			}
		}
	}

	public class ComboConfigurationListener implements SelectionListener {
		public ComboConfigurationListener(String key, Combo combo) {
			this.key = key;
			this.combo = combo;
		}

		private final String key;
		private final Combo combo;

		@SuppressWarnings("unchecked")
		@Override
		public void widgetSelected(SelectionEvent e) {
			object.put(key, combo.getText());
			setDirty(true);
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
		}
	}

	public class RadioConfigurationListener implements SelectionListener {
		public RadioConfigurationListener(String key, String value) {
			this.key = key;
			this.value = value;
		}

		private final String key;
		private final String value;

		@SuppressWarnings("unchecked")
		@Override
		public void widgetSelected(SelectionEvent e) {
			object.put(key, value);
			setDirty(true);
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
		}
	}

	public class CostButtonListener implements SelectionListener {
		public CostButtonListener(String key, Button button, Text text) {
			this.key = key;
			this.button = button;
			this.text = text;
		}

		private final String key;
		private final Button button;
		private final Text text;

		@SuppressWarnings("unchecked")
		@Override
		public void widgetSelected(SelectionEvent e) {
			text.setEnabled(button.getSelection());
			object.put(key, button.getSelection());
			setDirty(true);
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
		}
	}

	public class CostKeyListener implements KeyListener {
		public CostKeyListener(String key, Text text) {
			this.key = key;
			this.text = text;
		}

		private final String key;
		private final Text text;

		@Override
		public void keyPressed(KeyEvent e) {
		}

		@SuppressWarnings("unchecked")
		@Override
		public void keyReleased(KeyEvent e) {
			object.put(key, text.getText());
			setDirty(true);
		}
	}

	private final void updateTiles() {
		JSONArray places = (JSONArray) object.get("places");
		for (int i = 0; i < 6; i++) {
			tiles.get(i).updateTile(
					String.valueOf(places.indexOf(String.valueOf(i + 1)) + 1));
		}
	}

	private final Subscriber showGraphSubscriber = new Subscriber() {
		@Override
		public void fireChange() {
			PlatformUI
					.getWorkbench()
					.getDisplay()
					.asyncExec(
							() -> GraphControl.openFrameWindow(new FrameInfo(0,
									"Расстановка_фишек")));
			simulatorSubscriberManager.deinitialize();
		}
	};
}
