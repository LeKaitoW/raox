package ru.bmstu.rk9.rdo.lib;

import java.nio.ByteBuffer;

public interface Traceable
{
	public ByteBuffer createTracerEntry(int reserve);
	public boolean isBeingTraced();
}
