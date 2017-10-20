package ru.bmstu.rk9.raox.plugin.game5;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Supplier;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import ru.bmstu.rk9.rao.lib.notification.Subscriber;
import ru.bmstu.rk9.rao.lib.notification.Subscription.SubscriptionType;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator.ExecutionState;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorSubscriberManager;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorSubscriberManager.SimulatorSubscriberInfo;
import ru.bmstu.rk9.rao.ui.execution.ExecutionManager;
import ru.bmstu.rk9.rao.ui.graph.GraphControl;
import ru.bmstu.rk9.rao.ui.graph.GraphControl.FrameInfo;
import ru.bmstu.rk9.rao.ui.serialization.SerializationConfigView;

public class Game5View extends EditorPart {

	protected static boolean dirty = false;
	public static final String ID = "raox-game5.Game5View";
	private static JSONObject object;
	private final List<TileButton> tiles = new ArrayList<>();
	private final int tilesCountX = 3;
	private final int tilesCountY = 2;

	@SuppressWarnings("unchecked")
	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());
		ScrolledComposite scrolledComposite = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FILL);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);
		scrolledComposite.setLayout(new FillLayout());

		Composite composite = new Composite(scrolledComposite, SWT.NONE | SWT.FILL);
		scrolledComposite.setContent(composite);

		Color color = parent.getDisplay().getSystemColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW);
		composite.setBackground(color);

		final IFileEditorInput input = (IFileEditorInput) this.getEditorInput();
		final IFile configIFile = input.getFile();
		setPartName(configIFile.getProject().getName());
		object = ConfigurationParser.parseObject(configIFile);
		try {
			fillModelFile(configIFile);
		} catch (IOException | CoreException e) {
			e.printStackTrace();
			MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Error",
					"Internal error occured while saving file:\n" + e.getMessage());
			throw new Game5Exception(e);
		}

		final GridLayout gridLayout = new GridLayout(5, false);
		composite.setLayout(gridLayout);

		final Group boardGroup = new Group(composite, SWT.NONE);
		final GridLayout boardLayout = new GridLayout(3, false);
		final GridData boardData = new GridData(SWT.FILL, SWT.FILL, false, false);
		boardData.verticalSpan = 3;
		boardGroup.setLayoutData(boardData);
		boardGroup.setLayout(boardLayout);
		boardGroup.setText("Board:");
		boardGroup.setBackground(color);
		JSONArray places = (JSONArray) object.get("places");
		for (int i = 0; i < tilesCountX * tilesCountY; i++) {
			tiles.add(new TileButton(boardGroup, SWT.NONE, String.valueOf(places.indexOf(String.valueOf(i + 1)) + 1),
					i + 1));
		}

		final Group shuffleGroup = new Group(composite, SWT.SHADOW_IN);
		shuffleGroup.setText("Shuffle:");
		shuffleGroup.setBackground(color);
		final GridData shuffleData = new GridData(SWT.FILL, SWT.FILL, false, false);
		shuffleData.verticalSpan = 2;
		shuffleGroup.setLayoutData(shuffleData);
		shuffleGroup.setLayout(new GridLayout(1, false));
		final Button solvableOnly = new Button(shuffleGroup, SWT.RADIO);
		solvableOnly.setText("Solvable only");
		final Button unsolvableOnly = new Button(shuffleGroup, SWT.RADIO);
		unsolvableOnly.setText("Unsolvable only");
		final Button allSituations = new Button(shuffleGroup, SWT.RADIO);
		allSituations.setText("All situations");

		if (object.get("solvable").equals("true")) {
			solvableOnly.setSelection(true);
		} else if (object.get("solvable").equals("false")) {
			unsolvableOnly.setSelection(true);
		} else {
			allSituations.setSelection(true);
		}

		final Button shuffle = new Button(shuffleGroup, SWT.PUSH);
		shuffle.setText("Shuffle");

		final Group setOrderGroup = new Group(composite, SWT.NONE);
		setOrderGroup.setText("Set order:");
		setOrderGroup.setBackground(color);
		setOrderGroup.setLayout(new FormLayout());
		final GridData setOrderData = new GridData(SWT.FILL, SWT.FILL, false, false);
		setOrderGroup.setLayoutData(setOrderData);
		final Button setOrderButton = new Button(setOrderGroup, SWT.TOGGLE);
		setOrderButton.setText("Set...");
		final Label setOrderError = new Label(setOrderGroup, SWT.NONE);
		setOrderError.setText("Invalid order");
		final Color red = new Color(PlatformUI.getWorkbench().getDisplay(), 0x9B, 0x11, 0x1E);
		setOrderError.setForeground(red);
		red.dispose();
		setOrderError.setVisible(false);
		final Text setOrderText = new Text(setOrderGroup, SWT.BORDER);
		final String order = OrderConfigurator.convertPlacesToString((JSONArray) object.get("places"));
		setOrderText.setText(order);
		setOrderText.setEnabled(false);
		final Button setOrderOkButton = new Button(setOrderGroup, SWT.PUSH);
		setOrderOkButton.setText("Ok");
		setOrderOkButton.setEnabled(false);
		FormData setOrderErrorData = new FormData();
		setOrderErrorData.left = new FormAttachment(setOrderButton, 5);
		setOrderErrorData.top = new FormAttachment(0, 5);
		setOrderError.setLayoutData(setOrderErrorData);
		FormData setOrderTextData = new FormData();
		setOrderTextData.top = new FormAttachment(setOrderButton, 1);
		setOrderText.setLayoutData(setOrderTextData);
		setOrderText.setTextLimit(2 * tilesCountX * tilesCountY - 1);
		FormData setOrderOkButtonData = new FormData();
		setOrderOkButtonData.left = new FormAttachment(setOrderText, 2);
		setOrderOkButtonData.right = new FormAttachment(100, -2);
		setOrderOkButtonData.top = new FormAttachment(setOrderButton, 1);
		setOrderOkButton.setLayoutData(setOrderOkButtonData);

		final Group ruleCost = new Group(composite, SWT.SHADOW_IN);
		ruleCost.setText("Rules cost:");
		ruleCost.setBackground(color);
		final GridLayout ruleCostLayout = new GridLayout(4, false);
		ruleCost.setLayout(ruleCostLayout);
		final GridData ruleCostData = new GridData(SWT.BEGINNING, SWT.FILL, false, false);
		ruleCostData.verticalSpan = 3;
		ruleCost.setLayoutData(ruleCostData);

		final Group traverseGraph = new Group(composite, SWT.SHADOW_IN);
		traverseGraph.setText("Traverse graph:");
		traverseGraph.setBackground(color);
		traverseGraph.setLayout(gridLayout);
		final Button compareTops = new Button(traverseGraph, SWT.CHECK);
		compareTops.setText("Compare tops");
		compareTops.setSelection((boolean) object.get("compare"));

		final Group inOrderGroup = new Group(composite, SWT.SHADOW_IN);
		inOrderGroup.setText("Set in order:");
		inOrderGroup.setBackground(color);
		inOrderGroup.setLayout(gridLayout);
		final Button inOrder = new Button(inOrderGroup, SWT.PUSH);
		inOrder.setText("In order");
		final GridData inOrderData = new GridData(SWT.FILL, SWT.FILL, false, false);
		inOrderGroup.setLayoutData(inOrderData);

		final Group simulationGroup = new Group(composite, SWT.SHADOW_IN);
		simulationGroup.setText("Experiment:");
		simulationGroup.setBackground(color);
		simulationGroup.setLayout(new GridLayout());
		final GridData simulationData = new GridData(SWT.BEGINNING, SWT.FILL, true, false);
		simulationData.verticalSpan = 2;
		simulationGroup.setLayoutData(simulationData);

		final Group heuristicSelection = new Group(composite, SWT.SHADOW_IN);
		heuristicSelection.setText("Heuristic:");
		heuristicSelection.setBackground(color);
		heuristicSelection.setLayout(gridLayout);
		final GridData heuristicGridData = new GridData(SWT.FILL, SWT.FILL, false, false);
		heuristicGridData.horizontalSpan = 2;
		heuristicGridData.widthHint = setOrderGroup.getBorderWidth() + shuffleGroup.getBorderWidth();
		heuristicSelection.setLayoutData(heuristicGridData);
		final Combo heuristicList = new Combo(heuristicSelection, SWT.BORDER | SWT.DROP_DOWN | SWT.V_SCROLL);
		final String zeroHeuristic = "Поиск_в_ширину()";
		heuristicList.add(zeroHeuristic);
		final String tilesHeuristic = "Кол_во_фишек_не_на_месте()";
		heuristicList.add(tilesHeuristic);
		final String manhattanDistanceHeuristic = "Расстояния_фишек_до_мест()";
		heuristicList.add(manhattanDistanceHeuristic);
		heuristicList.setText((String) object.get("heuristic"));

		final Button simulationButton = new Button(simulationGroup, SWT.PUSH);
		simulationButton.setText("Run experiment");

		final Label moveLabel = new Label(ruleCost, SWT.NONE);
		moveLabel.setText("Move");

		final Label computeLabel = new Label(ruleCost, SWT.NONE);
		computeLabel.setText("Compute");

		final Label costLabel = new Label(ruleCost, SWT.NONE);
		costLabel.setText("Cost");
		final GridData costLabelData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		costLabelData.horizontalSpan = 2;
		costLabel.setLayoutData(costLabelData);

		final Label leftLabel = new Label(ruleCost, SWT.NONE);
		leftLabel.setText("Left");

		final Combo leftCombo = new Combo(ruleCost, SWT.BORDER | SWT.DROP_DOWN | SWT.V_SCROLL | SWT.READ_ONLY);
		leftCombo.add("After");
		leftCombo.add("Before");
		leftCombo.select(object.get("computeLeft").equals("After") ? 0 : 1);

		final Button leftButton = new Button(ruleCost, SWT.CHECK);
		final Text leftCost = new Text(ruleCost, SWT.BORDER);
		leftCost.setText(object.get("costLeft").toString());
		leftCost.setEnabled((boolean) object.get("enableLeft"));
		leftButton.setSelection((boolean) object.get("enableLeft"));
		leftButton.addSelectionListener(new CostButtonListener("enableLeft", leftCost));

		final Label rightLabel = new Label(ruleCost, SWT.NONE);
		rightLabel.setText("Right");

		final Combo rightCombo = new Combo(ruleCost, SWT.BORDER | SWT.DROP_DOWN | SWT.V_SCROLL | SWT.READ_ONLY);
		rightCombo.add("After");
		rightCombo.add("Before");
		rightCombo.select(object.get("computeRight").equals("After") ? 0 : 1);

		final Button rightButton = new Button(ruleCost, SWT.CHECK);
		final Text rightCost = new Text(ruleCost, SWT.BORDER);
		rightCost.setText(object.get("costRight").toString());
		rightCost.setEnabled((boolean) object.get("enableRight"));
		rightButton.setSelection((boolean) object.get("enableRight"));
		rightButton.addSelectionListener(new CostButtonListener("enableRight", rightCost));

		final Label upLabel = new Label(ruleCost, SWT.NONE);
		upLabel.setText("Up");

		final Combo upCombo = new Combo(ruleCost, SWT.BORDER | SWT.DROP_DOWN | SWT.V_SCROLL | SWT.READ_ONLY);
		upCombo.add("After");
		upCombo.add("Before");
		upCombo.select(object.get("computeUp").equals("After") ? 0 : 1);

		final Button upButton = new Button(ruleCost, SWT.CHECK);
		final Text upCost = new Text(ruleCost, SWT.BORDER);
		upCost.setText(object.get("costUp").toString());
		upCost.setEnabled((boolean) object.get("enableUp"));
		upButton.setSelection((boolean) object.get("enableUp"));
		upButton.addSelectionListener(new CostButtonListener("enableUp", upCost));

		final Label downLabel = new Label(ruleCost, SWT.NONE);
		downLabel.setText("Down");

		final Combo downCombo = new Combo(ruleCost, SWT.BORDER | SWT.DROP_DOWN | SWT.V_SCROLL | SWT.READ_ONLY);
		downCombo.add("After");
		downCombo.add("Before");
		downCombo.select(object.get("computeDown").equals("After") ? 0 : 1);

		final Button downButton = new Button(ruleCost, SWT.CHECK);
		final Text downCost = new Text(ruleCost, SWT.BORDER);
		downCost.setText(object.get("costDown").toString());
		downCost.setEnabled((boolean) object.get("enableDown"));
		downButton.setSelection((boolean) object.get("enableDown"));
		downButton.addSelectionListener(new CostButtonListener("enableDown", downCost));

		setOrderButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (setOrderButton.getSelection()) {
					setOrderText.setEnabled(true);
					setOrderOkButton.setEnabled(true);
					JSONArray places = (JSONArray) object.get("places");
					String order = OrderConfigurator.convertPlacesToString(places);
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
				JSONArray places = OrderConfigurator.convertStringToPlaces(setOrderText.getText());
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

		heuristicList.addSelectionListener(new ConfigurationListener("heuristic", () -> heuristicList.getText()));

		leftCost.addKeyListener(new ConfigurationKeyListener("costLeft", () -> leftCost.getText()));
		leftCombo.addSelectionListener(new ConfigurationListener("computeLeft", () -> leftCombo.getText()));

		rightCost.addKeyListener(new ConfigurationKeyListener("costRight", () -> rightCost.getText()));
		rightCombo.addSelectionListener(new ConfigurationListener("computeRight", () -> rightCombo.getText()));

		upCost.addKeyListener(new ConfigurationKeyListener("costUp", () -> upCost.getText()));
		upCombo.addSelectionListener(new ConfigurationListener("computeUp", () -> upCombo.getText()));

		downCost.addKeyListener(new ConfigurationKeyListener("costDown", () -> downCost.getText()));
		downCombo.addSelectionListener(new ConfigurationListener("computeDown", () -> downCombo.getText()));

		solvableOnly.addSelectionListener(new ConfigurationListener("solvable", () -> "true"));
		unsolvableOnly.addSelectionListener(new ConfigurationListener("solvable", () -> "false"));
		allSituations.addSelectionListener(new ConfigurationListener("solvable", () -> "all"));

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
					IServiceLocator serviceLocator = PlatformUI.getWorkbench();
					((SerializationConfigView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
							.showView(SerializationConfigView.ID)).setCheckedStateForAll();

					ICommandService commandService = (ICommandService) serviceLocator.getService(ICommandService.class);
					Command command = commandService.getCommand("ru.bmstu.rk9.rao.ui.runtime.execute");

					ExecutionManager.registerBeforeRunSubcriber(() -> {
						new SimulatorSubscriberManager().initialize(
								Arrays.asList(new SimulatorSubscriberInfo(showGraphSubscriber,
										ExecutionState.EXECUTION_COMPLETED)),
								EnumSet.of(SubscriptionType.IGNORE_ACCUMULATED, SubscriptionType.ONE_SHOT));
					});

					command.executeWithChecks(new ExecutionEvent());
				} catch (PartInitException | ExecutionException | NotDefinedException | NotEnabledException
						| NotHandledException e) {
					e.printStackTrace();
					MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Error",
							"Failed to run experiment:\n" + e.getMessage());
					throw new Game5Exception(e);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent event) {
			}
		});

		shuffle.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				setOrderButton.setSelection(false);
				setOrderText.setEnabled(false);
				setOrderOkButton.setEnabled(false);
				JSONArray places = (JSONArray) object.get("places");
				JSONArray newPlaces = OrderConfigurator.shuffle(places, (String) object.get("solvable"));
				object.put("places", newPlaces);
				updateTiles();
				setOrderText.setText(OrderConfigurator.convertPlacesToString(newPlaces));
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
				try {
					OrderConfigurator.setInOrder(object);
				} catch (IOException | ParseException e) {
					e.printStackTrace();
					MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Error",
							"Failed to set tiles in order:\n" + e.getMessage());
					throw new Game5Exception(e);
				}
				updateTiles();
				setOrderText.setText(OrderConfigurator.convertPlacesToString((JSONArray) object.get("places")));
				setDirty(true);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		for (int i = 0; i < tiles.size(); i++) {
			tiles.get(i).addMouseListener(new MouseListener() {
				@Override
				public void mouseUp(MouseEvent arg0) {
					setOrderButton.setSelection(false);
					setOrderText.setEnabled(false);
					setOrderOkButton.setEnabled(false);
					final TileButton tile = (TileButton) arg0.getSource();
					final JSONArray places = (JSONArray) object.get("places");
					final int tileNumber = tile.getTileNumber();
					final int tilePlace = tile.getTilePlace();
					final int freePlace = Integer.valueOf((String) places.get(tilesCountX * tilesCountY - 1));
					if (TileButton.isFreePlaceNearby(tilePlace, freePlace)) {
						tiles.get(freePlace - 1).updateTile(String.valueOf(tileNumber));
						tile.updateTile(String.valueOf(tilesCountX * tilesCountY));
						places.set(tileNumber - 1, String.valueOf(freePlace));
						places.set(tilesCountX * tilesCountY - 1, String.valueOf(tilePlace));
						setOrderText.setText(OrderConfigurator.convertPlacesToString(places));
						setDirty(true);
					}
				}

				@Override
				public void mouseDown(MouseEvent arg0) {
				}

				@Override
				public void mouseDoubleClick(MouseEvent arg0) {
				}
			});
		}

		heuristicList.addModifyListener(new ConfigurationModifyListener("heuristic", () -> heuristicList.getText()));
		scrolledComposite.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	@Override
	public void setFocus() {
	}

	@Override
	public void doSave(IProgressMonitor arg0) {
		try {
			IFile configIFile = (IFile) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.getActiveEditor().getEditorInput().getAdapter(IFile.class);
			fillModelFile(configIFile);
		} catch (IOException | CoreException e) {
			e.printStackTrace();
			MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Error",
					"Internal error occured while saving file:\n" + e.getMessage());
			throw new Game5Exception(e);
		}

		setDirty(false);
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
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

	public static final JSONObject getConfigurations() {
		return object;
	}

	protected void setDirty(boolean value) {
		dirty = value;
		firePropertyChange(IEditorPart.PROP_DIRTY);
	}

	private static final void fillModelFile(IFile configIFile) throws IOException, CoreException {
		OutputStream outputStream = null;
		final InputStream inputStream = Game5ProjectConfigurator.class.getClassLoader()
				.getResourceAsStream(Game5ProjectConfigurator.modelTemplatePath);

		try {
			OutputStream configOutputStream = new FileOutputStream(configIFile.getRawLocation().toString());
			PrintStream outputPrintStream = new PrintStream(configOutputStream, true, StandardCharsets.UTF_8.name());
			outputPrintStream.print(object.toString());
			outputPrintStream.close();

			InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8.name());
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			outputStream = new FileOutputStream(configIFile.getLocation().removeLastSegments(1)
					.append(Game5ProjectConfigurator.modelPath).toString());
			PrintStream printStream = new PrintStream(outputStream, true, StandardCharsets.UTF_8.name());

			final String resourcesCode = ConfigurationParser.getResourcesCode(object);
			printStream.print(resourcesCode);

			String modelTemplateCode = bufferedReader.readLine();
			while (modelTemplateCode != null) {
				printStream.println(modelTemplateCode);
				modelTemplateCode = bufferedReader.readLine();
			}

			final String searchCode = ConfigurationParser.getSearchCode(object);
			printStream.print(searchCode);

			printStream.close();

			configIFile.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
		} finally {
			inputStream.close();
			if (outputStream != null) {
				outputStream.flush();
				outputStream.close();
			}
		}

	}

	public class ConfigurationListener implements SelectionListener {
		public ConfigurationListener(String key, Supplier<String> value) {
			this.key = key;
			this.value = value;
		}

		private final String key;
		private final Supplier<String> value;

		@SuppressWarnings("unchecked")
		@Override
		public void widgetSelected(SelectionEvent e) {
			object.put(key, value.get());
			setDirty(true);
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
		}
	}

	public class CostButtonListener implements SelectionListener {
		public CostButtonListener(String key, Text text) {
			this.key = key;
			this.text = text;
		}

		private final String key;
		private final Text text;

		@SuppressWarnings("unchecked")
		@Override
		public void widgetSelected(SelectionEvent e) {
			text.setEnabled(((Button) e.getSource()).getSelection());
			object.put(key, ((Button) e.getSource()).getSelection());
			setDirty(true);
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
		}
	}

	public class ConfigurationModifyListener implements ModifyListener {
		public ConfigurationModifyListener(String key, Supplier<String> value) {
			this.key = key;
			this.value = value;
		}

		private final String key;
		private final Supplier<String> value;

		@SuppressWarnings("unchecked")
		@Override
		public void modifyText(ModifyEvent e) {
			object.put(key, value.get());
			setDirty(true);
		}
	}

	public class ConfigurationKeyListener implements KeyListener {
		public ConfigurationKeyListener(String key, Supplier<String> value) {
			this.key = key;
			this.value = value;
		}

		private final String key;
		private final Supplier<String> value;

		@Override
		public void keyPressed(KeyEvent e) {
		}

		@SuppressWarnings("unchecked")
		@Override
		public void keyReleased(KeyEvent e) {
			object.put(key, value.get());
			setDirty(true);
		}
	}

	private final void updateTiles() {
		JSONArray places = (JSONArray) object.get("places");
		for (int i = 0; i < tilesCountX * tilesCountY; i++) {
			tiles.get(i).updateTile(String.valueOf(places.indexOf(String.valueOf(i + 1)) + 1));
		}
	}

	private final Subscriber showGraphSubscriber = new Subscriber() {
		@Override
		public void fireChange() {
			final Display display = PlatformUI.getWorkbench().getDisplay();
			display.asyncExec(() -> {
				GraphControl.openFrameWindow(new FrameInfo(0, "Расстановка_фишек"));
				new GraphManager(GraphControl.getOpenedGraphMap().get(0).getGraphPanel(),
						OrderConfigurator.inverseOrderPlaces((JSONArray) object.get("places")));
			});
		}
	};
}
