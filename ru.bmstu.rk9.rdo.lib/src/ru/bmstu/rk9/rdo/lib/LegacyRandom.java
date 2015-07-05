package ru.bmstu.rk9.rdo.lib;

public class LegacyRandom {
	private long seed = 0;

	public LegacyRandom(long seed) {
		this.seed = seed;
	}

	public void setSeed(long seed) {
		this.seed = seed;
	}

	public double nextDouble() {
		seed = (seed * 69069L + 1L) % 4294967296L;
		return seed / 4294967296.0;
	}
}
