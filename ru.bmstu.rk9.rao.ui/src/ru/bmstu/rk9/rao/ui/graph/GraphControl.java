package ru.bmstu.rk9.rao.ui.graph;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

public class GraphControl {
	public static class FrameInfo {
		public int dptNumber;
		public String frameName;

		public FrameInfo(int dptNum, String frameName) {
			this.dptNumber = dptNum;
			this.frameName = frameName;
		}
	}

	private static void createFrameWindow(FrameInfo frameInfo) {
		Display display = PlatformUI.getWorkbench().getDisplay();

		int dptNum = frameInfo.dptNumber;
		String frameName = frameInfo.frameName;

		GraphShell graphShell = new GraphShell(display, dptNum);
		graphShell.open();

		GraphControl.openedGraphMap.put(dptNum, graphShell);
		graphShell.setText(frameName);
		graphShell.getGraphFrame().setVisible(true);

		graphShell.getGraphFrame().addWindowListener(new WindowListener() {
			@Override
			public void windowOpened(WindowEvent e) {
			}

			@Override
			public void windowIconified(WindowEvent e) {
			}

			@Override
			public void windowDeiconified(WindowEvent e) {
			}

			@Override
			public void windowDeactivated(WindowEvent e) {
			}

			@Override
			public void windowClosing(WindowEvent e) {
			}

			@Override
			public void windowClosed(WindowEvent e) {
				GraphControl.openedGraphMap.remove(dptNum);
			}

			@Override
			public void windowActivated(WindowEvent e) {
			}
		});
	}

	public static void openFrameWindow(FrameInfo frameInfo) {
		if (!GraphControl.openedGraphMap.containsKey(frameInfo.dptNumber)) {
			GraphControl.createFrameWindow(frameInfo);
		} else {
			GraphShell currentGraphShell = GraphControl.openedGraphMap.get(frameInfo.dptNumber);
			currentGraphShell.forceActive();
		}
	}

	private static final Map<Integer, GraphShell> openedGraphMap = new HashMap<Integer, GraphShell>();

	public static final Map<Integer, GraphShell> getOpenedGraphMap() {
		return openedGraphMap;
	}
}
