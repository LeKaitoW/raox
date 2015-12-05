package ru.bmstu.rk9.rao.ui.plot;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import ru.bmstu.rk9.rao.lib.database.CollectedDataNode;
import ru.bmstu.rk9.rao.lib.database.Database;
import ru.bmstu.rk9.rao.lib.database.CollectedDataNode.Index;
import ru.bmstu.rk9.rao.lib.database.CollectedDataNode.PatternIndex;
import ru.bmstu.rk9.rao.lib.database.CollectedDataNode.ResourceIndex;
import ru.bmstu.rk9.rao.lib.database.CollectedDataNode.ResourceParameterIndex;
import ru.bmstu.rk9.rao.lib.database.CollectedDataNode.ResultIndex;
import ru.bmstu.rk9.rao.lib.database.Database.Entry;
import ru.bmstu.rk9.rao.lib.database.Database.TypeSize;
import ru.bmstu.rk9.rao.lib.modelStructure.ResultCache;
import ru.bmstu.rk9.rao.lib.simulator.Simulator;
import ru.bmstu.rk9.rao.ui.trace.Tracer;

public class PlotDataParser {

	public PlotDataParser(final CollectedDataNode node) {
		this.node = node;
	}

	private final CollectedDataNode node;
	private int currentItemNumber = 0;
	private int patternCount = 0;

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

	public List<PlotItem> parseEntries() {
		final Index index = node.getIndex();
		ParseInfo parseInfo;

		switch (index.getType()) {
		case RESOURCE_PARAMETER:
			final ResourceParameterIndex resourceParameterIndex = (ResourceParameterIndex) index;
			final CollectedDataNode resourceNode = node.getParent();
			final ResourceIndex resourceIndex = (ResourceIndex) resourceNode.getIndex();
			parseInfo = parseResourceParameter(resourceParameterIndex, resourceIndex, currentItemNumber);
			break;
		case RESULT:
			final ResultIndex resultIndex = (ResultIndex) index;
			parseInfo = parseResult(resultIndex, currentItemNumber);
			break;
		case PATTERN:
			final PatternIndex patternIndex = (PatternIndex) index;
			parseInfo = parsePattern(patternIndex, currentItemNumber);
			break;
		default:
			throw new PlotDataParserException("Unexpected index type: " + index.getType());
		}
		currentItemNumber = parseInfo.itemNumber;

		return parseInfo.dataset;
	}

	private ParseInfo parsePattern(final PatternIndex patternIndex, final int startItemNumber) {
		final List<PlotItem> dataset = new ArrayList<PlotItem>();
		final List<Integer> entriesNumbers = patternIndex.getEntryNumbers();
		final List<Entry> allEntries = Simulator.getDatabase().getAllEntries();

		if (patternCount == 0) {
			dataset.add(new PlotItem(0, patternCount));
		}

		while (currentItemNumber < entriesNumbers.size()) {
			int currentEntryNumber = entriesNumbers.get(currentItemNumber);
			final Entry currentEntry = allEntries.get(currentEntryNumber);
			final ByteBuffer header = Tracer.prepareBufferForReading(currentEntry.getHeader());

			Tracer.skipPart(header, TypeSize.BYTE);
			final double time = header.getDouble();

			final Database.PatternType entryType = Database.PatternType.values()[header.get()];

			switch (entryType) {
			case OPERATION_BEGIN:
				patternCount++;
				break;
			case OPERATION_END:
				patternCount--;
				break;
			default:
				throw new PlotDataParserException("Unexpected entry type: " + entryType);
			}
			dataset.add(new PlotItem(time, patternCount));
			currentItemNumber++;
		}

		return new ParseInfo(dataset, currentItemNumber);
	}

	private ParseInfo parseResult(final ResultIndex resultIndex, final int startItemNumber) {
		final List<PlotItem> dataset = new ArrayList<PlotItem>();
		final List<Integer> entriesNumbers = resultIndex.getEntryNumbers();
		final List<Entry> allEntries = Simulator.getDatabase().getAllEntries();

		while (currentItemNumber < entriesNumbers.size()) {
			int currentEntryNumber = entriesNumbers.get(currentItemNumber);
			final Entry currentEntry = allEntries.get(currentEntryNumber);
			final ByteBuffer header = Tracer.prepareBufferForReading(currentEntry.getHeader());
			final ByteBuffer data = Tracer.prepareBufferForReading(currentEntry.getData());

			Tracer.skipPart(header, TypeSize.BYTE);
			final double time = header.getDouble();
			final int resultNum = header.getInt();
			final ResultCache resultCache = Simulator.getModelStructureCache().getResultsInfo().get(resultNum);
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
				throw new PlotDataParserException("Unexpected value type: " + resultCache.getValueType());
			}
			dataset.add(item);
			currentItemNumber++;
		}

		return new ParseInfo(dataset, currentItemNumber);
	}

	private ParseInfo parseResourceParameter(final ResourceParameterIndex resourceParameterIndex,
			final ResourceIndex resourceIndex, final int startItemNumber) {
		final List<PlotItem> dataset = new ArrayList<PlotItem>();
		final List<Integer> entriesNumbers = resourceIndex.getEntryNumbers();
		final List<Entry> allEntries = Simulator.getDatabase().getAllEntries();

		while (currentItemNumber < entriesNumbers.size()) {
			int currentEntryNumber = entriesNumbers.get(currentItemNumber);
			final Entry currentEntry = allEntries.get(currentEntryNumber);

			final ByteBuffer header = Tracer.prepareBufferForReading(currentEntry.getHeader());
			final ByteBuffer data = Tracer.prepareBufferForReading(currentEntry.getData());

			Tracer.skipPart(header, TypeSize.BYTE);
			final double time = header.getDouble();
			Tracer.skipPart(header, TypeSize.BYTE);
			PlotItem item = null;

			switch (resourceParameterIndex.getValueCache().getType()) {
			case INTEGER:
				item = new PlotItem(time, data.getInt(resourceParameterIndex.getOffset()));
				break;
			case REAL:
				item = new PlotItem(time, data.getDouble(resourceParameterIndex.getOffset()));
				break;
			case ENUM:
				item = new PlotItem(time, data.getShort(resourceParameterIndex.getOffset()));
				break;
			case BOOLEAN:
				item = new PlotItem(time, data.get(resourceParameterIndex.getOffset()) != 0 ? 1 : 0);
				break;
			default:
				throw new PlotDataParserException(
						"Unexpected value type: " + resourceParameterIndex.getValueCache().getType());
			}

			dataset.add(item);
			currentItemNumber++;
		}

		return new ParseInfo(dataset, currentItemNumber);
	}

	public static List<String> getEnumNames(final CollectedDataNode node) {
		List<String> enumNames = null;
		final Index index = node.getIndex();
		if (index != null) {
			switch (index.getType()) {
			case RESOURCE_PARAMETER:
				enumNames = ((ResourceParameterIndex) index).getValueCache().getEnumNames();
				break;
			case RESULT:
				int resultNumber = index.getNumber();
				final ResultCache resultCache = Simulator.getModelStructureCache().getResultsInfo().get(resultNumber);
				enumNames = resultCache.getEnumNames();
				break;
			default:
				break;
			}
		}

		return enumNames;
	}
}
