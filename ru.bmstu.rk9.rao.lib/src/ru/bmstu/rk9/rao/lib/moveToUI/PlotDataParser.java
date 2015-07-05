package ru.bmstu.rk9.rao.lib.moveToUI;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.bmstu.rk9.rao.lib.database.Database;
import ru.bmstu.rk9.rao.lib.database.Database.Entry;
import ru.bmstu.rk9.rao.lib.database.Database.TypeSize;
import ru.bmstu.rk9.rao.lib.exception.PlotDataParserException;
import ru.bmstu.rk9.rao.lib.modelStructure.ResultCache;
import ru.bmstu.rk9.rao.lib.moveToUI.CollectedDataNode.Index;
import ru.bmstu.rk9.rao.lib.moveToUI.CollectedDataNode.PatternIndex;
import ru.bmstu.rk9.rao.lib.moveToUI.CollectedDataNode.ResourceIndex;
import ru.bmstu.rk9.rao.lib.moveToUI.CollectedDataNode.ResourceParameterIndex;
import ru.bmstu.rk9.rao.lib.moveToUI.CollectedDataNode.ResultIndex;
import ru.bmstu.rk9.rao.lib.simulator.Simulator;

public class PlotDataParser {

	private static final Map<Index, Integer> lastItemMap = new HashMap<Index, Integer>();
	private static final Map<Index, Integer> lastPatternCountMap = new HashMap<Index, Integer>();

	public final static class PlotItem {
		PlotItem(final double x, final double y) {
			this.x = x;
			this.y = y;
		}

		final public double x;
		final public double y;
	}

	public final static class ParseInfo {
		ParseInfo(final List<PlotItem> dataset, final int itemNumber) {
			this.dataset = dataset;
			this.itemNumber = itemNumber;
		}

		final public List<PlotItem> dataset;
		final public int itemNumber;
	}

	public static void removeIndexFromMaps(Index index) {
		if (!lastItemMap.isEmpty() && lastItemMap.containsKey(index)) {
			lastItemMap.remove(index);
		}
		if (!lastPatternCountMap.isEmpty()
				&& lastPatternCountMap.containsKey(index)) {
			lastPatternCountMap.remove(index);
		}
	}

	public static List<PlotItem> parseEntries(final CollectedDataNode node) {
		final Index index = node.getIndex();
		ParseInfo parseInfo;

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
			parseInfo = parseResourceParameter(resourceParameterIndex,
					resourceIndex, startItemNumber);
			break;
		case RESULT:
			final ResultIndex resultIndex = (ResultIndex) index;
			parseInfo = parseResult(resultIndex, startItemNumber);
			break;
		case PATTERN:
			final PatternIndex patternIndex = (PatternIndex) index;
			parseInfo = parsePattern(patternIndex, startItemNumber);
			break;
		default:
			throw new PlotDataParserException("Unexpected index type: "
					+ index.getType());
		}
		lastItemMap.put(index, parseInfo.itemNumber);

		return parseInfo.dataset;
	}

	private static ParseInfo parsePattern(final PatternIndex patternIndex,
			final int startItemNumber) {
		final List<PlotItem> dataset = new ArrayList<PlotItem>();
		final List<Integer> entriesNumbers = patternIndex.getEntryNumbers();
		final List<Entry> allEntries = Simulator.getDatabase().getAllEntries();
		int count;
		int itemNumber = startItemNumber;

		if (lastPatternCountMap.containsKey(patternIndex)) {
			count = lastPatternCountMap.get(patternIndex);
		} else {
			count = 0;
			dataset.add(new PlotItem(0, count));
		}

		while (itemNumber < entriesNumbers.size()) {
			int currentEntryNumber = entriesNumbers.get(itemNumber);
			final Entry currentEntry = allEntries.get(currentEntryNumber);
			final ByteBuffer header = Tracer
					.prepareBufferForReading(currentEntry.getHeader());

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
				throw new PlotDataParserException("Unexpected entry type: "
						+ entryType);
			}
			dataset.add(new PlotItem(time, count));
			itemNumber++;
		}

		lastPatternCountMap.put(patternIndex, count);

		return new ParseInfo(dataset, itemNumber);
	}

	private static ParseInfo parseResult(final ResultIndex resultIndex,
			final int startItemNumber) {
		final List<PlotItem> dataset = new ArrayList<PlotItem>();
		final List<Integer> entriesNumbers = resultIndex.getEntryNumbers();
		final List<Entry> allEntries = Simulator.getDatabase().getAllEntries();
		int itemNumber = startItemNumber;

		while (itemNumber < entriesNumbers.size()) {
			int currentEntryNumber = entriesNumbers.get(itemNumber);
			final Entry currentEntry = allEntries.get(currentEntryNumber);
			final ByteBuffer header = Tracer
					.prepareBufferForReading(currentEntry.getHeader());
			final ByteBuffer data = Tracer.prepareBufferForReading(currentEntry
					.getData());

			Tracer.skipPart(header, TypeSize.BYTE);
			final double time = header.getDouble();
			final int resultNum = header.getInt();
			final ResultCache resultCache = Simulator.getModelStructureCache()
					.getResultsInfo().get(resultNum);
			PlotItem item = null;
			switch (resultCache.getValueType()) {
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
				throw new PlotDataParserException("Unexpected value type: "
						+ resultCache.getValueType());
			}
			dataset.add(item);
			itemNumber++;
		}

		return new ParseInfo(dataset, itemNumber);
	}

	private static ParseInfo parseResourceParameter(
			final ResourceParameterIndex resourceParameterIndex,
			final ResourceIndex resourceIndex, final int startItemNumber) {
		final List<PlotItem> dataset = new ArrayList<PlotItem>();
		final List<Integer> entriesNumbers = resourceIndex.getEntryNumbers();
		final List<Entry> allEntries = Simulator.getDatabase().getAllEntries();
		int itemNumber = startItemNumber;

		while (itemNumber < entriesNumbers.size()) {
			int currentEntryNumber = entriesNumbers.get(itemNumber);
			final Entry currentEntry = allEntries.get(currentEntryNumber);

			final ByteBuffer header = Tracer
					.prepareBufferForReading(currentEntry.getHeader());
			final ByteBuffer data = Tracer.prepareBufferForReading(currentEntry
					.getData());

			Tracer.skipPart(header, TypeSize.BYTE);
			final double time = header.getDouble();
			Tracer.skipPart(header, TypeSize.BYTE);
			PlotItem item = null;

			switch (resourceParameterIndex.getValueCache().getType()) {
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
				throw new PlotDataParserException("Unexpected value type: "
						+ resourceParameterIndex.getValueCache().getType());
			}

			dataset.add(item);
			itemNumber++;
		}

		return new ParseInfo(dataset, itemNumber);
	}

	public static List<String> getEnumNames(final CollectedDataNode node) {
		List<String> enumNames = null;
		final Index index = node.getIndex();
		if (index != null) {
			switch (index.getType()) {
			case RESOURCE_PARAMETER:
				enumNames = ((ResourceParameterIndex) index).getValueCache()
						.getEnumNames();
				break;
			case RESULT:
				int resultNumber = index.getNumber();
				final ResultCache resultCache = Simulator
						.getModelStructureCache().getResultsInfo()
						.get(resultNumber);
				enumNames = resultCache.getEnumNames();
				break;
			default:
				break;
			}
		}

		return enumNames;
	}
}
