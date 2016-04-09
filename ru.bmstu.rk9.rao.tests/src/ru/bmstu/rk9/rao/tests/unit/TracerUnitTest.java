package ru.bmstu.rk9.rao.tests.unit;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import java.nio.ByteBuffer;

import ru.bmstu.rk9.rao.lib.database.Database.*;
import ru.bmstu.rk9.rao.lib.modeldata.StaticModelData;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorPreinitializationInfo;
import ru.bmstu.rk9.rao.ui.trace.Tracer;
import ru.bmstu.rk9.rao.ui.trace.Tracer.TraceOutput;
import ru.bmstu.rk9.rao.ui.trace.Tracer.TraceType;

public class TracerUnitTest {
	@Test
	public void TracerOutputTest() {
		Tracer tracer = new Tracer(new StaticModelData(SimulatorPreinitializationInfo.generateModelStructureStrub()));

		final ByteBuffer header = ByteBuffer.allocate(EntryType.SYSTEM.HEADER_SIZE);
		header.put((byte) EntryType.SYSTEM.ordinal()).putDouble(0).put((byte) SystemEntryType.TRACE_START.ordinal());
		Entry entry = new Entry(header, null);

		TraceOutput output = tracer.parseSerializedData(entry);

		assertEquals("ES 0.0 Tracing started", output.content());
		assertEquals(TraceType.SYSTEM, output.type());
	}
}
