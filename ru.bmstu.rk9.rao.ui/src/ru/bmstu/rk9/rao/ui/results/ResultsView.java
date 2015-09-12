package ru.bmstu.rk9.rao.ui.results;

import java.util.Comparator;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.themes.ITheme;
import org.eclipse.ui.themes.IThemeManager;
import org.osgi.framework.Bundle;

import com.google.common.collect.ImmutableMap;

import ru.bmstu.rk9.rao.lib.json.JSONObject;
import ru.bmstu.rk9.rao.lib.result.Result;

public class ResultsView extends ViewPart {
	public static final String ID = "ru.bmstu.rk9.rao.ui.ResultsView"; //$NON-NLS-1$

	private static IEclipsePreferences prefs = InstanceScope.INSTANCE
			.getNode("ru.bmstu.rk9.rao.ui");

	private static class Formatter {
		static int counter = 0;

		int value;
		String name;
	}

	private static Formatter format(String name) {
		Formatter format = new Formatter();

		format.value = Formatter.counter++;
		format.name = name;

		return format;
	}

	private static Map<String, Formatter> sortList = ImmutableMap
			.<String, Formatter> builder().put("value", format("Value"))
			.put("last", format("Last value"))
			.put("counter", format("Times registered"))
			.put("minTrue", format("Shortest true"))
			.put("maxTrue", format("Longest true"))
			.put("minFalse", format("Shortest false"))
			.put("maxFalse", format("Longest false"))
			.put("percent", format("Percent true"))
			.put("min", format("Minimum value"))
			.put("max", format("Maximum value")).put("mean", format("Mean"))
			.put("deviation", format("Standard deviation"))
			.put("varcoef", format("Coefficient of variation"))
			.put("median", format("Median")).build();

	private static Comparator<String> comparator = (a, b) -> Integer.compare(
			sortList.get(a).value, sortList.get(b).value);

	private static List<Result> results;

	private static boolean viewAsText = prefs.getBoolean("ResultsViewAsText",
			false);

	private static HashMap<String, TreeItem> models;

	private static void parseResult(JSONObject data) {
		String fullName = data.getString("name");
		int modelDot = fullName.indexOf('.');

		String model = fullName.substring(0, modelDot);
		String name = fullName.substring(modelDot + 1);

		TreeItem modelItem = models.get(model);
		if (modelItem == null) {
			modelItem = new TreeItem(tree, SWT.NONE);
			modelItem.setText(model);
			models.put(model, modelItem);
		}

		TreeItem result = new TreeItem(modelItem, SWT.NONE);
		result.setText(new String[] { name, data.getString("type") });

		String origin = text.getText();

		StyleRange styleRange = new StyleRange();
		styleRange.start = origin.length();
		styleRange.fontStyle = SWT.BOLD;

		String[] resultText = { origin + (origin.length() > 0 ? "\n\n" : "")
				+ data.getString("name") + ": " + data.getString("type") };

		styleRange.length = resultText[0].length() - origin.length();

		LinkedList<StyleRange> numberStyles = new LinkedList<StyleRange>();

		data.keySet()
				.stream()
				.filter(e -> sortList.containsKey(e))
				.sorted(comparator)
				.forEachOrdered(
						e -> {
							String[] text = new String[] {
									sortList.get(e).name,
									data.get(e).toString() };

							TreeItem child = new TreeItem(result, SWT.NONE);
							child.setText(text);

							StyleRange numberStyle = new StyleRange();
							numberStyle.start = resultText[0].length()
									+ text[0].length() + 4;
							numberStyle.length = text[1].length();
							numberStyle.fontStyle = SWT.ITALIC;
							numberStyle.foreground = child.getDisplay()
									.getSystemColor(SWT.COLOR_DARK_BLUE);

							numberStyles.add(numberStyle);

							resultText[0] += "\n\t" + text[0] + ": " + text[1];
						});

		StyleRange[] styles = text.getStyleRanges();
		text.setText(resultText[0]);
		text.setStyleRanges(styles);
		text.setStyleRange(styleRange);

		for (StyleRange style : numberStyles)
			text.setStyleRange(style);
	}

	public static void setResults(List<Result> results) {
		ResultsView.results = results;

		if (!isInitialized())
			return;

		models = new HashMap<String, TreeItem>();

		for (TreeItem item : tree.getItems())
			item.dispose();

		text.setText("");

		for (Result r : results)
			parseResult(r.getData());

		for (TreeItem model : tree.getItems())
			model.setExpanded(true);
	}

	private static ScrolledComposite composite;
	private static StyledText text;
	private static Tree tree;

	private static int nameWidth = prefs.getInt("ResultsNameColumnWidth", 200);
	private static int valueWidth = prefs
			.getInt("ResultsValueColumnWidth", 200);

	public static void savePreferences() {
		prefs.putBoolean("ResultsViewAsText", viewAsText);
		prefs.putInt("ResultsNameColumnWidth", nameWidth);
		prefs.putInt("ResultsValueColumnWidth", valueWidth);
	}

	private static IActionBars actionBars;
	private static IToolBarManager toolbarMgr;

	private static abstract class SwitchAction extends Action {
		public abstract void updateLook();
	}

	private static final String TREE_TEXT_SWITCH_ID = "ResultsView.actions.treeTextSwitch";
	private static SwitchAction actionTreeTextSwitch = new SwitchAction() {
		ImageDescriptor text, tree;
		{
			Bundle ui = Platform.getBundle("ru.bmstu.rk9.rao.ui");
			setId(TREE_TEXT_SWITCH_ID);

			text = ImageDescriptor.createFromURL(FileLocator.find(ui, new Path(
					"icons/script-text.png"), null));
			tree = ImageDescriptor.createFromURL(FileLocator.find(ui, new Path(
					"icons/tree.png"), null));
		}

		public void updateLook() {
			if (viewAsText) {
				setText("View as tree");
				setImageDescriptor(tree);
				composite.setContent(ResultsView.text);

				setTreeActions(false);
			} else {
				setText("View as text");
				setImageDescriptor(text);
				composite.setContent(ResultsView.tree);

				setTreeActions(true);
			}
		}

		@Override
		public void run() {
			viewAsText = !viewAsText;
			updateLook();
		}
	};

	private static final String EXPAND_ALL_ID = "ResultsView.actions.expandAll";
	private static Action actionExpandAll = new Action() {
		{
			setId(EXPAND_ALL_ID);

			setText("Expand All");
			setImageDescriptor(ImageDescriptor.createFromURL(FileLocator.find(
					Platform.getBundle("ru.bmstu.rk9.rao.ui"), new Path(
							"icons/zones-stack.png"), null)));
		}

		public void run() {
			for (TreeItem item : tree.getItems()) {
				item.setExpanded(true);
				for (TreeItem child : item.getItems())
					child.setExpanded(true);
			}
		};
	};

	private static final String COLLAPSE_ALL_ID = "ResultsView.actions.collapseAll";
	private static Action actionCollapseAll = new Action() {
		{
			setId(COLLAPSE_ALL_ID);

			setText("Collapse All");
			setImageDescriptor(ImageDescriptor.createFromURL(FileLocator.find(
					Platform.getBundle("ru.bmstu.rk9.rao.ui"), new Path(
							"icons/zones.png"), null)));
		}

		public void run() {
			for (TreeItem item : tree.getItems()) {
				item.setExpanded(true);
				for (TreeItem child : item.getItems())
					child.setExpanded(false);
			}
		};
	};

	private static final String COLLAPSE_MODELS_ID = "ResultsView.actions.collapseModels";
	private static Action actionCollapseModels = new Action() {
		{
			setId(COLLAPSE_MODELS_ID);

			setText("Collapse Including Models");
			setImageDescriptor(ImageDescriptor.createFromURL(FileLocator.find(
					Platform.getBundle("ru.bmstu.rk9.rao.ui"), new Path(
							"icons/zone-medium.png"), null)));
		}

		public void run() {
			for (TreeItem item : tree.getItems()) {
				for (TreeItem child : item.getItems())
					child.setExpanded(false);
				item.setExpanded(false);
			}
		};
	};

	private static void setTreeActions(boolean state) {
		if (state) {
			toolbarMgr.insertBefore(TREE_TEXT_SWITCH_ID, actionExpandAll);
			toolbarMgr.insertBefore(TREE_TEXT_SWITCH_ID, actionCollapseAll);
			toolbarMgr.insertBefore(TREE_TEXT_SWITCH_ID, actionCollapseModels);
		} else {
			toolbarMgr.remove(EXPAND_ALL_ID);
			toolbarMgr.remove(COLLAPSE_ALL_ID);
			toolbarMgr.remove(COLLAPSE_MODELS_ID);
		}

		actionBars.updateActionBars();
	}

	@Override
	public void createPartControl(Composite parent) {
		composite = new ScrolledComposite(parent, SWT.NONE);
		composite.setExpandHorizontal(true);
		composite.setExpandVertical(true);

		tree = new Tree(composite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		tree.setHeaderVisible(true);

		TreeColumn nameCol = new TreeColumn(tree, SWT.NONE);
		nameCol.setWidth(nameWidth);
		nameCol.setText("Name");
		nameCol.addControlListener(new ControlListener() {
			@Override
			public void controlResized(ControlEvent e) {
				nameWidth = nameCol.getWidth();
			}

			@Override
			public void controlMoved(ControlEvent e) {
			}
		});

		TreeColumn valueCol = new TreeColumn(tree, SWT.NONE);
		valueCol.setWidth(valueWidth);
		valueCol.setText("Value");
		nameCol.addControlListener(new ControlListener() {
			@Override
			public void controlResized(ControlEvent e) {
				valueWidth = valueCol.getWidth();
			}

			@Override
			public void controlMoved(ControlEvent e) {
			}
		});

		Menu treeMenu = new Menu(tree);
		MenuItem expand = new MenuItem(treeMenu, SWT.CASCADE);
		expand.setText("Expand All");
		expand.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				actionExpandAll.run();
			}
		});
		MenuItem collapse = new MenuItem(treeMenu, SWT.CASCADE);
		collapse.setText("Collapse All");
		collapse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				actionCollapseAll.run();
			}
		});
		MenuItem collapseModels = new MenuItem(treeMenu, SWT.CASCADE);
		collapseModels.setText("Collapse Including Models");
		collapseModels.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				actionCollapseModels.run();
			}
		});
		tree.setMenu(treeMenu);

		text = new StyledText(composite, SWT.H_SCROLL | SWT.V_SCROLL);
		text.setAlwaysShowScrollBars(false);
		text.setEditable(false);
		text.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, true));
		text.setMargins(4, 4, 4, 4);

		Menu popupMenu = new Menu(text);
		MenuItem copy = new MenuItem(popupMenu, SWT.CASCADE);
		copy.setText("Copy\tCtrl+C");
		copy.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				text.invokeAction(ST.COPY);
			}
		});
		MenuItem selectAll = new MenuItem(popupMenu, SWT.CASCADE);
		selectAll.setText("Select All\tCtrl+A");
		selectAll.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				text.invokeAction(ST.SELECT_ALL);
			}
		});
		text.setMenu(popupMenu);

		composite.setContent(viewAsText ? text : tree);

		registerTextFontUpdateListener();
		updateTextFont();

		actionBars = getViewSite().getActionBars();
		toolbarMgr = actionBars.getToolBarManager();
		toolbarMgr.add(actionTreeTextSwitch);
		actionTreeTextSwitch.updateLook();

		if (results != null)
			setResults(results);
	}

	private void updateTextFont() {
		IThemeManager themeManager = PlatformUI.getWorkbench()
				.getThemeManager();
		ITheme currentTheme = themeManager.getCurrentTheme();
		FontRegistry fontRegistry = currentTheme.getFontRegistry();
		text.setFont(fontRegistry.get(PreferenceConstants.EDITOR_TEXT_FONT));
	}

	private void registerTextFontUpdateListener() {
		IThemeManager themeManager = PlatformUI.getWorkbench()
				.getThemeManager();
		IPropertyChangeListener listener = new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty().equals(
						PreferenceConstants.EDITOR_TEXT_FONT))
					updateTextFont();
			}
		};
		themeManager.addPropertyChangeListener(listener);
	}

	public static class ResultsViewSwitchHandler extends AbstractHandler {
		@Override
		public Object execute(ExecutionEvent event) throws ExecutionException {
			return null;
		}
	}

	private static boolean isInitialized() {
		return tree != null && text != null && !tree.isDisposed()
				&& !text.isDisposed();
	}

	@Override
	public void setFocus() {
	}
}
