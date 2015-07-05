package ru.bmstu.rk9.rao.lib.moveToUI;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import ru.bmstu.rk9.rao.lib.common.Subscriber;
import ru.bmstu.rk9.rao.lib.database.Database;
import ru.bmstu.rk9.rao.lib.database.Database.Entry;
import ru.bmstu.rk9.rao.lib.database.Database.EntryType;
import ru.bmstu.rk9.rao.lib.database.Database.TypeSize;
import ru.bmstu.rk9.rao.lib.dpt.DecisionPointSearch;
import ru.bmstu.rk9.rao.lib.modelStructure.ActivityCache;
import ru.bmstu.rk9.rao.lib.modelStructure.ResourceTypeCache;
import ru.bmstu.rk9.rao.lib.modelStructure.ResultCache;
import ru.bmstu.rk9.rao.lib.modelStructure.ValueCache;
import ru.bmstu.rk9.rao.lib.moveToUI.RaoLibStringJoiner.StringFormat;
import ru.bmstu.rk9.rao.lib.simulator.Simulator;

public class Tracer implements Subscriber {
	public static enum TraceType {
		RESOURCE_CREATE("RC"), RESOURCE_KEEP("RK"), RESOURCE_ERASE("RE"), SYSTEM(
				"ES"), OPERATION_BEGIN("EB"), OPERATION_END("EF"), EVENT("EI"), RULE(
				"ER"), RESULT("V "), SEARCH_BEGIN("SB"), SEARCH_OPEN("SO "), SEARCH_SPAWN_NEW(
				"STN"), SEARCH_SPAWN_WORSE("STD"), SEARCH_SPAWN_BETTER("STR"), SEARCH_RESOURCE_KEEP(
				"SRK"), SEARCH_DECISION("SD "), SEARCH_END_ABORTED("SEA"), SEARCH_END_CONDITION(
				"SEC"), SEARCH_END_SUCCESS("SES"), SEARCH_END_FAIL("SEN");

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
		TraceOutput(TraceType type, String content) {
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

	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //
	// -----------------------NOTIFICATION SYSTEM -------------------------- //
	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //

	// TODO unify with DbIndexHelper notification System

	private boolean paused = true;

	public final synchronized void setPaused(boolean paused) {
		if (this.paused == paused)
			return;

		this.paused = paused;
		fireChange();
	}

	private Subscriber realTimeSubscriber = null;

	public final void setRealTimeSubscriber(Subscriber subscriber) {
		this.realTimeSubscriber = subscriber;
	}

	private final void notifyRealTimeSubscriber() {
		if (realTimeSubscriber != null)
			realTimeSubscriber.fireChange();
	}

	private Subscriber commonSubscriber = null;

	public final void setCommonSubscriber(Subscriber subscriber) {
		this.commonSubscriber = subscriber;
	}

	public final void notifyCommonSubscriber() {
		if (commonSubscriber != null)
			commonSubscriber.fireChange();
	}

	@Override
	public void fireChange() {
		if (paused)
			return;

		notifyRealTimeSubscriber();
	}

	static private final String delimiter = " ";

	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //
	// ------------------------------ GENERAL ------------------------------ //
	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //

	public final TraceOutput parseSerializedData(final Entry entry) {
		final EntryType type = EntryType.values()[entry.getHeader().get(
				TypeSize.Internal.ENTRY_TYPE_OFFSET)];
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
		default:
			return null;
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
		final Database.SystemEntryType type = Database.SystemEntryType.values()[header
				.get()];

		final String headerLine = new RaoLibStringJoiner(delimiter)
				.add(traceType.toString()).add(time).add(type.getDescription())
				.getString();

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
		final TraceType traceType;

		final Database.ResourceEntryType entryType = Database.ResourceEntryType
				.values()[header.get()];
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
			return null;
		}

		final int typeNum = header.getInt();
		final ResourceTypeCache typeInfo = Simulator.getModelStructureCache()
				.getResourceTypesInfo().get(typeNum);
		final int resNum = header.getInt();
		final String name = Simulator.getModelStructureCache()
				.getResourceNames().get(typeNum).get(resNum);

		final String resourceName = name != null ? name : typeInfo.getName()
				+ encloseIndex(resNum);

		return new TraceOutput(traceType, new RaoLibStringJoiner(delimiter)
				.add(traceType.toString()).add(time).add(resourceName).add("=")
				.add(parseResourceParameters(data, typeInfo)).getString());
	}

	private String parseResourceParameters(final ByteBuffer data,
			final ResourceTypeCache typeInfo) {
		final RaoLibStringJoiner stringJoiner = new RaoLibStringJoiner(
				RaoLibStringJoiner.StringFormat.STRUCTURE);

		for (int paramNum = 0; paramNum < typeInfo.getNumberOfParameters(); paramNum++) {
			ValueCache valueCache = typeInfo.getParamTypes().get(paramNum);
			// TODO trace arrays when they are implemented
			switch (valueCache.getType()) {
			case INTEGER:
				stringJoiner.add(data.getInt());
				break;
			case REAL:
				stringJoiner.add(data.getDouble());
				break;
			case BOOLEAN:
				stringJoiner.add(data.get() != 0);
				break;
			case ENUM:
				stringJoiner.add(valueCache.getEnumNames().get(
						(int) data.getShort()));
				break;
			case STRING:
				final int index = typeInfo.getIndexList().get(paramNum);
				final int stringPosition = data.getInt(typeInfo
						.getFinalOffset() + (index - 1) * TypeSize.Rao.INTEGER);
				final int length = data.getInt(stringPosition);

				byte rawString[] = new byte[length];
				for (int i = 0; i < length; i++)
					rawString[i] = data.get(stringPosition
							+ TypeSize.Rao.INTEGER + i);
				stringJoiner.add("\""
						+ new String(rawString, StandardCharsets.UTF_8) + "\"");
				break;
			default:
				return null;
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
		final TraceType traceType;

		final Database.PatternType entryType = Database.PatternType.values()[header
				.get()];
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
			return null;
		}

		return new TraceOutput(traceType, new RaoLibStringJoiner(delimiter)
				.add(traceType.toString()).add(time)
				.add(parsePatternData(data)).getString());
	}

	private String parsePatternData(final ByteBuffer data) {
		final RaoLibStringJoiner stringJoiner = new RaoLibStringJoiner(
				delimiter);

		int dptNumber = data.getInt();
		int activityNumber = data.getInt();
		int actionNumber = data.getInt();
		ActivityCache activity = Simulator.getModelStructureCache().decisionPointsInfo
				.get(dptNumber).getActivitiesInfo().get(activityNumber);
		int patternNumber = activity.getPatternNumber();
		stringJoiner.add(activity.getName() + encloseIndex(actionNumber));

		final RaoLibStringJoiner relResStringJoiner = new RaoLibStringJoiner(
				RaoLibStringJoiner.StringFormat.FUNCTION);

		int numberOfRelevantResources = data.getInt();
		for (int num = 0; num < numberOfRelevantResources; num++) {
			final int resNum = data.getInt();
			final int typeNum = Simulator.getModelStructureCache()
					.getPatternsInfo().get(patternNumber).getRelResTypes()
					.get(num);
			final String typeName = Simulator.getModelStructureCache()
					.getResourceTypesInfo().get(typeNum).getName();

			final String name = Simulator.getModelStructureCache()
					.getResourceNames().get(typeNum).get(resNum);
			final String resourceName = name != null ? name : typeName
					+ encloseIndex(resNum);

			relResStringJoiner.add(resourceName);
		}
		return stringJoiner.getString() + relResStringJoiner.getString();
	}

	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //
	// --------------------------- EVENT ENTRIES --------------------------- //
	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //

	private TraceOutput parseEventEntry(final Entry entry) {
		final ByteBuffer header = prepareBufferForReading(entry.getHeader());
		final ByteBuffer data = prepareBufferForReading(entry.getData());

		skipPart(header, TypeSize.BYTE);
		final double time = header.getDouble();
		final TraceType traceType = TraceType.EVENT;

		return new TraceOutput(traceType, new RaoLibStringJoiner(delimiter)
				.add(traceType.toString()).add(time).add(parseEventData(data))
				.getString());
	}

	private String parseEventData(final ByteBuffer data) {
		final RaoLibStringJoiner stringJoiner = new RaoLibStringJoiner(
				delimiter);

		int eventNumber = data.getInt();
		int actionNumber = data.getInt();
		stringJoiner.add(Simulator.getModelStructureCache().getEventNames()
				.get(eventNumber)
				+ encloseIndex(actionNumber));
		return stringJoiner.getString();
	}

	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //
	// -------------------------- SEARCH ENTRIES --------------------------- //
	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //

	private TraceOutput parseSearchEntry(final Entry entry) {
		final ByteBuffer header = prepareBufferForReading(entry.getHeader());

		final RaoLibStringJoiner stringJoiner = new RaoLibStringJoiner(
				delimiter);

		final TraceType traceType;
		skipPart(header, TypeSize.BYTE);
		final Database.SearchEntryType entryType = Database.SearchEntryType
				.values()[header.get()];
		final double time = header.getDouble();
		final int dptNumber = header.getInt();

		switch (entryType) {
		case BEGIN: {
			traceType = TraceType.SEARCH_BEGIN;
			stringJoiner
					.add(traceType.toString())
					.add(time)
					.add(Simulator.getModelStructureCache().decisionPointsInfo
							.get(dptNumber).getName());
			break;
		}
		case END: {
			final ByteBuffer data = prepareBufferForReading(entry.getData());
			final DecisionPointSearch.StopCode endStatus = DecisionPointSearch.StopCode
					.values()[data.get()];
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
				// TODO throw exception
				return null;
			}

			skipPart(data, TypeSize.LONG * 2);
			final double finalCost = data.getDouble();
			final int totalOpened = data.getInt();
			final int totalNodes = data.getInt();
			final int totalAdded = data.getInt();
			final int totalSpawned = data.getInt();
			stringJoiner
					.add(traceType.toString())
					.add(time)
					.add(new RaoLibStringJoiner(StringFormat.ENUMERATION)
							.add("solution cost = " + finalCost)
							.add("nodes opened = " + totalOpened)
							.add("nodes total = " + totalNodes)
							.add("nodes added = " + totalAdded)
							.add("nodes spawned = " + totalSpawned).getString());
			break;
		}
		case OPEN: {
			final ByteBuffer data = prepareBufferForReading(entry.getData());
			traceType = TraceType.SEARCH_OPEN;
			final int currentNumber = data.getInt();
			skipPart(data, TypeSize.INTEGER + 2 * TypeSize.DOUBLE);
			stringJoiner.add(traceType.toString()).add(
					encloseIndex(currentNumber));
			break;
		}
		case SPAWN: {
			final ByteBuffer data = prepareBufferForReading(entry.getData());
			final DecisionPointSearch.SpawnStatus spawnStatus = DecisionPointSearch.SpawnStatus
					.values()[data.get()];
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
				// TODO throw exception
				return null;
			}
			final int childNumber = data.getInt();
			final int parentNumber = data.getInt();
			final double g = data.getDouble();
			final double h = data.getDouble();
			final int ruleNumber = data.getInt();
			ActivityCache activity = Simulator.getModelStructureCache().decisionPointsInfo
					.get(dptNumber).getActivitiesInfo().get(ruleNumber);
			final int patternNumber = activity.getPatternNumber();
			final double ruleCost = data.getDouble();
			final int numberOfRelevantResources = Simulator
					.getModelStructureCache().getPatternsInfo()
					.get(patternNumber).getRelResTypes().size();

			RaoLibStringJoiner relResStringJoiner = new RaoLibStringJoiner(
					StringFormat.FUNCTION);

			for (int num = 0; num < numberOfRelevantResources; num++) {
				final int resNum = data.getInt();
				final int typeNum = Simulator.getModelStructureCache()
						.getPatternsInfo().get(patternNumber).getRelResTypes()
						.get(num);
				final String typeName = Simulator.getModelStructureCache()
						.getResourceTypesInfo().get(typeNum).getName();

				final String name = Simulator.getModelStructureCache()
						.getResourceNames().get(typeNum).get(resNum);
				final String resourceName = name != null ? name : typeName
						+ encloseIndex(resNum);

				relResStringJoiner.add(resourceName);
			}

			stringJoiner
					.add(traceType.toString())
					.add(encloseIndex(parentNumber) + "->"
							+ encloseIndex(childNumber))
					.add(activity.getName() + relResStringJoiner.getString())
					.add("=").add(ruleCost + ",")
					.add("[" + (g + h) + " = " + g + " + " + h + "]");
			break;
		}
		case DECISION: {
			final ByteBuffer data = prepareBufferForReading(entry.getData());
			traceType = TraceType.SEARCH_DECISION;
			final int nodeNumber = data.getInt();
			final int activityNumber = data.getInt();
			ActivityCache activity = Simulator.getModelStructureCache().decisionPointsInfo
					.get(dptNumber).getActivitiesInfo().get(activityNumber);
			final int patternNumber = activity.getPatternNumber();
			final int numberOfRelevantResources = Simulator
					.getModelStructureCache().getPatternsInfo()
					.get(patternNumber).getRelResTypes().size();

			RaoLibStringJoiner relResStringJoiner = new RaoLibStringJoiner(
					StringFormat.FUNCTION);
			for (int num = 0; num < numberOfRelevantResources; num++) {
				final int resNum = data.getInt();
				final int typeNum = Simulator.getModelStructureCache()
						.getPatternsInfo().get(patternNumber).getRelResTypes()
						.get(num);
				final String typeName = Simulator.getModelStructureCache()
						.getResourceTypesInfo().get(typeNum).getName();

				final String name = Simulator.getModelStructureCache()
						.getResourceNames().get(typeNum).get(resNum);
				final String resourceName = name != null ? name : typeName
						+ encloseIndex(resNum);

				relResStringJoiner.add(resourceName);
			}

			stringJoiner.add(traceType.toString())
					.add(encloseIndex(nodeNumber))
					.add(activity.getName() + relResStringJoiner.getString());
			break;
		}
		default:
			return null;
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
		final double time = header.getDouble();
		final int resultNum = header.getInt();
		final ResultCache resultCache = Simulator.getModelStructureCache()
				.getResultsInfo().get(resultNum);

		return new TraceOutput(TraceType.RESULT, new RaoLibStringJoiner(
				delimiter).add(TraceType.RESULT.toString()).add(time)
				.add(resultCache.getName()).add("=")
				.add(parseResultParameter(data, resultCache)).getString());
	}

	private String parseResultParameter(final ByteBuffer data,
			final ResultCache resultCache) {
		switch (resultCache.getValueType()) {
		case INTEGER:
			return String.valueOf(data.getInt());
		case REAL:
			return String.valueOf(data.getDouble());
		case BOOLEAN:
			return String.valueOf(data.get() != 0);
		case ENUM:
			return String.valueOf(resultCache.getEnumNames().get(
					(int) data.getShort()));
		case STRING:
			final ByteArrayOutputStream rawString = new ByteArrayOutputStream();
			while (data.hasRemaining()) {
				rawString.write(data.get());
			}
			return "\"" + rawString.toString() + "\"";
		default:
			break;
		}

		return null;
	}

	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //
	// -------------------------- HELPER METHODS --------------------------- //
	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //

	public final static void skipPart(final ByteBuffer buffer, final int size) {
		for (int i = 0; i < size; i++)
			buffer.get();
	}

	final static String encloseIndex(final int index) {
		return "[" + String.valueOf(index) + "]";
	}

	public final static ByteBuffer prepareBufferForReading(
			final ByteBuffer buffer) {
		return (ByteBuffer) buffer.duplicate().rewind();
	}
}

class RaoLibStringJoiner {
	private final String delimiter;
	private final String prefix;
	private final String suffix;

	private String current = null;

	enum StringFormat {
		FUNCTION(", ", "(", ")"), STRUCTURE(", ", "{", "}"), ARRAY(", ", "[",
				"]"), ENUMERATION(", ", "", "");

		StringFormat(String delimiter, String prefix, String suffix) {
			this.delimiter = delimiter;
			this.prefix = prefix;
			this.suffix = suffix;
		}

		private final String delimiter;
		private final String prefix;
		private final String suffix;
	}

	final String getString() {
		return prefix + current + suffix;
	}

	RaoLibStringJoiner(StringFormat format) {
		this.delimiter = format.delimiter;
		this.prefix = format.prefix;
		this.suffix = format.suffix;
	}

	RaoLibStringJoiner(String delimiter) {
		this(delimiter, "", "");
	}

	RaoLibStringJoiner(String delimiter, String prefix, String postfix) {
		this.delimiter = delimiter;
		this.prefix = prefix;
		this.suffix = postfix;
	}

	final RaoLibStringJoiner add(final String toAppend) {
		if (current == null)
			current = new String(toAppend);
		else
			current += delimiter + toAppend;
		return this;
	}

	final RaoLibStringJoiner add(final int toAppend) {
		return add(String.valueOf(toAppend));
	}

	final RaoLibStringJoiner add(final short toAppend) {
		return add(String.valueOf(toAppend));
	}

	final RaoLibStringJoiner add(final double toAppend) {
		return add(String.valueOf(toAppend));
	}

	final RaoLibStringJoiner add(final boolean toAppend) {
		return add(String.valueOf(toAppend));
	}
}
