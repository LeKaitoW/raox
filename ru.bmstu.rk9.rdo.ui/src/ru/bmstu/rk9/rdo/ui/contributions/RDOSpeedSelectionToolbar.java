package ru.bmstu.rk9.rdo.ui.contributions;

import org.eclipse.ui.menus.WorkbenchWindowControlContribution;

import org.eclipse.swt.SWT;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ProgressBar;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;

import ru.bmstu.rk9.rdo.ui.runtime.SimulationSynchronizer;

public class RDOSpeedSelectionToolbar extends WorkbenchWindowControlContribution
{
	private static volatile int speedValue; 

	public static int getSpeed()
	{
		return speedValue;
	}

	public static void setSpeed(int val)
	{
		speedValue = val < RDOSpeedSelector.MIN ?
			RDOSpeedSelector.MIN :
			val > RDOSpeedSelector.MAX ?
				RDOSpeedSelector.MAX :
				val;
	}

	public class RDOSpeedSelector extends ProgressBar
	{
		private static final int MIN = 1;
		private static final int MAX = 100;

		public RDOSpeedSelector(Composite parent, int style)
		{
			super(parent, style);

			this.setMinimum(0);
			this.setMaximum(MAX);

			// this is a Windows' progress bar animation issue workaround
			// fancy yellowish color on windows and no effect on Linux and OSX
			this.setState(SWT.PAUSED);

			FontData[] fD = this.getFont().getFontData();
			fD[0].setHeight(8);
			this.setFont(new Font(this.getDisplay(),fD[0]));

			this.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLACK));

			addPaintListener
			(
				new PaintListener()
				{
					@Override
					public void paintControl(final PaintEvent e)
					{
						final Point widgetSize = getSize();
						final int percentage = (int) (100f * getSelection() / (getMaximum() - getMinimum()));
						final String text = percentage + "%";
						final Point textSize = e.gc.stringExtent(text);

						int alpha = e.gc.getAlpha();
						e.gc.setAlpha(150);

						e.gc.fillRoundRectangle
						(
							(widgetSize.x - textSize.x) / 2 - 2,
							(widgetSize.y - textSize.y) / 2 - 1,
							textSize.x + 4,
							textSize.y + 2,
							3,
							3
						);

						e.gc.setAlpha(alpha);

						e.gc.drawString
						(
							text,
							(widgetSize.x - textSize.x) / 2,
							(widgetSize.y - textSize.y) / 2,
							true
						);
					}
				}
			);

		    addMouseListener
		    (
				new MouseListener()
				{
					@Override
					public void mouseUp(MouseEvent e) {}

					@Override
					public void mouseDown(MouseEvent e)
					{
						if(e.button == 1)
							RDOSpeedSelector.this.setSpeed(e.x, RDOSpeedSelector.this.getSize().x);
					}

					@Override
					public void mouseDoubleClick(MouseEvent e){}
				}
			);

		    addMouseMoveListener
		    (
		    	new MouseMoveListener()
			    {
					@Override
					public void mouseMove(MouseEvent e)
					{
						if((e.stateMask & SWT.BUTTON1) != 0)
							RDOSpeedSelector.this.setSpeed(e.x, speed.getSize().x);
					}
				}
			);

		    addMouseWheelListener
		    (
		   		new MouseWheelListener()
		   		{
					@Override
					public void mouseScrolled(MouseEvent e)
					{
						RDOSpeedSelector.this.modifySpeed((e.count > 0) ? 5 : -5);		
					}
				}
			);

			setToolTipText("Simulation Speed");
		}

		public void setSpeed(int part, int all)
		{
			int val = part * MAX / all;
			speed.setSelection(val < MIN ? MIN : val > MAX ? MAX : val);
		}

		public void modifySpeed(int value)
		{
			int val = this.getSelection() + value;
			speed.setSelection(val < MIN ? MIN : val > MAX ? MAX : val);
		}

		@Override
		public void setSelection(int value)
		{
			super.setSelection(value);
			speedValue = value;
			SimulationSynchronizer.setSimulationSpeed(value);
		}

		@Override
		protected void checkSubclass(){}
	}

	private static RDOSpeedSelector speed;

	@Override
	protected Control createControl(Composite parent)
	{
		Composite container = new Composite(parent, SWT.NONE);

		GridLayout gl = new GridLayout(2, false);
		gl.marginTop = 0;
		gl.marginLeft = 4;
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		container.setLayout(gl);

		GridData gd = new GridData(SWT.CENTER, SWT.CENTER, false, true);
		gd.widthHint = 100;
		gd.heightHint = 20;

		speed = new RDOSpeedSelector(container, SWT.SMOOTH);
		speed.setLayoutData(gd);
		speed.setSpeed(speedValue, 100);
		speed.setEnabled(true);

		return container;
	}
}
