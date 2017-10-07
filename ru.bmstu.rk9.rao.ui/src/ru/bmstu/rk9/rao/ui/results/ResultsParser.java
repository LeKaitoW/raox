package ru.bmstu.rk9.rao.ui.results;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import ru.bmstu.rk9.rao.lib.json.JSONObject;
import ru.bmstu.rk9.rao.lib.result.AbstractResult;

public class ResultsParser {
	public static void updateResultTreeView(Tree tree, List<AbstractResult<?>> results) {
		models = new HashMap<String, TreeItem>();

		for (TreeItem item : tree.getItems())
			item.dispose();

		for (AbstractResult<?> r : results) {
			addResultToTreeView(tree, r.getData());
		}
	}

	public static void updateResultTextView(StyledText styledText, List<AbstractResult<?>> results) {
		styledText.setText("");

		for (AbstractResult<?> r : results) {
			addResultToTextView(styledText, r.getData());
		}
	}

	public static String parse(List<AbstractResult<?>> results) {
		StyledText tempStyledText = new StyledText(null, SWT.NONE);
		
		updateResultTextView(tempStyledText, results);
		
		return tempStyledText.getText();
	}
	
	private static void addResultToTreeView(Tree tree, JSONObject data) {
		String fullName = data.getString("name");
		int projDot = fullName.indexOf('.');
		int modelDot = projDot + 1 + fullName.substring(projDot + 1).indexOf('.');

		String model = fullName.substring(projDot + 1, modelDot);
		String name = fullName.substring(modelDot + 1);

		TreeItem modelItem = models.get(model);
		if (modelItem == null) {
			modelItem = new TreeItem(tree, SWT.NONE);
			modelItem.setText(model);
			models.put(model, modelItem);
		}

		TreeItem resultTreeItem = new TreeItem(modelItem, SWT.NONE);
		resultTreeItem.setText(new String[] { name });

		data.keySet().stream().filter(e -> e != "type" && e != "name").forEach(e -> {
			String[] text = new String[] { e, data.get(e).toString() };

			TreeItem child = new TreeItem(resultTreeItem, SWT.NONE);
			child.setText(text);
		});
	}

	private static void addResultToTextView(StyledText styledText, JSONObject data) {
		String originText = styledText.getText();

		StyleRange styleRange = new StyleRange();
		styleRange.start = originText.length();
		styleRange.fontStyle = SWT.BOLD;

		final StringBuilder resultText = new StringBuilder();
		resultText.append(originText + (originText.length() > 0 ? "\n\n" : "") + data.getString("name"));

		styleRange.length = resultText.length() - originText.length();

		LinkedList<StyleRange> numberStyles = new LinkedList<StyleRange>();

		data.keySet().stream().filter(e -> e != "type" && e != "name").forEach(e -> {
			String[] text = new String[] { e, data.get(e).toString() };

			StyleRange numberStyle = new StyleRange();
			numberStyle.start = resultText.length() + text[0].length() + 4;
			numberStyle.length = text[1].length();
			numberStyle.fontStyle = SWT.ITALIC;
			numberStyle.foreground = styledText.getDisplay().getSystemColor(SWT.COLOR_DARK_BLUE);

			numberStyles.add(numberStyle);

			resultText.append("\n\t" + text[0] + ": " + text[1]);
		});

		StyleRange[] styles = styledText.getStyleRanges();
		styledText.setText(resultText.toString());
		styledText.setStyleRanges(styles);
		styledText.setStyleRange(styleRange);

		for (StyleRange style : numberStyles)
			styledText.setStyleRange(style);
	}

	private static HashMap<String, TreeItem> models;
}
