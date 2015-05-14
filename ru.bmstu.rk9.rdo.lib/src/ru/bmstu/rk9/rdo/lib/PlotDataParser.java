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
	private static final Map<AbstractIndex, Integer> lastPatternCountMap = new HashMap<AbstractIndex, Integer>();

	public final static class PlotItem {
		PlotItem(final double x, final double y) {
			this.x = x;
			this.y = y;
		}

		final public double x;
		final public double y;
	}

	public static void removeIndexFromMaps(AbstractIndex index) {
		if (!lastItemMap.isEmpty() && lastItemMap.containsKey(index)) {
			lastItemMap.remove(index);
		}
		if (!lastPatternCountMap.isEmpty()
				&& lastPatternCountMap.containsKey(index)) {
			lastPatternCountMap.remove(index);
		}
	}

	public static List<PlotItem> parseEntries(final CollectedDataNode node) {
		final AbstractIndex index = node.getIndex();

		int startItemNumber = 0;
		if (lastItemMap.containsKey(index)) {
			startItemNumber = lastItemMap.get(index);
		}

		switch (index.getType()) {
		case RESOURCE_PARAMETER:
			final ResourceParameterIndex resourceParameterIndex = (ResourceParameterIndex) index;
			final CollectedDataNode resourceNode = node.getParent();
			final ResourceIndex resourceIndex = (ResourceIndex) resourceNode
					.getIndex();
			return parseResourceParameter(resourceParameterIndex,
					resourceIndex, startItemNumber);
		case RESULT:
			final ResultIndex resultIndex = (ResultIndex) index;
			return parseResult(resultIndex, startItemNumber);
		case PATTERN:
			final PatternIndex patternIndex = (PatternIndex) index;
			return parsePattern(patternIndex, startItemNumber);
		default:
			return null;
		}
	}

	private static List<PlotItem> parsePattern(final PatternIndex patternIndex,
			int startItemNumber) {
		final List<PlotItem> dataset = new ArrayList<PlotItem>();
		final List<Integer> entriesNumbers = patternIndex.getEntryNumbers();
		final List<Entry> allEntries = Simulator.getDatabase().getAllEntries();
		int count;

		if (lastPatternCountMap.containsKey(patternIndex)) {
			count = lastPatternCountMap.get(patternIndex);
		} else {
			count = 0;
			dataset.add(new PlotItem(0, count));
		}

		while (startItemNumber < entriesNumbers.size()) {
			int currentEntryNumber = entriesNumbers.get(startItemNumber);
			final Entry currentEntry = allEntries.get(currentEntryNumber);
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
			startItemNumber++;
		}

		lastPatternCountMap.put(patternIndex, count);
		lastItemMap.put(patternIndex, startItemNumber);

		return dataset;
	}

	private static List<PlotItem> parseResult(final ResultIndex resultIndex,
			int startItemNumber) {
		final List<PlotItem> dataset = new ArrayList<PlotItem>();
		final List<Integer> entriesNumbers = resultIndex.getEntryNumbers();
		final List<Entry> allEntries = Simulator.getDatabase().getAllEntries();

		while (startItemNumber < entriesNumbers.size()) {
			int currentEntryNumber = entriesNumbers.get(startItemNumber);
			final Entry currentEntry = allEntries.get(currentEntryNumber);
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
			startItemNumber++;
		}

		lastItemMap.put(resultIndex, startItemNumber);

		return dataset;
	}

	private static List<PlotItem> parseResourceParameter(
			final ResourceParameterIndex resourceParameterIndex,
			final ResourceIndex resourceIndex, int startItemNumber) {
		final List<PlotItem> dataset = new ArrayList<PlotItem>();
		final List<Integer> entriesNumbers = resourceIndex.getEntryNumbers();
		final List<Entry> allEntries = Simulator.getDatabase().getAllEntries();

		while (startItemNumber < entriesNumbers.size()) {
			int currentEntryNumber = entriesNumbers.get(startItemNumber);
			final Entry currentEntry = allEntries.get(currentEntryNumber);

			final ByteBuffer header = Tracer
					.prepareBufferForReading(currentEntry.header);
			final ByteBuffer data = Tracer
					.prepareBufferForReading(currentEntry.data);

			Tracer.skipPart(header, TypeSize.BYTE);
			final double time = header.getDouble();
			Tracer.skipPart(header, TypeSize.BYTE);
			PlotItem item = null;

			switch (resourceParameterIndex.getValueCache().type) {
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
			startItemNumber++;
		}

		lastItemMap.put(resourceParameterIndex, startItemNumber);

		return dataset;
	}

	public static List<String> getEnumNames(final CollectedDataNode node) {
		List<String> enumNames = null;
		final AbstractIndex index = node.getIndex();
		if (index != null) {
			switch (index.getType()) {
			case RESOURCE_PARAMETER:
				enumNames = ((ResourceParameterIndex) index).getValueCache().enumNames;
				break;
			case RESULT:
				int resultNumber = index.getNumber();
				final ResultCache resultCache = Simulator
						.getModelStructureCache().resultsInfo.get(resultNumber);
				enumNames = resultCache.enumNames;
				break;
			default:
				break;
			}
		}
		return enumNames;

	}
}
