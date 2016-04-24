package ru.bmstu.rk9.rao.ui.process.blocks.selectpath;

import java.io.Serializable;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import ru.bmstu.rk9.rao.ui.process.BlockConverterInfo;
import ru.bmstu.rk9.rao.ui.process.blocks.BlockNode;

public class SelectPathNode extends BlockNode implements Serializable {

	private static final long serialVersionUID = 1;

	public static final String DOCK_IN = "IN";
	public static final String DOCK_TRUE_OUT = "TRUE_OUT";
	public static final String DOCK_FALSE_OUT = "FALSE_OUT";
	public static String name = "SelectPath";
	protected static final String PROPERTY_PROBABILITY = "Probability";

	private String probability = "0.5";

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

	@Override
	public BlockConverterInfo createBlock() {
		ru.bmstu.rk9.rao.lib.process.SelectPath selectPath = new ru.bmstu.rk9.rao.lib.process.SelectPath(
				Double.valueOf(this.probability));
		BlockConverterInfo selectPathInfo = new BlockConverterInfo();
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
	}

	@Override
	public Object getPropertyValue(Object propertyName) {
		if (propertyName.equals(PROPERTY_PROBABILITY))
			return getProbability();

		return super.getPropertyValue(propertyName);
	}

	@Override
	public void setPropertyValue(Object propertyName, Object value) {
		super.setPropertyValue(propertyName, value);

		if (propertyName.equals(PROPERTY_PROBABILITY))
			setProbability((String) value);
	}

	private final void validateProbability(IResource file) throws CoreException {
		boolean valid = false;
		try {
			double probability = Double.valueOf(this.probability);
			valid = 0 <= probability && probability <= 1;
		} catch (NumberFormatException e) {
		}

		if (!valid)
			createProblemMarker(file, "Wrong probability", IMarker.SEVERITY_ERROR);
	}

	@Override
	public void validateProperty(IResource file) throws CoreException {
		validateProbability(file);
		validateConnections(file, 2, 1);
	}
}
