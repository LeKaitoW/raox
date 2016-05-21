package ru.bmstu.rk9.rao.ui.execution;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import ru.bmstu.rk9.rao.lib.animation.AnimationFrame;
import ru.bmstu.rk9.rao.lib.database.Database;
import ru.bmstu.rk9.rao.lib.database.Database.DataType;
import ru.bmstu.rk9.rao.lib.dpt.AbstractDecisionPoint;
import ru.bmstu.rk9.rao.lib.dpt.Activity;
import ru.bmstu.rk9.rao.lib.dpt.Edge;
import ru.bmstu.rk9.rao.lib.dpt.Logic;
import ru.bmstu.rk9.rao.lib.dpt.Search;
import ru.bmstu.rk9.rao.lib.event.Event;
import ru.bmstu.rk9.rao.lib.modeldata.ModelStructureConstants;
import ru.bmstu.rk9.rao.lib.naming.NamingHelper;
import ru.bmstu.rk9.rao.lib.naming.RaoNameable;
import ru.bmstu.rk9.rao.lib.pattern.Operation;
import ru.bmstu.rk9.rao.lib.pattern.Pattern;
import ru.bmstu.rk9.rao.lib.resource.ComparableResource;
import ru.bmstu.rk9.rao.lib.resource.Resource;
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

		simulatorPreinitializationInfo.modelStructure.addProperty(ModelStructureConstants.NAME, project.getName());

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

		for (Class<?> nestedModelClass : modelClass.getDeclaredClasses()) {
			String className = NamingHelper.changeDollarToDot(nestedModelClass.getName());

			if (Event.class.isAssignableFrom(nestedModelClass)) {
				JsonObject obj = new JsonObject();
				obj.addProperty(ModelStructureConstants.NAME, className);
				simulatorPreinitializationInfo.modelStructure.get(ModelStructureConstants.EVENTS).getAsJsonArray()
						.add(obj);
				continue;
			}

			if (ComparableResource.class.isAssignableFrom(nestedModelClass)) {
				simulatorPreinitializationInfo.resourceClasses.add((Class<? extends Resource>) nestedModelClass);
				JsonArray parameters = new JsonArray();
				JsonObject obj = new JsonObject();
				Gson gson = new Gson();
				int offset = 0;
				int variableWidthParameterIndex = 0;
				for (Field field : nestedModelClass.getDeclaredFields()) {
					DataType dataType = Database.getDataType(field.getType());
					String element = gson.toJson(dataType);

					obj.addProperty(ModelStructureConstants.NAME, field.getName().substring(1));
					obj.addProperty(ModelStructureConstants.TYPE, element);
					obj.addProperty(ModelStructureConstants.OFFSET, offset);
					obj.addProperty(ModelStructureConstants.VARIABLE_WIDTH_PARAMETER_INDEX,
							dataType == DataType.OTHER ? variableWidthParameterIndex : -1);
					parameters.add(obj);
					if (dataType == DataType.OTHER)
						variableWidthParameterIndex++;
					else
						offset += dataType.getSize();
				}
				JsonObject data = new JsonObject();
				data.addProperty(ModelStructureConstants.NAME, className);
				JsonArray jsonArray = new JsonArray();
				data.add(ModelStructureConstants.NAMED_RESOURCES, jsonArray);
				data.add(ModelStructureConstants.PARAMETERS, parameters);
				data.addProperty(ModelStructureConstants.FINAL_OFFSET, offset);
				simulatorPreinitializationInfo.modelStructure.get(ModelStructureConstants.RESOURCE_TYPES)
						.getAsJsonArray().add(data);
				continue;
			}

			if (Pattern.class.isAssignableFrom(nestedModelClass)) {
				JsonArray relevantResources = new JsonArray();
				for (Field field : nestedModelClass.getDeclaredFields()) {
					String fieldName = NamingHelper.createFullNameForField(field);
					if (Resource.class.isAssignableFrom(field.getType())) {
						JsonObject obj = new JsonObject();
						obj.addProperty(ModelStructureConstants.NAME, fieldName);
						obj.addProperty(ModelStructureConstants.TYPE,
								NamingHelper.changeDollarToDot(field.getType().getName()));
						relevantResources.add(obj);
					}
				}

				String type = Operation.class.isAssignableFrom(nestedModelClass) ? ModelStructureConstants.OPERATION
						: ModelStructureConstants.RULE;

				JsonObject obj = new JsonObject();
				obj.addProperty(ModelStructureConstants.NAME, className);
				obj.addProperty(ModelStructureConstants.TYPE, type);
				obj.add(ModelStructureConstants.RELEVANT_RESOURCES, relevantResources);
				simulatorPreinitializationInfo.modelStructure.get(ModelStructureConstants.PATTERNS).getAsJsonArray()
						.add(obj);

				continue;
			}

			if (Logic.class.isAssignableFrom(nestedModelClass)) {
				decisionPointClasses.add(nestedModelClass);

				JsonArray activities = new JsonArray();
				for (Field field : nestedModelClass.getDeclaredFields()) {
					String fieldName = field.getName();

					JsonObject obj = new JsonObject();
					obj.addProperty(ModelStructureConstants.NAME, fieldName);
					if (Activity.class.isAssignableFrom(field.getType())) {
						activities.add(obj);
					}
				}

				JsonObject obj = new JsonObject();
				obj.addProperty(ModelStructureConstants.NAME, className);
				obj.add(ModelStructureConstants.ACTIVITIES, activities);
				simulatorPreinitializationInfo.modelStructure.get(ModelStructureConstants.LOGICS).getAsJsonArray()
						.add(obj);
				continue;
			}

			if (Search.class.isAssignableFrom(nestedModelClass)) {
				decisionPointClasses.add(nestedModelClass);

				JsonArray edges = new JsonArray();
				for (Field field : nestedModelClass.getDeclaredFields()) {
					String fieldName = field.getName();
					if (Edge.class.isAssignableFrom(field.getType())) {

						JsonObject obj = new JsonObject();
						obj.addProperty(ModelStructureConstants.NAME, fieldName);
						edges.add(obj);
					}
				}
				JsonObject obj = new JsonObject();
				obj.addProperty(ModelStructureConstants.NAME, className);
				obj.add(ModelStructureConstants.EDGES, edges);

				simulatorPreinitializationInfo.modelStructure.get(ModelStructureConstants.SEARCHES).getAsJsonArray()
						.add(obj);

				continue;
			}

			if (AnimationFrame.class.isAssignableFrom(nestedModelClass)) {
				animationClasses.add(nestedModelClass);
			}
		}

		for (Field field : modelClass.getDeclaredFields()) {
			if (RaoNameable.class.isAssignableFrom(field.getType()))
				nameableFields.add(field);

			if (Resource.class.isAssignableFrom(field.getType())) {
				String typeName = NamingHelper.changeDollarToDot(field.getType().getName());
				JsonArray resourceTypes = simulatorPreinitializationInfo.modelStructure
						.get(ModelStructureConstants.RESOURCE_TYPES).getAsJsonArray();
				JsonObject resourceType = null;
				for (int i = 0; i < resourceTypes.size(); i++) {
					resourceType = resourceTypes.get(i).getAsJsonObject();
					if (resourceType.get(ModelStructureConstants.NAME).getAsString().equals(typeName))
						break;
				}

				if (resourceType == null)
					throw new RuntimeException("Invalid resource type + " + field.getType());
				JsonObject obj = new JsonObject();
				obj.addProperty(ModelStructureConstants.NAME, NamingHelper.createFullNameForField(field));
				resourceType.get(ModelStructureConstants.NAMED_RESOURCES).getAsJsonArray().add(obj);

			}
		}
	}

	public final void postprocess() throws IllegalArgumentException, IllegalAccessException, InstantiationException {
		for (Field field : nameableFields) {
			String name = NamingHelper.createFullNameForField(field);
			RaoNameable nameable = (RaoNameable) field.get(null);
			nameable.setName(name);
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
