package ru.bmstu.rk9.rdo.ui.contributions;

import java.text.DecimalFormat;

import org.eclipse.jdt.ui.PreferenceConstants;

import org.eclipse.jface.resource.FontRegistry;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import org.eclipse.swt.SWT;

import org.eclipse.swt.custom.ScrolledComposite;

import org.eclipse.swt.graphics.Font;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import org.eclipse.ui.PlatformUI;

import org.eclipse.ui.part.ViewPart;

import org.eclipse.ui.themes.ITheme;
import org.eclipse.ui.themes.IThemeManager;

import ru.bmstu.rk9.rdo.ui.runtime.SetSimulationScaleHandler;

public class RDOStatusView extends ViewPart
{
	public static final String ID = "ru.bmstu.rk9.rdo.ui.RDOStatusView"; //$NON-NLS-1$

	private static ScrolledComposite scrolledComposite = null;
	private static FillLayout scrolledCompositeLayout;

	private static GridData leftGridData;
	private static GridData leftGridDataForLabels;

	private static Label scaleLabel = null;
	private static Label simulationScale = null;

	private static Label actualScaleLabel = null;
	private static Label actualSimulationScale = null;

	private static Label timeLabel = null;
	private static Label simulationTime = null;

	private static Label realTimeLabel = null;
	private static Label realTime = null;

	private static DecimalFormat scaleFormatter = new DecimalFormat("0.######");
	private static DecimalFormat actualScaleFormatter = new DecimalFormat("0.0");

	public static void setSimulationScale(double scale)
	{
		if(!isInitialized())
			return;

		simulationScale.setText(scaleFormatter.format(scale));

		calculateMinWidth();

		simulationScale.setSize(simulationScale.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	public static void setActualSimulationScale(double scale)
	{
		if(!isInitialized())
			return;

		actualSimulationScale.setText(actualScaleFormatter.format(scale));

		calculateMinWidth();

		actualSimulationScale.setSize(actualSimulationScale.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	private static DecimalFormat timeFormatter = new DecimalFormat("0.0#####");

	public static void setSimulationTime(double time)
	{
		if(!isInitialized())
			return;

		simulationTime.setText(timeFormatter.format(time));

		calculateMinWidth();

		simulationTime.setSize(simulationTime.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	private static DecimalFormat realTimeFormatter = new DecimalFormat("0.0");

	public static void setRealTime(long time)
	{
		if(!isInitialized())
			return;

		realTime.setText(realTimeFormatter.format(time/1000d) + "s");

		calculateMinWidth();

		realTime.setSize(realTime.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	private static Control[] controls;

	private static IThemeManager themeManager;
	private static IPropertyChangeListener fontListener;

	@Override
	public void createPartControl(Composite parent)
	{
		scrolledComposite = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);

		themeManager = PlatformUI.getWorkbench().getThemeManager();
		ITheme currentTheme = themeManager.getCurrentTheme();
		FontRegistry fontRegistry = currentTheme.getFontRegistry();
		Font editorFont = fontRegistry.get(PreferenceConstants.EDITOR_TEXT_FONT);

		Composite composite = new Composite(scrolledComposite, SWT.NONE);
			scrolledCompositeLayout = new FillLayout(SWT.VERTICAL);
				scrolledCompositeLayout.marginHeight = 5;
				scrolledCompositeLayout.marginWidth = 5;
				scrolledCompositeLayout.spacing = 2;
			composite.setLayout(scrolledCompositeLayout);

			GridLayout gridLayout = new GridLayout(2, false);
			gridLayout.marginHeight = 0;
			gridLayout.marginWidth = 0;
			gridLayout.horizontalSpacing = 0;

			leftGridDataForLabels = new GridData(SWT.LEFT, SWT.CENTER, false, true);
			leftGridData = new GridData(SWT.LEFT, SWT.CENTER, true, true);

			Composite scaleComposite = new Composite(composite, SWT.NONE);
				scaleComposite.setLayout(gridLayout);
				scaleLabel = new Label(scaleComposite, SWT.LEFT);
					scaleLabel.setText("Simulation scale:");
					scaleLabel.setLayoutData(leftGridDataForLabels);
				simulationScale = new Label(scaleComposite, SWT.LEFT);
					simulationScale.setLayoutData(leftGridData);

			Composite actualScaleComposite = new Composite(composite, SWT.NONE);
				actualScaleComposite.setLayout(gridLayout);
				actualScaleLabel = new Label(actualScaleComposite, SWT.LEFT);
					actualScaleLabel.setText("Actual scale:");
					actualScaleLabel.setLayoutData(leftGridDataForLabels);
				actualSimulationScale = new Label(actualScaleComposite, SWT.LEFT);
					actualSimulationScale.setLayoutData(leftGridData);
					actualSimulationScale.setText("-");

			Composite timeComposite = new Composite(composite, SWT.NONE);
				timeComposite.setLayout(gridLayout);
				timeLabel = new Label(timeComposite, SWT.LEFT);
					timeLabel.setText("Simulation time:");
					timeLabel.setLayoutData(leftGridDataForLabels);
				simulationTime = new Label(timeComposite, SWT.LEFT);
					simulationTime.setLayoutData(leftGridData);
					simulationTime.setText("-");

			Composite realTimeComposite = new Composite(composite, SWT.LEFT);
				realTimeComposite.setLayout(gridLayout);
				realTimeLabel = new Label(realTimeComposite, SWT.LEFT);
					realTimeLabel.setText("Time elapsed:");
					realTimeLabel.setLayoutData(leftGridDataForLabels);
				realTime = new Label(realTimeComposite, SWT.LEFT);
					realTime.setLayoutData(leftGridData);
					realTime.setText("-");

		controls = new Control[]
		{
			scrolledComposite, composite,
			scaleComposite, scaleLabel, simulationScale,
			actualScaleComposite, actualScaleLabel, actualSimulationScale,
			timeComposite, timeLabel, simulationTime,
			realTimeComposite, realTimeLabel, realTime
		};

		for(Control c : controls)
		{
			c.setFont(editorFont);
			c.setBackground(parent.getBackground());
		}

		fontListener = new IPropertyChangeListener()
		{
			@Override
			public void propertyChange(PropertyChangeEvent event)
			{
				if(event.getProperty().equals(PreferenceConstants.EDITOR_TEXT_FONT))
				{
					Font editorFont =
						fontRegistry.get(PreferenceConstants.EDITOR_TEXT_FONT);

					for(Control c : controls)
						c.setFont(editorFont);

					calculateLeftLabelWidth();
					scrolledComposite.layout(true, true);
				}
			}
		};
		themeManager.addPropertyChangeListener(fontListener);

		calculateLeftLabelWidth();

		scrolledComposite.setContent(composite);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		setSimulationScale(SetSimulationScaleHandler.getSimulationScale());
	}

	private static void calculateLeftLabelWidth()
	{
		leftGridDataForLabels.widthHint = 5 + Math.max
		(
			scaleLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT).x,
			Math.max
			(
				actualScaleLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT).x,
				Math.max
				(
					timeLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT).x,
					realTimeLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT).x
				)
			)
		);
	}

	@Override
	public void dispose()
	{
		themeManager.removePropertyChangeListener(fontListener);
		super.dispose();
	}

	private static void calculateMinWidth()
	{
		int scaleSize = simulationScale.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
		int actualScaleSize = actualSimulationScale.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
		int timeSize = simulationTime.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
		int realTimeSize = realTime.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;

		leftGridData.widthHint = Math.max(scaleSize, Math.max(actualScaleSize,
			Math.max(timeSize, realTimeSize)));

		scrolledComposite.setMinSize
		(
			leftGridData.widthHint + leftGridDataForLabels.widthHint +
				scrolledCompositeLayout.marginWidth * 2,
			calculateOverallHeight()
		);
	}

	private static int calculateOverallHeight()
	{
		int scaleSize = simulationScale.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
		int actualScaleSize = actualSimulationScale.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
		int timeSize = simulationTime.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
		int realTimeSize = realTime.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;

		return scaleSize + actualScaleSize + timeSize + realTimeSize +
			scrolledCompositeLayout.spacing * 2 + scrolledCompositeLayout.marginHeight * 2;
	}

	private static boolean isInitialized()
	{
		return
			scrolledComposite != null
			&& !scrolledComposite.isDisposed()
			&& simulationScale != null
			&& actualSimulationScale != null
			&& simulationTime != null
			&& realTime != null;
	}

	@Override
	public void setFocus() {}
}
