package ru.bmstu.rk9.rdo.lib;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import ru.bmstu.rk9.rdo.lib.Database.Entry;
import ru.bmstu.rk9.rdo.lib.Database.EntryType;
import ru.bmstu.rk9.rdo.lib.Database.TypeSize;
import ru.bmstu.rk9.rdo.lib.ActivityCache;
import ru.bmstu.rk9.rdo.lib.ResourceTypeCache;
import ru.bmstu.rk9.rdo.lib.ResultCache;
import ru.bmstu.rk9.rdo.lib.ValueCache;
import ru.bmstu.rk9.rdo.lib.RDOLibStringJoiner.StringFormat;

public class Tracer implements Subscriber {
	public static enum TraceType {
		RESOURCE_CREATE("RC"),
		RESOURCE_KEEP("RK"),
		RESOURCE_ERASE("RE"),
		SYSTEM("ES"),
		OPERATION_BEGIN("EB"),
		OPERATION_END("EF"),
		EVENT("EI"),
		RULE("ER"),
		RESULT("V "),
		SEARCH_BEGIN("SB"),
		SEARCH_OPEN("SO "),
		SEARCH_SPAWN_NEW("STN"),
		SEARCH_SPAWN_WORSE("STD"),
		SEARCH_SPAWN_BETTER("STR"),
		SEARCH_RESOURCE_KEEP("SRK"),
		SEARCH_DECISION("SD "),
		SEARCH_END_ABORTED("SEA"),
		SEARCH_END_CONDITION("SEC"),
		SEARCH_END_SUCCESS("SES"),
		SEARCH_END_FAIL("SEN");

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

	//TODO unify with DbIndexHelper notification System

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

  /*――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――/
 /                                   GENERAL                                 /
/――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――*/

	public final TraceOutput parseSerializedData(final Entry entry) {
		final EntryType type = EntryType.values()[entry.header
				.get(TypeSize.Internal.ENTRY_TYPE_OFFSET)];
		switch (type) {
		case SYSTEM:
			return parseSystemEntry(entry);
		case RESOURCE:
			return parseResourceEntry(entry);
		case PATTERN:
			return parsePatternEntry(entry);
		case SEARCH:
			return parseSearchEntry(entry);
		case RESULT:
			return parseResultEntry(entry);
		default:
			return null;
		}
	}

  /*――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――/
 /                          PARSING SYSTEM ENTRIES                           /
/――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――*/

	protected TraceOutput parseSystemEntry(final Entry entry) {
		final ByteBuffer header = prepareBufferForReading(entry.header);

		final TraceType traceType = TraceType.SYSTEM;

		skipPart(header, TypeSize.BYTE);
		final double time = header.getDouble();
		final Database.SystemEntryType type = Database.SystemEntryType.values()[header
				.get()];

		final String headerLine = new RDOLibStringJoiner(delimiter)
				.add(traceType.toString()).add(time).add(type.getDescription())
				.getString();

		return new TraceOutput(traceType, headerLine);
	}

  /*――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――/
 /                          PARSING RESOURCE ENTRIES                         /
/――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――*/

	protected TraceOutput parseResourceEntry(final Entry entry) {
		final ByteBuffer header = prepareBufferForReading(entry.header);
		final ByteBuffer data = prepareBufferForReading(entry.data);

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
		final ResourceTypeCache typeInfo = Simulator.getModelStructureCache().resourceTypesInfo
				.get(typeNum);
		final int resNum = header.getInt();
		final String name = Simulator.getModelStructureCache().resourceNames
				.get(typeNum).get(resNum);

		final String resourceName = name != null ? name : typeInfo.name
				+ encloseIndex(resNum);

		return new TraceOutput(traceType, new RDOLibStringJoiner(delimiter)
				.add(traceType.toString()).add(time).add(resourceName).add("=")
				.add(parseResourceParameters(data, typeInfo)).getString());
	}

	protected String parseResourceParameters(final ByteBuffer data,
			final ResourceTypeCache typeInfo) {
		final RDOLibStringJoiner stringJoiner = new RDOLibStringJoiner(
				RDOLibStringJoiner.StringFormat.STRUCTURE);

		for (int paramNum = 0; paramNum < typeInfo.numberOfParameters; paramNum++) {
			ValueCache valueCache = typeInfo.paramTypes.get(paramNum);
			// TODO trace arrays when they are implemented
			switch (valueCache.type) {
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
				stringJoiner
						.add(valueCache.enumNames.get((int) data.getShort()));
				break;
			case STRING:
				final int index = typeInfo.indexList.get(paramNum);
				final int stringPosition = data.getInt(typeInfo.finalOffset
						+ (index - 1) * TypeSize.RDO.INTEGER);
				final int length = data.getInt(stringPosition);

				byte rawString[] = new byte[length];
				for (int i = 0; i < length; i++)
					rawString[i] = data.get(stringPosition
							+ TypeSize.RDO.INTEGER + i);
				stringJoiner.add("\""
						+ new String(rawString, StandardCharsets.UTF_8) + "\"");
				break;
			default:
				return null;
			}
		}
		return stringJoiner.getString();
	}

  /*――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――/
 /                          PARSING PATTERN ENTRIES                          /
/――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――*/

	protected TraceOutput parsePatternEntry(final Entry entry) {
		final ByteBuffer header = prepareBufferForReading(entry.header);
		final ByteBuffer data = prepareBufferForReading(entry.data);

		skipPart(header, TypeSize.BYTE);
		final double time = header.getDouble();
		final TraceType traceType;

		final Database.PatternType entryType = Database.PatternType.values()[header
				.get()];
		switch (entryType) {
		case EVENT:
			traceType = TraceType.EVENT;
			break;
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

		return new TraceOutput(traceType, new RDOLibStringJoiner(delimiter)
				.add(traceType.toString()).add(time)
				.add(parsePatternData(data, traceType)).getString());
	}

	protected String parsePatternData(final ByteBuffer data,
			final TraceType patternType) {
		final RDOLibStringJoiner stringJoiner = new RDOLibStringJoiner(
				delimiter);

		int patternNumber;

		switch (patternType) {
		case EVENT: {
			int eventNumber = data.getInt();
			int actionNumber = data.getInt();
			patternNumber = eventNumber;
			stringJoiner.add(Simulator.getModelStructureCache().patternsInfo
					.get(eventNumber).name + encloseIndex(actionNumber));
			break;
		}
		case RULE:
		case OPERATION_BEGIN:
		case OPERATION_END: {
			int dptNumber = data.getInt();
			int activityNumber = data.getInt();
			int actionNumber = data.getInt();
			ActivityCache activity = Simulator.getModelStructureCache().decisionPointsInfo
					.get(dptNumber).activitiesInfo.get(activityNumber);
			patternNumber = activity.patternNumber;
			stringJoiner.add(activity.name + encloseIndex(actionNumber));
			break;
		}
		default:
			return null;
		}

		final RDOLibStringJoiner relResStringJoiner = new RDOLibStringJoiner(
				RDOLibStringJoiner.StringFormat.FUNCTION);

		int numberOfRelevantResources = data.getInt();
		for (int num = 0; num < numberOfRelevantResources; num++) {
			final int resNum = data.getInt();
			final int typeNum = Simulator.getModelStructureCache().patternsInfo
					.get(patternNumber).relResTypes.get(num);
			final String typeName = Simulator.getModelStructureCache().resourceTypesInfo
					.get(typeNum).name;

			final String name = Simulator.getModelStructureCache().resourceNames
					.get(typeNum).get(resNum);
			final String resourceName = name != null ? name : typeName
					+ encloseIndex(resNum);

			relResStringJoiner.add(resourceName);
		}

		return stringJoiner.getString() + relResStringJoiner.getString();
	}

  /*――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――/
 /                              SEARCH ENTRIES                               /
/――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――*/

	protected TraceOutput parseSearchEntry(final Entry entry) {
		final ByteBuffer header = prepareBufferForReading(entry.header);

		final RDOLibStringJoiner stringJoiner = new RDOLibStringJoiner(
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
							.get(dptNumber).name);
			break;
		}
		case END: {
			final ByteBuffer data = prepareBufferForReading(entry.data);
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
					.add(new RDOLibStringJoiner(StringFormat.ENUMERATION)
							.add("solution cost = " + finalCost)
							.add("nodes opened = " + totalOpened)
							.add("nodes total = " + totalNodes)
							.add("nodes added = " + totalAdded)
							.add("nodes spawned = " + totalSpawned).getString());
			break;
		}
		case OPEN: {
			final ByteBuffer data = prepareBufferForReading(entry.data);
			traceType = TraceType.SEARCH_OPEN;
			final int currentNumber = data.getInt();
			skipPart(data, TypeSize.INTEGER + 2 * TypeSize.DOUBLE);
			stringJoiner.add(traceType.toString()).add(
					encloseIndex(currentNumber));
			break;
		}
		case SPAWN: {
			final ByteBuffer data = prepareBufferForReading(entry.data);
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
					.get(dptNumber).activitiesInfo.get(ruleNumber);
			final int patternNumber = activity.patternNumber;
			final double ruleCost = data.getDouble();
			final int numberOfRelevantResources = Simulator
					.getModelStructureCache().patternsInfo.get(patternNumber).relResTypes
					.size();

			RDOLibStringJoiner relResStringJoiner = new RDOLibStringJoiner(
					StringFormat.FUNCTION);

			for (int num = 0; num < numberOfRelevantResources; num++) {
				final int resNum = data.getInt();
				final int typeNum = Simulator.getModelStructureCache().patternsInfo
						.get(patternNumber).relResTypes.get(num);
				final String typeName = Simulator.getModelStructureCache().resourceTypesInfo
						.get(typeNum).name;

				final String name = Simulator.getModelStructureCache().resourceNames
						.get(typeNum).get(resNum);
				final String resourceName = name != null ? name : typeName
						+ encloseIndex(resNum);

				relResStringJoiner.add(resourceName);
			}

			stringJoiner
					.add(traceType.toString())
					.add(encloseIndex(parentNumber) + "->"
							+ encloseIndex(childNumber))
					.add(activity.name + relResStringJoiner.getString())
					.add("=").add(ruleCost + ",")
					.add("[" + (g + h) + " = " + g + " + " + h + "]");
			break;
		}
		case DECISION: {
			final ByteBuffer data = prepareBufferForReading(entry.data);
			traceType = TraceType.SEARCH_DECISION;
			final int nodeNumber = data.getInt();
			final int activityNumber = data.getInt();
			ActivityCache activity = Simulator.getModelStructureCache().decisionPointsInfo
					.get(dptNumber).activitiesInfo.get(activityNumber);
			final int patternNumber = activity.patternNumber;
			final int numberOfRelevantResources = Simulator
					.getModelStructureCache().patternsInfo.get(patternNumber).relResTypes
					.size();

			RDOLibStringJoiner relResStringJoiner = new RDOLibStringJoiner(
					StringFormat.FUNCTION);
			for (int num = 0; num < numberOfRelevantResources; num++) {
				final int resNum = data.getInt();
				final int typeNum = Simulator.getModelStructureCache().patternsInfo
						.get(patternNumber).relResTypes.get(num);
				final String typeName = Simulator.getModelStructureCache().resourceTypesInfo
						.get(typeNum).name;

				final String name = Simulator.getModelStructureCache().resourceNames
						.get(typeNum).get(resNum);
				final String resourceName = name != null ? name : typeName
						+ encloseIndex(resNum);

				relResStringJoiner.add(resourceName);
			}

			stringJoiner.add(traceType.toString())
					.add(encloseIndex(nodeNumber))
					.add(activity.name + relResStringJoiner.getString());
			break;
		}
		default:
			return null;
		}

		return new TraceOutput(traceType, stringJoiner.getString());
	}

  /*――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――/
 /                           PARSING RESULT ENTRIES                          /
/――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――*/

	protected TraceOutput parseResultEntry(final Entry entry) {
		final ByteBuffer header = prepareBufferForReading(entry.header);
		final ByteBuffer data = prepareBufferForReading(entry.data);

		skipPart(header, TypeSize.BYTE);
		final double time = header.getDouble();
		final int resultNum = header.getInt();
		final ResultCache resultCache = Simulator.getModelStructureCache().resultsInfo
				.get(resultNum);

		return new TraceOutput(TraceType.RESULT, new RDOLibStringJoiner(
				delimiter).add(TraceType.RESULT.toString()).add(time)
				.add(resultCache.name).add("=")
				.add(parseResultParameter(data, resultCache)).getString());
	}

	protected String parseResultParameter(final ByteBuffer data,
			final ResultCache resultCache) {
		switch (resultCache.valueType) {
		case INTEGER:
			return String.valueOf(data.getInt());
		case REAL:
			return String.valueOf(data.getDouble());
		case BOOLEAN:
			return String.valueOf(data.get() != 0);
		case ENUM:
			return String.valueOf(resultCache.enumNames.get((int) data
					.getShort()));
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

  /*――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――/
 /                               HELPER METHODS                              /
/――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――*/

	public final static void skipPart(final ByteBuffer buffer, final int size)
	{
		for(int i = 0; i < size; i++)
			buffer.get();
	}

	final static String encloseIndex(final int index) {
		return "[" + String.valueOf(index) + "]";
	}

	public final static ByteBuffer prepareBufferForReading(final ByteBuffer buffer)
	{
		return (ByteBuffer) buffer.duplicate().rewind();
	}
}

class RDOLibStringJoiner {
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

	RDOLibStringJoiner(StringFormat format) {
		this.delimiter = format.delimiter;
		this.prefix = format.prefix;
		this.suffix = format.suffix;
	}

	RDOLibStringJoiner(String delimiter) {
		this(delimiter, "", "");
	}

	RDOLibStringJoiner(String delimiter, String prefix, String postfix) {
		this.delimiter = delimiter;
		this.prefix = prefix;
		this.suffix = postfix;
	}

	final RDOLibStringJoiner add(final String toAppend) {
		if (current == null)
			current = new String(toAppend);
		else
			current += delimiter + toAppend;
		return this;
	}

	final RDOLibStringJoiner add(final int toAppend) {
		return add(String.valueOf(toAppend));
	}

	final RDOLibStringJoiner add(final short toAppend) {
		return add(String.valueOf(toAppend));
	}

	final RDOLibStringJoiner add(final double toAppend) {
		return add(String.valueOf(toAppend));
	}

	final RDOLibStringJoiner add(final boolean toAppend) {
		return add(String.valueOf(toAppend));
	}
}
