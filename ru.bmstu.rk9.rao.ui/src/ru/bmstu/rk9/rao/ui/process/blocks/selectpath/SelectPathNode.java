package ru.bmstu.rk9.rao.ui.process.blocks.selectpath;

import java.io.Serializable;
import java.util.List;
import java.util.function.Supplier;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import ru.bmstu.rk9.rao.lib.process.SelectPath.SelectPathMode;
import ru.bmstu.rk9.rao.ui.execution.ModelContentsInfo;
import ru.bmstu.rk9.rao.ui.gef.EnumComboBoxPropertyDescriptor;
import ru.bmstu.rk9.rao.ui.process.BlockConverterInfo;
import ru.bmstu.rk9.rao.ui.process.EResourceRetriever;
import ru.bmstu.rk9.rao.ui.process.blocks.BlockNode;
import ru.bmstu.rk9.rao.ui.process.model.ProcessModelNode;

public class SelectPathNode extends BlockNode implements Serializable {

	private static final long serialVersionUID = 1;

	public static final String DOCK_IN = "IN";
	public static final String DOCK_TRUE_OUT = "TRUE_OUT";
	public static final String DOCK_FALSE_OUT = "FALSE_OUT";
	public static String name = "SelectPath";
	private static final String PROPERTY_PROBABILITY = "Probability";
	private static final String PROPERTY_CONDITION = "SelectPathCondition";
	private static final String PROPERTY_MODE = "SelectPathMode";

	private String probability = "0.5";
	private String condition;
	private SelectPathMode mode = SelectPathMode.PROBABILITY;

	public SelectPathNode() {
		setName(name);
		registerDock(DOCK_IN);
		registerDock(DOCK_FALSE_OUT);
		registerDock(DOCK_TRUE_OUT);
	}

	private final String getProbability() {
		return probability;
	}

	private final void setProbability(String probability) {
		String previousValue = this.probability;
		this.probability = probability;
		getListeners().firePropertyChange(PROPERTY_PROBABILITY, previousValue, probability);
	}

	private final int getConditionIndex() {
		return getBooleanFunctionsNames().indexOf(condition);
	}

	private final void setConditionIndex(int index) {
		String previousValue = this.condition;
		this.condition = getBooleanFunctionsNames().get(index);
		getListeners().firePropertyChange(PROPERTY_CONDITION, previousValue, condition);
	}

	private final SelectPathMode getMode() {
		return mode;
	}

	private final void setMode(SelectPathMode mode) {
		SelectPathMode previousValue = this.mode;
		this.mode = mode;
		getListeners().firePropertyChange(PROPERTY_MODE, previousValue, mode);
	}

	@Override
	public BlockConverterInfo createBlock(ModelContentsInfo modelContentsInfo) {
		BlockConverterInfo selectPathInfo = new BlockConverterInfo();
		ru.bmstu.rk9.rao.lib.process.SelectPath selectPath = null;
		switch (mode) {
		case PROBABILITY:
			selectPath = new ru.bmstu.rk9.rao.lib.process.SelectPath(Double.valueOf(this.probability));
			break;
		case CONDITION:
			Supplier<Boolean> supplier = modelContentsInfo.booleanFunctions.get(condition);
			if (supplier == null) {
				selectPathInfo.isSuccessful = false;
				selectPathInfo.errorMessage = "Condition not selected";
				condition = "";
				return selectPathInfo;
			}
			selectPath = new ru.bmstu.rk9.rao.lib.process.SelectPath(supplier);
			break;
		}

		selectPathInfo.setBlock(selectPath);
		selectPathInfo.inputDocks.put(DOCK_IN, selectPath.getInputDock());
		selectPathInfo.outputDocks.put(DOCK_TRUE_OUT, selectPath.getTrueOutputDock());
		selectPathInfo.outputDocks.put(DOCK_FALSE_OUT, selectPath.getFalseOutputDock());
		return selectPathInfo;
	}

	@Override
	public void createProperties(List<PropertyDescriptor> properties) {
		super.createProperties(properties);

		properties.add(new TextPropertyDescriptor(PROPERTY_PROBABILITY, "Probability"));
		properties.add(new EnumComboBoxPropertyDescriptor<>(PROPERTY_MODE, "Mode", SelectPathMode.class));
		properties.add(new ComboBoxPropertyDescriptor(PROPERTY_CONDITION, "Condition",
				getBooleanFunctionsNames().stream().toArray(String[]::new)));
	}

	@Override
	public Object getPropertyValue(String propertyName) {

		switch (propertyName) {
		case PROPERTY_MODE:
			return getMode();
		case PROPERTY_PROBABILITY:
			return getProbability();
		case PROPERTY_CONDITION:
			return getConditionIndex();
		}

		return super.getPropertyValue(propertyName);
	}

	@Override
	public void setPropertyValue(String propertyName, Object value) {
		super.setPropertyValue(propertyName, value);

		switch (propertyName) {
		case PROPERTY_MODE:
			setMode((SelectPathMode) value);
			break;
		case PROPERTY_PROBABILITY:
			setProbability((String) value);
			break;
		case PROPERTY_CONDITION:
			setConditionIndex((int) value);
			break;
		}
	}

	private final void validateProbability(IResource file) throws CoreException {
		if (mode != SelectPathMode.PROBABILITY)
			return;

		boolean valid = false;
		try {
			double probability = Double.valueOf(this.probability);
			valid = 0 <= probability && probability <= 1;
		} catch (NumberFormatException e) {
		}

		if (!valid)
			createProblemMarker(file, "Wrong probability", IMarker.SEVERITY_ERROR);
	}

	private final void validateCondition(IResource file) throws CoreException {
		if (mode != SelectPathMode.CONDITION)
			return;

		if (!getBooleanFunctionsNames().contains(condition))
			createProblemMarker(file, "Wrong condition", IMarker.SEVERITY_ERROR);
	}

	@Override
	public void validateProperty(IResource file) throws CoreException {
		validateProbability(file);
		validateCondition(file);
		validateConnections(file, 2, 1);
	}

	public List<String> getBooleanFunctionsNames() {
		EResourceRetriever resourceRetriever = ((ProcessModelNode) getParent()).getResourceRetriever();
		return resourceRetriever.getBooleanFunctionsNames();
	}
}
