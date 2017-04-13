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
import ru.bmstu.rk9.rao.lib.database.Database.ResultType;
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

	public final static class PlotItem {
		PlotItem(final double x, final double y) {
			this.x = x;
			this.y = y;
		}

		final public double x;
		final public double y;
	}

	public final static class DataParserResult {
		DataParserResult(final List<PlotItem> dataset, AxisHelper axisHelper) {
			this.dataset = dataset;
			this.axisHelper = axisHelper;
		}

		public final List<PlotItem> dataset;
		public final AxisHelper axisHelper;
	}

	public final static class AxisHelper {
		AxisHelper(boolean axisChanged, List<String> axisValues) {
			this.axisChanged = axisChanged;
			this.axisValues = axisValues;
		}

		public final boolean axisChanged;
		public final List<String> axisValues;
	}

	private final static class SymbolAxisEntry {
		SymbolAxisEntry(int value, AxisHelper axisHelper) {
			this.value = value;
			this.axisHelper = axisHelper;
		}

		public final int value;
		public final AxisHelper axisHelper;
	}

	private SymbolAxisEntry stringToSymbolAxisEntry(String descriptor) {
		boolean axisChanged = false;
		List<String> axisValues = null;

		int value;
		if (uniqueValues.containsKey(descriptor)) {
			value = uniqueValues.get(descriptor);
		} else {
			value = uniqueValues.size();
			uniqueValues.put(descriptor, value);
			axisChanged = true;
			axisValues = Lists.newArrayList(uniqueValues.keySet());
		}

		return new SymbolAxisEntry(value, new AxisHelper(axisChanged, axisValues));
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

		return new ParseInfo(new DataParserResult(dataset, new AxisHelper(false, null)), currentItemNumber);
	}

	private ParseInfo parseResult(final ResultIndex resultIndex, final int startItemNumber) {
		final List<PlotItem> dataset = new ArrayList<PlotItem>();
		final List<Integer> entriesNumbers = resultIndex.getEntryNumbers();
		final List<Entry> allEntries = CurrentSimulator.getDatabase().getAllEntries();

		AxisHelper axisHelper = new AxisHelper(false, null);

		while (currentItemNumber < entriesNumbers.size()) {
			int currentEntryNumber = entriesNumbers.get(currentItemNumber);
			final Entry currentEntry = allEntries.get(currentEntryNumber);
			final ByteBuffer data = Tracer.prepareBufferForReading(currentEntry.getData());
			final ByteBuffer header = Tracer.prepareBufferForReading(currentEntry.getHeader());

			Tracer.skipPart(header, TypeSize.BYTE);
			Tracer.skipPart(header, TypeSize.INT);
			final double time = header.getDouble();
			final ResultType resultType = ResultType.values()[header.get()];

			PlotItem item = null;
			switch (resultType) {
			case OTHER:
				final int length = data.getInt();

				byte rawString[] = new byte[length];
				for (int i = 0; i < length; i++)
					rawString[i] = data.get(TypeSize.INT + i);
				String descriptor = new String(rawString, StandardCharsets.UTF_8);

				SymbolAxisEntry entry = stringToSymbolAxisEntry(descriptor);
				axisHelper = entry.axisHelper.axisChanged ? entry.axisHelper : axisHelper;

				item = new PlotItem(time, entry.value);
				break;
			case NUMBER:
				final double dataValue = data.getDouble();
				item = new PlotItem(time, dataValue);
				break;
			default:
				break;
			}

			dataset.add(item);
			currentItemNumber++;
		}

		return new ParseInfo(new DataParserResult(dataset, axisHelper), currentItemNumber);
	}

	private ParseInfo parseResourceParameter(final ResourceIndex resourceIndex, final int typeNumber,
			final int parameterNumber, final int startItemNumber) {
		final List<PlotItem> dataset = new ArrayList<PlotItem>();
		final List<Integer> entriesNumbers = resourceIndex.getEntryNumbers();
		final List<Entry> allEntries = CurrentSimulator.getDatabase().getAllEntries();

		final int parameterOffset = CurrentSimulator.getStaticModelData().getResourceTypeParameterOffset(typeNumber,
				parameterNumber);
		final int finalOffset = CurrentSimulator.getStaticModelData().getResourceTypeFinalOffset(typeNumber);

		AxisHelper axisHelper = new AxisHelper(false, null);

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
				String boolValueStr = data.get(parameterOffset) != 0 ? "true" : "false";

				SymbolAxisEntry boolValueEntry = stringToSymbolAxisEntry(boolValueStr);
				axisHelper = boolValueEntry.axisHelper.axisChanged ? boolValueEntry.axisHelper : axisHelper;

				item = new PlotItem(time, boolValueEntry.value);
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

				SymbolAxisEntry entry = stringToSymbolAxisEntry(descriptor);
				axisHelper = entry.axisHelper.axisChanged ? entry.axisHelper : axisHelper;

				item = new PlotItem(time, entry.value);

				break;
			default:
				throw new PlotDataParserException("Unexpected value type: " + dataType);
			}

			dataset.add(item);
			currentItemNumber++;
		}

		return new ParseInfo(new DataParserResult(dataset, axisHelper), currentItemNumber);
	}
}
