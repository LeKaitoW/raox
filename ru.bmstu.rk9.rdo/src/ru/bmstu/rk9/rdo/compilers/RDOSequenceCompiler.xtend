package ru.bmstu.rk9.rdo.compilers

import static extension ru.bmstu.rk9.rdo.generator.RDOExpressionCompiler.*

import ru.bmstu.rk9.rdo.generator.LocalContext

import ru.bmstu.rk9.rdo.rdo.Sequence

import ru.bmstu.rk9.rdo.rdo.RegularSequence
import ru.bmstu.rk9.rdo.rdo.RegularSequenceType

import ru.bmstu.rk9.rdo.rdo.EnumerativeSequence

import ru.bmstu.rk9.rdo.rdo.HistogramSequence

import ru.bmstu.rk9.rdo.rdo.RDOType
import ru.bmstu.rk9.rdo.rdo.RDOEnum

class RDOSequenceCompiler
{
	//FIXME: temporally commented out usage of non-legacy sequences
	// because org.apache package cannot be resolved
	def public static compileSequence(Sequence seq, String filename)
	{
		'''
		package «filename»;

		import ru.bmstu.rk9.rdo.lib.*;
		@SuppressWarnings("all")

		public class «seq.name»
		{
			«IF seq.type instanceof RegularSequence»
				«IF (seq.type as RegularSequence).legacy»
				private static RDOLegacyRandom prng =
					new RDOLegacyRandom(«(seq.type as RegularSequence).seed»);
				«ELSE»
				private static RDOLegacyRandom prng =
					new RDOLegacyRandom(«(seq.type as RegularSequence).seed»);
				/*
				private static org.apache.commons.math3.random.MersenneTwister prng =
					new org.apache.commons.math3.random.MersenneTwister(«(seq.type as RegularSequence).seed»);
				*/
				«ENDIF»

				public static void setSeed(long seed)
				{
					prng.setSeed(seed);
				}

				«(seq.type as RegularSequence).compileRegularSequence(seq.returntype, (seq.type as RegularSequence).legacy)»
			«ENDIF»
			«IF seq.type instanceof EnumerativeSequence»
				private «seq.returntype.compileTypePrimitive»[] values = new «seq.returntype.compileTypePrimitive»[]
					{
						«FOR i : 0 ..< (seq.type as EnumerativeSequence).values.size»
							«(seq.type as EnumerativeSequence).values.get(i).compileExpression.value»«
								IF i != (seq.type as EnumerativeSequence).values.size - 1»,«ENDIF»
						«ENDFOR»
					};

				private int current = 0;

				public «seq.returntype.compileTypePrimitive» getNext()
				{
					if(current == values.length)
						current = 0;

					return values[current++];
				}
			«ENDIF»
			«IF seq.type instanceof HistogramSequence»
				«IF (seq.type as HistogramSequence).legacy»
				private static RDOLegacyRandom prng =
					new RDOLegacyRandom(«(seq.type as HistogramSequence).seed»);
				«ELSE»
				private static RDOLegacyRandom prng =
					new RDOLegacyRandom(«(seq.type as HistogramSequence).seed»);
				/* private static org.apache.commons.math3.random.MersenneTwister prng =
					new org.apache.commons.math3.random.MersenneTwister(«(seq.type as HistogramSequence).seed»);
				*/
				«ENDIF»

				public static void setSeed(long seed)
				{
					prng.setSeed(seed);
				}

				«IF seq.returntype.compileType.endsWith("_enum")»
				private static «seq.returntype.compileType»[] enums = new «seq.returntype.compileType»[]{«(seq.type as HistogramSequence).compileHistogramEnums»};
				«ENDIF»

				private static HistogramSequence histogram = new HistogramSequence(
					new double[]{«(seq.type as HistogramSequence).compileHistogramValues»},
					new double[]{«(seq.type as HistogramSequence).compileHistogramWeights»}
				);

				public static «seq.returntype.compileTypePrimitive» getNext()
				{
					double x = histogram.calculateValue(prng.nextDouble());
					return «IF seq.returntype.compileType.endsWith("_enum")»enums[ (int)x ]«ELSE»(«seq.returntype.compileTypePrimitive»)x«ENDIF»;
				}
			«ENDIF»
		}
		'''
	}

	def public static compileHistogramValues(HistogramSequence seq)
	{
		var ret = ""
		var flag = false
		val histEnum =
			if((seq.eContainer as Sequence).returntype.compileType.endsWith("_enum"))
				true
			else
				false

		if(histEnum)
			for(i : 0 ..< seq.values.size/2 + 1)
			{
				ret = ret + (if(flag) ", " else "") + i.toString
				flag = true
			}
		else
		{
			for(i : 0 ..< seq.values.size/3)
			{
				ret = ret + (if(flag) ", " else "") + seq.values.get(3*i).compileExpression.value
				flag = true
			}
			ret = ret + ", " + seq.values.get(seq.values.size - 2).compileExpression.value
		}

		return ret
	}

	def public static compileHistogramWeights(HistogramSequence seq)
	{
		var ret = ""
		var flag = false
		val weightPos =
			if((seq.eContainer as Sequence).returntype.compileType.endsWith("_enum"))
				2
			else
				3

		for(i : 0 ..< seq.values.size/weightPos)
		{
			ret = ret + (if(flag) ", " else "") + seq.values.get(weightPos*(i + 1) - 1).compileExpression.value
			flag = true
		}

		return ret
	}

	def public static compileHistogramEnums(HistogramSequence seq)
	{
		var ret = ""
		var flag = false

		val context = (new LocalContext).populateWithEnums((seq.eContainer as Sequence).returntype as RDOEnum)

		for(i : 0 ..< seq.values.size/2)
		{
			ret = ret + (if(flag) ", " else "") + seq.values.get(i*2).compileExpressionContext(context).value
			flag = true
		}

		return ret
	}

	def public static compileRegularSequence(RegularSequence seq, RDOType rtype, boolean legacy)
	{
		switch seq.type
		{
			case RegularSequenceType.UNIFORM:
				return
					'''
					public static «rtype.compileTypePrimitive» getNext(«rtype.compileTypePrimitive» from, «rtype.compileTypePrimitive» to)
					{
						return («rtype.compileTypePrimitive»)((to - from) * prng.nextDouble() + from);
					}
					'''
			case RegularSequenceType.EXPONENTIAL:
				/*return if(!legacy)
					'''
					public static «rtype.compileTypePrimitive» getNext(«rtype.compileTypePrimitive» mean)
					{
						return («rtype.compileTypePrimitive»)(-1.0 * mean * org.apache.commons.math3.util.FastMath.log(1 - prng.nextDouble()));
					}
					'''
				else*/
					'''
					public static «rtype.compileTypePrimitive» getNext(«rtype.compileTypePrimitive» mean)
					{
						return («rtype.compileTypePrimitive»)(-mean * Math.log(prng.nextDouble()));
					}
					'''
			case RegularSequenceType.NORMAL:
				/*return if(!legacy)
					'''
					public static «rtype.compileTypePrimitive» getNext(«rtype.compileTypePrimitive» mean, «rtype.compileTypePrimitive» deviation)
					{
						return («rtype.compileTypePrimitive»)(mean + deviation * org.apache.commons.math3.util.FastMath.sqrt(2) * org.apache.commons.math3.special.Erf.erfInv(2 * prng.nextDouble() - 1));
					}
					'''
				else*/
					'''
					public static «rtype.compileTypePrimitive» getNext(«rtype.compileTypePrimitive» mean, «rtype.compileTypePrimitive» deviation)
					{
						double ran = 0;
						for(int i = 0; i < 12; ++i)
						{
							ran += prng.nextDouble();
						}
						return deviation * (ran - 6) + mean;
					}
					'''
			case RegularSequenceType.TRIANGULAR:
				return
					'''
					public static «rtype.compileTypePrimitive» getNext(«rtype.compileTypePrimitive» a, «rtype.compileTypePrimitive» c, «rtype.compileTypePrimitive» b)
					{
						double next = prng.nextDouble();
						double edge = (double)(c - a) / (double)(b - a);

					if(next < edge)
							return («rtype.compileTypePrimitive»)(a + «IF !legacy»org.apache.commons.math3.util.Fast«ENDIF»Math.sqrt((b - a) * (c - a) * next));
						else
							return («rtype.compileTypePrimitive»)(b - «IF !legacy»org.apache.commons.math3.util.Fast«ENDIF»Math.sqrt((1 - next) * (b - a) * (b - c)));
					}
					'''
		}
	}
}
