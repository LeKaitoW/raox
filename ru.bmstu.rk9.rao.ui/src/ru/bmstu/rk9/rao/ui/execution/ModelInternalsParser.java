package ru.bmstu.rk9.rao.ui.execution;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;

import ru.bmstu.rk9.rao.lib.animation.AnimationFrame;
import ru.bmstu.rk9.rao.lib.database.Database;
import ru.bmstu.rk9.rao.lib.database.Database.DataType;
import ru.bmstu.rk9.rao.lib.dpt.AbstractDecisionPoint;
import ru.bmstu.rk9.rao.lib.dpt.Activity;
import ru.bmstu.rk9.rao.lib.dpt.Edge;
import ru.bmstu.rk9.rao.lib.dpt.Logic;
import ru.bmstu.rk9.rao.lib.dpt.Search;
import ru.bmstu.rk9.rao.lib.event.Event;
import ru.bmstu.rk9.rao.lib.json.JSONArray;
import ru.bmstu.rk9.rao.lib.json.JSONObject;
import ru.bmstu.rk9.rao.lib.modeldata.ModelStructureConstants;
import ru.bmstu.rk9.rao.lib.naming.NamingHelper;
import ru.bmstu.rk9.rao.lib.naming.RaoNameable;
import ru.bmstu.rk9.rao.lib.pattern.Operation;
import ru.bmstu.rk9.rao.lib.pattern.Pattern;
import ru.bmstu.rk9.rao.lib.resource.ComparableResource;
import ru.bmstu.rk9.rao.lib.resource.Resource;
import ru.bmstu.rk9.rao.lib.result.Result;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorInitializationInfo;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorPreinitializationInfo;

public class ModelInternalsParser {
	private final SimulatorPreinitializationInfo simulatorPreinitializationInfo = new SimulatorPreinitializationInfo();
	private final SimulatorInitializationInfo simulatorInitializationInfo = new SimulatorInitializationInfo();
	private final List<Class<?>> decisionPointClasses = new ArrayList<>();
	private final List<Class<?>> animationClasses = new ArrayList<>();
	private final List<Field> nameableFields = new ArrayList<>();
	private final List<AnimationFrame> animationFrames = new ArrayList<>();
	private final List<Field> resultFields = new ArrayList<>();
	private URLClassLoader classLoader;
	private final IProject project;

	public final SimulatorPreinitializationInfo getSimulatorPreinitializationInfo() {
		return simulatorPreinitializationInfo;
	}

	public final SimulatorInitializationInfo getSimulatorInitializationInfo() {
		return simulatorInitializationInfo;
	}

	public ModelInternalsParser(IProject project) {
		this.project = project;
	}

	public final void parse()
			throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, ClassNotFoundException, MalformedURLException {
		URL modelURL = new URL("file:///" + ResourcesPlugin.getWorkspace().getRoot().getLocation().toString() + "/"
				+ project.getName() + "/bin/");

		URL[] urls = new URL[] { modelURL };

		classLoader = new URLClassLoader(urls, CurrentSimulator.class.getClassLoader());

		simulatorPreinitializationInfo.modelStructure.put(ModelStructureConstants.NAME, project.getName());

		for (IResource raoFile : BuildUtil.getAllRaoFilesInProject(project)) {
			String raoFileName = raoFile.getName();
			raoFileName = raoFileName.substring(0, raoFileName.length() - ".rao".length());
			String modelClassName = project.getName() + "." + raoFileName;
			parseModel(modelClassName);
		}
	}

	@SuppressWarnings("unchecked")
	public final void parseModel(String modelClassName)
			throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		Class<?> modelClass = Class.forName(modelClassName, false, classLoader);

		try {
			Class<?> init = Class.forName(modelClassName + "$init", false, classLoader);
			Constructor<?> initConstructor = init.getDeclaredConstructor();
			initConstructor.setAccessible(true);
			simulatorInitializationInfo.initList.add((Runnable) initConstructor.newInstance());
		} catch (ClassNotFoundException classException) {
		}

		try {
			Class<?> terminate = Class.forName(modelClassName + "$terminateCondition", false, classLoader);
			Constructor<?> terminateConstructor = terminate.getDeclaredConstructor();
			terminateConstructor.setAccessible(true);
			simulatorInitializationInfo.terminateConditions.add((Supplier<Boolean>) terminateConstructor.newInstance());
		} catch (ClassNotFoundException classException) {
		}

		try {
			Class<?> resourcePreinitializer = Class.forName(modelClassName + "$resourcesPreinitializer", false,
					classLoader);
			Constructor<?> resourcePreinitializerConstructor = resourcePreinitializer.getDeclaredConstructor();
			resourcePreinitializerConstructor.setAccessible(true);
			simulatorPreinitializationInfo.resourcePreinitializers
					.add((Runnable) resourcePreinitializerConstructor.newInstance());
		} catch (ClassNotFoundException classException) {
		}

		for (Class<?> nestedModelClass : modelClass.getDeclaredClasses()) {
			String className = NamingHelper.changeDollarToDot(nestedModelClass.getName());

			if (Event.class.isAssignableFrom(nestedModelClass)) {
				simulatorPreinitializationInfo.modelStructure.getJSONArray(ModelStructureConstants.EVENTS)
						.put(new JSONObject().put(ModelStructureConstants.NAME, className));
				continue;
			}

			if (ComparableResource.class.isAssignableFrom(nestedModelClass)) {
				simulatorPreinitializationInfo.resourceClasses.add(nestedModelClass);
				JSONArray parameters = new JSONArray();
				int offset = 0;
				int variableWidthParameterIndex = 0;
				for (Field field : nestedModelClass.getDeclaredFields()) {
					DataType dataType = Database.getDataType(field.getType());

					parameters.put(new JSONObject().put(ModelStructureConstants.NAME, field.getName().substring(1))
							.put(ModelStructureConstants.TYPE, dataType).put(ModelStructureConstants.OFFSET, offset)
							.put(ModelStructureConstants.VARIABLE_WIDTH_PARAMETER_INDEX,
									dataType == DataType.OTHER ? variableWidthParameterIndex : -1));

					if (dataType == DataType.OTHER)
						variableWidthParameterIndex++;
					else
						offset += dataType.getSize();
				}

				simulatorPreinitializationInfo.modelStructure.getJSONArray(ModelStructureConstants.RESOURCE_TYPES)
						.put(new JSONObject().put(ModelStructureConstants.NAME, className)
								.put(ModelStructureConstants.NAMED_RESOURCES, new JSONArray())
								.put(ModelStructureConstants.PARAMETERS, parameters)
								.put(ModelStructureConstants.FINAL_OFFSET, offset));
				continue;
			}

			if (Pattern.class.isAssignableFrom(nestedModelClass)) {
				JSONArray relevantResources = new JSONArray();
				for (Field field : nestedModelClass.getDeclaredFields()) {
					String fieldName = NamingHelper.createFullNameForField(field);
					if (Resource.class.isAssignableFrom(field.getType())) {
						relevantResources.put(new JSONObject().put(ModelStructureConstants.NAME, fieldName).put(
								ModelStructureConstants.TYPE,
								NamingHelper.changeDollarToDot(field.getType().getName())));
					}
				}

				String type = Operation.class.isAssignableFrom(nestedModelClass) ? ModelStructureConstants.OPERATION
						: ModelStructureConstants.RULE;

				simulatorPreinitializationInfo.modelStructure.getJSONArray(ModelStructureConstants.PATTERNS)
						.put(new JSONObject().put(ModelStructureConstants.NAME, className)
								.put(ModelStructureConstants.TYPE, type)
								.put(ModelStructureConstants.RELEVANT_RESOURCES, relevantResources));
				continue;
			}

			if (Logic.class.isAssignableFrom(nestedModelClass)) {
				decisionPointClasses.add(nestedModelClass);

				JSONArray activities = new JSONArray();
				for (Field field : nestedModelClass.getDeclaredFields()) {
					String fieldName = field.getName();
					if (Activity.class.isAssignableFrom(field.getType())) {
						activities.put(new JSONObject().put(ModelStructureConstants.NAME, fieldName));
					}
				}

				simulatorPreinitializationInfo.modelStructure.getJSONArray(ModelStructureConstants.LOGICS)
						.put(new JSONObject().put(ModelStructureConstants.NAME, className)
								.put(ModelStructureConstants.ACTIVITIES, activities));
				continue;
			}

			if (Search.class.isAssignableFrom(nestedModelClass)) {
				decisionPointClasses.add(nestedModelClass);

				JSONArray edges = new JSONArray();
				for (Field field : nestedModelClass.getDeclaredFields()) {
					String fieldName = field.getName();
					if (Edge.class.isAssignableFrom(field.getType())) {
						edges.put(new JSONObject().put(ModelStructureConstants.NAME, fieldName));
					}
				}

				simulatorPreinitializationInfo.modelStructure.getJSONArray(ModelStructureConstants.SEARCHES)
						.put(new JSONObject().put(ModelStructureConstants.NAME, className)
								.put(ModelStructureConstants.EDGES, edges));
				continue;
			}

			if (AnimationFrame.class.isAssignableFrom(nestedModelClass)) {
				animationClasses.add(nestedModelClass);
			}
		}

		for (Method method : modelClass.getDeclaredMethods()) {
			if (Resource.class.isAssignableFrom(method.getReturnType())) {
				if (!method.getName().startsWith("initialize"))
					continue;

				String typeName = NamingHelper.changeDollarToDot(method.getReturnType().getName());
				JSONArray resourceTypes = simulatorPreinitializationInfo.modelStructure
						.getJSONArray(ModelStructureConstants.RESOURCE_TYPES);
				JSONObject resourceType = null;
				for (int i = 0; i < resourceTypes.length(); i++) {
					resourceType = resourceTypes.getJSONObject(i);
					if (resourceType.getString(ModelStructureConstants.NAME).equals(typeName))
						break;
				}

				if (resourceType == null)
					throw new RuntimeException("Invalid resource type + " + method.getReturnType());

				resourceType.getJSONArray(ModelStructureConstants.NAMED_RESOURCES).put(new JSONObject()
						.put(ModelStructureConstants.NAME, NamingHelper.createFullNameFromInitializeMethod(method)));
			}
		}

		for (Field field : modelClass.getDeclaredFields()) {
			if (RaoNameable.class.isAssignableFrom(field.getType()))
				nameableFields.add(field);
			if (Result.class.isAssignableFrom(field.getType()))
				resultFields.add(field);
		}
	}

	public final void postprocess() throws IllegalArgumentException, IllegalAccessException, InstantiationException {
		for (Field field : nameableFields) {
			String name = NamingHelper.createFullNameForField(field);
			RaoNameable nameable = (RaoNameable) field.get(null);
			nameable.setName(name);
		}

		for (Field resultField : resultFields) {
			Result<?> result = (Result<?>) resultField.get(null);
			simulatorInitializationInfo.results.add(result);
		}

		for (Class<?> decisionPointClass : decisionPointClasses) {
			AbstractDecisionPoint dpt = (AbstractDecisionPoint) decisionPointClass.newInstance();
			simulatorInitializationInfo.decisionPoints.add(dpt);
		}

		for (Class<?> animationClass : animationClasses) {
			AnimationFrame frame = (AnimationFrame) animationClass.newInstance();
			animationFrames.add(frame);
		}
	}

	public final List<AnimationFrame> getAnimationFrames() {
		return animationFrames;
	}

	public final void closeClassLoader() {
		if (classLoader != null) {
			try {
				classLoader.close();
			} catch (IOException e) {
			}
		}
	}
}
