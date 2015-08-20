package ru.bmstu.rk9.rao.compilers

import static extension ru.bmstu.rk9.rao.generator.RaoExpressionCompiler.*

import ru.bmstu.rk9.rao.generator.LocalContext

import ru.bmstu.rk9.rao.rao.Sequence

import ru.bmstu.rk9.rao.rao.RegularSequence

import ru.bmstu.rk9.rao.rao.EnumerativeSequence

import ru.bmstu.rk9.rao.rao.HistogramSequence

import ru.bmstu.rk9.rao.rao.RaoType
import ru.bmstu.rk9.rao.rao.RaoEnum
import ru.bmstu.rk9.rao.rao.UniformSequence
import ru.bmstu.rk9.rao.rao.ExponentialSequence
import ru.bmstu.rk9.rao.rao.NormalSequence
import ru.bmstu.rk9.rao.rao.TriangularSequence

class SequenceCompiler
{
	def static compileSequence(Sequence sequence, String filename)
	{
		'''
		package «filename»;

		import ru.bmstu.rk9.rao.lib.*;
		import ru.bmstu.rk9.rao.lib.sequence.*;
		import ru.bmstu.rk9.rao.lib.math.MersenneTwisterFast;
		import ru.bmstu.rk9.rao.lib.math.Erf;
		@SuppressWarnings("all")

		public class «sequence.name»
		{
			«IF sequence.type instanceof RegularSequence»
				«IF (sequence.type as RegularSequence).legacy»
				private static LegacyRandom prng =
					new LegacyRandom(«(sequence.type as RegularSequence).seed»);
				«ELSE»
				private static MersenneTwisterFast prng =
					new MersenneTwisterFast(«(sequence.type as RegularSequence).seed»);
				«ENDIF»

				public static void setSeed(long seed)
				{
					prng.setSeed(seed);
				}

				«(sequence.type as RegularSequence).compileRegularSequence(sequence.returnType, (sequence.type as RegularSequence).legacy)»
			«ENDIF»
			«IF sequence.type instanceof EnumerativeSequence»
				private static «sequence.returnType.compileTypePrimitive»[] values = new «sequence.returnType.compileTypePrimitive»[]
					{
						«FOR i : 0 ..< (sequence.type as EnumerativeSequence).values.size»
							«(sequence.type as EnumerativeSequence).values.get(i).compileExpression.value»«
								IF i != (sequence.type as EnumerativeSequence).values.size - 1»,«ENDIF»
						«ENDFOR»
					};

				private static int current = 0;

				public static «sequence.returnType.compileTypePrimitive» next()
				{
					if (current == values.length)
						current = 0;

					return values[current++];
				}
			«ENDIF»
			«IF sequence.type instanceof HistogramSequence»
				«IF (sequence.type as HistogramSequence).legacy»
				private static LegacyRandom prng =
					new LegacyRandom(«(sequence.type as HistogramSequence).seed»);
				«ELSE»
				private static MersenneTwisterFast prng =
					new MersenneTwisterFast(«(sequence.type as HistogramSequence).seed»);
				«ENDIF»

				public static void setSeed(long seed)
				{
					prng.setSeed(seed);
				}

				«IF sequence.returnType.compileType.endsWith("_enum")»
				private static «sequence.returnType.compileType»[] enums = new «sequence.returnType.compileType»[]{«(sequence.type as HistogramSequence).compileHistogramEnums»};
				«ENDIF»

				private static HistogramSequence histogram = new HistogramSequence(
					new double[]{«(sequence.type as HistogramSequence).compileHistogramValues»},
					new double[]{«(sequence.type as HistogramSequence).compileHistogramWeights»}
				);

				public static «sequence.returnType.compileTypePrimitive» next()
				{
					double x = histogram.calculateValue(prng.nextDouble());
					return «IF sequence.returnType.compileType.endsWith("_enum")»enums[ (int)x ]«ELSE»(«sequence.returnType.compileTypePrimitive»)x«ENDIF»;
				}
			«ENDIF»
		}
		'''
	}

	def private static compileHistogramValues(HistogramSequence sequence)
	{
		var ret = ""
		var flag = false
		val histogramEnum =
			if ((sequence.eContainer as Sequence).returnType.compileType.endsWith("_enum"))
				true
			else
				false

		if (histogramEnum)
			for (i : 0 ..< sequence.values.size / 2 + 1)
			{
				ret = ret + (if (flag) ", " else "") + i.toString
				flag = true
			}
		else
		{
			for (i : 0 ..< sequence.values.size / 3)
			{
				ret = ret + (if (flag) ", " else "") + sequence.values.get(3 * i).compileExpression.value
				flag = true
			}
			ret = ret + ", " + sequence.values.get(sequence.values.size - 2).compileExpression.value
		}

		return ret
	}

	def private static compileHistogramWeights(HistogramSequence sequence)
	{
		var ret = ""
		var flag = false
		val weight =
			if ((sequence.eContainer as Sequence).returnType.compileType.endsWith("_enum"))
				2
			else
				3

		for (i : 0 ..< sequence.values.size / weight)
		{
			ret = ret + (if (flag) ", " else "") + sequence.values.get(weight * (i + 1) - 1).compileExpression.value
			flag = true
		}

		return ret
	}

	def private static compileHistogramEnums(HistogramSequence sequence)
	{
		var ret = ""
		var flag = false

		val context = (new LocalContext).populateWithEnums((sequence.eContainer as Sequence).returnType as RaoEnum)

		for (i : 0 ..< sequence.values.size / 2)
		{
			ret = ret + (if (flag) ", " else "") + sequence.values.get(i * 2).compileExpressionContext(context).value
			flag = true
		}

		return ret
	}

	def private static compileRegularSequence(RegularSequence sequence,
			RaoType returnType, boolean legacy
	)
	{
		switch sequence.type
		{
			case "uniform": {
				var ret =
					'''
					«IF (sequence as UniformSequence).a != null»
						private static final double from = «
							(sequence as UniformSequence).a.compileExpression.value»;
						private static final double to = «
							(sequence as UniformSequence).b.compileExpression.value»;

						public static «returnType.compileTypePrimitive» next()
						{
							«returnType.compileUniformBody»
						}
					«ENDIF»
					'''
				ret +=
					'''
					public static «returnType.compileTypePrimitive» next(double from, double to)
					{
						«returnType.compileUniformBody»
					}
					'''
				return ret
			}
			case "exponential": {
				var ret =
					'''
					«IF (sequence as ExponentialSequence).rate != null»
						private static final double rate = «
							(sequence as ExponentialSequence).rate.compileExpression.value»;

						public static «returnType.compileTypePrimitive» next()
						{
							«returnType.compileExponentialBody»
						}
					«ENDIF»
					'''
				ret +=
					'''
					public static «returnType.compileTypePrimitive» next(double rate)
					{
						«returnType.compileExponentialBody»
					}
					'''
				return ret
			}

			case "normal": {
				var ret =
					'''
					«IF (sequence as NormalSequence).mean != null»
						private static final double mean = «
							(sequence as NormalSequence).mean.compileExpression.value»;
						private static final double deviation = «
							(sequence as NormalSequence).deviation.compileExpression.value»;

						«IF !legacy»
						public static «returnType.compileTypePrimitive» next()
						{
							«returnType.compileNormalBody»
						}
						«ELSE»
						public static «returnType.compileTypePrimitive» next()
						{
							«returnType.compileNormalBodyLegacy»
						}
						«ENDIF»
					«ENDIF»
					'''
				ret +=
					'''
					«IF !legacy»
					public static «returnType.compileTypePrimitive» next(double mean, double deviation)
					{
						«returnType.compileNormalBody»
					}
					«ELSE»
					public static «returnType.compileTypePrimitive» next(double mean, double deviation)
					{
						«returnType.compileNormalBodyLegacy»
					}
					«ENDIF»
					'''
				return ret
			}
			case "triangular": {
				var ret =
					'''
					«IF (sequence as TriangularSequence).a != null»
					private static final double a = «
						(sequence as TriangularSequence).a.compileExpression.value»;
					private static final double b = «
						(sequence as TriangularSequence).b.compileExpression.value»;
					private static final double c = «
						(sequence as TriangularSequence).c.compileExpression.value»;

					public static «returnType.compileTypePrimitive» next()
					{
						«returnType.compileTriangularBody»
					}
					«ENDIF»
					'''
				ret +=
					'''
					public static «returnType.compileTypePrimitive» next(double a, double b, double c)
					{
						«returnType.compileTriangularBody»
					}
					'''
				return ret
			}
		}
	}

	def private static compileUniformBody(RaoType returnType)
	{
		return '''return («returnType.compileTypePrimitive»)((to - from) * prng.nextDouble() + from);'''
	}

	def private static compileExponentialBody(RaoType returnType)
	{
		return '''return («returnType.compileTypePrimitive»)(-1.0 / rate * Math.log(1 - prng.nextDouble()));'''
	}

	def private static compileNormalBodyLegacy(RaoType returnType)
	{
		return
			'''
			double ran = 0;
			for (int i = 0; i < 12; ++i)
			{
				ran += prng.nextDouble();
			}
			return deviation * (ran - 6) + mean;
			'''
	}

	def private static compileNormalBody(RaoType returnType)
	{
		return '''return («returnType.compileTypePrimitive»)(mean + deviation * Math.sqrt(2) * Erf.erfInv(2 * prng.nextDouble() - 1));'''
	}

	def private static compileTriangularBody(RaoType returnType)
	{
		return
			'''
			double next = prng.nextDouble();
			double edge = (double)(c - a) / (double)(b - a);

			if (next < edge)
				return («returnType.compileTypePrimitive»)(a + Math.sqrt((b - a) * (c - a) * next));
			else
				return («returnType.compileTypePrimitive»)(b - Math.sqrt((1 - next) * (b - a) * (b - c)));
			'''
	}
}
