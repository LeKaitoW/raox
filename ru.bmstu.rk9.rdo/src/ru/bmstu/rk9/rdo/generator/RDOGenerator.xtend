package ru.bmstu.rk9.rdo.generator

import java.util.List

import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.ResourceSet

import org.eclipse.xtext.generator.IFileSystemAccess

import static extension org.eclipse.xtext.xbase.lib.IteratorExtensions.*

import static extension ru.bmstu.rk9.rdo.generator.RDONaming.*
import static extension ru.bmstu.rk9.rdo.customizations.RDOQualifiedNameProvider.*
import static extension ru.bmstu.rk9.rdo.generator.RDOExpressionCompiler.*
import static extension ru.bmstu.rk9.rdo.generator.RDOStatementCompiler.*

import ru.bmstu.rk9.rdo.customizations.IMultipleResourceGenerator

import ru.bmstu.rk9.rdo.rdo.RDOModel

import ru.bmstu.rk9.rdo.rdo.ResourceType
import ru.bmstu.rk9.rdo.rdo.ResourceTypeParameter
import ru.bmstu.rk9.rdo.rdo.RDORTPParameterType
import ru.bmstu.rk9.rdo.rdo.RDORTPParameterBasic
import ru.bmstu.rk9.rdo.rdo.RDORTPParameterString
import ru.bmstu.rk9.rdo.rdo.RDOEnum

import ru.bmstu.rk9.rdo.rdo.ResourceDeclaration

import ru.bmstu.rk9.rdo.rdo.ConstantDeclaration

import ru.bmstu.rk9.rdo.rdo.Sequence
import ru.bmstu.rk9.rdo.rdo.EnumerativeSequence
import ru.bmstu.rk9.rdo.rdo.RegularSequence
import ru.bmstu.rk9.rdo.rdo.RegularSequenceType
import ru.bmstu.rk9.rdo.rdo.HistogramSequence

import ru.bmstu.rk9.rdo.rdo.Function
import ru.bmstu.rk9.rdo.rdo.FunctionParameter
import ru.bmstu.rk9.rdo.rdo.FunctionTable
import ru.bmstu.rk9.rdo.rdo.FunctionAlgorithmic
import ru.bmstu.rk9.rdo.rdo.FunctionList

import ru.bmstu.rk9.rdo.rdo.Pattern
import ru.bmstu.rk9.rdo.rdo.PatternParameter
import ru.bmstu.rk9.rdo.rdo.PatternChoiceFrom
import ru.bmstu.rk9.rdo.rdo.PatternChoiceMethod
import ru.bmstu.rk9.rdo.rdo.Operation
import ru.bmstu.rk9.rdo.rdo.Rule
import ru.bmstu.rk9.rdo.rdo.Event

import ru.bmstu.rk9.rdo.rdo.DecisionPoint
import ru.bmstu.rk9.rdo.rdo.DecisionPointPrior
import ru.bmstu.rk9.rdo.rdo.DecisionPointSome

import ru.bmstu.rk9.rdo.rdo.Results
import ru.bmstu.rk9.rdo.rdo.ResultDeclaration
import ru.bmstu.rk9.rdo.rdo.ResultType
import ru.bmstu.rk9.rdo.rdo.ResultGetValue

import ru.bmstu.rk9.rdo.rdo.SimulationRun

import ru.bmstu.rk9.rdo.rdo.RDOType


class RDOGenerator implements IMultipleResourceGenerator
{
	override void doGenerate(Resource resource, IFileSystemAccess fsa)
	{}

	override void doGenerate(ResourceSet resources, IFileSystemAccess fsa)
	{
		//===== rdo_lib ====================================================================
		fsa.generateFile("rdo_lib/Simulator.java",                compileLibSimulator    ())
		fsa.generateFile("rdo_lib/EventScheduler.java",           compileEventScheduler  ())
		fsa.generateFile("rdo_lib/PermanentResourceManager.java", compilePermanentManager())
		fsa.generateFile("rdo_lib/TemporaryResourceManager.java", compileTemporaryManager())
		fsa.generateFile("rdo_lib/DPTManager.java",               compileDPTManager      ())
		fsa.generateFile("rdo_lib/RDOLegacyRandom.java",          compileRDOLegacyRandom ())
		fsa.generateFile("rdo_lib/HistogramSequence.java",        compileHistogram       ())
		fsa.generateFile("rdo_lib/SimpleChoiceFrom.java",         compileSimpleChoiceFrom())
		fsa.generateFile("rdo_lib/CombinationalChoiceFrom.java",  compileCommonChoiceFrom())
		fsa.generateFile("rdo_lib/Converter.java",                compileConverter       ())
		fsa.generateFile("rdo_lib/Event.java",                    compileEvent           ())
		fsa.generateFile("rdo_lib/DecisionPoint.java",            compileDecisionPoint   ())
		fsa.generateFile("rdo_lib/Result.java",                   compileResult          ())
		fsa.generateFile("rdo_lib/ResultManager.java",            compileResultManager   ())
		fsa.generateFile("rdo_lib/TerminateCondition.java",       compileTerminate       ())
		//==================================================================================

		val declarationList = new java.util.ArrayList<ResourceDeclaration>();
		for (resource : resources.resources)
			declarationList.addAll(resource.allContents.filter(typeof(ResourceDeclaration)).toIterable)

		val simulationList = new java.util.ArrayList<SimulationRun>();
		for (resource : resources.resources)
			simulationList.addAll(resource.allContents.filter(typeof(SimulationRun)).toIterable)

		var smr = if (simulationList.size > 0) simulationList.get(0) else null;

		for (resource : resources.resources)
			if (resource.contents.head != null)
			{
				val filename = (resource.contents.head as RDOModel).filenameFromURI

				for (e : resource.allContents.toIterable.filter(typeof(ResourceType)))
					fsa.generateFile(filename + "/" + e.name + ".java", e.compileResourceType(filename,
						declarationList.filter[r | r.reference.fullyQualifiedName == e.fullyQualifiedName]))

				for (e : resource.allContents.toIterable.filter(typeof(ConstantDeclaration)))
					fsa.generateFile(filename + "/" + e.name + ".java", e.compileConstant(filename))

				for (e : resource.allContents.toIterable.filter(typeof(Sequence)))
					fsa.generateFile(filename + "/" + e.name + ".java", e.compileSequence(filename))

				for (e : resource.allContents.toIterable.filter(typeof(Function)))
					fsa.generateFile(filename + "/" + e.name + ".java", e.compileFunction(filename))

				for (e : resource.allContents.toIterable.filter(typeof(Operation)))
					fsa.generateFile(filename + "/" + e.name + ".java", e.compileOperation(filename))

				for (e : resource.allContents.toIterable.filter(typeof(Rule)))
					fsa.generateFile(filename + "/" + e.name + ".java", e.compileRule(filename))

				for (e : resource.allContents.toIterable.filter(typeof(Event)))
					fsa.generateFile(filename + "/" + e.name + ".java", e.compileEvent(filename))

				for (e : resource.allContents.toIterable.filter(typeof(DecisionPoint)))
					fsa.generateFile(filename + "/" + e.name + ".java", e.compileDecisionPoint(filename))

				for (e : resource.allContents.toIterable.filter(typeof(ResultDeclaration)))
					fsa.generateFile(filename + "/" + (
						if ((e.eContainer as Results).name != null)	(e.eContainer as Results).name + "_"
							else "") + e.name + ".java", e.compileResult(filename))
			}

		fsa.generateFile("rdo_model/MainClass.java", compileMain(resources, smr))
	}

	def compileMain(ResourceSet rs, SimulationRun smr)
	{
		'''
		package rdo_model;

		public class MainClass
		{
			public static void main(String[] args)
			{
				long startTime = System.currentTimeMillis();

				System.out.println(" === RDO-Simulator ===\n");
				System.out.println("   Project «RDONaming.getProjectName(rs.resources.get(0).URI)»");
				System.out.println("      Source files are «rs.resources.map[r | r.contents.head.nameGeneric].toString»\n");

				«IF smr != null»«FOR c :smr.commands»
					«c.compileStatement»
				«ENDFOR»«ENDIF»

				«FOR r : rs.resources»
				«FOR c : r.allContents.filter(typeof(DecisionPoint)).toIterable»
					«c.fullyQualifiedName».init();
				«ENDFOR»
				«ENDFOR»

				«FOR r : rs.resources»
				«FOR c : r.allContents.filter(typeof(ResultDeclaration)).toIterable»
					«c.fullyQualifiedName».init();
				«ENDFOR»
				«ENDFOR»

				System.out.println("   Started model");

				int result = rdo_lib.Simulator.run();

				if (result == 1)
					System.out.println("\n   Stopped by terminate condition");

				if (result == 0)
					System.out.println("\n   Stopped (no more events)");

				System.out.println("\n   Finished model in " + String.valueOf((System.currentTimeMillis() - startTime)/1000.0) + "s");

				System.out.println("\nResults:");
				System.out.println("-------------------------------------------------------------------------------");
				rdo_lib.Simulator.getResults();
				System.out.println("-------------------------------------------------------------------------------");
			}
		}
		'''
	}

	def compileConstant(ConstantDeclaration con, String filename)
	{
		'''
		package «filename»;

		public class «con.name»
		{
			public static final «con.type.compileType» value = «con.value.compileExpression»;
			«IF con.type instanceof RDOEnum»

				public enum «(con.type as RDOEnum).getEnumParentName(false)»_enum
				{
					«(con.type as RDOEnum).makeEnumBody»
				}«
			ENDIF»
		}
		'''
	}

	def compileSequence(Sequence seq, String filename)
	{
		'''
		package «filename»;

		public class «seq.name»
		{
			«IF seq.type instanceof RegularSequence»
				«IF (seq.type as RegularSequence).legacy»
				private static rdo_lib.RDOLegacyRandom prng =
					new rdo_lib.RDOLegacyRandom(«(seq.type as RegularSequence).seed»);
				«ELSE»
				private static org.apache.commons.math3.random.MersenneTwister prng =
					new org.apache.commons.math3.random.MersenneTwister(«(seq.type as RegularSequence).seed»);
				«ENDIF»

				public static void setSeed(long seed)
				{
					prng.setSeed(seed);
				}

				«(seq.type as RegularSequence).compileRegularSequence(seq.returntype)»
			«ENDIF»
			«IF seq.type instanceof EnumerativeSequence»
				private «seq.returntype.compileTypePrimitive»[] values = new «seq.returntype.compileTypePrimitive»[]
					{
						«FOR i : 0 ..< (seq.type as EnumerativeSequence).values.size»
							«(seq.type as EnumerativeSequence).values.get(i).compileExpression»«
								IF i != (seq.type as EnumerativeSequence).values.size - 1»,«ENDIF»
						«ENDFOR»
					};

				private int current = 0;

				public «seq.returntype.compileTypePrimitive» getNext()
				{
					if (current == values.length)
						current = 0;

					return values[current++];
				}
			«ENDIF»
			«IF seq.type instanceof HistogramSequence»
				«IF (seq.type as HistogramSequence).legacy»
				private static rdo_lib.RDOLegacyRandom prng =
					new rdo_lib.RDOLegacyRandom(«(seq.type as HistogramSequence).seed»);
				«ELSE»
				private static org.apache.commons.math3.random.MersenneTwister prng =
					new org.apache.commons.math3.random.MersenneTwister(«(seq.type as HistogramSequence).seed»);
				«ENDIF»

				public static void setSeed(long seed)
				{
					prng.setSeed(seed);
				}

				«IF seq.returntype.compileType.endsWith("_enum")»
				private static «seq.returntype.compileType»[] enums = new «seq.returntype.compileType»[]{«(seq.type as HistogramSequence).compileHistogramEnums»};
				«ENDIF»

				private static rdo_lib.HistogramSequence histogram = new rdo_lib.HistogramSequence(
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

	def compileHistogramValues(HistogramSequence seq)
	{
		var ret = ""
		var flag = false
		val histEnum =
			if ((seq.eContainer as Sequence).returntype.compileType.endsWith("_enum"))
				true
			else
				false

		if (histEnum)
			for (i : 0 ..< seq.values.size/2 + 1)
			{
				ret = ret + (if (flag) ", " else "") + i.toString
				flag = true
			}
		else
		{
			for (i : 0 ..< seq.values.size/3)
			{
				ret = ret + (if (flag) ", " else "") + seq.values.get(3*i).compileExpression
				flag = true
			}
			ret = ret + ", " + seq.values.get(seq.values.size - 2).compileExpression
		}

		return ret
	}

	def compileHistogramWeights(HistogramSequence seq)
	{
		var ret = ""
		var flag = false
		val weightPos =
			if ((seq.eContainer as Sequence).returntype.compileType.endsWith("_enum"))
				2
			else
				3

		for (i : 0 ..< seq.values.size/weightPos)
		{
			ret = ret + (if (flag) ", " else "") + seq.values.get(weightPos*(i + 1) - 1).compileExpression
			flag = true
		}

		return ret
	}

	def compileHistogramEnums(HistogramSequence seq)
	{
		var ret = ""
		var flag = false

		for (i : 0 ..< seq.values.size/2)
		{
			ret = ret + (if (flag) ", " else "") + seq.values.get(i*2).compileExpression
			flag = true
		}

		return ret
	}		

	def compileRegularSequence(RegularSequence seq, RDOType rtype)
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
				return
					'''
					public static «rtype.compileTypePrimitive» getNext(«rtype.compileTypePrimitive» mean)
					{
						return («rtype.compileTypePrimitive»)(-1.0 * mean * org.apache.commons.math3.util.FastMath.log(1 - prng.nextDouble()));
					}
					'''
			case RegularSequenceType.NORMAL:
				return
					'''
					public static «rtype.compileTypePrimitive» getNext(«rtype.compileTypePrimitive» mean, «rtype.compileTypePrimitive» deviation)
					{
						return («rtype.compileTypePrimitive»)(mean + deviation * org.apache.commons.math3.util.FastMath.sqrt(2) * org.apache.commons.math3.special.Erf.erfInv(2 * prng.nextDouble() - 1));
					}
					'''
			case RegularSequenceType.TRIANGULAR:
				return
					'''
					public static «rtype.compileTypePrimitive» getNext(«rtype.compileTypePrimitive» a, «rtype.compileTypePrimitive» c, «rtype.compileTypePrimitive» b)
					{
						double next = prng.nextDouble();
						double edge = (double)(c - a) / (double)(b - a);

					if (next < edge)
							return («rtype.compileTypePrimitive»)(a + org.apache.commons.math3.util.FastMath.sqrt((b - a) * (c - a) * next));
						else
							return («rtype.compileTypePrimitive»)(b - org.apache.commons.math3.util.FastMath.sqrt((1 - next) * (b - a) * (b - c)));
					}
					'''
		}
	}

	def withFirstUpper(String s)
	{
		return Character.toUpperCase(s.charAt(0)) + s.substring(1)
	}

	def compileResourceType(ResourceType rtp, String filename, Iterable<ResourceDeclaration> instances)
	{
		'''
		package «filename»;

		public class «rtp.name»
		{
			private final static rdo_lib.«rtp.type.literal.withFirstUpper
				»ResourceManager<«rtp.name»> manager = new rdo_lib.«rtp.type.literal.withFirstUpper»ResourceManager<«rtp.name»>();

			public static rdo_lib.«rtp.type.literal.withFirstUpper»ResourceManager<«rtp.name»> getManager()
			{
				return manager;
			}

			«IF rtp.eAllContents.filter(typeof(RDOEnum)).toList.size > 0»// ENUMS«ENDIF»
			«FOR e : rtp.eAllContents.toIterable.filter(typeof(RDOEnum))»
				public enum «e.getEnumParentName(false)»_enum
				{
					«e.makeEnumBody»
				}

			«ENDFOR»
			«FOR parameter : rtp.parameters»
				public «parameter.type.compileType» «parameter.name»«parameter.type.getDefault»;
			«ENDFOR»

			public «rtp.name»(«rtp.parameters.compileResourceTypeParameters»)
			{
				«FOR parameter : rtp.parameters»
					if («parameter.name» != null)
						this.«parameter.name» = «parameter.name»;
				«ENDFOR»
			}
			«FOR r : instances»

				public static final «rtp.name» «r.name» = new «rtp.name»(«
					if (r.parameters != null) r.parameters.compileExpression else ""»);
				static
				{
					«rtp.name».getManager().addResource("«r.name»", «r.name»);
				}
			«ENDFOR»
		}
		'''
	}

	def makeEnumBody(RDOEnum e)
	{
		var flag = false
		var body = ""

		for (i : e.enums)
		{
			if (flag)
				body = body + ", "
			body = body + i.name
			flag = true
		}
		return body
	}

	def compileFunction(Function fun, String filename)
	{
		'''
		package «filename»;

		public class «fun.name»
		'''
		 +
		switch fun.type
		{
			FunctionAlgorithmic:
			'''
			{

			}
			'''
			FunctionTable:
			'''
			{

			}
			'''
			FunctionList:
			'''
			{

			}
			'''
		}
	}

	def compileEvent(Event evn, String filename)
	{
		'''
		package «filename»;

		public class «evn.name» implements rdo_lib.Event
		{
			private static final String name =  "«evn.fullyQualifiedName»";

			private static class RelevantResources
			{
				«FOR r : evn.relevantresources»
					«IF r.type instanceof ResourceType»
						public «r.type.fullyQualifiedName» «r.name»;
					«ELSE»
						public «(r.type as ResourceDeclaration).reference.fullyQualifiedName» «r.name»;
					«ENDIF»
				«ENDFOR»

				public RelevantResources init()
				{
					«FOR r : evn.relevantresources»
						«IF r.type instanceof ResourceDeclaration»
							«r.name» = «(r.type as ResourceDeclaration).reference.fullyQualifiedName».«(r.type as ResourceDeclaration).name»;
						«ELSE»
							«IF r.rule.literal == "Create"»
								«r.name» = new «r.type.fullyQualifiedName»(«(r.type as ResourceType).parameters.size.compileAllDefault»);
							«ENDIF»
						«ENDIF»
					«ENDFOR»

					return this;
				}
			}

			private static RelevantResources staticResources = new RelevantResources();

			private static class Parameters
			{
				«IF evn.parameters.size > 0»
				«FOR p : evn.parameters»
					public «p.type.compileType» «p.name»«p.type.getDefault»;
				«ENDFOR»

				public Parameters(«evn.parameters.compilePatternParameters»)
				{
					«FOR parameter : evn.parameters»
						if («parameter.name» != null)
							this.«parameter.name» = «parameter.name»;
					«ENDFOR»
				}
				«ENDIF»
			}

			private Parameters parameters;

			private double time;

			@Override
			public double getTime()
			{
				return time;
			}

			private static rdo_lib.Converter<RelevantResources, Parameters> converter =
				new rdo_lib.Converter<RelevantResources, Parameters>()
				{
					@Override
					public void run(RelevantResources resources, Parameters parameters)
					{
						«FOR e : evn.algorithms»
							«e.compileConvert(0)»
						«ENDFOR»
					}
				};

			@Override
			public void run()
			{
				converter.run(staticResources.init(), parameters);
				«IF evn.relevantresources.filter[t | t.rule.literal == "Create"].size > 0»

					// add resources
					«FOR r : evn.relevantresources.filter[t |t.rule.literal == "Create"]»
						«(r.type as ResourceType).fullyQualifiedName».getManager().addResource(staticResources.«r.name»);
					«ENDFOR»

				«ENDIF»
			}

			public «evn.name»(double time«IF evn.parameters.size > 0», «ENDIF»«evn.parameters.compilePatternParameters»)
			{
				this.time = time;
				this.parameters = new Parameters(«evn.parameters.compilePatternParametersCall»);
			}
		}
		'''
	}

	def compileRule(Rule rule, String filename)
	{
		'''
		package «filename»;

		public class «rule.name»
		{
			private static final String name = "«filename».«rule.name»";

			private static class RelevantResources
			{
				«FOR r : rule.relevantresources.filter[res | res.rule.literal != "Create"]»
					«IF r.type instanceof ResourceType»
						public «r.type.fullyQualifiedName» «r.name»;
					«ELSE»
						public «(r.type as ResourceDeclaration).reference.fullyQualifiedName» «r.name» = «(r.type as ResourceDeclaration).reference.fullyQualifiedName».«(r.type as ResourceDeclaration).name»;
					«ENDIF»
				«ENDFOR»

				public RelevantResources copy()
				{
					RelevantResources clone = new RelevantResources();

					«FOR r : rule.relevantresources.filter[res | res.rule.literal != "Create"]»
						clone.«r.name» = this.«r.name»;
					«ENDFOR»

					return clone;
				}

				public void clear()
				{
					«FOR r : rule.relevantresources.filter[res | res.type instanceof ResourceType && res.rule.literal != "Create"]»
						this.«r.name» = null;
					«ENDFOR»
				}
			}

			private static RelevantResources staticResources = new RelevantResources();
			private RelevantResources instanceResources;

			public static class Parameters
			{
				«IF rule.parameters.size > 0»
				«FOR p : rule.parameters»
					public «p.type.compileType» «p.name»«p.type.getDefault»;
				«ENDFOR»

				public Parameters(«rule.parameters.compilePatternParameters»)
				{
					«FOR p : rule.parameters»
						if («p.name» != null)
							this.«p.name» = «p.name»;
					«ENDFOR»
				}
				«ENDIF»
			}

			private Parameters parameters;

			private «rule.name»(double time, RelevantResources resources, Parameters parameters)
			{
				this.time = time;
				this.instanceResources = resources;
				this.parameters = parameters;
			}

			«IF rule.combinational != null»
			static private rdo_lib.CombinationalChoiceFrom<RelevantResources> choice =
				new rdo_lib.CombinationalChoiceFrom<RelevantResources>
				(
					staticResources,
					«rule.combinational.compileChoiceMethod("RelevantResources", "RelevantResources")»,
					new rdo_lib.CombinationalChoiceFrom.RelevantResourcesManager<RelevantResources>()
					{
						@Override
						public RelevantResources create(RelevantResources set)
						{
							RelevantResources clone = new RelevantResources();

							«FOR relres : rule.relevantresources.filter[r | r.rule.literal != "Create"]»
								clone.«relres.name» = set.«relres.name»;
							«ENDFOR»

							return clone;
						}

						@Override
						public void apply(RelevantResources origin, RelevantResources set)
						{
							«FOR relres : rule.relevantresources.filter[r | r.rule.literal != "Create"]»
								origin.«relres.name» = set.«relres.name»;
							«ENDFOR»
						}
					}
				);
				static
				{
				«FOR rc : rule.algorithms.filter[r | r.relres.type instanceof ResourceType && r.relres.rule.literal != "Create"]»
					choice.addFinder
					(
						new rdo_lib.CombinationalChoiceFrom.Finder<RelevantResources, «rc.relres.type.fullyQualifiedName»>
						(
							new rdo_lib.CombinationalChoiceFrom.Retriever<«rc.relres.type.fullyQualifiedName»>()
							{
								@Override
								public java.util.Collection<«rc.relres.type.fullyQualifiedName»> getResources()
								{
									return «rc.relres.type.fullyQualifiedName».getManager().get«
										IF rc.relres.rule.literal == "Erase"»Temporary«ELSE»All«ENDIF»();
								}
							},
							new rdo_lib.SimpleChoiceFrom<RelevantResources, «rc.relres.type.fullyQualifiedName»>
							(
								«rc.choicefrom.compileChoiceFrom(rule, rc.relres.type.fullyQualifiedName, rc.relres.name, rc.relres.type.fullyQualifiedName)»,
								null
							),
							new rdo_lib.CombinationalChoiceFrom.Setter<RelevantResources, «rc.relres.type.fullyQualifiedName»>()
							{
								public void set(RelevantResources set, «rc.relres.type.fullyQualifiedName» resource)
								{
									set.«rc.relres.name» = resource;
								}
							}
						)
					);

				«ENDFOR»
				}

			«ELSE»
			«FOR rc : rule.algorithms.filter[r | r.relres.type instanceof ResourceType && r.relres.rule.literal != "Create"]»
			// choice «rc.relres.name»
			private static rdo_lib.SimpleChoiceFrom<RelevantResources, «rc.relres.type.fullyQualifiedName»> «rc.relres.name»Choice =
				new rdo_lib.SimpleChoiceFrom<RelevantResources, «rc.relres.type.fullyQualifiedName»>
				(
					«rc.choicefrom.compileChoiceFrom(rule, rc.relres.type.fullyQualifiedName, rc.relres.name, rc.relres.type.fullyQualifiedName)»,
					«rc.choicemethod.compileChoiceMethod(rule.name, rc.relres.type.fullyQualifiedName)»
				);

			«ENDFOR»
			«ENDIF»

			private static rdo_lib.Converter<RelevantResources, Parameters> rule =
					new rdo_lib.Converter<RelevantResources, Parameters>()
					{
						@Override
						public void run(RelevantResources resources, Parameters parameters)
						{
							«FOR e : rule.algorithms.filter[r | r.haverule]»
								«e.compileConvert(0)»
							«ENDFOR»
						}
					};

			public static boolean tryRule(Parameters parameters)
			{
				staticResources.clear();

				«IF rule.combinational != null»
				if (!choice.find())
					return false;

				«ELSE»
				«FOR rc : rule.algorithms.filter[r | r.relres.type instanceof ResourceType && r.relres.rule.literal != "Create"]»
					staticResources.«rc.relres.name» = «rc.relres.name»Choice.find(staticResources, «rc.relres.type.fullyQualifiedName».getManager().get«
						IF rc.relres.rule.literal == "Erase"»Temporary«ELSE»All«ENDIF»());

					if (staticResources.«rc.relres.name» == null)
						return false;

				«ENDFOR»
				«ENDIF»
				RelevantResources matched = staticResources.copy();

				«IF rule.relevantresources.filter[t | t.rule.literal == "Create"].size > 0»
					// create resources
					«FOR r : rule.relevantresources.filter[t |t.rule.literal == "Create"]»
						matched.«r.name» = new «(r.type as ResourceType).fullyQualifiedName»(«
							(r.type as ResourceType).parameters.size.compileAllDefault»);
					«ENDFOR»
					«FOR r : rule.relevantresources.filter[t |t.rule.literal == "Create"]»
						«(r.type as ResourceType).fullyQualifiedName».getManager().addResource(matched.«r.name»);
					«ENDFOR»

				«ENDIF»
				«IF rule.relevantresources.filter[t | t.rule.literal == "Erase"].size > 0»
					// erase resources
					«FOR r : rule.relevantresources.filter[t |t.rule.literal == "Erase"]»
						«(r.type as ResourceType).fullyQualifiedName».getManager().eraseResource(matched.«r.name»);
					«ENDFOR»

				«ENDIF»
				rule.run(matched, parameters);

				return true;
			}
		}
		'''
	}

	def compileOperation(Operation op, String filename)
	{
		'''
		package «filename»;

		public class «op.name» implements rdo_lib.Event
		{
			private static final String name = "«filename».«op.name»";

			private static class RelevantResources
			{
				«FOR r : op.relevantresources.filter[res | res.begin.literal != "Create" && res.end.literal != "Create"]»
					«IF r.type instanceof ResourceType»
						public «r.type.fullyQualifiedName» «r.name»;
					«ELSE»
						public «(r.type as ResourceDeclaration).reference.fullyQualifiedName» «r.name» = «(r.type as ResourceDeclaration).reference.fullyQualifiedName».«(r.type as ResourceDeclaration).name»;
					«ENDIF»
				«ENDFOR»

				public RelevantResources copy()
				{
					RelevantResources clone = new RelevantResources();

					«FOR r : op.relevantresources.filter[res | res.begin.literal != "Create" && res.end.literal != "Create"]»
						clone.«r.name» = this.«r.name»;
					«ENDFOR»

					return clone;
				}

				public void clear()
				{
					«FOR r : op.relevantresources.filter[res | res.type instanceof ResourceType && res.begin.literal != "Create" && res.end.literal != "Create"]»
						this.«r.name» = null;
					«ENDFOR»
				}
			}

			private static RelevantResources staticResources = new RelevantResources();
			private RelevantResources instanceResources;

			public static class Parameters
			{
				«IF op.parameters.size > 0»
				«FOR p : op.parameters»
					public «p.type.compileType» «p.name»«p.type.getDefault»;
				«ENDFOR»

				public Parameters(«op.parameters.compilePatternParameters»)
				{
					«FOR p : op.parameters»
						if («p.name» != null)
							this.«p.name» = «p.name»;
					«ENDFOR»
				}
				«ENDIF»
			}

			private Parameters parameters;

			private «op.name»(double time, RelevantResources resources, Parameters parameters)
			{
				this.time = time;
				this.instanceResources = resources;
				this.parameters = parameters;
			}

			«IF op.combinational != null»
			static private rdo_lib.CombinationalChoiceFrom<RelevantResources> choice =
				new rdo_lib.CombinationalChoiceFrom<RelevantResources>
				(
					staticResources,
					«op.combinational.compileChoiceMethod("RelevantResources", "RelevantResources")»,
					new rdo_lib.CombinationalChoiceFrom.RelevantResourcesManager<RelevantResources>()
					{
						@Override
						public RelevantResources create(RelevantResources set)
						{
							RelevantResources clone = new RelevantResources();

							«FOR relres : op.relevantresources.filter[r | r.begin.literal != "Create" && r.end.literal != "Create"]»
								clone.«relres.name» = set.«relres.name»;
							«ENDFOR»

							return clone;
						}

						@Override
						public void apply(RelevantResources origin, RelevantResources set)
						{
							«FOR relres : op.relevantresources.filter[r | r.begin.literal != "Create" && r.end.literal != "Create"]»
								origin.«relres.name» = set.«relres.name»;
							«ENDFOR»
						}
					}
				);
				static
				{
				«FOR rc : op.algorithms.filter[r | r.relres.type instanceof ResourceType &&
						r.relres.begin.literal != "Create" && r.relres.end.literal != "Create"]»
					choice.addFinder
					(
						new rdo_lib.CombinationalChoiceFrom.Finder<RelevantResources, «rc.relres.type.fullyQualifiedName»>
						(
							new rdo_lib.CombinationalChoiceFrom.Retriever<«rc.relres.type.fullyQualifiedName»>()
							{
								@Override
								public java.util.Collection<«rc.relres.type.fullyQualifiedName»> getResources()
								{
									return «rc.relres.type.fullyQualifiedName».getManager().get«
										IF rc.relres.begin.literal == "Erase" || rc.relres.end.literal == "Erase"»Temporary«ELSE»All«ENDIF»();
								}
							},
							new rdo_lib.SimpleChoiceFrom<RelevantResources, «rc.relres.type.fullyQualifiedName»>
							(
								«rc.choicefrom.compileChoiceFrom(op, rc.relres.type.fullyQualifiedName, rc.relres.name, rc.relres.type.fullyQualifiedName)»,
								null
							),
							new rdo_lib.CombinationalChoiceFrom.Setter<RelevantResources, «rc.relres.type.fullyQualifiedName»>()
							{
								public void set(RelevantResources set, «rc.relres.type.fullyQualifiedName» resource)
								{
									set.«rc.relres.name» = resource;
								}
							}
						)
					);

				«ENDFOR»
				}

			«ELSE»
			«FOR rc : op.algorithms.filter[r | r.relres.type instanceof ResourceType &&
				r.relres.begin.literal != "Create" && r.relres.end.literal != "Create"]»
			// choice «rc.relres.name»
			private static rdo_lib.SimpleChoiceFrom<RelevantResources, «rc.relres.type.fullyQualifiedName»> «rc.relres.name»Choice =
				new rdo_lib.SimpleChoiceFrom<RelevantResources, «rc.relres.type.fullyQualifiedName»>
				(
					«rc.choicefrom.compileChoiceFrom(op, rc.relres.type.fullyQualifiedName, rc.relres.name, rc.relres.type.fullyQualifiedName)»,
					«rc.choicemethod.compileChoiceMethod(op.name, rc.relres.type.fullyQualifiedName)»
				);

			«ENDFOR»
			«ENDIF»

			private static rdo_lib.Converter<RelevantResources, Parameters> begin =
					new rdo_lib.Converter<RelevantResources, Parameters>()
					{
						@Override
						public void run(RelevantResources resources, Parameters parameters)
						{
							«FOR e : op.algorithms.filter[r | r.havebegin]»
								«e.compileConvert(0)»
							«ENDFOR»
						}
					};

			private static rdo_lib.Converter<RelevantResources, Parameters> end =
					new rdo_lib.Converter<RelevantResources, Parameters>()
					{
						@Override
						public void run(RelevantResources resources, Parameters parameters)
						{
							«FOR e : op.algorithms.filter[r | r.haveend]»
								«e.compileConvert(1)»
							«ENDFOR»
						}
					};

			public static boolean tryRule(Parameters parameters)
			{
				staticResources.clear();

				«IF op.combinational != null»
				if (!choice.find())
					return false;

				«ELSE»
				«FOR rc : op.algorithms.filter[r | r.relres.type instanceof ResourceType &&
					r.relres.begin.literal != "Create" && r.relres.end.literal != "Create"]»
					staticResources.«rc.relres.name» = «rc.relres.name»Choice.find(staticResources, «rc.relres.type.fullyQualifiedName».getManager().get«
						IF rc.relres.begin.literal == "Erase" || rc.relres.end.literal == "Erase"»Temporary«ELSE»All«ENDIF»());

					if (staticResources.«rc.relres.name» == null)
						return false;

				«ENDFOR»
				«ENDIF»
				RelevantResources matched = staticResources.copy();

				«IF op.relevantresources.filter[t | t.begin.literal == "Create"].size > 0»
					// create resources
					«FOR r : op.relevantresources.filter[t |t.begin.literal == "Create"]»
						matched.«r.name» = new «(r.type as ResourceType).fullyQualifiedName»(«
							(r.type as ResourceType).parameters.size.compileAllDefault»);
					«ENDFOR»
					«FOR r : op.relevantresources.filter[t |t.begin.literal == "Create"]»
						«(r.type as ResourceType).fullyQualifiedName».getManager().addResource(matched.«r.name»);
					«ENDFOR»

				«ENDIF»
				«IF op.relevantresources.filter[t | t.begin.literal == "Erase" || t.end.literal == "Erase"].size > 0»
					// erase resources
					«FOR r : op.relevantresources.filter[t |t.begin.literal == "Erase" || t.end.literal == "Erase"]»
						«(r.type as ResourceType).fullyQualifiedName».getManager().eraseResource(matched.«r.name»);
					«ENDFOR»

				«ENDIF»
				begin.run(matched, parameters);

				rdo_lib.Simulator.pushEvent(new «op.name»(rdo_lib.Simulator.getTime() + «op.time.compileExpression», matched, parameters));

				return true;
			}

			private double time;

			@Override
			public double getTime()
			{
				return time;
			}

			@Override
			public void run()
			{
				«IF op.relevantresources.filter[t | t.end.literal == "Create"].size > 0»
					// create resources
					«FOR r : op.relevantresources.filter[t |t.end.literal == "Create"]»
						instanceResources.«r.name» = new «(r.type as ResourceType).fullyQualifiedName»(«
							(r.type as ResourceType).parameters.size.compileAllDefault»);
					«ENDFOR»
					«FOR r : op.relevantresources.filter[t |t.end.literal == "Create"]»
						«(r.type as ResourceType).fullyQualifiedName».getManager().addResource(instanceResources.«r.name»);
					«ENDFOR»

				«ENDIF»
				end.run(instanceResources, parameters);
			}
		}
		'''
	}

	def static compileChoiceFrom(PatternChoiceFrom cf, Pattern pattern, String resource, String relres, String relrestype)
	{
		val havecf = cf != null && !cf.nocheck;

		var List<String> relreslist;
		var List<String> relrestypes;

		switch pattern
		{
			Operation:
			{
				relreslist = pattern.relevantresources.map[r | r.name]
				relrestypes = pattern.relevantresources.map[r |
					if (r.type instanceof ResourceType)
						(r.type as ResourceType).fullyQualifiedName
					else
						(r.type as ResourceDeclaration).reference.fullyQualifiedName
				]
			}

			Rule:
			{
				relreslist = pattern.relevantresources.map[r | r.name]
				relrestypes = pattern.relevantresources.map[r |
					if (r.type instanceof ResourceType)
						(r.type as ResourceType).fullyQualifiedName
					else
						(r.type as ResourceDeclaration).reference.fullyQualifiedName
				]
			}
		}

		return
			'''
			new rdo_lib.SimpleChoiceFrom.Checker<RelevantResources, «resource»>()
			{
				@Override
				public boolean check(RelevantResources pattern, «resource» «relres»)
				{
					return «IF havecf»(«cf.compileForPattern»)«ELSE»true«ENDIF»«FOR i : 0 ..< relreslist.size»«
						IF relres != relreslist.get(i) && relrestype == relrestypes.get(i)»
							«"\t"»&& «relres» != pattern.«relreslist.get(i)»«ENDIF»«ENDFOR»;
				}
			}'''
	}

	def compileChoiceMethod(PatternChoiceMethod cm, String pattern, String resource)
	{
		if (cm == null || cm.first)
			return "null"
		else
		{
			return
				'''
				new rdo_lib.SimpleChoiceFrom.ChoiceMethod<RelevantResources, «resource»>()
				{
					@Override
					public int compare(«resource» x, «resource» y)
					{
						if («cm.compileForPattern»)
							return -1;
						else
							return  1;
					}
				}'''
		}
	}

	def static String getDefault(RDORTPParameterType parameter)
	{
		switch parameter
		{
			RDORTPParameterBasic:
				return if (parameter.^default != null) " = " + RDOExpressionCompiler.compileExpression(parameter.^default) else ""
			RDORTPParameterString:
				return if (parameter.^default != null) ' = "' + parameter.^default + '"' else ""
			default:
				return ""
		}
	}

	def static compileResourceTypeParameters(List<ResourceTypeParameter> parameters)
	{
		'''«IF parameters.size > 0»«parameters.get(0).type.compileType» «
			parameters.get(0).name»«
			FOR parameter : parameters.subList(1, parameters.size)», «
				parameter.type.compileType» «
				parameter.name»«
			ENDFOR»«
		ENDIF»'''
	}

	def static compilePatternParametersCall(List<PatternParameter> parameters)
	{
		'''«IF parameters.size > 0»«
			parameters.get(0).name»«
			FOR parameter : parameters.subList(1, parameters.size)», «
				parameter.name»«
			ENDFOR»«
		ENDIF»'''
	}

	def static compilePatternParameters(List<PatternParameter> parameters)
	{
		'''«IF parameters.size > 0»«parameters.get(0).type.compileType» «
			parameters.get(0).name»«
			FOR parameter : parameters.subList(1, parameters.size)», «
				parameter.type.compileType» «
				parameter.name»«
			ENDFOR»«
		ENDIF»'''
	}

	def static compileFunctionParameters(List<FunctionParameter> parameters)
	{
		'''«IF parameters.size > 0»«parameters.get(0).type.compileType» «
			parameters.get(0).name»«
			FOR parameter : parameters.subList(1, parameters.size)», «
				parameter.type.compileType» «
				parameter.name»«
			ENDFOR»«
		ENDIF»'''
	}

	def compileDecisionPoint(DecisionPoint dpt, String filename)
	{
		val activities = switch dpt
		{
			DecisionPointSome :dpt.activities
			DecisionPointPrior: dpt.activities
		}

		val parameters = activities.map[a |
			if(a.pattern instanceof Operation)
				(a.pattern as Operation).parameters
			else
				(a.pattern as Rule).parameters
		]

		return
			'''
			package «filename»;

			public class «dpt.name»
			{
				«IF dpt instanceof DecisionPointSome || dpt instanceof DecisionPointPrior»
				«FOR i : 0 ..< activities.size»
				«IF activities.get(i).parameters.size == parameters.get(i).size»
				private static «activities.get(i).pattern.fullyQualifiedName».Parameters «activities.get(i).name» =
					new «activities.get(i).pattern.fullyQualifiedName».Parameters(«activities.get(i).compileExpression»);
				«ENDIF»
				«ENDFOR»

				«ENDIF»
				private static rdo_lib.DecisionPoint dpt =
					new rdo_lib.DecisionPoint
					(
						"«dpt.fullyQualifiedName»",
						«IF dpt.parent != null»«dpt.parent.fullyQualifiedName».getDPT()«ELSE»null«ENDIF»,
						«IF dpt instanceof DecisionPointPrior»«dpt.priority.compileExpression»«ELSE»null«ENDIF»,
						«IF dpt.condition != null
						»new rdo_lib.DecisionPoint.Condition()
						{
							@Override
							public boolean check()
							{
								return true;
							}
						}«ELSE»null«ENDIF»
					);

				public static void init()
				{
					«IF dpt instanceof DecisionPointSome || dpt instanceof DecisionPointPrior»
					«FOR a : activities»
						dpt.addActivity(
							new rdo_lib.DecisionPoint.Activity()
							{
								@Override
								public String getName()
								{
									return "«filename».«a.name»";
								}

								@Override
								public boolean checkActivity()
								{
									return («a.pattern.fullyQualifiedName».tryRule(«a.name»));
								}
							}
						);
					«ENDFOR»

					«ENDIF»
					rdo_lib.Simulator.addDecisionPoint(dpt);
				}

				public rdo_lib.DecisionPoint getDPT()
				{
					return dpt;
				}
			}
			'''
	}

	def compileResult(ResultDeclaration result, String filename)
	{
		'''
		package «filename»;

		public class «IF (result.eContainer as Results).name != null»«(result.eContainer as Results).name
			»_«ENDIF»«result.name»
		{
			private static rdo_lib.Result result = new rdo_lib.Result()
				{
					«result.type.compileResultBody»
				};

			public static void init()
			{
				rdo_lib.Simulator.addResult(result);
			}
		}
		'''
	}

	def compileResultBody (ResultType type)
	{
		switch type
		{
			ResultGetValue:
				'''
				@Override
				public void update() {}

				@Override
				public void get()
				{
					System.out.println("«(type.eContainer as ResultDeclaration).name»\t\t|\tType: get_value\t\t|\tValue: " +
						«type.expression.compileExpression»);
				}
				'''
			default:
				'''
				'''
		}
	}

	def compilePermanentManager()
	{
		'''
		package rdo_lib;

		import java.util.Map;
		import java.util.HashMap;

		public class PermanentResourceManager<T>
		{
			protected Map<String, T> resources = new HashMap<String, T>();

			public void addResource(String name, T res)
			{
				resources.put(name, res);
			}

			public T getResource(String name)
			{
				return resources.get(name);
			}

			public java.util.Collection<T> getAll()
			{
				return resources.values();
			}
		}
		'''
	}

	def compileTemporaryManager()
	{
		'''
		package rdo_lib;

		import java.util.Map;
		import java.util.HashMap;

		import java.util.Queue;
		import java.util.LinkedList;

		import java.util.Collection;

		public class TemporaryResourceManager<T> extends PermanentResourceManager<T>
		{
			private Map<T, Integer> temporary = new HashMap<T, Integer>();

			private Queue<Integer> vacantList = new LinkedList<Integer>();
			private int currentLast = 0;

			public void addResource(T res)
			{
				if (temporary.containsKey(res))
					return;

				int number;
				if (vacantList.size() > 0)
					number = vacantList.poll();
				else
					number = currentLast++;

				temporary.put(res, number);
			}

			public void eraseResource(T res)
			{
				vacantList.add(temporary.get(res));
				temporary.remove(res);
			}

			@Override
			public Collection<T> getAll()
			{
				Collection<T> all = resources.values();
				all.addAll(temporary.keySet());
				return all;
			}

			public Collection<T> getTemporary()
			{
				return temporary.keySet();
			}
		}
		'''
	}

	def compileDPTManager()
	{
		'''
		package rdo_lib;

		import java.util.Iterator;
		import java.util.LinkedList;

		class DPTManager
		{
			private LinkedList<DecisionPoint> dptList = new LinkedList<DecisionPoint>();

			void addDecisionPoint(DecisionPoint dpt)
			{
				dptList.add(dpt);
			}

			boolean checkDPT()
			{
				Iterator<DecisionPoint> dptIterator = dptList.iterator();

				while (dptIterator.hasNext())
					if (dptIterator.next().checkActivities())
						return true;

				return false;
			}
		}
		'''
	}

	def compileRDOLegacyRandom()
	{
		'''
		package rdo_lib;

		public class RDOLegacyRandom
		{
			private long seed = 0;

			public RDOLegacyRandom(long seed)
			{
				this.seed = seed;
			}

			public void setSeed(long seed)
			{
				this.seed = seed;
			}

			public double nextDouble()
			{
				seed = (seed * 69069L + 1L) % 4294967296L;
				return seed / 4294967296.0;
			}
		}
		'''
	}

	def compileHistogram()
	{
		'''
		package rdo_lib;

		public class HistogramSequence
		{
			public HistogramSequence(double[] values, double[] weights)
			{
				this.values = values;
				this.weights = weights;

				this.range = new double[weights.length];

				calculateSum();
				calculateRange();
			}

			private double[] values;
			private double[] weights;

			private double sum = 0;
			private double[] range;

			private void calculateSum()
			{
				for (int i = 0; i < weights.length; i++)
					sum += weights[i] * (values[i + 1] - values[i]);
			}

			private void calculateRange()
			{
				double crange = 0;
				for (int i = 0; i < weights.length; i++)
				{
					crange += (weights[i] * (values[i + 1] - values[i])) / sum;
					range[i] = crange;
				}
			}

			public double calculateValue(double rand)
			{
				double x = values[0];

				for (int i = 0; i < range.length; i++)
					if (range[i] <= rand)
						x = values[i + 1];
					else
					{
						x += (sum / weights[i]) * (rand - (i > 0 ? range[i - 1] : 0));
						break;
					}

				return x;
			}
		}
		'''
	}

	def compileSimpleChoiceFrom()
	{
		'''
		package rdo_lib;

		import java.util.Collection;
		import java.util.Iterator;

		import java.util.Queue;
		import java.util.LinkedList;

		import java.util.PriorityQueue;
		import java.util.Comparator;

		public class SimpleChoiceFrom<P, T>
		{
			public static interface Checker<P, T>
			{
				public boolean check(P pattern, T res);
			}

			public static abstract class ChoiceMethod<P, T> implements Comparator<T>
			{
				protected P pattern;

				private void setPattern(P pattern)
				{
					this.pattern = pattern;
				}
			}

			private Checker<P, T> checker;
			private ChoiceMethod<P, T> comparator;

			public SimpleChoiceFrom(Checker<P, T> checker, ChoiceMethod<P, T> comparator)
			{
				this.checker = checker;
				if (comparator != null)
				{
					this.comparator = comparator;
					matchingList = new PriorityQueue<T>(1, comparator);
				}
				else
					matchingList = new LinkedList<T>();
			}

			private Queue<T> matchingList;

			public Collection<T> findAll(P pattern, Collection<T> reslist)
			{
				matchingList.clear();
				if (comparator != null)
					comparator.setPattern(pattern);

				T res;
				for (Iterator<T> iterator = reslist.iterator(); iterator.hasNext();)
				{
					res = iterator.next();
					if (checker.check(pattern, res))
						matchingList.add(res);
				}

				return matchingList;
			}

			public T find(P pattern, Collection<T> reslist)
			{
				matchingList.clear();
				if (comparator != null)
					comparator.setPattern(pattern);

				T res;
				for (Iterator<T> iterator = reslist.iterator(); iterator.hasNext();)
				{
					res = iterator.next();
					if (checker.check(pattern, res))
						if (matchingList instanceof LinkedList)
							return res;
						else
							matchingList.add(res);
				}

				if (matchingList.size() == 0)
					return null;
				else
					return matchingList.poll();
			}
		}
		'''
	}

	def compileCommonChoiceFrom()
	{
		'''
		package rdo_lib;

		import java.util.List;
		import java.util.ArrayList;

		import java.util.Collection;
		import java.util.Iterator;

		import java.util.PriorityQueue;

		import rdo_lib.SimpleChoiceFrom.ChoiceMethod;

		public class CombinationalChoiceFrom<R>
		{
			private R set;
			private RelevantResourcesManager<R> setManager;
			private PriorityQueue<R> matchingList;

			public CombinationalChoiceFrom(R set, ChoiceMethod<R, R> comparator, RelevantResourcesManager<R> setManager)
			{
				this.set = set;
				this.setManager = setManager;
				matchingList = new PriorityQueue<R>(1, comparator);
			}

			public static abstract class Setter<R, T>
			{
				public abstract void set(R set, T resource);
			}

			public static interface Retriever<T>
			{
				public Collection<T> getResources();
			}

			public static abstract class RelevantResourcesManager<R>
			{
				public abstract R create(R set);
				public abstract void apply(R origin, R set);
			}

			public static class Finder<R, T>
			{
				private Retriever<T> retriever;
				private SimpleChoiceFrom<R, T> choice;
				private Setter<R, T> setter;

				public Finder(Retriever<T> retriever, SimpleChoiceFrom<R, T> choice, Setter<R, T> setter)
				{
					this.retriever = retriever;
					this.choice  = choice;
					this.setter  = setter;
				}

				public boolean find(R set, Iterator<Finder<R, ?>> finder, PriorityQueue<R> matchingList, RelevantResourcesManager<R> setManager)
				{
					Collection<T> all = choice.findAll(set, retriever.getResources());
					Iterator<T> iterator = all.iterator();
					if (finder.hasNext())
					{
						Finder<R, ?> currentFinder = finder.next();
						while (iterator.hasNext())
						{
							setter.set(set, iterator.next());
							if (currentFinder.find(set, finder, matchingList, setManager))
								if (matchingList.iterator() == null)
									return true;
						}
						return false;
					}
					else
					{
						if (all.size() > 0)
							if (matchingList.comparator() == null)
							{
								setter.set(set, all.iterator().next());
								return true;
							}
							else
							{
								for (T a : all)
								{
									setter.set(set, a);
									matchingList.add(setManager.create(set));
								}
								return true;
							}
						else
							return false;
					}
				}
			}

			private List<Finder<R, ?>> finders = new ArrayList<Finder<R, ?>>();

			public void addFinder(Finder<R, ?> finder)
			{
				finders.add(finder);
			}

			public boolean find()
			{
				matchingList.clear();

				if (finders.size() == 0)
					return false;

				Iterator<Finder<R, ?>> finder = finders.iterator();

				if (finder.next().find(set, finder, matchingList, setManager)
					&& matchingList.comparator() == null)
						return true;

				if (matchingList.size() > 0)
				{
					setManager.apply(set, matchingList.poll());
					return true;
				}
				else
					return false;
			}
		}
		'''
	}

	def compileLibSimulator()
	{
		'''
		package rdo_lib;

		import java.util.LinkedList;

		public abstract class Simulator
		{
			private static double time = 0;

			public static double getTime()
			{
				return time;
			}

			private static EventScheduler eventScheduler = new EventScheduler();

			public static void pushEvent(Event event)
			{
				eventScheduler.pushEvent(event);
			}

			private static LinkedList<TerminateCondition> terminateList = new LinkedList<TerminateCondition>();

			public static void addTerminateCondition(TerminateCondition c)
			{
				terminateList.add(c);
			}

			private static DPTManager dptManager = new DPTManager();

			public static void addDecisionPoint(DecisionPoint dpt)
			{
				dptManager.addDecisionPoint(dpt);
			}

			private static ResultManager resultManager = new ResultManager();

			public static void addResult(Result result)
			{
				resultManager.addResult(result);
			}	

			public static void getResults()
			{
				resultManager.getResults();
			}

			public static int run()
			{
				while(dptManager.checkDPT());

				while(eventScheduler.haveEvents())
				{
					Event current = eventScheduler.popEvent();

					time = current.getTime();

					current.run();

					for (TerminateCondition c : terminateList)
						if (c.check())
							return 1;

					while(dptManager.checkDPT());
				}
				return 0;
			}
		}
		'''
	}

	def compileEventScheduler()
	{
		'''
		package rdo_lib;

		import java.util.PriorityQueue;
		import java.util.Comparator;

		class EventScheduler
		{
			private static Comparator<Event> comparator = new Comparator<Event>()
			{
				@Override
				public int compare(Event x, Event y)
				{
					if (x.getTime() < y.getTime()) return -1;
					if (x.getTime() > y.getTime()) return  1;
					return 0;
				}
			};

			private PriorityQueue<Event> eventList = new PriorityQueue<Event>(1, comparator);

			void pushEvent(Event event)
			{
				if (event.getTime() >= Simulator.getTime())
					eventList.add(event);
			}

			Event popEvent()
			{
				return eventList.poll();
			}

			boolean haveEvents()
			{
				return eventList.size() > 0;
			}
		}
		'''
	}

	def compileTerminate()
	{
		'''
		package rdo_lib;

		public interface TerminateCondition
		{
			public boolean check();
		}
		'''
	}

	def compileConverter()
	{
		'''
		package rdo_lib;

		public interface Converter<R, P>
		{
			public void run(R resources, P parameters);
		}
		'''
	}

	def compileEvent()
	{
		'''
		package rdo_lib;

		public interface Event
		{
			public double getTime();
			public void run();
		}
		'''
	}

	def compileDecisionPoint()
	{
		'''
		package rdo_lib;

		import java.util.List;
		import java.util.LinkedList;

		public class DecisionPoint
		{
			private String name;
			private DecisionPoint parent;
			private Double priority;

			public static interface Condition
			{
				public boolean check();
			}

			private Condition condition;

			public DecisionPoint(String name, DecisionPoint parent, Double priority, Condition condition)
			{
				this.name = name;
				this.parent = parent;
				this.priority = priority;
				this.condition = condition;
			}

			public String getName()
			{
				return name;
			}

			public Double getPriority()
			{
				return priority;
			}

			public static interface Activity
			{
				public String getName();
				public boolean checkActivity();
			}

			private List<Activity> activities = new LinkedList<Activity>();

			public void addActivity(Activity a)
			{
				activities.add(a);
			}

			public boolean checkActivities()
			{
				if (condition != null && !condition.check())
					return false;

				for (Activity a : activities)
					if (a.checkActivity())
						return true;

				return false;
			}
		}
		'''
	}

	def compileResultManager()
	{
		'''
		package rdo_lib;

		import java.util.LinkedList;

		class ResultManager
		{
			private LinkedList<Result> results = new LinkedList<Result>();

			void addResult(Result result)
			{
				results.add(result);
			}

			void getResults()
			{
				for (Result r : results)
					r.get();
			}
		}
		'''
	}

	def compileResult()
	{
		'''
		package rdo_lib;

		public interface Result
		{
			public void update();
			public void get();
		}
		'''
	}
}

