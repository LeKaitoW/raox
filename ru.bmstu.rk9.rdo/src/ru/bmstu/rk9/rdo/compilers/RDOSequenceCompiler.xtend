package ru.bmstu.rk9.rdo.compilers

import static extension ru.bmstu.rk9.rdo.generator.RDOExpressionCompiler.*

import ru.bmstu.rk9.rdo.generator.LocalContext

import ru.bmstu.rk9.rdo.rdo.Sequence

import ru.bmstu.rk9.rdo.rdo.RegularSequence

import ru.bmstu.rk9.rdo.rdo.EnumerativeSequence

import ru.bmstu.rk9.rdo.rdo.HistogramSequence

import ru.bmstu.rk9.rdo.rdo.RDOType
import ru.bmstu.rk9.rdo.rdo.RDOEnum
import ru.bmstu.rk9.rdo.rdo.UniformSequence
import ru.bmstu.rk9.rdo.rdo.ExponentialSequence
import ru.bmstu.rk9.rdo.rdo.NormalSequence
import ru.bmstu.rk9.rdo.rdo.TriangularSequence

class RDOSequenceCompiler
{
	def static compileSequence(Sequence seq, String filename)
	{
		'''
		package «filename»;

		import ru.bmstu.rk9.rdo.lib.*;
		import ru.bmstu.rk9.rdo.lib.math.MersenneTwisterFast;
		import ru.bmstu.rk9.rdo.lib.math.Erf;
		@SuppressWarnings("all")

		public class «seq.name»
		{
			«IF seq.type instanceof RegularSequence»
				«IF (seq.type as RegularSequence).legacy»
				private static RDOLegacyRandom prng =
					new RDOLegacyRandom(«(seq.type as RegularSequence).seed»);
				«ELSE»
				private static MersenneTwisterFast prng =
					new MersenneTwisterFast(«(seq.type as RegularSequence).seed»);
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

				public «seq.returntype.compileTypePrimitive» next()
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
				private static MersenneTwisterFast prng =
					new MersenneTwisterFast(«(seq.type as HistogramSequence).seed»);
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

				public static «seq.returntype.compileTypePrimitive» next()
				{
					double x = histogram.calculateValue(prng.nextDouble());
					return «IF seq.returntype.compileType.endsWith("_enum")»enums[ (int)x ]«ELSE»(«seq.returntype.compileTypePrimitive»)x«ENDIF»;
				}
			«ENDIF»
		}
		'''
	}

	def private static compileHistogramValues(HistogramSequence seq)
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

	def private static compileHistogramWeights(HistogramSequence seq)
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

	def private static compileHistogramEnums(HistogramSequence seq)
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

	def private static compileRegularSequence(RegularSequence seq,
			RDOType rtype, boolean legacy
	)
	{
		switch seq.type
		{
			case "uniform": {
				var ret =
					'''
					«IF (seq as UniformSequence).a != null»
						private static final double from = «
							(seq as UniformSequence).a.compileExpression.value»;
						private static final double to = «
							(seq as UniformSequence).b.compileExpression.value»;

						public static «rtype.compileTypePrimitive» next()
						{
							return («rtype.compileTypePrimitive»)((to - from) * prng.nextDouble() + from);
						}
					«ENDIF»
					'''
				ret +=
					'''
					public static «rtype.compileTypePrimitive» next(double from, double to)
					{
						return («rtype.compileTypePrimitive»)((to - from) * prng.nextDouble() + from);
					}
					'''
				return ret
			}
			case "exponential": {
				var ret =
					'''
					«IF (seq as ExponentialSequence).rate != null»
						private static final double rate = «
							(seq as ExponentialSequence).rate.compileExpression.value»;

						public static «rtype.compileTypePrimitive» next()
						{
							return («rtype.compileTypePrimitive»)(-1.0 / rate * Math.log(1 - prng.nextDouble()));
						}
					«ENDIF»
					'''
				ret +=
					'''
					public static «rtype.compileTypePrimitive» next(double rate)
					{
						return («rtype.compileTypePrimitive»)(-1.0 / rate * Math.log(1 - prng.nextDouble()));
					}
					'''
				return ret
			}

			case "normal": {
				var ret =
					'''
					«IF (seq as NormalSequence).mean != null»
						private static final double mean = «
							(seq as NormalSequence).mean.compileExpression.value»;
						private static final double deviation = «
							(seq as NormalSequence).deviation.compileExpression.value»;

						«IF !legacy»
						public static «rtype.compileTypePrimitive» next()
						{
							return («rtype.compileTypePrimitive»)(mean + deviation * Math.sqrt(2) * Erf.erfInv(2 * prng.nextDouble() - 1));
						}
						«ELSE»
						public static «rtype.compileTypePrimitive» next()
						{
							double ran = 0;
							for(int i = 0; i < 12; ++i)
							{
								ran += prng.nextDouble();
							}
							return deviation * (ran - 6) + mean;
						}
						«ENDIF»
					«ENDIF»
					'''
				ret +=
					'''
					«IF !legacy»
					public static «rtype.compileTypePrimitive» next(double mean, double deviation)
					{
						return («rtype.compileTypePrimitive»)(mean + deviation * Math.sqrt(2) * Erf.erfInv(2 * prng.nextDouble() - 1));
					}
					«ELSE»
					public static «rtype.compileTypePrimitive» next(double mean, double deviation)
					{
						double ran = 0;
						for(int i = 0; i < 12; ++i)
						{
							ran += prng.nextDouble();
						}
						return deviation * (ran - 6) + mean;
					}
					«ENDIF»
					'''
				return ret
			}
			case "triangular": {
				var ret =
					'''
					«IF (seq as TriangularSequence).a != null»
					private static final double a = «
						(seq as TriangularSequence).a.compileExpression.value»;
					private static final double b = «
						(seq as TriangularSequence).b.compileExpression.value»;
					private static final double c = «
						(seq as TriangularSequence).c.compileExpression.value»;

					public static «rtype.compileTypePrimitive» next()
					{
						double next = prng.nextDouble();
						double edge = (double)(c - a) / (double)(b - a);

					if(next < edge)
							return («rtype.compileTypePrimitive»)(a + Math.sqrt((b - a) * (c - a) * next));
						else
							return («rtype.compileTypePrimitive»)(b - Math.sqrt((1 - next) * (b - a) * (b - c)));
					}
					«ENDIF»
					'''
				ret +=
					'''
					public static «rtype.compileTypePrimitive» next(double a, double b, double c)
					{
						double next = prng.nextDouble();
						double edge = (double)(c - a) / (double)(b - a);

					if(next < edge)
							return («rtype.compileTypePrimitive»)(a + Math.sqrt((b - a) * (c - a) * next));
						else
							return («rtype.compileTypePrimitive»)(b - Math.sqrt((1 - next) * (b - a) * (b - c)));
					}
					'''
				return ret
			}
		}
	}
}
