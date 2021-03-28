package ru.bmstu.rk9.rao.ui.trace;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.TreeSet;

import ru.bmstu.rk9.rao.lib.database.Database;
import ru.bmstu.rk9.rao.lib.database.Database.DataType;
import ru.bmstu.rk9.rao.lib.database.Database.Entry;
import ru.bmstu.rk9.rao.lib.database.Database.EntryType;
import ru.bmstu.rk9.rao.lib.database.Database.TypeSize;
import ru.bmstu.rk9.rao.lib.dpt.Search;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator;
import ru.bmstu.rk9.rao.ui.trace.Tracer.TraceOutput;
import ru.bmstu.rk9.rao.ui.trace.Tracer.TraceType;

public class LegacyTracer {

	public LegacyTracer() {
		super();

		initializeTypes();
		initializeActivities();
	}

	private final Map<Integer, HashMap<Integer, Integer>> legacyResourceIds = new HashMap<Integer, HashMap<Integer, Integer>>();
	private final TreeSet<Integer> takenResourceIds = new TreeSet<Integer>();
	private final PriorityQueue<Integer> vacantActionNumbers = new PriorityQueue<Integer>();
	private final Map<Integer, HashMap<Integer, HashMap<Integer, Integer>>> legacyActionNumbers = new HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>();

	static private final String DELIMITER = " ";

	private RealFormatter realFormatter = new RealFormatter();

	private class RealFormatter {
		public String format(double number) {
			String output;
			if (number < 1000000) {
				BigDecimal raw = BigDecimal.valueOf(number);
				BigDecimal result = raw.round(new MathContext(6, RoundingMode.HALF_UP)).stripTrailingZeros();
				output = result.toPlainString();
			} else {
				DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
				symbols.setExponentSeparator("e+");
				DecimalFormat formatter = new DecimalFormat("0.#####E00");
				formatter.setDecimalFormatSymbols(symbols);
				output = formatter.format(number);
			}
			return output;
		}
	}

	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //
	// ------------------------------ GENERAL ------------------------------ //
	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //

	private List<TraceOutput> traceList = new ArrayList<TraceOutput>();

	public final List<TraceOutput> getTraceList() {
		return Collections.unmodifiableList(traceList);
	}

	public final void parseAllEntries() {
		final List<Entry> entries = CurrentSimulator.getDatabase().getAllEntries();

		for (Entry entry : entries) {
			final TraceOutput traceOutput = parseDatabaseEntry(entry);
			if (traceOutput != null)
				traceList.add(traceOutput);
		}
	}

	private boolean simulationStarted = false;
	private boolean dptSearchJustStarted = false;
	private boolean dptSearchDecisionFound = false;
	private boolean dptSearchJustFinished = false;
	private double dptSearchTime = 0;

	private final TraceOutput parseDatabaseEntry(final Entry entry) {
		if (dptSearchJustStarted) {
			addLegacySearchEntriesOnStart();
			dptSearchJustStarted = false;
		}

		if (dptSearchJustFinished) {
			addLegacySearchEntriesOnFinish(dptSearchTime);
			dptSearchJustFinished = false;
		}

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
		default:
			throw new TracerException("Unexpected entry type: " + type);
		}
	}

	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //
	// -------------------------- SYSTEM ENTRIES --------------------------- //
	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //

	protected TraceOutput parseSystemEntry(final Entry entry) {
		final ByteBuffer header = Tracer.prepareBufferForReading(entry.getHeader());

		final TraceType traceType = TraceType.SYSTEM;

		Tracer.skipPart(header, TypeSize.BYTE);
		final double time = header.getDouble();
		final Database.SystemEntryType type = Database.SystemEntryType.values()[header.get()];

		int legacyCode;

		switch (type) {
		case TRACE_START:
			legacyCode = 1;
			break;
		case SIM_START:
			legacyCode = 3;
			simulationStarted = true;
			break;
		default:
			legacyCode = 2;
			break;
		}

		final String headerLine = new StringJoiner(DELIMITER).add(traceType.toString())
				.add(realFormatter.format((time))).add(legacyCode).getString();

		return new TraceOutput(traceType, headerLine);
	}

	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //
	// --------------------------- RESOURCE ENTRIES ------------------------ //
	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //

	protected final TraceOutput parseResourceEntry(final Entry entry) {
		final ByteBuffer header = Tracer.prepareBufferForReading(entry.getHeader());
		final ByteBuffer data = Tracer.prepareBufferForReading(entry.getData());

		Tracer.skipPart(header, TypeSize.BYTE);
		final double time = header.getDouble();
		final TraceType traceType;
		final Database.ResourceEntryType entryType = Database.ResourceEntryType.values()[header.get()];
		final int typeNum = header.getInt();
		final int resNum = header.getInt();

		int legacyId;
		switch (entryType) {
		case CREATED:
			traceType = simulationStarted ? TraceType.RESOURCE_CREATE : TraceType.RESOURCE_KEEP;

			if (legacyResourceIds.get(typeNum).get(resNum) == null)
				legacyId = getNewResourceId(typeNum, resNum);
			else
				legacyId = legacyResourceIds.get(typeNum).get(resNum);

			break;
		case ERASED:
			traceType = TraceType.RESOURCE_ERASE;
			legacyId = legacyResourceIds.get(typeNum).get(resNum);
			freeResourceId(typeNum, resNum);
			break;
		case ALTERED:
			traceType = TraceType.RESOURCE_KEEP;
			legacyId = legacyResourceIds.get(typeNum).get(resNum);
			break;
		case SEARCH:
			traceType = TraceType.SEARCH_RESOURCE_KEEP;
			legacyId = legacyResourceIds.get(typeNum).get(resNum);
			break;
		case SOLUTION:
		default:
			throw new TracerException("Unexpected resource entry type: " + entryType);
		}

		final String headerLine = new StringJoiner(DELIMITER).add(traceType.toString())
				.add(realFormatter.format((time))).add(typeNum + 1).add(legacyId).getString();

		return new TraceOutput(traceType,
				new StringJoiner(DELIMITER).add(headerLine).add(parseResourceParameters(data, typeNum)).getString());
	}

	protected final String parseResourceParameters(final ByteBuffer data, final int typeNum) {
		final StringJoiner stringJoiner = new StringJoiner(DELIMITER);
		final int numberOfParameters = CurrentSimulator.getStaticModelData().getNumberOfResourceTypeParameters(typeNum);
		final int finalOffset = CurrentSimulator.getStaticModelData().getResourceTypeFinalOffset(typeNum);

		for (int paramNum = 0; paramNum < numberOfParameters; paramNum++) {
			DataType dataType = CurrentSimulator.getStaticModelData().getResourceTypeParameterType(typeNum, paramNum);
			switch (dataType) {
			case INT:
				stringJoiner.add(data.getInt());
				break;
			case DOUBLE:
				stringJoiner.add(realFormatter.format(data.getDouble()));
				break;
			case BOOLEAN:
				stringJoiner.add(legacyBooleanString(data.get() != 0));
				break;
			// TODO fix this when enum serialization is implemented
			// case ENUM:
			// stringJoiner.add(data.getShort());
			// break;
			case OTHER:
				final int index = CurrentSimulator.getStaticModelData().getVariableWidthParameterIndex(typeNum,
						paramNum);
				final int stringPosition = data.getInt(finalOffset + (index - 1) * TypeSize.INT);
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

	protected final TraceOutput parsePatternEntry(final Entry entry) {
		final ByteBuffer header = Tracer.prepareBufferForReading(entry.getHeader());
		final ByteBuffer data = Tracer.prepareBufferForReading(entry.getData());

		Tracer.skipPart(header, TypeSize.BYTE);
		final double time = header.getDouble();
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

		return new TraceOutput(traceType, new StringJoiner(DELIMITER).add(traceType.toString())
				.add(realFormatter.format(time)).add(parsePatternData(data, traceType)).getString());
	}

	protected final String parsePatternData(final ByteBuffer data, final TraceType patternType) {
		final StringJoiner stringJoiner = new StringJoiner(DELIMITER);

		int patternNumber;

		switch (patternType) {
		case RULE: {
			Tracer.skipPart(data, TypeSize.INT);
			int activityNumber = data.getInt();
			patternNumber = data.getInt();
			Tracer.skipPart(data, TypeSize.INT);
			stringJoiner.add(1).add(activityNumber + 1).add(patternNumber + 1);
			break;
		}
		case OPERATION_BEGIN: {
			int dptNumber = data.getInt();
			int activityNumber = data.getInt();
			patternNumber = data.getInt();
			int actionNumber = data.getInt();

			Map<Integer, Integer> activityActions = legacyActionNumbers.get(dptNumber).get(activityNumber);

			int legacyNumber;
			if (vacantActionNumbers.isEmpty())
				legacyNumber = activityActions.size();
			else
				legacyNumber = vacantActionNumbers.poll();

			activityActions.put(actionNumber, legacyNumber);

			stringJoiner.add(legacyNumber + 1).add(activityNumber + 1).add(patternNumber + 1);
			break;
		}
		case OPERATION_END: {
			int dptNumber = data.getInt();
			int activityNumber = data.getInt();
			patternNumber = data.getInt();
			int actionNumber = data.getInt();

			Map<Integer, Integer> activityActions = legacyActionNumbers.get(dptNumber).get(activityNumber);
			int legacyNumber = activityActions.remove(actionNumber);
			vacantActionNumbers.add(legacyNumber);

			stringJoiner.add(legacyNumber + 1).add(activityNumber + 1).add(patternNumber + 1);
		}
			break;
		default:
			throw new TracerException("Unexpected pattern type: " + patternType);
		}

		int numberOfRelevantResources = data.getInt();
		stringJoiner.add(numberOfRelevantResources).add("");
		for (int num = 0; num < numberOfRelevantResources; num++) {
			final int typeNum = CurrentSimulator.getStaticModelData().getRelevantResourceTypeNumber(patternNumber, num);
			final int resNum = data.getInt();
			if (legacyResourceIds.get(typeNum).get(resNum) == null) {
				stringJoiner.add(getNewResourceId(typeNum, resNum));
			} else {
				final int legacyId = legacyResourceIds.get(typeNum).get(resNum);
				stringJoiner.add(legacyId);
			}
		}

		return stringJoiner.getString();
	}

	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //
	// --------------------------- EVENT ENTRIES --------------------------- //
	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //

	private TraceOutput parseEventEntry(final Entry entry) {
		final ByteBuffer header = Tracer.prepareBufferForReading(entry.getHeader());
		final ByteBuffer data = Tracer.prepareBufferForReading(entry.getData());

		Tracer.skipPart(header, TypeSize.BYTE);
		final double time = header.getDouble();
		final TraceType traceType = TraceType.EVENT;

		return new TraceOutput(traceType,
				new StringJoiner(DELIMITER).add(traceType.toString()).add(time).add(parseEventData(data)).getString());
	}

	private String parseEventData(final ByteBuffer data) {
		final StringJoiner stringJoiner = new StringJoiner(DELIMITER);

		int eventNumber = data.getInt();
		Tracer.skipPart(data, TypeSize.INT);
		stringJoiner.add(eventNumber + 1).add(eventNumber + 1);
		return stringJoiner.getString();
	}

	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //
	// -------------------------- SEARCH ENTRIES --------------------------- //
	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //

	protected TraceOutput parseSearchEntry(final Entry entry) {
		final ByteBuffer header = Tracer.prepareBufferForReading(entry.getHeader());

		final StringJoiner stringJoiner = new StringJoiner(DELIMITER);

		final TraceType traceType;
		Tracer.skipPart(header, TypeSize.BYTE);
		final Database.SearchEntryType entryType = Database.SearchEntryType.values()[header.get()];
		final double time = header.getDouble();
		final int dptNumber = header.getInt();

		switch (entryType) {
		case BEGIN: {
			dptSearchDecisionFound = false;
			dptSearchJustStarted = true;
			traceType = TraceType.SEARCH_BEGIN;
			dptSearchTime = time;
			stringJoiner.add(traceType.toString()).add(realFormatter.format(time)).add(dptNumber + 1);
			break;
		}
		case END: {
			final ByteBuffer data = Tracer.prepareBufferForReading(entry.getData());
			dptSearchJustFinished = true;
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

			final long timeMillis = data.getLong();
			final long mem = data.getLong();
			final double finalCost = data.getDouble();
			final int countClosed = data.getInt();
			final int countOpen = data.getInt();
			final int countSpawned = data.getInt();
			stringJoiner.add(traceType.toString()).add(realFormatter.format(time)).add(timeMillis).add(mem)
					.add(realFormatter.format(finalCost)).add(countClosed).add(countClosed + countOpen)
					.add(countClosed + countOpen).add(countSpawned);
			break;
		}
		case OPEN: {
			final ByteBuffer data = Tracer.prepareBufferForReading(entry.getData());
			traceType = TraceType.SEARCH_OPEN;
			final int currentNumber = data.getInt();
			final int parentNumber = data.getInt();
			final double g = data.getDouble();
			final double h = data.getDouble();
			stringJoiner.add("SO").add(currentNumber + 1).add(parentNumber + 1).add(realFormatter.format(g))
					.add(realFormatter.format(g + h));
			break;
		}
		case SPAWN: {
			final ByteBuffer data = Tracer.prepareBufferForReading(entry.getData());
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
			final int activityNumber = data.getInt();
			final int patternNumber = data.getInt();
			final double ruleCost = data.getDouble();
			final int numberOfRelevantResources = CurrentSimulator.getStaticModelData()
					.getNumberOfRelevantResources(patternNumber);

			stringJoiner.add(traceType.toString()).add(childNumber + 1).add(parentNumber + 1)
					.add(realFormatter.format(g)).add(realFormatter.format(g + h)).add(activityNumber + 1)
					.add(patternNumber + 1).add(realFormatter.format(ruleCost)).add(numberOfRelevantResources).add("");

			for (int num = 0; num < numberOfRelevantResources; num++) {
				final String typeName = CurrentSimulator.getStaticModelData().getRelevantResourceTypeName(patternNumber,
						num);
				final int typeNum = CurrentSimulator.getStaticModelData().getResourceTypeNumber(typeName);
				final int resNum = data.getInt();
				final int legacyNum = legacyResourceIds.get(typeNum).get(resNum);
				stringJoiner.add(legacyNum);
			}
			break;
		}
		case DECISION: {
			final ByteBuffer data = Tracer.prepareBufferForReading(entry.getData());
			traceType = TraceType.SEARCH_DECISION;
			final int number = data.getInt();
			final int activityNumber = data.getInt();
			final int patternNumber = data.getInt();
			final int numberOfRelevantResources = CurrentSimulator.getStaticModelData()
					.getNumberOfRelevantResources(patternNumber);
			if (!dptSearchDecisionFound) {
				addLegacySearchEntryDecision();
				dptSearchDecisionFound = true;
			}
			stringJoiner.add(number + 1).add(activityNumber + 1).add(patternNumber + 1).add(numberOfRelevantResources)
					.add("");

			for (int num = 0; num < numberOfRelevantResources; num++) {
				final String typeName = CurrentSimulator.getStaticModelData().getRelevantResourceTypeName(patternNumber,
						num);
				final int typeNum = CurrentSimulator.getStaticModelData().getResourceTypeNumber(typeName);
				final int resNum = data.getInt();
				final int legacyNum = legacyResourceIds.get(typeNum).get(resNum);
				stringJoiner.add(legacyNum);
			}
			break;
		}
		default:
			throw new TracerException("Unexpected search entry type: " + entryType);
		}

		return new TraceOutput(traceType, stringJoiner.getString());
	}

	private final void addLegacySearchEntryDecision() {
		traceList.add(new TraceOutput(TraceType.SEARCH_DECISION, new StringJoiner(DELIMITER).add("SD").getString()));
	}

	private final void addLegacySearchEntriesOnStart() {
		traceList.add(new TraceOutput(TraceType.SEARCH_SPAWN_NEW, new StringJoiner(DELIMITER).add("STN").add(1).add(0)
				.add(0).add(0).add(-1).add(-1).add(0).add(0).getString()));

		traceList.add(new TraceOutput(TraceType.SEARCH_OPEN,
				new StringJoiner(DELIMITER).add("SO").add(1).add(0).add(0).add(0).getString()));
	}

	private final void addLegacySearchEntriesOnFinish(double time) {
		traceList.add(new TraceOutput(TraceType.SYSTEM, new StringJoiner(DELIMITER).add(TraceType.SYSTEM.toString())
				.add(realFormatter.format(time)).add(4).getString()));
	}

	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //
	// --------------------------- RESULT ENTRIES -------------------------- //
	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //

	protected final TraceOutput parseResultEntry(final Entry entry) {
		final ByteBuffer header = Tracer.prepareBufferForReading(entry.getHeader());
		final ByteBuffer data = Tracer.prepareBufferForReading(entry.getData());

		Tracer.skipPart(header, TypeSize.BYTE);
		final double time = header.getDouble();
		final int resultNum = header.getInt();

		return new TraceOutput(TraceType.RESULT,
				new StringJoiner(DELIMITER).add(TraceType.RESULT.toString()).add(realFormatter.format(time))
						.add(resultNum + 1).add("").add(parseResultParameter(data, resultNum)).getString());
	}

	protected final String parseResultParameter(final ByteBuffer data, final int resultNumber) {
		// TODO implement
		return "";
	}

	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //
	// -------------------------- HELPER METHODS --------------------------- //
	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //

	private final void initializeTypes() {
		final int numberOfResourceTypes = CurrentSimulator.getStaticModelData().getNumberOfResourceTypes();

		for (int num = 0; num < numberOfResourceTypes; num++) {
			legacyResourceIds.put(num, new HashMap<Integer, Integer>());
		}
	}

	private final void initializeActivities() {
		final int numberOfLogics = CurrentSimulator.getStaticModelData().getNumberOfLogics();

		for (int dptNum = 0; dptNum < numberOfLogics; dptNum++) {
			HashMap<Integer, HashMap<Integer, Integer>> activities = new HashMap<Integer, HashMap<Integer, Integer>>();
			int numberOfActivities = CurrentSimulator.getStaticModelData().getNumberOfActivities(dptNum);
			for (int actNum = 0; actNum < numberOfActivities; actNum++) {
				HashMap<Integer, Integer> activity = new HashMap<Integer, Integer>();
				activities.put(actNum, activity);
			}
			legacyActionNumbers.put(dptNum, activities);
		}
	}

	private final void freeResourceId(int typeNum, int resNum) {
		int legacyId = legacyResourceIds.get(typeNum).get(resNum);
		legacyResourceIds.get(typeNum).remove(resNum);
		takenResourceIds.remove(legacyId);
	}

	private final int getNewResourceId(int typeNum, int resNum) {
		int current;
		int legacyId = 1;
		Iterator<Integer> it = takenResourceIds.iterator();
		while (it.hasNext()) {
			current = it.next();
			if (current != legacyId)
				break;
			legacyId++;
		}
		legacyResourceIds.get(typeNum).put(resNum, legacyId);
		takenResourceIds.add(legacyId);
		return legacyId;
	}

	private final String legacyBooleanString(boolean value) {
		return value ? "TRUE" : "FALSE";
	}
}
