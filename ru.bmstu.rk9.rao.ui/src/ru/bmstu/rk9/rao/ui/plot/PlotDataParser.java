package ru.bmstu.rk9.rao.ui.plot;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import ru.bmstu.rk9.rao.lib.database.CollectedDataNode;
import ru.bmstu.rk9.rao.lib.database.CollectedDataNode.Index;
import ru.bmstu.rk9.rao.lib.database.CollectedDataNode.PatternIndex;
import ru.bmstu.rk9.rao.lib.database.CollectedDataNode.ResourceIndex;
import ru.bmstu.rk9.rao.lib.database.CollectedDataNode.ResourceParameterIndex;
import ru.bmstu.rk9.rao.lib.database.CollectedDataNode.ResourceTypeIndex;
import ru.bmstu.rk9.rao.lib.database.CollectedDataNode.ResultIndex;
import ru.bmstu.rk9.rao.lib.database.Database;
import ru.bmstu.rk9.rao.lib.database.Database.DataType;
import ru.bmstu.rk9.rao.lib.database.Database.Entry;
import ru.bmstu.rk9.rao.lib.database.Database.TypeSize;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator;
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
			final ResourceTypeIndex resourceTypeIndex = (ResourceTypeIndex) resourceNode.getParent().getIndex();
			parseInfo = parseResourceParameter(resourceIndex, resourceTypeIndex.getNumber(),
					resourceParameterIndex.getNumber(), currentItemNumber);
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
		final List<Entry> allEntries = CurrentSimulator.getDatabase().getAllEntries();

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

		while (currentItemNumber < entriesNumbers.size()) {
			PlotItem item = null;
			// TODO implement
			dataset.add(item);
			currentItemNumber++;
		}

		return new ParseInfo(dataset, currentItemNumber);
	}

	private ParseInfo parseResourceParameter(final ResourceIndex resourceIndex, final int typeNumber,
			final int parameterNumber, final int startItemNumber) {
		final List<PlotItem> dataset = new ArrayList<PlotItem>();
		final List<Integer> entriesNumbers = resourceIndex.getEntryNumbers();
		final List<Entry> allEntries = CurrentSimulator.getDatabase().getAllEntries();

		final int parameterOffset = CurrentSimulator.getStaticModelData().getResourceTypeParameterOffset(typeNumber,
				parameterNumber);
		while (currentItemNumber < entriesNumbers.size()) {
			int currentEntryNumber = entriesNumbers.get(currentItemNumber);
			final Entry currentEntry = allEntries.get(currentEntryNumber);

			final ByteBuffer header = Tracer.prepareBufferForReading(currentEntry.getHeader());
			final ByteBuffer data = Tracer.prepareBufferForReading(currentEntry.getData());

			Tracer.skipPart(header, TypeSize.BYTE);
			final double time = header.getDouble();
			Tracer.skipPart(header, TypeSize.BYTE);
			PlotItem item = null;

			DataType dataType = CurrentSimulator.getStaticModelData().getResourceTypeParameterType(typeNumber,
					parameterNumber);
			switch (dataType) {
			case INT:
				item = new PlotItem(time, data.getInt(parameterOffset));
				break;
			case DOUBLE:
				item = new PlotItem(time, data.getDouble(parameterOffset));
				break;
			case BOOLEAN:
				item = new PlotItem(time, data.get(parameterOffset) != 0 ? 1 : 0);
				break;
			default:
				throw new PlotDataParserException("Unexpected value type: " + dataType);
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
				// TODO fix this when enums serialization is implemented
				enumNames = null;
				break;
			case RESULT:
				// TODO fix this when enums serialization is implemented
				enumNames = new ArrayList<String>();
				break;
			default:
				break;
			}
		}

		return enumNames;
	}
}
