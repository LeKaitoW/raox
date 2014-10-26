package ru.bmstu.rk9.rdo.ui.contributions;

import java.text.DecimalFormat;

import org.eclipse.jdt.ui.PreferenceConstants;

import org.eclipse.jface.resource.FontRegistry;

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

	private static ScrolledComposite scrolledComposite;
	private static FillLayout scrolledCompositeLayout;

	private static GridData leftGridData;
	private static GridData leftGridDataForLabels;

	private static Label simulationScale;

	private static Label simulationTime;

	private static Label realTime;

	private static DecimalFormat scaleFormatter = new DecimalFormat("#.###");

	public static void setSimulationScale(double scale)
	{
		simulationScale.setText(scaleFormatter.format(scale));

		calculateMinWidth();

		simulationScale.setSize(simulationScale.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	private static DecimalFormat timeFormatter = new DecimalFormat("#.0#####");

	public static void setSimulationTime(double time)
	{
		simulationTime.setText(timeFormatter.format(time));

		calculateMinWidth();

		simulationTime.setSize(simulationTime.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	private static DecimalFormat realTimeFormatter = new DecimalFormat("0.0");

	public static void setRealTime(long time)
	{
		realTime.setText(realTimeFormatter.format(time/1000d) + "s");

		calculateMinWidth();

		realTime.setSize(realTime.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	@Override
	public void createPartControl(Composite parent)
	{
		scrolledComposite = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);

		IThemeManager themeManager = PlatformUI.getWorkbench().getThemeManager();
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
				Label scaleLabel = new Label(scaleComposite, SWT.LEFT);
					scaleLabel.setText("Simulation scale:");
					scaleLabel.setLayoutData(leftGridDataForLabels);
				simulationScale = new Label(scaleComposite, SWT.LEFT);
					simulationScale.setLayoutData(leftGridData);

			Composite timeComposite = new Composite(composite, SWT.NONE);
				timeComposite.setLayout(gridLayout);
				Label timeLabel = new Label(timeComposite, SWT.LEFT);
					timeLabel.setText("Simulation time:");
					timeLabel.setLayoutData(leftGridDataForLabels);
				simulationTime = new Label(timeComposite, SWT.LEFT);
					simulationTime.setLayoutData(leftGridData);
					simulationTime.setText("-");

			Composite realTimeComposite = new Composite(composite, SWT.LEFT);
				realTimeComposite.setLayout(gridLayout);
				Label realTimeLabel = new Label(realTimeComposite, SWT.LEFT);
					realTimeLabel.setText("Time elapsed:");
					realTimeLabel.setLayoutData(leftGridDataForLabels);
				realTime = new Label(realTimeComposite, SWT.LEFT);
					realTime.setLayoutData(leftGridData);
					realTime.setText("-");

		for(Control c : new Control[]
		{
			scrolledComposite, composite,
			scaleComposite,	scaleLabel, simulationScale,
			timeComposite, timeLabel, simulationTime,
			realTimeComposite, realTimeLabel, realTime
		})
		{
			c.setFont(editorFont);
			c.setBackground(parent.getBackground());
		}

		leftGridDataForLabels.widthHint = 5 + Math.max
		(
			scaleLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT).x,
			Math.max
			(
				timeLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT).x,
				realTimeLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT).x
			)
		);

		scrolledComposite.setContent(composite);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		setSimulationScale(SetSimulationScaleHandler.getSimulationScale());
	}

	private static void calculateMinWidth()
	{
		int scaleSize = simulationScale.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
		int timeSize = simulationTime.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
		int realTimeSize = realTime.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;

		leftGridData.widthHint = Math.max(scaleSize, Math.max(timeSize, realTimeSize));

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
		int timeSize = simulationTime.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
		int realTimeSize = realTime.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;

		return scaleSize + timeSize + realTimeSize +
			scrolledCompositeLayout.spacing * 2 + scrolledCompositeLayout.marginHeight * 2;
	}

	@Override
	public void setFocus() {}
}
