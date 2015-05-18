package ru.bmstu.rk9.rdo.ui.graph;

import java.awt.Frame;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;

import ru.bmstu.rk9.rdo.lib.Simulator;
import ru.bmstu.rk9.rdo.lib.TreeBuilder;

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
		int dptNum = frameInfo.dptNumber;
		String frameName = frameInfo.frameName;

		if (!GraphControl.openedGraphMap.containsValue(dptNum)) {
			TreeBuilder treeBuilder = Simulator.getTreeBuilder();
			treeBuilder.buildTree();
			GraphFrame graphFrame = new GraphFrame(dptNum, 800, 600);
			GraphControl.openedGraphMap.put(dptNum, graphFrame);
			graphFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			graphFrame.setTitle(frameName);
			graphFrame.setLocationRelativeTo(null);
			graphFrame.setVisible(true);

			boolean isFinished;

			Simulator.getTreeBuilder().rwLock.readLock().lock();
			try {
				isFinished = Simulator.getTreeBuilder().dptSimulationInfoMap
						.get(dptNum).isFinished;
			} finally {
				Simulator.getTreeBuilder().rwLock.readLock().unlock();
			}

			if (!isFinished) {
				TimerTask graphUpdateTask = graphFrame
						.getGraphFrameUpdateTimerTask();
				Timer graphUpdateTimer = new Timer();
				graphUpdateTimer.scheduleAtFixedRate(graphUpdateTask, 0, 10);

				TimerTask graphFinTask = graphFrame.getGraphFrameFinTimerTask();
				Timer graphFinTimer = new Timer();
				graphFinTimer.scheduleAtFixedRate(graphFinTask, 0, 10);
			}

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
	}

	public static void openFrameWindow(FrameInfo frameInfo) {
		if (!GraphControl.openedGraphMap.containsKey(frameInfo.dptNumber))
			GraphControl.createFrameWindow(frameInfo);
		else {
			GraphFrame currentFrame = GraphControl.openedGraphMap
					.get(frameInfo.dptNumber);
			if (currentFrame.getState() == Frame.ICONIFIED)
				currentFrame.setState(Frame.NORMAL);
			else if (!currentFrame.isActive())
				currentFrame.setVisible(true);
		}
	}

	public static final Map<Integer, GraphFrame> openedGraphMap = new HashMap<Integer, GraphFrame>();
}
