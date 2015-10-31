package ru.bmstu.rk9.rao.ui.graph;

import java.awt.BorderLayout;
import java.awt.Frame;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class GraphShell extends Shell {
	public GraphShell(Display display, int dptNum) {
		super(display, SWT.SHELL_TRIM);
		setLayout(new FillLayout());

		Composite graphFrameContainer = new Composite(this, SWT.EMBEDDED
				| SWT.NO_BACKGROUND | SWT.FILL);
		graphFrameContainer.setLayout(new FillLayout());

		graphFrame = SWT_AWT.new_Frame(graphFrameContainer);
		graphFrame.setLayout(new BorderLayout());
		graphPanel = new GraphPanel(dptNum);
		graphFrame.add(BorderLayout.CENTER, graphPanel);
		graphFrame.addKeyListener(graphPanel.getZoomKeyListener());
		graphFrame.addComponentListener(graphPanel
				.getZoomToFitComponentListener());
		graphFrame.pack();
	}

	private final Frame graphFrame;
	private final GraphPanel graphPanel;

	public final Frame getGraphFrame() {
		return graphFrame;
	}

	public final GraphPanel getGraphPanel() {
		return graphPanel;
	}

	@Override
	public void dispose() {
		graphPanel.onDispose();
		super.dispose();
	}

	@Override
	protected void checkSubclass() {
	}
}
