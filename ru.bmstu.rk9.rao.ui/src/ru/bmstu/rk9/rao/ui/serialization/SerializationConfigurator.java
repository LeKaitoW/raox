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
import ru.bmstu.rk9.rao.lib.dpt.DecisionPointSearch.SerializationLevel;
import ru.bmstu.rk9.rao.rao.DecisionPointSearch;
import ru.bmstu.rk9.rao.rao.DecisionPointSome;
import ru.bmstu.rk9.rao.rao.Event;
import ru.bmstu.rk9.rao.rao.Pattern;
import ru.bmstu.rk9.rao.rao.Result;
import ru.bmstu.rk9.rao.ui.serialization.SerializationConfig.SerializationNode;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

class SerializationConfigurator {
	final void fillCategories(Resource model, SerializationNode modelNode) {
		for (SerializationNode category : modelNode.getVisibleChildren())
			category.hideChildren();

		fillCategory(modelNode.getVisibleChildren().get(SerializationCategory.PATTERNS.ordinal()), model,
				Pattern.class);

		fillCategory(modelNode.getVisibleChildren().get(SerializationCategory.EVENTS.ordinal()), model, Event.class);

		fillCategory(modelNode.getVisibleChildren().get(SerializationCategory.DECISION_POINTS.ordinal()), model,
				DecisionPointSome.class);

		fillCategory(modelNode.getVisibleChildren().get(SerializationCategory.RESULTS.ordinal()), model, Result.class);

		fillCategory(modelNode.getVisibleChildren().get(SerializationCategory.SEARCH.ordinal()), model,
				DecisionPointSearch.class);
	}

	private final <T extends EObject> void fillCategory(SerializationNode category, Resource model,
			Class<T> categoryClass) {
		final List<T> categoryItems = filterAllContents(model.getAllContents(), categoryClass);

		for (T categoryItem : categoryItems) {
			String name = RaoNaming.getFullyQualifiedName(categoryItem);

			SerializationNode child = category.addChild(name);

			if (categoryItem instanceof DecisionPointSearch) {
				for (SerializationLevel type : SerializationLevel.values())
					child.addChild(child.getFullName() + "." + type.toString());
			}
			if (categoryItem instanceof Pattern || categoryItem instanceof Event) {
				child.addChild(child.getFullName() + ".createdResources");
			}
		}
	}

	private final <T extends EObject> List<T> filterAllContents(TreeIterator<EObject> allContents,
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
