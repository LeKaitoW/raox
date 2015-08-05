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
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
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
import org.eclipse.ui.part.EditorPart;
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
		leftCombo.select(object.get("computeLeft").equals("After") ? 0 : 1);

		final Button leftButton = new Button(ruleCost, SWT.CHECK);
		final Text leftCost = new Text(ruleCost, SWT.BORDER);
		leftCost.setText(object.get("costLeft").toString());
		leftCost.setEnabled((boolean) object.get("enableLeft"));
		leftButton.setSelection((boolean) object.get("enableLeft"));

		leftButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (leftButton.getSelection()) {
					leftCost.setEnabled(true);
				} else {
					leftCost.setEnabled(false);
				}
				object.put("enableLeft", leftButton.getSelection());
				setDirty(true);
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
		rightCombo.select(object.get("computeRight").equals("After") ? 0 : 1);

		final Button rightButton = new Button(ruleCost, SWT.CHECK);
		final Text rightCost = new Text(ruleCost, SWT.BORDER);
		rightCost.setText(object.get("costRight").toString());
		rightCost.setEnabled((boolean) object.get("enableRight"));
		rightButton.setSelection((boolean) object.get("enableRight"));

		rightButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (rightButton.getSelection()) {
					rightCost.setEnabled(true);
				} else {
					rightCost.setEnabled(false);
				}
				object.put("enableRight", rightButton.getSelection());
				setDirty(true);
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
		upCombo.select(object.get("computeUp").equals("After") ? 0 : 1);

		final Button upButton = new Button(ruleCost, SWT.CHECK);
		final Text upCost = new Text(ruleCost, SWT.BORDER);
		upCost.setText(object.get("costUp").toString());
		upCost.setEnabled((boolean) object.get("enableUp"));
		upButton.setSelection((boolean) object.get("enableUp"));

		upButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (upButton.getSelection()) {
					upCost.setEnabled(true);
				} else {
					upCost.setEnabled(false);
				}
				object.put("enableUp", upButton.getSelection());
				setDirty(true);
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
		downCombo.select(object.get("computeDown").equals("After") ? 0 : 1);

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
				if (downButton.getSelection()) {
					downCost.setEnabled(true);
				} else
					downCost.setEnabled(false);
				object.put("enableDown", downButton.getSelection());
				setDirty(true);
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

		setOrderButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (setOrderButton.getSelection()) {
					setOrderText.setEnabled(true);
					setOrderOkButton.setEnabled(true);
					JSONArray places = (JSONArray) object.get("places");
					String order = OrderConfigurator
							.convertOrderToString(places);
					setOrderText.setText(order);
				} else {
					setOrderText.setEnabled(false);
					setOrderOkButton.setEnabled(false);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		setOrderOkButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				JSONArray places = OrderConfigurator
						.convertOrderToJSONArray(setOrderText.getText());
				if (places != null) {
					object.put("places", places);
				} else {
					setOrderError.setVisible(true);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
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
		editor = embeddedEditor.createPartialEditor("", object.get("code")
				.toString(), "", false);

		IXtextDocument document = embeddedEditor.getDocument();
		document.addModelListener(new IXtextModelListener() {
			@Override
			public void modelChanged(XtextResource resource) {
				setDirty(true);
			}
		});

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
				object.put(
						"places",
						OrderConfigurator.shuffle(places,
								(boolean) object.get("solvable")));
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
}
