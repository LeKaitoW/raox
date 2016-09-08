package ru.bmstu.rk9.rao.ui.plot;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

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

	private final Map<String, Integer> uniqueValues = new LinkedHashMap<>();
	private int uniqueValueCount = 0;

	public final static class PlotItem {
		PlotItem(final double x, final double y) {
			this.x = x;
			this.y = y;
		}

		final public double x;
		final public double y;
	}

	public final static class DataParserResult {
		DataParserResult(final List<PlotItem> dataset, boolean axisChanged, List<String> axisValues) {
			this.dataset = dataset;
			this.axisChanged = axisChanged;
			this.axisValues = axisValues;
		}

		public final boolean axisChanged;
		public final List<String> axisValues;
		public final List<PlotItem> dataset;
	}

	public final static class ParseInfo {
		ParseInfo(final DataParserResult dataParserResult, int itemNumber) {
			this.itemNumber = itemNumber;
			this.dataParserResult = dataParserResult;
		}

		public final int itemNumber;
		public final DataParserResult dataParserResult;
	}

	public DataParserResult parseEntries() {
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

		return parseInfo.dataParserResult;
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

		return new ParseInfo(new DataParserResult(dataset, false, null), currentItemNumber);
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

		return new ParseInfo(new DataParserResult(dataset, false, null), currentItemNumber);
	}

	private ParseInfo parseResourceParameter(final ResourceIndex resourceIndex, final int typeNumber,
			final int parameterNumber, final int startItemNumber) {
		final List<PlotItem> dataset = new ArrayList<PlotItem>();
		final List<Integer> entriesNumbers = resourceIndex.getEntryNumbers();
		final List<Entry> allEntries = CurrentSimulator.getDatabase().getAllEntries();

		final int parameterOffset = CurrentSimulator.getStaticModelData().getResourceTypeParameterOffset(typeNumber,
				parameterNumber);
		final int finalOffset = CurrentSimulator.getStaticModelData().getResourceTypeFinalOffset(typeNumber);

		boolean axisChanged = false;
		List<String> axisValues = null;

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
			case OTHER:
				final int index = CurrentSimulator.getStaticModelData().getVariableWidthParameterIndex(typeNumber,
						parameterNumber);
				final int stringPosition = data.getInt(finalOffset + index * TypeSize.INT);
				final int length = data.getInt(stringPosition);

				byte rawString[] = new byte[length];
				for (int i = 0; i < length; i++)
					rawString[i] = data.get(stringPosition + TypeSize.INT + i);
				String descriptor = new String(rawString, StandardCharsets.UTF_8);

				int value;
				if (uniqueValues.containsKey(descriptor)) {
					value = uniqueValues.get(descriptor);
				} else {
					value = uniqueValueCount++;
					uniqueValues.put(descriptor, value);
					axisChanged = true;
					axisValues = Lists.newArrayList(uniqueValues.keySet());
				}
				item = new PlotItem(time, value);

				break;
			default:
				throw new PlotDataParserException("Unexpected value type: " + dataType);
			}

			dataset.add(item);
			currentItemNumber++;
		}

		return new ParseInfo(new DataParserResult(dataset, axisChanged, axisValues), currentItemNumber);
	}
}
