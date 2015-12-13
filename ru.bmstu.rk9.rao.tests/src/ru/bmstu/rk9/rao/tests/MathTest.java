package ru.bmstu.rk9.rao.tests;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import ru.bmstu.rk9.rao.lib.math.Erf;
import ru.bmstu.rk9.rao.lib.math.MersenneTwisterFast;


public class MathTest {
	@Test
	public void erfInvTest() {
		assertTrue(Erf.erfInv(1) == (double)Double.POSITIVE_INFINITY);
		assertTrue(Erf.erfInv(-1) == (double)Double.NEGATIVE_INFINITY);
		assertTrue(Erf.erfInv(0) == 0.0);
		assertEquals(Erf.erfInv(0.5), 0.47693627620446977, 0.00000000000000001);
	}
	
	@Test
	public void MersenneTwisterFastTest() {
		MersenneTwisterFast mersenneTwisterFast = new MersenneTwisterFast(525252);
		assertEquals(mersenneTwisterFast.nextInt(),- 1850992327);
		assertEquals(mersenneTwisterFast.nextInt(), -1025079765);
		assertEquals(mersenneTwisterFast.nextInt(), 488545501);
		assertEquals(mersenneTwisterFast.nextInt(), -777709231);
	}
	

}
