package ru.bmstu.rk9.rdo.generator

import java.util.List

import java.util.HashMap

import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.ResourceSet

import org.eclipse.emf.ecore.EObject

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
import ru.bmstu.rk9.rdo.rdo.RDORTPParameterEnum
import ru.bmstu.rk9.rdo.rdo.RDORTPParameterSuchAs
import ru.bmstu.rk9.rdo.rdo.RDORTPParameterString

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
import ru.bmstu.rk9.rdo.rdo.OperationConvert
import ru.bmstu.rk9.rdo.rdo.Rule
import ru.bmstu.rk9.rdo.rdo.RuleConvert
import ru.bmstu.rk9.rdo.rdo.Event

import ru.bmstu.rk9.rdo.rdo.DecisionPoint
import ru.bmstu.rk9.rdo.rdo.DecisionPointPrior
import ru.bmstu.rk9.rdo.rdo.DecisionPointSome
import ru.bmstu.rk9.rdo.rdo.DecisionPointSearch

import ru.bmstu.rk9.rdo.rdo.Results
import ru.bmstu.rk9.rdo.rdo.ResultDeclaration
import ru.bmstu.rk9.rdo.rdo.ResultType
import ru.bmstu.rk9.rdo.rdo.ResultGetValue

import ru.bmstu.rk9.rdo.rdo.SimulationRun

import ru.bmstu.rk9.rdo.rdo.RDOType
import ru.bmstu.rk9.rdo.rdo.RDOEnum
import ru.bmstu.rk9.rdo.rdo.Expression
import ru.bmstu.rk9.rdo.rdo.RDOInteger
import ru.bmstu.rk9.rdo.rdo.IntConstant
import ru.bmstu.rk9.rdo.rdo.DoubleConstant

class RDOGenerator implements IMultipleResourceGenerator
{
	override void doGenerate(Resource resource, IFileSystemAccess fsa)
	{}

	override void doGenerate(ResourceSet resources, IFileSystemAccess fsa)
	{
		//===== rdo_lib ====================================================================
		fsa.generateFile("rdo_lib/Simulator.java",                compileLibSimulator    ())
		fsa.generateFile("rdo_lib/Tracer.java",                   compileTracer          ())
		fsa.generateFile("rdo_lib/EventScheduler.java",           compileEventScheduler  ())
		fsa.generateFile("rdo_lib/PermanentResource.java",        compilePermanentRes    ())
		fsa.generateFile("rdo_lib/TemporaryResource.java",        compileTemporaryRes    ())
		fsa.generateFile("rdo_lib/ResourceComparison.java",       compileResComparison   ())
		fsa.generateFile("rdo_lib/PermanentResourceManager.java", compilePermanentManager())
		fsa.generateFile("rdo_lib/TemporaryResourceManager.java", compileTemporaryManager())
		fsa.generateFile("rdo_lib/Database.java",                 compileDatabase        ())
		fsa.generateFile("rdo_lib/DPTManager.java",               compileDPTManager      ())
		fsa.generateFile("rdo_lib/Select.java",                   compileSelect          ())
		fsa.generateFile("rdo_lib/RDOLegacyRandom.java",          compileRDOLegacyRandom ())
		fsa.generateFile("rdo_lib/RDORangedInteger.java",         compileRDORangedInteger())
		fsa.generateFile("rdo_lib/RDORangedDouble.java",          compileRDORangedDouble ())
		fsa.generateFile("rdo_lib/HistogramSequence.java",        compileHistogram       ())
		fsa.generateFile("rdo_lib/SimpleChoiceFrom.java",         compileSimpleChoiceFrom())
		fsa.generateFile("rdo_lib/CombinationalChoiceFrom.java",  compileCommonChoiceFrom())
		fsa.generateFile("rdo_lib/Converter.java",                compileConverter       ())
		fsa.generateFile("rdo_lib/Event.java",                    compileEvent           ())
		fsa.generateFile("rdo_lib/DecisionPoint.java",            compileDecisionPoint   ())
		fsa.generateFile("rdo_lib/DecisionPointSearch.java",      compileDPTSearch       ())
		fsa.generateFile("rdo_lib/Result.java",                   compileResult          ())
		fsa.generateFile("rdo_lib/ResultManager.java",            compileResultManager   ())
		fsa.generateFile("rdo_lib/TerminateCondition.java",       compileTerminate       ())
		//==================================================================================

		exportVariableInfo(resources)

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

				for (e : resource.allContents.toIterable.filter[d |
						d instanceof DecisionPointSome || d instanceof DecisionPointPrior])
					fsa.generateFile(filename + "/" + (e as DecisionPoint).name + ".java",
						(e as DecisionPoint).compileDecisionPoint(filename))

				for (e : resource.allContents.toIterable.filter(typeof(DecisionPointSearch)))
					fsa.generateFile(filename + "/" + e.name + ".java",	e.compileDecisionPointSearch(filename))

				for (e : resource.allContents.toIterable.filter(typeof(ResultDeclaration)))
					fsa.generateFile(filename + "/" + (
						if ((e.eContainer as Results).name != null)	(e.eContainer as Results).name + "_"
							else "") + e.name + ".java", e.compileResult(filename))
			}

		fsa.generateFile("rdo_model/" + RDONaming.getProjectName(resources.resources.get(0).URI) +"_database.java",
			compileDatabase(resources, RDONaming.getProjectName(resources.resources.get(0).URI)))
		fsa.generateFile("rdo_model/MainClass.java", compileMain(resources, smr))
	}

	public static HashMap<String, GlobalContext> variableIndex = new HashMap<String, GlobalContext>

	def exportVariableInfo(ResourceSet rs)
	{
		variableIndex.clear
		for(r : rs.resources)
			variableIndex.put(r.resourceName, new GlobalContext)

		for(r : rs.resources)
		{
			val info = variableIndex.get(r.resourceName)

			for(rss : r.allContents.filter(typeof(ResourceDeclaration)).toIterable)
				info.resources.put(rss.name, info.newRSS(rss))

			for(seq : r.allContents.filter(typeof(Sequence)).toIterable)
				info.sequences.put(seq.name, info.newSEQ(seq))

			for(con : r.allContents.filter(typeof(ConstantDeclaration)).toIterable)
				info.constants.put(con.name, info.newCON(con))

			for(fun : r.allContents.filter(typeof(Function)).toIterable)
				info.functions.put(fun.name, info.newFUN(fun))
		}
	}

	def compileDatabase(ResourceSet rs, String project)
	{
		'''
		package rdo_model;

		public class «project»_database implements rdo_lib.Database<«project»_database>
		{
			«FOR r : rs.resources»
				«FOR rtp : r.allContents.filter(typeof(ResourceType)).toIterable»
					rdo_lib.«IF rtp.type.literal == 'temporary'»Temporary«ELSE»Permanent«ENDIF
						»ResourceManager<«rtp.fullyQualifiedName»> «r.allContents.head.nameGeneric
							»_«rtp.name»_manager;
				«ENDFOR»
			«ENDFOR»

			private static «project»_database current;

			public static void init()
			{
				(new «project»_database()).deploy();

				«FOR r : rs.resources»
					«FOR rtp : r.allContents.filter(typeof(ResourceDeclaration)).toIterable»
						(new «rtp.reference.fullyQualifiedName»(«if (rtp.parameters != null)
							rtp.parameters.compileExpression.value else ""»)).register("«rtp.name»");
					«ENDFOR»
				«ENDFOR»
			}

			public static «project»_database getCurrent()
			{
				return current;
			}

			private «project»_database()
			{
				«FOR r : rs.resources»
					«FOR rtp : r.allContents.filter(typeof(ResourceType)).toIterable»
						this.«r.allContents.head.nameGeneric»_«rtp.name»_manager = new rdo_lib.«
							IF rtp.type.literal == 'temporary'»Temporary«ELSE»Permanent«ENDIF
								»ResourceManager<«rtp.fullyQualifiedName»>();
					«ENDFOR»
				«ENDFOR»
			}

			private «project»_database(«project»_database other)
			{
				«FOR r : rs.resources»
					«FOR rtp : r.allContents.filter(typeof(ResourceType)).toIterable»
						this.«r.allContents.head.nameGeneric»_«rtp.name»_manager = other.«r.allContents.head.nameGeneric»_«rtp.name»_manager.copy();
					«ENDFOR»
				«ENDFOR»
			}

			@Override
			public void deploy()
			{
				current = this;

				«FOR r : rs.resources»
					«FOR rtp : r.allContents.filter(typeof(ResourceType)).toIterable»
						«rtp.fullyQualifiedName».setCurrentManager(«r.allContents.head.nameGeneric»_«rtp.name»_manager);
					«ENDFOR»
				«ENDFOR»
			}

			@Override
			public «project»_database copy()
			{
				return new «project»_database(this);
			}

			@Override
			public boolean checkEqual(«project»_database other)
			{
				«FOR r : rs.resources»
					«FOR rtp : r.allContents.filter(typeof(ResourceType)).toIterable»
						if (!this.«r.allContents.head.nameGeneric»_«rtp.name»_manager.checkEqual(other.«
							r.allContents.head.nameGeneric»_«rtp.name»_manager))
							return false;
					«ENDFOR»
				«ENDFOR»

				return true;
			}
		}
		'''
	}

	def compileMain(ResourceSet rs, SimulationRun smr)
	{
		'''
		package rdo_model;

		public class MainClass
		{
			public static void main(String[] args)
			{
				rdo_lib.Tracer.startTrace();

				long startTime = System.currentTimeMillis();

				System.out.println(" === RDO-Simulator ===\n");
				System.out.println("   Project «RDONaming.getProjectName(rs.resources.get(0).URI)»");
				System.out.println("      Source files are «rs.resources.map[r | r.contents.head.nameGeneric].toString»\n");

				«RDONaming.getProjectName(rs.resources.get(0).URI)»_database.init();

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

				rdo_lib.Tracer.stopTrace();
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
			public static final «con.type.compileType» value = «IF con.type.compileType.endsWith("_enum")»«
				con.value.compileExpressionContext((new LocalContext).populateWithEnums(
					con.type.resolveAllSuchAs as RDOEnum)).value»«ELSE»«con.value.compileExpression.value»«ENDIF»;
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
				ret = ret + (if (flag) ", " else "") + seq.values.get(3*i).compileExpression.value
				flag = true
			}
			ret = ret + ", " + seq.values.get(seq.values.size - 2).compileExpression.value
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
			ret = ret + (if (flag) ", " else "") + seq.values.get(weightPos*(i + 1) - 1).compileExpression.value
			flag = true
		}

		return ret
	}

	def compileHistogramEnums(HistogramSequence seq)
	{
		var ret = ""
		var flag = false

		val context = (new LocalContext).populateWithEnums((seq.eContainer as Sequence).returntype.resolveAllSuchAs as RDOEnum)

		for (i : 0 ..< seq.values.size/2)
		{
			ret = ret + (if (flag) ", " else "") + seq.values.get(i*2).compileExpressionContext(context).value
			flag = true
		}

		return ret
	}

	def compileRegularSequence(RegularSequence seq, RDOType rtype, boolean legacy)
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
				return if(!legacy)
					'''
					public static «rtype.compileTypePrimitive» getNext(«rtype.compileTypePrimitive» mean)
					{
						return («rtype.compileTypePrimitive»)(-1.0 * mean * org.apache.commons.math3.util.FastMath.log(1 - prng.nextDouble()));
					}
					'''
				else
					'''
					public static «rtype.compileTypePrimitive» getNext(«rtype.compileTypePrimitive» mean)
					{
						return («rtype.compileTypePrimitive»)(-mean * Math.log(prng.nextDouble()));
					}
					'''
			case RegularSequenceType.NORMAL:
				return if(!legacy)
					'''
					public static «rtype.compileTypePrimitive» getNext(«rtype.compileTypePrimitive» mean, «rtype.compileTypePrimitive» deviation)
					{
						return («rtype.compileTypePrimitive»)(mean + deviation * org.apache.commons.math3.util.FastMath.sqrt(2) * org.apache.commons.math3.special.Erf.erfInv(2 * prng.nextDouble() - 1));
					}
					'''
				else
					'''
					public static «rtype.compileTypePrimitive» getNext(«rtype.compileTypePrimitive» mean, «rtype.compileTypePrimitive» deviation)
					{
						double ran = 0;
						for (int i = 0; i < 12; ++i)
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

					if (next < edge)
							return («rtype.compileTypePrimitive»)(a + «IF !legacy»org.apache.commons.math3.util.Fast«ENDIF»Math.sqrt((b - a) * (c - a) * next));
						else
							return («rtype.compileTypePrimitive»)(b - «IF !legacy»org.apache.commons.math3.util.Fast«ENDIF»Math.sqrt((1 - next) * (b - a) * (b - c)));
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

		public class «rtp.name» implements rdo_lib.«rtp.type.literal.withFirstUpper»Resource, rdo_lib.ResourceComparison<«rtp.name»>
		{
			private static rdo_lib.«rtp.type.literal.withFirstUpper
				»ResourceManager<«rtp.name»> managerCurrent;

			private String name;

			@Override
			public String getName()
			{
				return name;
			}

			«IF rtp.type.literal == "temporary"»
			private Integer number = null;

			@Override
			public Integer getNumber()
			{
				return number;
			}

			«ENDIF»
			public void register(String name)
			{
				this.name = name;
				managerCurrent.addResource(this);
			}

			«IF rtp.type.literal == "temporary"»
			public void register()
			{
				this.number = managerCurrent.getNextNumber();
				managerCurrent.addResource(this);
			}

			«ENDIF»
			public static «rtp.name» getResource(String name)
			{
				return managerCurrent.getResource(name);
			}

			public static java.util.Collection<«rtp.name»> getAll()
			{
				return managerCurrent.getAll();
			}

			«IF rtp.type.literal == "temporary"»
			public static java.util.Collection<«rtp.name»> getTemporary()
			{
				return managerCurrent.getTemporary();
			}

			public static void eraseResource(«rtp.name» res)
			{
				managerCurrent.eraseResource(res);
			}

			«ENDIF»
			private rdo_lib.«rtp.type.literal.withFirstUpper»ResourceManager<«rtp.name»> managerOwner = managerCurrent;

			public static void setCurrentManager(rdo_lib.«rtp.type.literal.withFirstUpper»ResourceManager<«rtp.name»> manager)
			{
				managerCurrent = manager;
			}

			«IF rtp.eAllContents.filter(typeof(RDOEnum)).toList.size > 0»// ENUMS«ENDIF»
			«FOR e : rtp.eAllContents.toIterable.filter(typeof(RDOEnum))»
				public enum «e.getEnumParentName(false)»_enum
				{
					«e.makeEnumBody»
				}

			«ENDFOR»
			«FOR parameter : rtp.parameters»
				private «parameter.type.compileType» «parameter.name»«parameter.type.getDefault»;

				public «parameter.type.compileType» get_«parameter.name»()
				{
					return «parameter.name»;
				}

				public «parameter.type.compileType» set_«parameter.name»(«parameter.type.compileType» «parameter.name»)
				{
					if (managerOwner == managerCurrent)
						this.«parameter.name» = «parameter.name»;
					else
						this.copyForNewOwner().«parameter.name» = «parameter.name»;

					return «parameter.name»;
				}

				public «parameter.type.compileType» set_«parameter.name»_after(«parameter.type.compileType» «parameter.name»)
				{
					«parameter.type.compileTypePrimitive» copy = this.«parameter.name»;

					set_«parameter.name»(«parameter.name»);

					return copy;
				}

			«ENDFOR»
			private «rtp.name» copyForNewOwner()
			{
				«rtp.name» copy = new «rtp.name»(«rtp.parameters.compileResourceTypeParametersCopyCall»);
				if (name != null)
				{
					copy.name = name;
					managerCurrent.addResource(copy);
					return copy;
				}
				«IF rtp.type.literal == "temporary"»
				if (number != null)
				{
					copy.number = number;
					managerCurrent.addResource(copy);
					return copy;
				}
				«ENDIF»
				return null;
			}

			public «rtp.name»(«rtp.parameters.compileResourceTypeParameters»)
			{
				«FOR parameter : rtp.parameters»
					if («parameter.name» != null)
						this.«parameter.name» = «parameter.name»;
				«ENDFOR»
			}

			@Override
			public boolean checkEqual(«rtp.name» other)
			{
				«FOR parameter : rtp.parameters»
					if (this.«parameter.name» != other.«parameter.name»)
						return false;
				«ENDFOR»

				return true;
			}
		}
		'''
	}

	def compileResourceTypeParametersCopyCall(List<ResourceTypeParameter> parameters)
	{
		'''«IF parameters.size > 0»«
			parameters.get(0).name»«
			FOR parameter : parameters.subList(1, parameters.size)», «
				parameter.name»«
			ENDFOR»«
		ENDIF»'''
	}

	def static makeEnumBody(RDOEnum e)
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
		val type = fun.type

		return
		'''
		package «filename»;

		public class «fun.name»
		'''
		 +
		switch type
		{
			FunctionAlgorithmic:
			{
				var context = (new LocalContext).populateFromFunction(type)

				'''
				{
					«IF type.parameters != null»«type.parameters.parameters.compileEnumsForFunction»«ENDIF»
					public static «fun.returntype.compileType» evaluate(«IF type.parameters != null
						»«type.parameters.parameters.compileFunctionTypeParameters»«ENDIF»)
					{
						if (true)
						{
							«type.algorithm.compileStatementContext(context)»
						}
						return «IF fun.^default != null»«
							fun.^default.compileExpressionContext(context).value»«ELSE»null«ENDIF»;
					}
				}
				'''
			}

			FunctionTable:
			'''
			{
				«IF type.parameters != null»«type.parameters.parameters.compileEnumsForFunction»«ENDIF»
				private static «fun.returntype.compileType»[] values =
				{
					«type.table.compileTable(
						if(fun.returntype.compileType.endsWith("_enum"))
							(new LocalContext).populateWithEnums(fun.returntype.resolveAllSuchAs as RDOEnum)
						else
							null,
						type.parameters.parameters.get(0).type.resolveAllSuchAs.tableLength
					)»
				};

				public static «fun.returntype.compileType» evaluate(«IF type.parameters != null
						»«type.parameters.parameters.compileFunctionTypeParameters»«ENDIF»)
				{
					return values[
						«type.parameters.parameters.compileTableReturn»
					];
				}
			}
			'''
			FunctionList:
			{
				var context = (new LocalContext).populateFromFunction(type)

				var paramscontext =
					if (type.parameters != null)
						type.parameters.parameters.map
							[ p |
								if (p.type.compileType.endsWith("_enum"))
									(new LocalContext).populateWithEnums(p.type.resolveAllSuchAs as RDOEnum)
								else
									null
							]
					else
						null

				'''
				{
					«IF type.parameters != null
						»«type.parameters.parameters.compileEnumsForFunction»«ENDIF»
					public static «fun.returntype.compileType» evaluate(«IF type.parameters != null
							»«type.parameters.parameters.compileFunctionTypeParameters»«ENDIF»)
					{
						«IF type.parameters != null»
						«FOR i : 0 ..< type.list.size»
						«IF type.list.get(i).parameters.size == type.parameters.parameters.size»
							if
							(
								«FOR j : 0 ..< type.list.get(i).parameters.size»
									«type.parameters.parameters.get(j).name» == «
										type.list.get(i).parameters.get(j).compileExpressionContext(
											paramscontext.get(j)).value»«
										IF j < type.list.get(i).parameters.size - 1» &&«ENDIF»
								«ENDFOR»
							)
								return «type.list.get(i).value.compileExpressionContext(context).value»;

						«ENDIF»
						«ENDFOR»
						«ENDIF»
						return «IF fun.^default != null»«
							fun.^default.compileExpressionContext(context).value»«ELSE»null«ENDIF»;
					}
				}
				'''
			}
		}
	}

	def static int getTableLength(RDOType type)
	{
		switch type
		{
			RDOInteger:
				if(type.range != null)
					return 
						if(type.range.hi instanceof IntConstant)
							(type.range.hi as IntConstant).value
						else
							((type.range.hi as DoubleConstant).value as int)
						-
						if(type.range.lo instanceof IntConstant)
							(type.range.lo as IntConstant).value
						else
							((type.range.lo as DoubleConstant).value as int)
				else
					return 0

			RDOEnum:
				return type.enums.size

			default:
				return 0
		}
	}

	def static compileTable(List<Expression> table, LocalContext context, int cut)
	{
		var values = ""
		var flag = false
		var i = 0

		for(e : table)
		{
			values = values + (if(flag) "," + if(i != cut) " " else "" else "") + (if(i == cut) "\n" else "") + (
				if(context != null)
					e.compileExpressionContext(context).value
				else
					e.compileExpression.value)
			i = i + 1
			if(i > cut)
				i = 1
			flag = true
		}
		
		return values 
	}

	def static compileTableReturn(List<FunctionParameter> parameters)
	{
		val list = newIntArrayOfSize(parameters.size)
		var multiplier = 1; 
		for(i : 0 ..< parameters.size)
		{
			list.set(i, multiplier)
			multiplier = multiplier * parameters.get(i).type.resolveAllSuchAs.tableLength
		}

		var compiled = ""
		var flag = false
		for(i : 0 ..< list.size)
		{
			compiled = compiled + (if(flag) " +\n" else "") + list.get(i).toString + " * " +
				parameters.get(i).name + (if(parameters.get(i).type.compileType.endsWith("_enum")) ".ordinal()" else "")
			flag = true
		}
		return compiled		
	}
	
	def static compileFunctionTypeParameters(List<FunctionParameter> parameters)
	{
		'''«IF parameters.size > 0»«parameters.get(0).type.compileType» «
			parameters.get(0).name»«
			FOR parameter : parameters.subList(1, parameters.size)», «
				parameter.type.compileType» «
				parameter.name»«
			ENDFOR»«
		ENDIF»'''
	}

	def static compileEnumsForFunction(List<FunctionParameter> parameters)
	{
		'''
		«FOR p : parameters.filter[c | c.type instanceof RDOEnum]»
		public static enum «p.name»_enum
		{
			«(p.type as RDOEnum).makeEnumBody»
		}

		«ENDFOR»
		'''
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
							«r.name» = «(r.type as ResourceDeclaration).reference.fullyQualifiedName».getResource("«(r.type as ResourceDeclaration).name»");
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
						staticResources.«r.name».register();
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
						public «(r.type as ResourceDeclaration).reference.fullyQualifiedName» «r.name»;
					«ENDIF»
				«ENDFOR»
				«FOR r : rule.relevantresources.filter[res | res.rule.literal == "Create"]»
					public «r.type.fullyQualifiedName» «r.name»;
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
					«FOR r : rule.relevantresources.filter[res | res.rule.literal != "Create"]»
					«IF r.type instanceof ResourceDeclaration»
						this.«r.name» = «(r.type as ResourceDeclaration).reference.fullyQualifiedName».getResource("«(r.type as ResourceDeclaration).name»");
					«ELSE»
						this.«r.name» = null;
					«ENDIF»
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

			private «rule.name»(RelevantResources resources, Parameters parameters)
			{
				this.instanceResources = resources;
				this.parameters = parameters;
			}

			«IF rule.combinational != null»
			static private rdo_lib.CombinationalChoiceFrom<RelevantResources, Parameters> choice =
				new rdo_lib.CombinationalChoiceFrom<RelevantResources, Parameters>
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
				«FOR rc : rule.algorithms.filter[r | r.relres.rule.literal != "Create"]»
					choice.addFinder
					(
						new rdo_lib.CombinationalChoiceFrom.Finder<RelevantResources, «rc.relres.type.relResFullyQualifiedName», Parameters>
						(
							new rdo_lib.CombinationalChoiceFrom.Retriever<«rc.relres.type.relResFullyQualifiedName»>()
							{
								@Override
								public java.util.Collection<«rc.relres.type.relResFullyQualifiedName»> getResources()
								{
									«IF rc.relres.type instanceof ResourceDeclaration»
									java.util.LinkedList<«rc.relres.type.relResFullyQualifiedName»> singlelist =
										new java.util.LinkedList<«rc.relres.type.relResFullyQualifiedName»>();
									singlelist.add(«rc.relres.type.relResFullyQualifiedName».getResource("«
										(rc.relres.type as ResourceDeclaration).name»"));
									return singlelist;
									«ELSE»
									return «rc.relres.type.relResFullyQualifiedName».get«
										IF rc.relres.rule.literal == "Erase"»Temporary«ELSE»All«ENDIF»();
									«ENDIF»
								}
							},
							new rdo_lib.SimpleChoiceFrom<RelevantResources, «rc.relres.type.relResFullyQualifiedName», Parameters>
							(
								«rc.choicefrom.compileChoiceFrom(rule, rc.relres.type.relResFullyQualifiedName, rc.relres.name, rc.relres.type.relResFullyQualifiedName)»,
								null
							),
							new rdo_lib.CombinationalChoiceFrom.Setter<RelevantResources, «rc.relres.type.relResFullyQualifiedName»>()
							{
								public void set(RelevantResources set, «rc.relres.type.relResFullyQualifiedName» resource)
								{
									set.«rc.relres.name» = resource;
								}
							}
						)
					);

				«ENDFOR»
				}

			«ELSE»
			«FOR rc : rule.algorithms.filter[r | r.relres.rule.literal != "Create"]»
			«IF rc.relres.type instanceof ResourceDeclaration»
			// just checker «rc.relres.name»
			private static rdo_lib.SimpleChoiceFrom.Checker<RelevantResources, «rc.relres.type.relResFullyQualifiedName», Parameters> «
				rc.relres.name»Checker = «rc.choicefrom.compileChoiceFrom(rule, rc.relres.type.relResFullyQualifiedName,
						rc.relres.name, rc.relres.type.relResFullyQualifiedName)»;

			«ELSE»
			// choice «rc.relres.name»
			private static rdo_lib.SimpleChoiceFrom<RelevantResources, «rc.relres.type.relResFullyQualifiedName», Parameters> «rc.relres.name»Choice =
				new rdo_lib.SimpleChoiceFrom<RelevantResources, «rc.relres.type.relResFullyQualifiedName», Parameters>
				(
					«rc.choicefrom.compileChoiceFrom(rule, rc.relres.type.relResFullyQualifiedName, rc.relres.name, rc.relres.type.relResFullyQualifiedName)»,
					«rc.choicemethod.compileChoiceMethod(rule.name, rc.relres.type.relResFullyQualifiedName)»
				);

			«ENDIF»
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

			public static boolean findResources(Parameters parameters)
			{
				staticResources.clear();

				«IF rule.combinational != null»
				if (!choice.find(parameters))
					return false;

				«ELSE»
				«FOR rc : rule.algorithms.filter[r | r.relres.rule.literal != "Create"]»
				«IF rc.relres.type instanceof ResourceDeclaration»
					if (!«rc.relres.name»Checker.check(staticResources, staticResources.«rc.relres.name», parameters))
						return false;

				«ELSE»
					staticResources.«rc.relres.name» = «rc.relres.name»Choice.find(staticResources, «rc.relres.type.fullyQualifiedName».get«
						IF rc.relres.rule.literal == "Erase"»Temporary«ELSE»All«ENDIF»(), parameters);

					if (staticResources.«rc.relres.name» == null)
						return false;

				«ENDIF»
				«ENDFOR»
				«ENDIF»
				return true;
			}

			public static void executeRule(Parameters parameters)
			{
				RelevantResources resources = staticResources.copy();

				«IF rule.relevantresources.filter[t | t.rule.literal == "Create"].size > 0»
					// create resources
					«FOR r : rule.relevantresources.filter[t |t.rule.literal == "Create"]»
						resources.«r.name» = new «(r.type as ResourceType).fullyQualifiedName»(«
							(r.type as ResourceType).parameters.size.compileAllDefault»);
					«ENDFOR»
					«FOR r : rule.relevantresources.filter[t |t.rule.literal == "Create"]»
						resources.«r.name».register();
					«ENDFOR»

				«ENDIF»
				«IF rule.relevantresources.filter[t | t.rule.literal == "Erase"].size > 0»
					// erase resources
					«FOR r : rule.relevantresources.filter[t |t.rule.literal == "Erase"]»
						«(r.type as ResourceType).fullyQualifiedName».eraseResource(resources.«r.name»);
					«ENDFOR»

				«ENDIF»
				rule.run(resources, parameters);
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
						public «(r.type as ResourceDeclaration).reference.fullyQualifiedName» «r.name»;
					«ENDIF»
				«ENDFOR»
				«FOR r : op.relevantresources.filter[res | res.begin.literal == "Create" || res.end.literal == "Create"]»
					public «r.type.fullyQualifiedName» «r.name»;
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
					«FOR r : op.relevantresources.filter[res | res.begin.literal != "Create" && res.end.literal != "Create"]»
					«IF r.type instanceof ResourceDeclaration»
						this.«r.name» = «(r.type as ResourceDeclaration).reference.fullyQualifiedName».getResource("«(r.type as ResourceDeclaration).name»");
					«ELSE»
						this.«r.name» = null;
					«ENDIF»
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
			static private rdo_lib.CombinationalChoiceFrom<RelevantResources, Parameters> choice =
				new rdo_lib.CombinationalChoiceFrom<RelevantResources, Parameters>
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
				«FOR rc : op.algorithms.filter[r | r.relres.begin.literal != "Create" && r.relres.end.literal != "Create"]»
					choice.addFinder
					(
						new rdo_lib.CombinationalChoiceFrom.Finder<RelevantResources, «rc.relres.type.fullyQualifiedName», Parameters>
						(
							new rdo_lib.CombinationalChoiceFrom.Retriever<«rc.relres.type.fullyQualifiedName»>()
							{
								@Override
								public java.util.Collection<«rc.relres.type.fullyQualifiedName»> getResources()
								{
									«IF rc.relres.type instanceof ResourceDeclaration»
									java.util.LinkedList<«rc.relres.type.relResFullyQualifiedName»> singlelist =
										new java.util.LinkedList<«rc.relres.type.relResFullyQualifiedName»>();
									singlelist.add(«rc.relres.type.relResFullyQualifiedName».getResource("«
										(rc.relres.type as ResourceDeclaration).name»"));
									return singlelist;
									«ELSE»
									return «rc.relres.type.relResFullyQualifiedName».get«
										IF rc.relres.begin.literal == "Erase" || rc.relres.end.literal == "Erase"»Temporary«ELSE»All«ENDIF»();
									«ENDIF»
								}
							},
							new rdo_lib.SimpleChoiceFrom<RelevantResources, «rc.relres.type.fullyQualifiedName», Parameters>
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
			«FOR rc : op.algorithms.filter[r | r.relres.begin.literal != "Create" && r.relres.end.literal != "Create"]»
			«IF rc.relres.type instanceof ResourceDeclaration»
			// just checker «rc.relres.name»
			private static rdo_lib.SimpleChoiceFrom.Checker<RelevantResources, «rc.relres.type.relResFullyQualifiedName», Parameters> «
				rc.relres.name»Checker = «rc.choicefrom.compileChoiceFrom(op, rc.relres.type.relResFullyQualifiedName,
						rc.relres.name, rc.relres.type.relResFullyQualifiedName)»;

			«ELSE»
			// choice «rc.relres.name»
			private static rdo_lib.SimpleChoiceFrom<RelevantResources, «rc.relres.type.relResFullyQualifiedName», Parameters> «rc.relres.name»Choice =
				new rdo_lib.SimpleChoiceFrom<RelevantResources, «rc.relres.type.relResFullyQualifiedName», Parameters>
				(
					«rc.choicefrom.compileChoiceFrom(op, rc.relres.type.relResFullyQualifiedName, rc.relres.name, rc.relres.type.relResFullyQualifiedName)»,
					«rc.choicemethod.compileChoiceMethod(op.name, rc.relres.type.relResFullyQualifiedName)»
				);

			«ENDIF»
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

			public static boolean findResources(Parameters parameters)
			{
				staticResources.clear();

				«IF op.combinational != null»
				if (!choice.find(parameters))
					return false;

				«ELSE»
				«FOR rc : op.algorithms.filter[r | r.relres.begin.literal != "Create" && r.relres.end.literal != "Create"]»
				«IF rc.relres.type instanceof ResourceDeclaration»
					if (!«rc.relres.name»Checker.check(staticResources, staticResources.«rc.relres.name», parameters))
						return false;

				«ELSE»
					staticResources.«rc.relres.name» = «rc.relres.name»Choice.find(staticResources, «rc.relres.type.fullyQualifiedName».get«
						IF rc.relres.begin.literal == "Erase" || rc.relres.end.literal == "Erase"»Temporary«ELSE»All«ENDIF»(), parameters);

					if (staticResources.«rc.relres.name» == null)
						return false;

				«ENDIF»
				«ENDFOR»
				«ENDIF»
				return true;
			}

			public static void executeRule(Parameters parameters)
			{
				RelevantResources resources = staticResources.copy();

				«IF op.relevantresources.filter[t | t.begin.literal == "Create"].size > 0»
					// create resources
					«FOR r : op.relevantresources.filter[t |t.begin.literal == "Create"]»
						resources.«r.name» = new «(r.type as ResourceType).fullyQualifiedName»(«
							(r.type as ResourceType).parameters.size.compileAllDefault»);
					«ENDFOR»
					«FOR r : op.relevantresources.filter[t |t.begin.literal == "Create"]»
						resources.«r.name».register();
					«ENDFOR»

				«ENDIF»
				«IF op.relevantresources.filter[t | t.begin.literal == "Erase" || t.end.literal == "Erase"].size > 0»
					// erase resources
					«FOR r : op.relevantresources.filter[t |t.begin.literal == "Erase" || t.end.literal == "Erase"]»
						«(r.type as ResourceType).fullyQualifiedName».eraseResource(resources.«r.name»);
					«ENDFOR»

				«ENDIF»
				begin.run(resources, parameters);

				rdo_lib.Simulator.pushEvent(new «op.name»(rdo_lib.Simulator.getTime() + «
					op.time.compileExpressionContext((new LocalContext).populateFromOperation(op)).value», resources, parameters));
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
						instanceResources.«r.name».register();
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
			new rdo_lib.SimpleChoiceFrom.Checker<RelevantResources, «resource», Parameters>()
			{
				@Override
				public boolean check(RelevantResources resources, «resource» «relres», Parameters parameters)
				{
					return «IF havecf»(«IF pattern instanceof Operation»«
						cf.logic.compileExpressionContext((new LocalContext).populateFromOperation(pattern as Operation)).value.
							replaceAll("resources." + relres + ".", relres + ".")»«	ELSE»«
								cf.logic.compileExpressionContext((new LocalContext).populateFromRule(pattern as Rule)).value.
									replaceAll("resources." + relres + ".", relres + ".")»«ENDIF»)«ELSE»true«ENDIF»«
										FOR i : 0 ..< relreslist.size»«IF relres != relreslist.get(i) && relrestype ==
											relrestypes.get(i)»«"\t"»&& «relres» != resources.«relreslist.get(i)»«ENDIF»«ENDFOR»;
				}
			}'''
	}

	def compileChoiceMethod(PatternChoiceMethod cm, String pattern, String resource)
	{
		if (cm == null || cm.first)
			return "null"
		else
		{
			var EObject pat;
			var String relres = "@NOPE";
			if(cm.eContainer instanceof Rule || cm.eContainer instanceof Operation)
				pat = cm.eContainer
			else
			{
				pat = cm.eContainer.eContainer
				relres = (if(cm.eContainer instanceof OperationConvert) (cm.eContainer as OperationConvert).relres.name
					else (cm.eContainer as RuleConvert).relres.name)
			}

			val context = (if(pat instanceof Rule) (new LocalContext).populateFromRule(pat as Rule)
				else (new LocalContext).populateFromOperation(pat as Operation))

			val expr = cm.expression.compileExpressionContext(context).value
			return
				'''
				new rdo_lib.SimpleChoiceFrom.ChoiceMethod<RelevantResources, «resource», Parameters>()
				{
					@Override
					public int compare(«resource» x, «resource» y)
					{
						if («expr.replaceAll("resources." + relres + ".", "x.")» «IF cm.withtype.literal
							== "with_min"»<«ELSE»>«ENDIF» «expr.replaceAll("resources." + relres + ".", "y.")»)
							return -1;
						else
							return 1;
					}
				}'''
		}
	}

	def static String getDefault(RDORTPParameterType parameter)
	{
		switch parameter
		{
			RDORTPParameterBasic:
				return if(parameter.^default != null) " = " + RDOExpressionCompiler.compileExpression(parameter.^default).value else ""

			RDORTPParameterEnum:
				return if(parameter.^default != null) " = " + parameter.type.compileType + "." + parameter.^default.name else ""

			RDORTPParameterSuchAs:
				if(parameter.type.compileType.endsWith("_enum"))
					return if(parameter.^default != null) " = " + parameter.^default.compileExpressionContext((new LocalContext).
						populateWithEnums(parameter.type.resolveAllSuchAs as RDOEnum)).value else ""
				else
					return if(parameter.^default != null) " = " + parameter.^default.compileExpression.value else ""

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
				«FOR i : 0 ..< activities.size»
				«IF activities.get(i).parameters.size == parameters.get(i).size»
				private static «activities.get(i).pattern.fullyQualifiedName».Parameters «activities.get(i).name» =
					new «activities.get(i).pattern.fullyQualifiedName».Parameters(«activities.get(i).compileExpression.value»);
				«ENDIF»
				«ENDFOR»

				private static rdo_lib.DecisionPoint dpt =
					new rdo_lib.DecisionPoint
					(
						"«dpt.fullyQualifiedName»",
						«IF dpt.parent != null»«dpt.parent.fullyQualifiedName».getDPT()«ELSE»null«ENDIF»,
						«IF dpt instanceof DecisionPointPrior»«dpt.priority.compileExpression.value»«ELSE»null«ENDIF»,
						«IF dpt.condition != null
						»new rdo_lib.DecisionPoint.Condition()
						{
							@Override
							public boolean check()
							{
								return «dpt.condition.compileExpression.value»;
							}
						}«ELSE»null«ENDIF»
					);

				public static void init()
				{
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
									return «a.pattern.fullyQualifiedName».findResources(«a.name»);
								}

								@Override
								public void executeActivity()
								{
									«a.pattern.fullyQualifiedName».executeRule(«a.name»);
								}
							}
						);
					«ENDFOR»

					rdo_lib.Simulator.addDecisionPoint(dpt);
				}

				public rdo_lib.DecisionPoint getDPT()
				{
					return dpt;
				}
			}
			'''
	}

	def compileDecisionPointSearch(DecisionPointSearch dpt, String filename)
	{
		val activities = dpt.activities
		val parameters = activities.map[a | a.pattern.parameters]

		return
		'''
			package «filename»;

			public class «dpt.name»
			{
				«FOR i : 0 ..< activities.size»
				«IF (activities.get(i).parameters != null && activities.get(i).parameters.size == parameters.get(i).size) ||
					(activities.get(i).parameters == null && activities.get(i).pattern.parameters == null)»
				private static «activities.get(i).pattern.fullyQualifiedName».Parameters «activities.get(i).name» =
					new «activities.get(i).pattern.fullyQualifiedName».Parameters(«activities.get(i).compileExpression.value»);
				«ENDIF»
				«ENDFOR»

				private static rdo_lib.DecisionPointSearch<rdo_model.«dpt.eResource.URI.projectName»_database> dpt =
					new rdo_lib.DecisionPointSearch<rdo_model.«dpt.eResource.URI.projectName»_database>
					(
						"«dpt.fullyQualifiedName»",
						«IF dpt.condition != null
						»new rdo_lib.DecisionPoint.Condition()
						{
							@Override
							public boolean check()
							{
								return «dpt.condition.compileExpression.value»;
							}
						}«ELSE»null«ENDIF»,
						new rdo_lib.DecisionPoint.Condition()
						{
							@Override
							public boolean check()
							{
								return «dpt.termination.compileExpression.value»;
							}
						},
						new rdo_lib.DecisionPointSearch.EvaluateBy()
						{
							@Override
							public double get()
							{
								return «dpt.evaluateby.compileExpression.value»;
							}
						},
						«IF dpt.comparetops»true«ELSE»false«ENDIF»,
						new rdo_lib.DecisionPointSearch.DatabaseRetriever<rdo_model.«dpt.eResource.URI.projectName»_database>()
						{
							@Override
							public rdo_model.«dpt.eResource.URI.projectName»_database get()
							{
								return rdo_model.«dpt.eResource.URI.projectName»_database.getCurrent();
							}
						}
					);

				public static void init()
				{
					«FOR a : activities»
						dpt.addActivity(
							new rdo_lib.DecisionPointSearch.Activity(«IF a.valueafter != null
								»rdo_lib.DecisionPointSearch.Activity.ApplyMoment.after«
								ELSE»rdo_lib.DecisionPointSearch.Activity.ApplyMoment.before«ENDIF»)
							{
								@Override
								public String getName()
								{
									return "«filename».«a.name»";
								}

								@Override
								public boolean checkActivity()
								{
									return «a.pattern.fullyQualifiedName».findResources(«a.name»);
								}

								@Override
								public void executeActivity()
								{
									«a.pattern.fullyQualifiedName».executeRule(«a.name»);
								}

								@Override
								public double calculateValue()
								{
									return «IF a.valueafter != null»«a.valueafter.compileExpression.value
										»«ELSE»«a.valuebefore.compileExpression.value»«ENDIF»;
								}
							}
						);
					«ENDFOR»

					rdo_lib.Simulator.addDecisionPoint(dpt);
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
						(«type.expression.compileExpression.value»));
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

		import java.util.Iterator;

		import java.util.ArrayList;

		import java.util.HashMap;

		public class PermanentResourceManager<T extends PermanentResource & ResourceComparison<T>>
		{
			protected HashMap<String, T> resources;
			protected ArrayList<T> listResources;

			public void addResource(T res)
			{
				if(resources.get(res.getName()) != null)
					listResources.set(listResources.indexOf(resources.get(res.getName())), res);
				else
					listResources.add(res);

				resources.put(res.getName(), res);
			}

			public T getResource(String name)
			{
				return resources.get(name);
			}

			public java.util.Collection<T> getAll()
			{
				return listResources;
			}

			public PermanentResourceManager()
			{
				this.listResources = new ArrayList<T>();
				this.resources = new HashMap<String, T>();
			}

			private PermanentResourceManager(PermanentResourceManager<T> source)
			{
				this.listResources = new ArrayList<T>(source.listResources);
				this.resources = new HashMap<String, T>(source.resources);
			}

			public PermanentResourceManager<T> copy()
			{
				return new PermanentResourceManager<T>(this);
			}

			public boolean checkEqual(PermanentResourceManager<T> other)
			{
				if (resources.values().size() != other.resources.values().size())
					System.out.println("Runtime error: resource set in manager was altered");

				Iterator<T> itThis = resources.values().iterator();
				Iterator<T> itOther = other.resources.values().iterator();

				for (int i = 0; i < resources.values().size(); i++)
				{
					T resThis = itThis.next();
					T resOther = itOther.next();

					if (resThis != resOther && !resThis.checkEqual(resOther))
						return false;
				}
				return true;
			}
		}
		'''
	}

	def compileTemporaryManager()
	{
		'''
		package rdo_lib;

		import java.util.Collection;

		import java.util.ArrayList;
		import java.util.LinkedList;

		import java.util.HashMap;

		public class TemporaryResourceManager<T extends TemporaryResource & ResourceComparison<T>> extends PermanentResourceManager<T>
		{
			private ArrayList<T> temporary;

			private LinkedList<Integer> vacantList;
			private Integer currentLast;

			public int getNextNumber()
			{
				if (vacantList.size() > 0)
					return vacantList.poll();
				else
					return currentLast++;
			}

			@Override
			public void addResource(T res)
			{
				if (res.getName() != null)
					super.addResource(res);

				if (res.getNumber() != null)
					if (res.getNumber() == temporary.size())
						temporary.add(res);
					else
						temporary.set(res.getNumber(), res);
			}

			public void eraseResource(T res)
			{
				vacantList.add(res.getNumber());
				temporary.set(res.getNumber(), null);
			}

			@Override
			public Collection<T> getAll()
			{
				Collection<T> all = new LinkedList<T>(resources.values());
				all.addAll(temporary);
				return all;
			}

			public Collection<T> getTemporary()
			{
				return temporary;
			}

			public TemporaryResourceManager()
			{
				this.resources = new HashMap<String, T>();
				this.temporary = new ArrayList<T>();
				this.vacantList = new LinkedList<Integer>();
				this.currentLast = 0;
			}

			private TemporaryResourceManager(TemporaryResourceManager<T> source)
			{
				this.resources = new HashMap<String, T>(source.resources);
				this.temporary = new ArrayList<T>(source.temporary);
				this.vacantList = source.vacantList;
				this.currentLast = source.currentLast;
			}

			@Override
			public TemporaryResourceManager<T> copy()
			{
				return new TemporaryResourceManager<T>(this);
			}

			public boolean checkEqual(TemporaryResourceManager<T> other)
			{
				if (!super.checkEqual(this))
					return false;

				if (temporary.size() != other.temporary.size())
					System.out.println("Runtime error: temporary resource set in manager was altered");

				for (int i = 0; i < temporary.size(); i++)
				{
					T resThis = temporary.get(i);
					T resOther = other.temporary.get(i);

					if (resThis != resOther && !resThis.checkEqual(resOther))
						return false;
				}
				return true;
			}
		}
		'''
	}

	def compileDatabase()
	{
		'''
		package rdo_lib;

		public interface Database<T>
		{
			public void deploy();
			public T copy();
			public boolean checkEqual(T other);
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
					if (dptIterator.next().check())
						return true;

				return false;
			}
		}
		'''
	}

	def compileSelect()
	{
		'''
		package rdo_lib;

		import java.util.Collection;

		public class Select
		{
			public static interface Checker<T>
			{
				public boolean check(T res);
			}

			public static <T> boolean Exist(Collection<T> resources, Checker<T> checker)
			{
				for (T res : resources)
					if (checker.check(res))
						return true;
				return false;
			}

			public static <T> boolean Not_Exist(Collection<T> resources, Checker<T> checker)
			{
				for (T res : resources)
					if (checker.check(res))
						return false;
				return true;
			}

			public static <T> boolean For_All(Collection<T> resources, Checker<T> checker)
			{
				for (T res : resources)
					if (!checker.check(res))
						return false;
				return true;
			}

			public static <T> boolean Not_For_All(Collection<T> resources, Checker<T> checker)
			{
				for (T res : resources)
					if (!checker.check(res))
						return true;
				return false;
			}

			public static <T> boolean Empty(Collection<T> resources, Checker<T> checker)
			{
				return Not_Exist(resources, checker);
			}

			public static <T> int Size(Collection<T> resources, Checker<T> checker)
			{
				int count = 0;
				for (T res : resources)
					if (checker.check(res))
						count++;
				return count;
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

	def compileRDORangedInteger()
	{
		'''
		package rdo_lib;

		public class RDORangedInteger
		{
			private int lo;
			private int hi;

			public RDORangedInteger(int lo, int hi)
			{
				this.lo = lo;
				this.hi = hi;
			}

			private int value;

			public void set(int value) throws Exception
			{
				if(value > hi || value < lo)
					throw new Exception("Out of bounds");

				this.value = value;
			}

			public int get()
			{
				return value;
			}
		}
		'''
	}

	def compileRDORangedDouble()
	{
		'''
		package rdo_lib;

		public class RDORangedDouble
		{
			private double lo;
			private double hi;

			public RDORangedDouble(double lo, double hi)
			{
				this.lo = lo;
				this.hi = hi;
			}

			private double value;

			public void set(double value) throws Exception
			{
				if(value > hi || value < lo)
					throw new Exception("Out of bounds");

				this.value = value;
			}

			public double get()
			{
				return value;
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

		public class SimpleChoiceFrom<P, T, PT>
		{
			public static interface Checker<P, T, PT>
			{
				public boolean check(P resources, T res, PT parameters);
			}

			public static abstract class ChoiceMethod<P, T, PT> implements Comparator<T>
			{
				protected P resources;
				protected PT parameters;

				private void setPattern(P resources, PT parameters)
				{
					this.resources = resources;
					this.parameters = parameters;
				}
			}

			private Checker<P, T, PT> checker;
			private ChoiceMethod<P, T, PT> comparator;

			public SimpleChoiceFrom(Checker<P, T, PT> checker, ChoiceMethod<P, T, PT> comparator)
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

			public Collection<T> findAll(P resources, Collection<T> reslist, PT parameters)
			{
				matchingList.clear();
				if (comparator != null)
					comparator.setPattern(resources, parameters);

				T res;
				for (Iterator<T> iterator = reslist.iterator(); iterator.hasNext();)
				{
					res = iterator.next();
					if (checker.check(resources, res, parameters))
						matchingList.add(res);
				}

				return matchingList;
			}

			public T find(P resources, Collection<T> reslist, PT parameters)
			{
				matchingList.clear();
				if (comparator != null)
					comparator.setPattern(resources, parameters);

				T res;
				for (Iterator<T> iterator = reslist.iterator(); iterator.hasNext();)
				{
					res = iterator.next();
					if (res != null && checker.check(resources, res, parameters))
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

		public class CombinationalChoiceFrom<R, PT>
		{
			private R set;
			private RelevantResourcesManager<R> setManager;
			private PriorityQueue<R> matchingList;

			public CombinationalChoiceFrom(R set, ChoiceMethod<R, R, PT> comparator, RelevantResourcesManager<R> setManager)
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

			public static class Finder<R, T, PT>
			{
				private Retriever<T> retriever;
				private SimpleChoiceFrom<R, T, PT> choice;
				private Setter<R, T> setter;

				public Finder(Retriever<T> retriever, SimpleChoiceFrom<R, T, PT> choice, Setter<R, T> setter)
				{
					this.retriever = retriever;
					this.choice  = choice;
					this.setter  = setter;
				}

				public boolean find(R set, Iterator<Finder<R, ?, PT>> finder, PriorityQueue<R> matchingList, RelevantResourcesManager<R> setManager, PT parameters)
				{
					Collection<T> all = choice.findAll(set, retriever.getResources(), parameters);
					Iterator<T> iterator = all.iterator();
					if (finder.hasNext())
					{
						Finder<R, ?, PT> currentFinder = finder.next();
						while (iterator.hasNext())
						{
							setter.set(set, iterator.next());
							if (currentFinder.find(set, finder, matchingList, setManager, parameters))
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

			private List<Finder<R, ?, PT>> finders = new ArrayList<Finder<R, ?, PT>>();

			public void addFinder(Finder<R, ?, PT> finder)
			{
				finders.add(finder);
			}

			public boolean find(PT parameters)
			{
				matchingList.clear();

				if (finders.size() == 0)
					return false;

				Iterator<Finder<R, ?, PT>> finder = finders.iterator();

				if (finder.next().find(set, finder, matchingList, setManager, parameters)
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

	def compileTracer()
	{
		'''
		package rdo_lib;

		import java.io.FileWriter;
		import java.io.BufferedWriter;
		import java.io.IOException;

		public class Tracer
		{
			private static BufferedWriter trc;

			public static void startTrace()
			{
				try
				{
					trc = new BufferedWriter(new FileWriter("log.txt"));
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}

			public static void append(String entry)
			{
				try
				{
					trc.write(entry + "\n");
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}

			public static void stopTrace()
			{
				try
				{
					trc.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
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
					if (x.getTime() < y.getTime())
						return -1;
					if (x.getTime() > y.getTime())
						return 1;
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

	def compilePermanentRes()
	{
		'''
		package rdo_lib;

		public interface PermanentResource
		{
			public String getName();
		}
		'''
	}

	def compileTemporaryRes()
	{
		'''
		package rdo_lib;

		public interface TemporaryResource extends PermanentResource
		{
			public Integer getNumber();
		}
		'''
	}

	def compileResComparison()
	{
		'''
		package rdo_lib;

		public interface ResourceComparison<T>
		{
			public boolean checkEqual(T other);
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

			protected Condition condition;

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

			public static abstract class Activity
			{
				public abstract String getName();
				public abstract boolean checkActivity();
				public abstract void executeActivity();
			}

			private List<Activity> activities = new LinkedList<Activity>();

			public void addActivity(Activity a)
			{
				activities.add(a);
			}

			public boolean check()
			{
				if (condition != null && !condition.check())
					return false;

				return checkActivities();
			}

			private boolean checkActivities()
			{
				for (Activity a : activities)
					if (a.checkActivity())
					{
						a.executeActivity();
						return true;
					}

				return false;
			}
		}
		'''
	}

	def compileDPTSearch()
	{
		'''
		package rdo_lib;

		import java.util.Comparator;

		import java.util.List;
		import java.util.LinkedList;
		import java.util.PriorityQueue;

		public class DecisionPointSearch<T extends Database<T>> extends DecisionPoint
		{
			private DecisionPoint.Condition terminate;

			private DatabaseRetriever<T> retriever;

			private boolean compareTops;

			private EvaluateBy evaluateBy;

			public DecisionPointSearch
			(
				String name,
				Condition condition,
				Condition terminate,
				EvaluateBy evaluateBy,
				boolean compareTops,
				DatabaseRetriever<T> retriever
			)
			{
				super(name, null, null, condition);
				this.terminate = terminate;
				this.evaluateBy = evaluateBy;
				this.retriever = retriever;
				this.compareTops = compareTops;
			}

			public static interface EvaluateBy
			{
				public double get();
			}

			public static abstract class Activity extends DecisionPoint.Activity
			{
				public enum ApplyMoment { before, after }

				public Activity(ApplyMoment applyMoment)
				{
					this.applyMoment = applyMoment;
				}

				public abstract double calculateValue();

				public final ApplyMoment applyMoment;
			}

			private List<Activity> activities = new LinkedList<Activity>();

			public void addActivity(Activity a)
			{
				activities.add(a);
			}

			public static interface DatabaseRetriever<T extends Database<T>>
			{
				public T get();
			}

			private class GraphNode
			{
				public GraphNode parent = null;

				public LinkedList<GraphNode> children;

				public double g;
				public double h;

				public T state;
			}

			private Comparator<GraphNode> nodeComparator = new Comparator<GraphNode>()
			{
				@Override
				public int compare(GraphNode x, GraphNode y)
				{
					if (x.g + x.h < y.g + y.h)
						return -1;
					if (x.g + x.h > y.g + y.h)
						return 1;
					return 0;
				}
			};

			private PriorityQueue<GraphNode> nodesOpen = new PriorityQueue<GraphNode>(1, nodeComparator);
			private LinkedList<GraphNode> nodesClosed = new LinkedList<GraphNode>();

			@Override
			public boolean check()
			{
				if (terminate.check())
					return false;

				nodesOpen.clear();
				nodesClosed.clear();

				if (condition != null && !condition.check())
					return false;

				GraphNode head = new GraphNode();
				head.state = retriever.get();
				nodesOpen.add(head);

				while (nodesOpen.size() > 0)
				{
					GraphNode current = nodesOpen.poll();
					nodesClosed.add(current);
					current.state.deploy();

					if (terminate.check())
						return true;

					current.children = spawnChildren(current);
					nodesOpen.addAll(current.children);
				}
				head.state.deploy();
				return false;
			}

			private LinkedList<GraphNode> spawnChildren(GraphNode parent)
			{
				LinkedList<GraphNode> children = new LinkedList<GraphNode>();

				for (Activity a : activities)
				{
					double value = 0;

					if (a.checkActivity())
					{
						GraphNode newChild = new GraphNode();
						newChild.parent = parent;

						if(a.applyMoment == Activity.ApplyMoment.before)
							value = a.calculateValue();

						newChild.state = parent.state.copy();
						newChild.state.deploy();
						a.executeActivity();

						if(a.applyMoment == Activity.ApplyMoment.after)
							value = a.calculateValue();

						newChild.g = parent.g + value;
						newChild.h = evaluateBy.get();

						add_child:
						{
							compare_tops:
							if (compareTops)
							{
								for (GraphNode open : nodesOpen)
									if (newChild.state.checkEqual(open.state))
										if(newChild.g < open.g)
										{
											nodesOpen.remove(open);
											break compare_tops;
										}
										else
											break add_child;

								for (GraphNode closed : nodesClosed)
									if (newChild.state.checkEqual(closed.state))
										if(newChild.g < closed.g)
										{
											nodesClosed.remove(closed);
											break compare_tops;
										}
										else
											break add_child;
							}
							children.add(newChild);
						}
						parent.state.deploy();
					}
				}
				return children;
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

