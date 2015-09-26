package ru.bmstu.rk9.rao.ui.graph;

import java.awt.Frame;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JFrame;

import org.eclipse.swt.graphics.Rectangle;
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
		Rectangle monitorBounds = PlatformUI.getWorkbench().getDisplay()
				.getBounds();
		monitorAspectRatio = (double) monitorBounds.width
				/ monitorBounds.height;

		int dptNum = frameInfo.dptNumber;
		String frameName = frameInfo.frameName;

		int frameWidth = setWidth(monitorBounds, 1.2);
		int frameHeight = setHeight(monitorBounds, 0.8);

		GraphView graphFrame = new GraphView(dptNum, frameWidth, frameHeight);

		GraphControl.openedGraphMap.put(dptNum, graphFrame);
		graphFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		graphFrame.setTitle(frameName);
		graphFrame.setLocationRelativeTo(null);
		graphFrame.setVisible(true);

		graphFrame.addWindowListener(new WindowListener() {
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
			GraphView currentFrame = GraphControl.openedGraphMap
					.get(frameInfo.dptNumber);
			if (currentFrame.getState() == Frame.ICONIFIED)
				currentFrame.setState(Frame.NORMAL);
			else if (!currentFrame.isActive())
				currentFrame.setVisible(true);
		}
	}

	public static final Map<Integer, GraphView> openedGraphMap = new HashMap<Integer, GraphView>();

	private static double monitorAspectRatio;

	public static int setWidth(Rectangle r, double relativeWidth) {
		return (int) (r.width * relativeWidth / GraphControl.monitorAspectRatio);
	}

	public static int setHeight(Rectangle r, double relativeHeight) {
		return (int) (r.height * relativeHeight);
	}
}
