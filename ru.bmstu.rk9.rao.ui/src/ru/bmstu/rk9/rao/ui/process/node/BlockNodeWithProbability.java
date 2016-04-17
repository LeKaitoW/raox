package ru.bmstu.rk9.rao.ui.process.node;

import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

public abstract class BlockNodeWithProbability extends BlockNode {

	private static final long serialVersionUID = 1;

	protected static final String PROPERTY_PROBABILITY = "Probability";

	protected String probability = "0.5";

	public String getProbability() {
		return probability;
	}

	public void setProbability(String probability) {
		String previousValue = this.probability;
		this.probability = probability;
		getListeners().firePropertyChange(PROPERTY_PROBABILITY, previousValue, probability);
	}

	public void validateProbability(IResource file) throws CoreException {
		boolean valid = false;
		try {
			double probability = Double.valueOf(this.probability);
			valid = 0 <= probability && probability <= 1;
		} catch (NumberFormatException e) {
		}

		if (!valid)
			createErrorMarker(file, "Wrong probability", IMarker.SEVERITY_ERROR);
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
}
