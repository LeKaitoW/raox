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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.ui.resource.IResourceSetProvider;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.typesystem.IBatchTypeResolver;
import org.eclipse.xtext.xbase.typesystem.references.LightweightTypeReference;

import ru.bmstu.rk9.rao.lib.animation.AnimationFrame;
import ru.bmstu.rk9.rao.lib.database.Database.DataType;
import ru.bmstu.rk9.rao.lib.dpt.AbstractDecisionPoint;
import ru.bmstu.rk9.rao.lib.dpt.Logic;
import ru.bmstu.rk9.rao.lib.dpt.Search;
import ru.bmstu.rk9.rao.lib.exception.RaoLibException;
import ru.bmstu.rk9.rao.lib.json.JSONArray;
import ru.bmstu.rk9.rao.lib.json.JSONObject;
import ru.bmstu.rk9.rao.lib.modeldata.ModelStructureConstants;
import ru.bmstu.rk9.rao.lib.naming.NamingHelper;
import ru.bmstu.rk9.rao.lib.pattern.Pattern;
import ru.bmstu.rk9.rao.lib.process.Block;
import ru.bmstu.rk9.rao.lib.resource.ComparableResource;
import ru.bmstu.rk9.rao.lib.result.AbstractResult;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorInitializationInfo;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorPreinitializationInfo;
import ru.bmstu.rk9.rao.rao.PatternType;
import ru.bmstu.rk9.rao.rao.RaoEntity;
import ru.bmstu.rk9.rao.rao.RaoModel;
import ru.bmstu.rk9.rao.rao.RelevantResource;
import ru.bmstu.rk9.rao.rao.RelevantResourceTuple;
import ru.bmstu.rk9.rao.ui.gef.process.BlockConverter;
import ru.bmstu.rk9.rao.ui.gef.process.ProcessEditor;
import ru.bmstu.rk9.rao.ui.gef.process.model.ProcessModelNode;

@SuppressWarnings("restriction")
public class ModelInternalsParser {
	private final SimulatorPreinitializationInfo simulatorPreinitializationInfo = new SimulatorPreinitializationInfo();
	private final SimulatorInitializationInfo simulatorInitializationInfo = new SimulatorInitializationInfo();
	private final List<Class<?>> decisionPointClasses = new ArrayList<>();

	private final ModelContentsInfo modelContentsInfo = new ModelContentsInfo();

	private final List<Class<?>> animationClasses = new ArrayList<>();
	private final List<Class<?>> tupleClasses = new ArrayList<>();
	private final List<AnimationFrame> animationFrames = new ArrayList<>();
	private final List<Field> resultFields = new ArrayList<>();

	private URLClassLoader classLoader;
	private final IProject project;
	private final IResourceSetProvider resourceSetProvider;
	private final IBatchTypeResolver typeResolver;

	public final SimulatorPreinitializationInfo getSimulatorPreinitializationInfo() {
		return simulatorPreinitializationInfo;
	}

	public final SimulatorInitializationInfo getSimulatorInitializationInfo() {
		return simulatorInitializationInfo;
	}

	public ModelInternalsParser(IProject project, IResourceSetProvider resourceSetProvider,
			IBatchTypeResolver typeResolver) {
		this.project = project;
		this.resourceSetProvider = resourceSetProvider;
		this.typeResolver = typeResolver;
	}

	public final void parse() throws NoSuchMethodException, SecurityException, InstantiationException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException,
			MalformedURLException, CoreException {
		IProjectDescription description = project.getDescription();
		java.net.URI locationURI = description.getLocationURI();
		boolean useDefaultLocation = (locationURI == null);
		String location;

		if (useDefaultLocation)
			location = "file:///" + ResourcesPlugin.getWorkspace().getRoot().getLocation().toString() + "/"
					+ project.getName();
		else
			location = locationURI.toURL().toString();

		URL modelURL = new URL(location + "/bin/");

		URL[] urls = new URL[] { modelURL };

		classLoader = new URLClassLoader(urls, CurrentSimulator.class.getClassLoader());

		simulatorPreinitializationInfo.modelStructure.put(ModelStructureConstants.NAME, project.getName());
		simulatorPreinitializationInfo.modelStructure.put(ModelStructureConstants.LOCATION,
				project.getLocation().toString());

		final ResourceSet resourceSet = resourceSetProvider.get(project);
		if (resourceSet == null) {
			System.out.println("resource set is null");
			return;
		}

		List<IResource> raoFiles = BuildUtil.getAllFilesInProject(project, "rao");
		simulatorPreinitializationInfo.modelStructure.put(ModelStructureConstants.NUMBER_OF_MODELS, raoFiles.size());

		for (IResource raoFile : raoFiles) {
			String raoFileName = raoFile.getName();
			raoFileName = raoFileName.substring(0, raoFileName.length() - ".rao".length());
			String modelClassName = project.getName() + "." + raoFileName;

			URI uri = BuildUtil.getURI(raoFile);
			org.eclipse.emf.ecore.resource.Resource modelResource = resourceSet.getResource(uri, true);
			if (modelResource == null) {
				System.out.println("model resource is null");
				continue;
			}

			EList<EObject> contents = modelResource.getContents();
			if (contents.isEmpty())
				continue;

			RaoModel model = (RaoModel) contents.get(0);

			parseModel(model, modelClassName);
		}
	}

	@SuppressWarnings("unchecked")
	public final void parseModel(RaoModel model, String modelClassName)
			throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		Class<?> modelClass = Class.forName(modelClassName, false, classLoader);

		try {
			Class<?> experiments = Class.forName(modelClassName + "$experiments", false, classLoader);
			Constructor<?> experimentsConstructor = experiments.getDeclaredConstructor();
			experimentsConstructor.setAccessible(true);
			simulatorInitializationInfo.setExperiments((Runnable) experimentsConstructor.newInstance());
		} catch (ClassNotFoundException classException) {
		}

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

		EList<RaoEntity> entities = model.getObjects();

		for (RaoEntity entity : entities) {
			if (!(entity instanceof ru.bmstu.rk9.rao.rao.ResourceType))
				continue;

			String name = modelClassName + "." + entity.getName();

			JSONArray parameters = new JSONArray();
			int offset = 0;
			int variableWidthParameterIndex = 0;
			for (ru.bmstu.rk9.rao.rao.FieldDeclaration field : ((ru.bmstu.rk9.rao.rao.ResourceType) entity)
					.getParameters()) {
				DataType dataType = DataType.getByName(field.getDeclaration().getParameterType().getSimpleName());

				parameters.put(new JSONObject().put(ModelStructureConstants.NAME, field.getDeclaration().getName())
						.put(ModelStructureConstants.TYPE, dataType).put(ModelStructureConstants.OFFSET, offset)
						.put(ModelStructureConstants.VARIABLE_WIDTH_PARAMETER_INDEX,
								dataType == DataType.OTHER ? variableWidthParameterIndex : -1));

				if (dataType == DataType.OTHER)
					variableWidthParameterIndex++;
				else
					offset += dataType.getSize();
			}

			simulatorPreinitializationInfo.modelStructure.getJSONArray(ModelStructureConstants.RESOURCE_TYPES)
					.put(new JSONObject().put(ModelStructureConstants.NAME, name)
							.put(ModelStructureConstants.NAMED_RESOURCES, new JSONArray())
							.put(ModelStructureConstants.PARAMETERS, parameters)
							.put(ModelStructureConstants.FINAL_OFFSET, offset));

		}

		for (RaoEntity entity : entities) {
			String name = modelClassName + "." + entity.getName();

			if (entity instanceof ru.bmstu.rk9.rao.rao.Event) {
				simulatorPreinitializationInfo.modelStructure.getJSONArray(ModelStructureConstants.EVENTS)
						.put(new JSONObject().put(ModelStructureConstants.NAME, name));
				continue;
			}

			if (entity instanceof ru.bmstu.rk9.rao.rao.Pattern) {
				String typeString = null;
				PatternType type = ((ru.bmstu.rk9.rao.rao.Pattern) entity).getType();

				switch (type) {
				case OPERATION:
					typeString = ModelStructureConstants.OPERATION;
					break;
				case RULE:
					typeString = ModelStructureConstants.RULE;
					break;
				}

				JSONArray relevantResources = new JSONArray();
				for (RelevantResource relevant : ((ru.bmstu.rk9.rao.rao.Pattern) entity).getRelevantResources()) {
					LightweightTypeReference typeReference = typeResolver.resolveTypes(relevant.getValue())
							.getActualType(relevant.getValue());

					relevantResources.put(
							new JSONObject().put(ModelStructureConstants.NAME, name).put(ModelStructureConstants.TYPE,
									NamingHelper.changeDollarToDot(typeReference.getJavaIdentifier())));
				}

				for (RelevantResourceTuple tuple : ((ru.bmstu.rk9.rao.rao.Pattern) entity).getRelevantTuples()) {
					for (JvmTypeReference tupleType : tuple.getTypes()) {
						relevantResources.put(new JSONObject().put(ModelStructureConstants.NAME, name).put(
								ModelStructureConstants.TYPE,
								NamingHelper.changeDollarToDot(tupleType.getIdentifier())));
					}
				}

				simulatorPreinitializationInfo.modelStructure.getJSONArray(ModelStructureConstants.PATTERNS)
						.put(new JSONObject().put(ModelStructureConstants.NAME, name)
								.put(ModelStructureConstants.TYPE, typeString)
								.put(ModelStructureConstants.RELEVANT_RESOURCES, relevantResources));
				continue;
			}

			if (entity instanceof ru.bmstu.rk9.rao.rao.Logic) {
				JSONArray activities = new JSONArray();
				for (ru.bmstu.rk9.rao.rao.Activity activity : ((ru.bmstu.rk9.rao.rao.Logic) entity).getActivities())
					activities.put(new JSONObject().put(ModelStructureConstants.NAME, activity.getName()));

				simulatorPreinitializationInfo.modelStructure.getJSONArray(ModelStructureConstants.LOGICS)
						.put(new JSONObject().put(ModelStructureConstants.NAME, name)
								.put(ModelStructureConstants.ACTIVITIES, activities));
				continue;
			}

			if (entity instanceof ru.bmstu.rk9.rao.rao.Search) {
				JSONArray edges = new JSONArray();
				for (ru.bmstu.rk9.rao.rao.Edge edge : ((ru.bmstu.rk9.rao.rao.Search) entity).getEdges())
					edges.put(new JSONObject().put(ModelStructureConstants.NAME, edge.getName()));

				simulatorPreinitializationInfo.modelStructure.getJSONArray(ModelStructureConstants.SEARCHES)
						.put(new JSONObject().put(ModelStructureConstants.NAME, name).put(ModelStructureConstants.EDGES,
								edges));
				continue;
			}

			if (entity instanceof ru.bmstu.rk9.rao.rao.ResourceDeclaration) {
				XExpression constructor = ((ru.bmstu.rk9.rao.rao.ResourceDeclaration) entity).getConstructor();
				LightweightTypeReference typeReference = typeResolver.resolveTypes(constructor)
						.getActualType(constructor);
				String typeName = NamingHelper.changeDollarToDot(typeReference.getJavaIdentifier());

				JSONArray resourceTypes = simulatorPreinitializationInfo.modelStructure
						.getJSONArray(ModelStructureConstants.RESOURCE_TYPES);
				JSONObject resourceType = null;
				for (int i = 0; i < resourceTypes.length(); i++) {
					resourceType = resourceTypes.getJSONObject(i);
					if (resourceType.getString(ModelStructureConstants.NAME).equals(typeName))
						break;
				}

				if (resourceType == null)
					throw new RuntimeException("Invalid resource type + " + typeReference);

				resourceType.getJSONArray(ModelStructureConstants.NAMED_RESOURCES)
						.put(new JSONObject().put(ModelStructureConstants.NAME, name));
				continue;
			}

			if (entity instanceof ru.bmstu.rk9.rao.rao.Result) {
				simulatorPreinitializationInfo.modelStructure.getJSONArray(ModelStructureConstants.RESULTS)
						.put(new JSONObject().put(ModelStructureConstants.NAME, name));
				continue;
			}
		}

		for (Class<?> nestedModelClass : modelClass.getDeclaredClasses()) {
			if (ComparableResource.class.isAssignableFrom(nestedModelClass)) {
				simulatorPreinitializationInfo.resourceClasses.add(nestedModelClass);
				continue;
			}

			if (Logic.class.isAssignableFrom(nestedModelClass)) {
				decisionPointClasses.add(nestedModelClass);
				continue;
			}

			if (Search.class.isAssignableFrom(nestedModelClass)) {
				decisionPointClasses.add(nestedModelClass);
				continue;
			}

			if (AnimationFrame.class.isAssignableFrom(nestedModelClass)) {
				animationClasses.add(nestedModelClass);
				continue;
			}

			if (Pattern.class.isAssignableFrom(nestedModelClass)) {
				tupleClasses.add(nestedModelClass);
				// FIXME this workaround makes sure that nested class in
				// patterns initialize and get into classloader, proper solution
				// is needed
				nestedModelClass.getDeclaredClasses();
				continue;
			}
		}

		for (Field field : modelClass.getDeclaredFields()) {
			if (AbstractResult.class.isAssignableFrom(field.getType()))
				resultFields.add(field);
		}

		for (Method method : modelClass.getDeclaredMethods()) {
			if (!method.getReturnType().equals(Boolean.TYPE))
				continue;

			if (method.getParameterCount() > 0)
				continue;

			Supplier<Boolean> supplier = new Supplier<Boolean>() {
				@Override
				public Boolean get() {
					try {
						return (boolean) method.invoke(null);
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						e.printStackTrace();
						throw new RaoLibException("Internal error invoking function " + method.getName());
					}
				}
			};
			modelContentsInfo.booleanFunctions.put(NamingHelper.createFullNameForMember(method), supplier);
		}
	}

	public final void postprocess() throws IllegalArgumentException, IllegalAccessException, InstantiationException,
			InvocationTargetException, ClassNotFoundException, IOException, CoreException {
		for (Field resultField : resultFields) {
			resultField.setAccessible(true);
			AbstractResult<?> result = (AbstractResult<?>) resultField.get(null);

			String name = NamingHelper.createFullNameForMember(resultField);
			result.setName(name);
			simulatorInitializationInfo.results.add(result);
		}

		for (Class<?> decisionPointClass : decisionPointClasses) {
			AbstractDecisionPoint dpt = (AbstractDecisionPoint) decisionPointClass.newInstance();
			simulatorInitializationInfo.decisionPoints.add(dpt);
		}

		for (IResource processFile : BuildUtil.getAllFilesInProject(project, "proc")) {
			ProcessModelNode model = ProcessEditor.readModelFromFile((IFile) processFile);
			if (model == null)
				model = new ProcessModelNode();
			List<Block> blocks = BlockConverter.convertModelToBlocks(model, modelContentsInfo);
			simulatorInitializationInfo.processBlocks.addAll(blocks);
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
