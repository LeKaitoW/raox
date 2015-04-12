package ru.bmstu.rk9.rdo.lib;

import java.io.ObjectInputStream.GetField;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import ru.bmstu.rk9.rdo.lib.CollectedDataNode.AbstractIndex;
import ru.bmstu.rk9.rdo.lib.CollectedDataNode.ResourceIndex;
import ru.bmstu.rk9.rdo.lib.CollectedDataNode.ResourceParameterIndex;
import ru.bmstu.rk9.rdo.lib.Database.Entry;
import ru.bmstu.rk9.rdo.lib.Database.TypeSize;

public class PlotDataParser {
	public final static class PlotItem {
		PlotItem(double x, double y) {
			this.x = x;
			this.y = y;
		}

		final public double x;
		final public double y;

		public final double x() {
			return x;
		}

		public final double y() {
			return y;
		}
	}

	public static List<PlotItem> parseEntries(CollectedDataNode node) {
		AbstractIndex index = node.getIndex();
		switch (index.getType()) {
		case RESOURCE_PARAMETER:
			ResourceParameterIndex resourceParameterIndex = (ResourceParameterIndex) index;
			CollectedDataNode resourceNode = node.getParent();
			ResourceIndex resourceIndex = (ResourceIndex) resourceNode
					.getIndex();
			return parseResourceParameter(resourceParameterIndex, resourceIndex);
		case RESULT:
		default:
			return null;
		}
	}

	private static List<PlotItem> parseResourceParameter(
			ResourceParameterIndex resourceParameterIndex,
			ResourceIndex resourceIndex) {
		List<PlotItem> dataset = new ArrayList<PlotItem>();
		List<Integer> entriesNumbers = resourceIndex.getEntryNumbers();
		List<Entry> allEntries = Simulator.getDatabase().getAllEntries();

		for (int i = 0; i < entriesNumbers.size(); i++) {
			int currentNumberEntry = entriesNumbers.get(i);
			Entry currentEntry = allEntries.get(currentNumberEntry);

			final ByteBuffer header = Tracer
					.prepareBufferForReading(currentEntry.header);
			final ByteBuffer data = Tracer
					.prepareBufferForReading(currentEntry.data);

			Tracer.skipPart(header, TypeSize.BYTE);
			final double time = header.getDouble();
			Tracer.skipPart(header, TypeSize.BYTE);
			PlotItem item = null;
			final int typeNum = header.getInt();
			final ResourceTypeCache typeInfo = Simulator
					.getModelStructureCache().resourceTypesInfo.get(typeNum);

			switch (resourceParameterIndex.getValueType()) {
			case INTEGER:
				int offsetInt = resourceParameterIndex.getOffset();
				final int paramInt = data.getInt(offsetInt);
				item = new PlotItem(time, paramInt);
				break;
			case REAL:
				int offsetReal = resourceParameterIndex.getOffset();
				final double paramReal = data.getDouble(offsetReal);
				item = new PlotItem(time, paramReal);
				break;
			case BOOLEAN:
				Tracer.skipPart(data, TypeSize.BYTE);
				break;
			case ENUM:
				Tracer.skipPart(data, TypeSize.SHORT);
				break;
			default:
				return null;
			}

			dataset.add(item);
		}
		return dataset;
	}
}
