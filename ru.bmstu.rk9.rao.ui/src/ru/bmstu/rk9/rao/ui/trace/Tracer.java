package ru.bmstu.rk9.rao.ui.trace;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import ru.bmstu.rk9.rao.lib.database.Database;
import ru.bmstu.rk9.rao.lib.database.Database.DataType;
import ru.bmstu.rk9.rao.lib.database.Database.Entry;
import ru.bmstu.rk9.rao.lib.database.Database.EntryType;
import ru.bmstu.rk9.rao.lib.database.Database.ProcessEntryType;
import ru.bmstu.rk9.rao.lib.database.Database.ResultType;
import ru.bmstu.rk9.rao.lib.database.Database.TypeSize;
import ru.bmstu.rk9.rao.lib.dpt.Search;
import ru.bmstu.rk9.rao.lib.modeldata.ModelStructureConstants;
import ru.bmstu.rk9.rao.lib.modeldata.StaticModelData;
import ru.bmstu.rk9.rao.lib.naming.NamingHelper;
import ru.bmstu.rk9.rao.lib.process.Hold.HoldAction;
import ru.bmstu.rk9.rao.lib.process.Queue.QueueAction;
import ru.bmstu.rk9.rao.lib.process.SelectPath.SelectPathOutputs;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator;
import ru.bmstu.rk9.rao.ui.trace.StringJoiner.StringFormat;

public class Tracer {
	public static enum TraceType {
		RESOURCE_CREATE("RC"), RESOURCE_KEEP("RK"), RESOURCE_ERASE("RE"), SYSTEM("ES"), OPERATION_BEGIN("EB"),
		OPERATION_END("EF"), EVENT("EI"), RULE("ER"), RESULT("V "), SEARCH_BEGIN("SB"), SEARCH_OPEN("SO "),
		SEARCH_SPAWN_NEW("STN"), SEARCH_SPAWN_WORSE("STD"), SEARCH_SPAWN_BETTER("STR"), SEARCH_RESOURCE_KEEP("SRK"),
		SEARCH_DECISION("SD "), SEARCH_END_ABORTED("SEA"), SEARCH_END_CONDITION("SEC"), SEARCH_END_SUCCESS("SES"),
		SEARCH_END_FAIL("SEN"), PROCESS("PR");

		private final String traceCode;

		private TraceType(String traceCode) {
			this.traceCode = traceCode;
		}

		@Override
		public String toString() {
			return traceCode;
		}
	}

	public final static class TraceOutput {
		public TraceOutput(TraceType type, String content) {
			this.type = type;
			this.content = content;
		}

		private final TraceType type;
		private final String content;

		public final TraceType type() {
			return type;
		}

		public final String content() {
			return content;
		}
	}

	static private final String delimiter = " ";

	public Tracer(StaticModelData staticModelData) {
		this.staticModelData = staticModelData;
		this.useShortNames = staticModelData.getModelStructure().getInt(ModelStructureConstants.NUMBER_OF_MODELS) == 1;
	}

	private final StaticModelData staticModelData;
	private final boolean useShortNames;

	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //
	// ------------------------------ GENERAL ------------------------------ //
	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //

	public final TraceOutput parseSerializedData(final Entry entry) {
		final EntryType type = EntryType.values()[entry.getHeader().get(TypeSize.Internal.ENTRY_TYPE_OFFSET)];
		switch (type) {
		case SYSTEM:
			return parseSystemEntry(entry);
		case RESOURCE:
			return parseResourceEntry(entry);
		case PATTERN:
			return parsePatternEntry(entry);
		case EVENT:
			return parseEventEntry(entry);
		case SEARCH:
			return parseSearchEntry(entry);
		case RESULT:
			return parseResultEntry(entry);
		case PROCESS:
			return parseProcessEntry(entry);
		default:
			throw new TracerException("Unexpected entry type: " + type);
		}
	}

	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //
	// -------------------------- SYSTEM ENTRIES --------------------------- //
	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //

	private TraceOutput parseSystemEntry(final Entry entry) {
		final ByteBuffer header = prepareBufferForReading(entry.getHeader());

		final TraceType traceType = TraceType.SYSTEM;

		skipPart(header, TypeSize.BYTE);
		final double time = header.getDouble();
		final String formattedTime = CurrentSimulator.getDatabase().getTimeFormatter().apply(time);
		final Database.SystemEntryType type = Database.SystemEntryType.values()[header.get()];

		final String headerLine = new StringJoiner(delimiter).add(traceType.toString()).add(formattedTime)
				.add(type.getDescription()).getString();

		return new TraceOutput(traceType, headerLine);
	}

	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //
	// ------------------------- RESOURCE ENTRIES -------------------------- //
	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //

	private TraceOutput parseResourceEntry(final Entry entry) {
		final ByteBuffer header = prepareBufferForReading(entry.getHeader());
		final ByteBuffer data = prepareBufferForReading(entry.getData());

		skipPart(header, TypeSize.BYTE);
		final double time = header.getDouble();
		final String formattedTime = CurrentSimulator.getDatabase().getTimeFormatter().apply(time);
		final TraceType traceType;

		final Database.ResourceEntryType entryType = Database.ResourceEntryType.values()[header.get()];
		switch (entryType) {
		case CREATED:
			traceType = TraceType.RESOURCE_CREATE;
			break;
		case ERASED:
			traceType = TraceType.RESOURCE_ERASE;
			break;
		case ALTERED:
			traceType = TraceType.RESOURCE_KEEP;
			break;
		case SEARCH:
		case SOLUTION:
			traceType = TraceType.SEARCH_RESOURCE_KEEP;
			break;
		default:
			throw new TracerException("Unexpected resource entry type: " + entryType);
		}

		final int typeNum = header.getInt();
		final String typeName = staticModelData.getResourceTypeName(typeNum);
		final int resNum = header.getInt();
		final String name = staticModelData.getResourceName(typeNum, resNum);
		final String resourceName = NamingHelper.convertName(name != null ? name : typeName + encloseIndex(resNum),
				useShortNames);

		return new TraceOutput(traceType, new StringJoiner(delimiter).add(traceType.toString()).add(formattedTime)
				.add(resourceName).add("=").add(parseResourceParameters(data, typeNum)).getString());
	}

	private String parseResourceParameters(final ByteBuffer data, final int typeNum) {
		final StringJoiner stringJoiner = new StringJoiner(StringJoiner.StringFormat.STRUCTURE);
		final int numberOfParameters = staticModelData.getNumberOfResourceTypeParameters(typeNum);
		final int finalOffset = staticModelData.getResourceTypeFinalOffset(typeNum);

		for (int paramNum = 0; paramNum < numberOfParameters; paramNum++) {
			DataType dataType = staticModelData.getResourceTypeParameterType(typeNum, paramNum);
			switch (dataType) {
			case INT:
				stringJoiner.add(data.getInt());
				break;
			case DOUBLE:
				stringJoiner.add(data.getDouble());
				break;
			case BOOLEAN:
				stringJoiner.add(data.get() != 0);
				break;
			case OTHER:
				final int index = staticModelData.getVariableWidthParameterIndex(typeNum, paramNum);
				final int stringPosition = data.getInt(finalOffset + index * TypeSize.INT);
				final int length = data.getInt(stringPosition);

				byte rawString[] = new byte[length];
				for (int i = 0; i < length; i++)
					rawString[i] = data.get(stringPosition + TypeSize.INT + i);
				stringJoiner.add(new String(rawString, StandardCharsets.UTF_8));
				break;
			}
		}

		return stringJoiner.getString();
	}

	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //
	// -------------------------- PATTERN ENTRIES -------------------------- //
	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //

	private TraceOutput parsePatternEntry(final Entry entry) {
		final ByteBuffer header = prepareBufferForReading(entry.getHeader());
		final ByteBuffer data = prepareBufferForReading(entry.getData());

		skipPart(header, TypeSize.BYTE);
		final double time = header.getDouble();
		final String formattedTime = CurrentSimulator.getDatabase().getTimeFormatter().apply(time);
		final TraceType traceType;

		final Database.PatternType entryType = Database.PatternType.values()[header.get()];
		switch (entryType) {
		case RULE:
			traceType = TraceType.RULE;
			break;
		case OPERATION_BEGIN:
			traceType = TraceType.OPERATION_BEGIN;
			break;
		case OPERATION_END:
			traceType = TraceType.OPERATION_END;
			break;
		default:
			throw new TracerException("Unexpected pattern enrty type: " + entryType);
		}

		return new TraceOutput(traceType, new StringJoiner(delimiter).add(traceType.toString()).add(formattedTime)
				.add(parsePatternData(data)).getString());
	}

	private String parsePatternData(final ByteBuffer data) {
		final StringJoiner stringJoiner = new StringJoiner(delimiter);
		int dptNumber = data.getInt();
		int activityNumber = data.getInt();
		int patternNumber = data.getInt();
		int actionNumber = data.getInt();
		final String activityName = staticModelData.getActivityName(dptNumber, activityNumber);
		stringJoiner.add(activityName + encloseIndex(actionNumber));

		final StringJoiner relResStringJoiner = new StringJoiner(StringJoiner.StringFormat.FUNCTION);

		int numberOfRelevantResources = data.getInt();
		for (int num = 0; num < numberOfRelevantResources; num++) {
			final int resNum = data.getInt();
			final String typeName = staticModelData.getRelevantResourceTypeName(patternNumber, num);
			final int typeNum = staticModelData.getResourceTypeNumber(typeName);
			final String name = staticModelData.getResourceName(typeNum, resNum);
			String resourceName = NamingHelper.convertName(name != null ? name : typeName + encloseIndex(resNum),
					useShortNames);

			relResStringJoiner.add(resourceName);
		}

		String res = stringJoiner.getString() + relResStringJoiner.getString();
		return res;
	}

	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //
	// --------------------------- EVENT ENTRIES --------------------------- //
	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //

	private TraceOutput parseEventEntry(final Entry entry) {
		final ByteBuffer header = prepareBufferForReading(entry.getHeader());
		final ByteBuffer data = prepareBufferForReading(entry.getData());

		skipPart(header, TypeSize.BYTE);
		final double time = header.getDouble();
		final String formattedTime = CurrentSimulator.getDatabase().getTimeFormatter().apply(time);
		final TraceType traceType = TraceType.EVENT;

		return new TraceOutput(traceType, new StringJoiner(delimiter).add(traceType.toString()).add(formattedTime)
				.add(parseEventData(data)).getString());
	}

	private String parseEventData(final ByteBuffer data) {
		final StringJoiner stringJoiner = new StringJoiner(delimiter);

		int eventNumber = data.getInt();
		int actionNumber = data.getInt();
		final String eventName = NamingHelper.convertName(staticModelData.getEventName(eventNumber), useShortNames);
		stringJoiner.add(eventName + encloseIndex(actionNumber));
		return stringJoiner.getString();
	}

	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //
	// -------------------------- SEARCH ENTRIES --------------------------- //
	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //

	private TraceOutput parseSearchEntry(final Entry entry) {
		final ByteBuffer header = prepareBufferForReading(entry.getHeader());

		final StringJoiner stringJoiner = new StringJoiner(delimiter);

		final TraceType traceType;
		skipPart(header, TypeSize.BYTE);
		final Database.SearchEntryType entryType = Database.SearchEntryType.values()[header.get()];
		final double time = header.getDouble();
		final String formattedTime = CurrentSimulator.getDatabase().getTimeFormatter().apply(time);
		final int dptNumber = header.getInt();

		switch (entryType) {
		case BEGIN: {
			traceType = TraceType.SEARCH_BEGIN;
			stringJoiner.add(traceType.toString()).add(formattedTime)
					.add(NamingHelper.convertName(staticModelData.getSearchName(dptNumber), useShortNames));
			break;
		}
		case END: {
			final ByteBuffer data = prepareBufferForReading(entry.getData());
			final Search.StopCode endStatus = Search.StopCode.values()[data.get()];
			switch (endStatus) {
			case ABORTED:
				traceType = TraceType.SEARCH_END_ABORTED;
				break;
			case CONDITION:
				traceType = TraceType.SEARCH_END_CONDITION;
				break;
			case SUCCESS:
				traceType = TraceType.SEARCH_END_SUCCESS;
				break;
			case FAIL:
				traceType = TraceType.SEARCH_END_FAIL;
				break;
			default:
				throw new TracerException("Unexpected search end status: " + endStatus);
			}

			skipPart(data, TypeSize.LONG * 2);
			final double finalCost = data.getDouble();
			final int countClosed = data.getInt();
			final int countOpen = data.getInt();
			final int countSpawned = data.getInt();
			stringJoiner.add(traceType.toString()).add(formattedTime)
					.add(new StringJoiner(StringFormat.ENUMERATION)
							.add("solution cost = " + finalCost).add("total nodes = " + (countClosed + countOpen) + " ("
									+ countClosed + " closed + " + countOpen + " open)")
							.add("nodes spawned = " + countSpawned).getString());
			break;
		}
		case OPEN: {
			final ByteBuffer data = prepareBufferForReading(entry.getData());
			traceType = TraceType.SEARCH_OPEN;
			final int currentNumber = data.getInt();
			skipPart(data, TypeSize.INT + 2 * TypeSize.DOUBLE);
			stringJoiner.add(traceType.toString()).add(encloseIndex(currentNumber));
			break;
		}
		case SPAWN: {
			final ByteBuffer data = prepareBufferForReading(entry.getData());
			final Search.SpawnStatus spawnStatus = Search.SpawnStatus.values()[data.get()];
			switch (spawnStatus) {
			case NEW:
				traceType = TraceType.SEARCH_SPAWN_NEW;
				break;
			case WORSE:
				traceType = TraceType.SEARCH_SPAWN_WORSE;
				break;
			case BETTER:
				traceType = TraceType.SEARCH_SPAWN_BETTER;
				break;
			default:
				throw new TracerException("Unexpected search spawn status: " + spawnStatus);
			}
			final int childNumber = data.getInt();
			final int parentNumber = data.getInt();
			final double g = data.getDouble();
			final double h = data.getDouble();
			final int edgeNumber = data.getInt();
			final int patternNumber = data.getInt();
			String edgeName = staticModelData.getEdgeName(dptNumber, edgeNumber);
			final double ruleCost = data.getDouble();
			final int numberOfRelevantResources = staticModelData.getNumberOfRelevantResources(patternNumber);

			StringJoiner relResStringJoiner = new StringJoiner(StringFormat.FUNCTION);

			for (int num = 0; num < numberOfRelevantResources; num++) {
				final int resNum = data.getInt();
				final String typeName = staticModelData.getRelevantResourceTypeName(patternNumber, num);
				final int typeNum = staticModelData.getResourceTypeNumber(typeName);
				final String name = staticModelData.getResourceName(typeNum, resNum);
				final String resourceName = NamingHelper
						.convertName(name != null ? name : typeName + encloseIndex(resNum), useShortNames);

				relResStringJoiner.add(resourceName);
			}

			stringJoiner.add(traceType.toString()).add(encloseIndex(parentNumber) + "->" + encloseIndex(childNumber))
					.add(edgeName + relResStringJoiner.getString()).add("=").add(ruleCost + ",")
					.add("[" + (g + h) + " = " + g + " + " + h + "]");
			break;
		}
		case DECISION: {
			final ByteBuffer data = prepareBufferForReading(entry.getData());
			traceType = TraceType.SEARCH_DECISION;
			final int nodeNumber = data.getInt();
			final int edgeNumber = data.getInt();
			final int patternNumber = data.getInt();
			String edgeName = staticModelData.getEdgeName(dptNumber, edgeNumber);
			final int numberOfRelevantResources = staticModelData.getNumberOfRelevantResources(patternNumber);

			StringJoiner relResStringJoiner = new StringJoiner(StringFormat.FUNCTION);
			for (int num = 0; num < numberOfRelevantResources; num++) {
				final int resNum = data.getInt();
				final String typeName = staticModelData.getRelevantResourceTypeName(patternNumber, num);
				final int typeNum = staticModelData.getResourceTypeNumber(typeName);
				final String name = staticModelData.getResourceName(typeNum, resNum);
				final String resourceName = NamingHelper
						.convertName(name != null ? name : typeName + encloseIndex(resNum), useShortNames);

				relResStringJoiner.add(resourceName);
			}

			stringJoiner.add(traceType.toString()).add(encloseIndex(nodeNumber))
					.add(edgeName + relResStringJoiner.getString());
			break;
		}
		default:
			throw new TracerException("Unexpected search entry type: " + entryType);
		}

		return new TraceOutput(traceType, stringJoiner.getString());
	}

	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //
	// -------------------------- RESULT ENTRIES --------------------------- //
	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //

	private TraceOutput parseResultEntry(final Entry entry) {
		final ByteBuffer header = prepareBufferForReading(entry.getHeader());
		final ByteBuffer data = prepareBufferForReading(entry.getData());

		skipPart(header, TypeSize.BYTE);
		final int resultNum = header.getInt();
		String resultName = NamingHelper.convertName(staticModelData.getResultName(resultNum), useShortNames);
		final double time = header.getDouble();
		final String formattedTime = CurrentSimulator.getDatabase().getTimeFormatter().apply(time);
		final ResultType resultType = ResultType.values()[header.get()];

		String stringValue = null;
		switch (resultType) {
		case OTHER:
			final int length = data.getInt();

			byte rawString[] = new byte[length];
			for (int i = 0; i < length; i++)
				rawString[i] = data.get(TypeSize.INT + i);
			stringValue = new String(rawString, StandardCharsets.UTF_8);
			break;
		case NUMBER:
			final double dataValue = data.getDouble();
			stringValue = String.valueOf(dataValue);
			break;
		default:
			break;
		}

		return new TraceOutput(TraceType.RESULT, new StringJoiner(delimiter).add(TraceType.RESULT.toString())
				.add(formattedTime).add(resultName).add("=").add(stringValue).getString());
	}

	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //
	// ------------------------- PROCESS ENTRIES --------------------------- //
	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //

	private TraceOutput parseProcessEntry(final Entry entry) {
		final ByteBuffer header = prepareBufferForReading(entry.getHeader());
		ByteBuffer data = null;
		if (entry.getData() != null)
			data = prepareBufferForReading(entry.getData());

		skipPart(header, TypeSize.BYTE);
		final double time = header.getDouble();
		final String formattedTime = CurrentSimulator.getDatabase().getTimeFormatter().apply(time);
		final ProcessEntryType entryType = ProcessEntryType.values()[header.get()];
		final String blockName = entryType.getString();
		final int transactIndex = header.getInt();

		return new TraceOutput(TraceType.PROCESS,
				new StringJoiner(delimiter).add(TraceType.PROCESS.toString()).add(formattedTime).add(blockName)
						.add(transactIndex).add(parseProcessData(data, entryType)).getString());
	}

	private String parseProcessData(final ByteBuffer data, final ProcessEntryType entryType) {
		switch (entryType) {
		case GENERATE:
		case TERMINATE:
			return "";
		case SELECT_PATH:
			SelectPathOutputs output = SelectPathOutputs.values()[data.get()];
			String outputName = output.getString();
			return outputName;
		case HOLD:
			HoldAction holdAction = HoldAction.values()[data.get()];
			String holdActionDescription = holdAction.getString();
			return holdActionDescription;
		case QUEUE:
			QueueAction queueAction = QueueAction.values()[data.get()];
			String queueActionDescription = queueAction.getString();
			return queueActionDescription;
		case SEIZE:
		case RELEASE:
			int resourceTypeIndex = data.getInt();
			int resourceIndex = data.getInt();
			String resourceName = staticModelData.getResourceName(resourceTypeIndex, resourceIndex);
			return resourceName;
		default:
			throw new TracerException("Unexpected process entry type: " + entryType);
		}
	}

	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //
	// -------------------------- HELPER METHODS --------------------------- //
	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //

	public final static void skipPart(final ByteBuffer buffer, final int size) {
		for (int i = 0; i < size; i++)
			buffer.get();
	}

	public final static String encloseIndex(final int index) {
		return "[" + String.valueOf(index) + "]";
	}

	public final static ByteBuffer prepareBufferForReading(final ByteBuffer buffer) {
		return (ByteBuffer) buffer.duplicate().rewind();
	}
}
