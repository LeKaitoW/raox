package ru.bmstu.rk9.rdo.lib;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.bmstu.rk9.rdo.lib.CollectedDataNode.AbstractIndex;
import ru.bmstu.rk9.rdo.lib.CollectedDataNode.PatternIndex;
import ru.bmstu.rk9.rdo.lib.CollectedDataNode.ResourceIndex;
import ru.bmstu.rk9.rdo.lib.CollectedDataNode.ResourceParameterIndex;
import ru.bmstu.rk9.rdo.lib.CollectedDataNode.ResultIndex;
import ru.bmstu.rk9.rdo.lib.Database.Entry;
import ru.bmstu.rk9.rdo.lib.Database.TypeSize;

public class PlotDataParser {

	private static final Map<AbstractIndex, Integer> lastItemMap = new HashMap<AbstractIndex, Integer>();

	public final static class PlotItem {
		PlotItem(final double x, final double y) {
			this.x = x;
			this.y = y;
		}

		final public double x;
		final public double y;
	}

	public static Map<AbstractIndex, Integer> getLastItemMap() {
		return lastItemMap;
	}

	public static List<PlotItem> parseEntries(final CollectedDataNode node) {
		final AbstractIndex index = node.getIndex();
		switch (index.getType()) {
		case RESOURCE_PARAMETER:
			final ResourceParameterIndex resourceParameterIndex = (ResourceParameterIndex) index;
			final CollectedDataNode resourceNode = node.getParent();
			final ResourceIndex resourceIndex = (ResourceIndex) resourceNode
					.getIndex();
			return parseResourceParameter(resourceParameterIndex, resourceIndex);
		case RESULT:
			final ResultIndex resultIndex = (ResultIndex) index;
			return parseResult(resultIndex);
		case PATTERN:
			final PatternIndex patternIndex = (PatternIndex) index;
			return parsePattern(patternIndex);
		default:
			return null;
		}
	}

	private static List<PlotItem> parsePattern(final PatternIndex patternIndex) {
		final List<PlotItem> dataset = new ArrayList<PlotItem>();
		final List<Integer> entriesNumbers = patternIndex.getEntryNumbers();
		final List<Entry> allEntries = Simulator.getDatabase().getAllEntries();
		int count = 0;
		dataset.add(new PlotItem(0, count));
		for (int i = 0; i < entriesNumbers.size(); i++) {
			final int currentNumberEntry = entriesNumbers.get(i);
			final Entry currentEntry = allEntries.get(currentNumberEntry);
			final ByteBuffer header = Tracer
					.prepareBufferForReading(currentEntry.header);

			Tracer.skipPart(header, TypeSize.BYTE);
			final double time = header.getDouble();

			final Database.PatternType entryType = Database.PatternType
					.values()[header.get()];

			switch (entryType) {
			case OPERATION_BEGIN:
				count++;
				break;
			case OPERATION_END:
				count--;
				break;
			default:
				return null;
			}
			dataset.add(new PlotItem(time, count));
		}
		return dataset;
	}

	private static List<PlotItem> parseResult(final ResultIndex resultIndex) {
		final List<PlotItem> dataset = new ArrayList<PlotItem>();
		final List<Integer> entriesNumbers = resultIndex.getEntryNumbers();
		final List<Entry> allEntries = Simulator.getDatabase().getAllEntries();

		int i = 0;
		if (lastItemMap.containsKey(resultIndex)) {
			i = lastItemMap.get(resultIndex);
		}

		int currentNumberEntry = 0;
		while (i < entriesNumbers.size()) {
			currentNumberEntry = entriesNumbers.get(i);
			final Entry currentEntry = allEntries.get(currentNumberEntry);
			final ByteBuffer header = Tracer
					.prepareBufferForReading(currentEntry.header);
			final ByteBuffer data = Tracer
					.prepareBufferForReading(currentEntry.data);

			Tracer.skipPart(header, TypeSize.BYTE);
			final double time = header.getDouble();
			final int resultNum = header.getInt();
			final ResultCache resultCache = Simulator.getModelStructureCache().resultsInfo
					.get(resultNum);
			PlotItem item = null;
			switch (resultCache.valueType) {
			case INTEGER:
				item = new PlotItem(time, data.getInt());
				break;
			case REAL:
				item = new PlotItem(time, data.getDouble());
				break;
			case ENUM:
				item = new PlotItem(time, data.getShort());
				break;
			case BOOLEAN:
				item = new PlotItem(time, data.get() != 0 ? 1 : 0);
				break;
			default:
				return null;
			}
			dataset.add(item);
			i++;
		}

		lastItemMap.put(resultIndex, i);

		return dataset;
	}

	private static List<PlotItem> parseResourceParameter(
			final ResourceParameterIndex resourceParameterIndex,
			final ResourceIndex resourceIndex) {
		final List<PlotItem> dataset = new ArrayList<PlotItem>();
		final List<Integer> entriesNumbers = resourceIndex.getEntryNumbers();
		final List<Entry> allEntries = Simulator.getDatabase().getAllEntries();

		for (int i = 0; i < entriesNumbers.size(); i++) {
			final int currentNumberEntry = entriesNumbers.get(i);
			final Entry currentEntry = allEntries.get(currentNumberEntry);

			final ByteBuffer header = Tracer
					.prepareBufferForReading(currentEntry.header);
			final ByteBuffer data = Tracer
					.prepareBufferForReading(currentEntry.data);

			Tracer.skipPart(header, TypeSize.BYTE);
			final double time = header.getDouble();
			Tracer.skipPart(header, TypeSize.BYTE);
			PlotItem item = null;

			switch (resourceParameterIndex.getValueType()) {
			case INTEGER:
				item = new PlotItem(time, data.getInt(resourceParameterIndex
						.getOffset()));
				break;
			case REAL:
				item = new PlotItem(time, data.getDouble(resourceParameterIndex
						.getOffset()));
				break;
			case ENUM:
				item = new PlotItem(time, data.getShort(resourceParameterIndex
						.getOffset()));
				break;
			case BOOLEAN:
				item = new PlotItem(time, data.get(resourceParameterIndex
						.getOffset()) != 0 ? 1 : 0);
				break;
			default:
				return null;
			}

			dataset.add(item);
		}
		return dataset;
	}
}
