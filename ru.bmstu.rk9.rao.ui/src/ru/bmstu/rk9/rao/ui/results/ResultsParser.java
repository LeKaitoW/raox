package ru.bmstu.rk9.rao.ui.results;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import ru.bmstu.rk9.rao.lib.json.JSONObject;
import ru.bmstu.rk9.rao.lib.result.AbstractResult;

public class ResultsParser {
	private static class ParsedStyledText {
		private String parsedText;
		private LinkedList<StyleRange> styleRanges;

		public ParsedStyledText(String parsedText, LinkedList<StyleRange> styleRanges) {
			this.parsedText = parsedText;
			this.styleRanges = styleRanges;
		}
	}

	public static void updateResultTreeView(Tree tree, List<AbstractResult<?>> results) {

		for (TreeItem item : tree.getItems())
			item.dispose();

		for (AbstractResult<?> r : results) {
			addResultToTreeView(tree, r.getData());
		}
	}

	public static void updateResultTextView(StyledText styledText, List<AbstractResult<?>> results) {
		styledText.setText("");

		ParsedStyledText parsedStyleText = parse(results);

		styledText.setText(parsedStyleText.parsedText);

		for (StyleRange styleRange : parsedStyleText.styleRanges)
			styledText.setStyleRange(styleRange);
	}

	public static String parseAsString(List<AbstractResult<?>> results) {
		return parse(results).parsedText;
	}

	private static void addResultToTreeView(Tree tree, JSONObject data) {
		String fullName = data.getString("name");
		int projDot = fullName.indexOf('.');
		int modelDot = projDot + 1 + fullName.substring(projDot + 1).indexOf('.');

		String model = fullName.substring(projDot + 1, modelDot);
		String name = fullName.substring(modelDot + 1);

		Optional<TreeItem> foundModelItem = Arrays.stream(tree.getItems())
				.filter(item -> item.getText().equalsIgnoreCase(model)).findFirst();
		TreeItem modelItem;
		if (foundModelItem.isPresent()) {
			modelItem = foundModelItem.get();
		} else {
			modelItem = new TreeItem(tree, SWT.NONE);
			modelItem.setText(model);
		}

		TreeItem resultTreeItem = new TreeItem(modelItem, SWT.NONE);
		resultTreeItem.setText(new String[] { name });

		data.keySet().stream().sequential().filter(e -> e != "type" && e != "name").forEach(e -> {
			String[] text = new String[] { e, data.get(e).toString() };

			TreeItem child = new TreeItem(resultTreeItem, SWT.NONE);
			child.setText(text);
		});
	}

	private static ParsedStyledText parse(List<AbstractResult<?>> results) {
		final StringBuilder resultParsedText = new StringBuilder();
		LinkedList<StyleRange> styles = new LinkedList<StyleRange>();

		for (AbstractResult<?> r : results) {
			JSONObject data = r.getData();

			StyleRange nameStyleRange = new StyleRange();
			nameStyleRange.start = resultParsedText.length();
			nameStyleRange.fontStyle = SWT.BOLD;

			if (resultParsedText.length() > 0)
				resultParsedText.append(System.lineSeparator()).append(System.lineSeparator());
			resultParsedText.append(data.getString("name"));

			nameStyleRange.length = resultParsedText.length() - nameStyleRange.start;
			styles.add(nameStyleRange);

			data.keySet().stream().sequential().filter(e -> e != "type" && e != "name").forEach(e -> {
				String[] text = new String[] { e, data.get(e).toString() };

				StyleRange numberStyle = new StyleRange();
				numberStyle.start = resultParsedText.length() + text[0].length() + 4;
				numberStyle.length = text[1].length();
				numberStyle.fontStyle = SWT.ITALIC;
				numberStyle.foreground = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_BLUE);

				styles.add(numberStyle);

				resultParsedText.append(System.lineSeparator()).append("  " + text[0] + ": " + text[1]);
			});
		}

		return new ParsedStyledText(resultParsedText.toString(), styles);
	}
}
