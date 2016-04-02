package ru.bmstu.rk9.rao.ui.serialization;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.xbase.lib.IteratorExtensions;

import ru.bmstu.rk9.rao.jvmmodel.RaoNaming;
import ru.bmstu.rk9.rao.lib.database.Database.SerializationCategory;
import ru.bmstu.rk9.rao.lib.database.SerializationConstants;
import ru.bmstu.rk9.rao.lib.dpt.Search.SerializationLevel;
import ru.bmstu.rk9.rao.lib.naming.NamingHelper;
import ru.bmstu.rk9.rao.rao.Event;
import ru.bmstu.rk9.rao.rao.Logic;
import ru.bmstu.rk9.rao.rao.Pattern;
import ru.bmstu.rk9.rao.rao.ResourceDeclaration;
import ru.bmstu.rk9.rao.rao.Result;
import ru.bmstu.rk9.rao.rao.Search;
import ru.bmstu.rk9.rao.ui.serialization.SerializationConfig.SerializationNode;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

public class SerializationConfigurator {
	final void fillCategories(Resource model, SerializationNode modelNode) {
		for (SerializationNode category : modelNode.getVisibleChildren())
			category.hideChildren();

		fillCategory(modelNode.getVisibleChildren().get(SerializationCategory.RESOURCE.ordinal()), model,
				ResourceDeclaration.class);

		fillCategory(modelNode.getVisibleChildren().get(SerializationCategory.PATTERN.ordinal()), model, Pattern.class);

		fillCategory(modelNode.getVisibleChildren().get(SerializationCategory.EVENT.ordinal()), model, Event.class);

		fillCategory(modelNode.getVisibleChildren().get(SerializationCategory.LOGIC.ordinal()), model, Logic.class);

		fillCategory(modelNode.getVisibleChildren().get(SerializationCategory.RESULT.ordinal()), model, Result.class);

		fillCategory(modelNode.getVisibleChildren().get(SerializationCategory.SEARCH.ordinal()), model, Search.class);
	}

	private final <T extends EObject> void fillCategory(SerializationNode category, Resource model,
			Class<T> categoryClass) {
		final List<T> categoryItems = filterAllContents(model.getAllContents(), categoryClass);

		for (T categoryItem : categoryItems) {
			String name = NamingHelper.createFullName(model.getURI().toPlatformString(false),
					RaoNaming.getNameGeneric(categoryItem));

			SerializationNode child = category.addChild(name);

			if (categoryItem instanceof Search) {
				for (SerializationLevel type : SerializationLevel.values())
					child.addChild(child.getFullName() + "." + type.toString());
			}
			if (categoryItem instanceof Pattern || categoryItem instanceof Event) {
				child.addChild(child.getFullName() + "." + SerializationConstants.CREATED_RESOURCES);
			}
		}
	}

	public static final <T extends EObject> List<T> filterAllContents(TreeIterator<EObject> allContents,
			Class<T> categoryClass) {
		final ArrayList<T> categoryList = new ArrayList<T>();
		Iterator<T> filter = Iterators.<T> filter(allContents, categoryClass);
		Iterable<T> iterable = IteratorExtensions.<T> toIterable(filter);
		Iterables.addAll(categoryList, iterable);
		return categoryList;
	}

	final SerializationNode initModel(SerializationNode root, Resource model) {
		SerializationNode modelNode = root.addChild(model.getURI().toPlatformString(false), false, true);
		for (SerializationCategory category : SerializationCategory.values())
			modelNode.addChild(modelNode.getName() + "." + category.getName());
		return modelNode;
	}
}
