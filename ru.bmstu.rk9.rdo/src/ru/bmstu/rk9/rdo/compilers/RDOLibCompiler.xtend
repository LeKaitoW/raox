package ru.bmstu.rk9.rdo.compilers

import org.eclipse.xtext.generator.IFileSystemAccess

class RDOLibCompiler
{
	def public static void generateAll(IFileSystemAccess fsa)
	{
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
	}

	def private static compilePermanentManager()
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

	def private static compileTemporaryManager()
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

	def private static compileDatabase()
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

	def private static compileDPTManager()
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

	def private static compileSelect()
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

	def private static compileRDOLegacyRandom()
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

	def private static compileRDORangedInteger()
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

	def private static compileRDORangedDouble()
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

	def private static compileHistogram()
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

	def private static compileSimpleChoiceFrom()
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

	def private static compileCommonChoiceFrom()
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

	def private static compileLibSimulator()
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

	def private static compileTracer()
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

	def private static compileEventScheduler()
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

	def private static compilePermanentRes()
	{
		'''
		package rdo_lib;

		public interface PermanentResource
		{
			public String getName();
		}
		'''
	}

	def private static compileTemporaryRes()
	{
		'''
		package rdo_lib;

		public interface TemporaryResource extends PermanentResource
		{
			public Integer getNumber();
		}
		'''
	}

	def private static compileResComparison()
	{
		'''
		package rdo_lib;

		public interface ResourceComparison<T>
		{
			public boolean checkEqual(T other);
		}
		'''
	}

	def private static compileTerminate()
	{
		'''
		package rdo_lib;

		public interface TerminateCondition
		{
			public boolean check();
		}
		'''
	}

	def private static compileConverter()
	{
		'''
		package rdo_lib;

		public interface Converter<R, P>
		{
			public void run(R resources, P parameters);
		}
		'''
	}

	def private static compileEvent()
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

	def private static compileDecisionPoint()
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

	def private static compileDPTSearch()
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

	def private static compileResultManager()
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

	def private static compileResult()
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