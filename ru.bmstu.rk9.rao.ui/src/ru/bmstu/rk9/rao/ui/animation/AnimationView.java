package ru.bmstu.rk9.rao.ui.animation;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.State;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.part.ViewPart;

import ru.bmstu.rk9.rao.lib.animation.AnimationFrame;
import ru.bmstu.rk9.rao.ui.notification.RealTimeSubscriberManager;
import ru.bmstu.rk9.rao.ui.simulation.SimulationModeDispatcher;
import ru.bmstu.rk9.rao.ui.simulation.SimulationSynchronizer.ExecutionMode;

public class AnimationView extends ViewPart {

	public static int getFrameListSize() {
		return lastListWidth;
	}

	private static Composite parent;

	private static ScrolledComposite scrolledComposite;

	private static List frameList;
	private static GridData listSize;

	private static Canvas frameView;
	private static GridData frameSize;

	public static void setFrameSize(int width, int height) {
		frameSize.widthHint = width;
		frameSize.heightHint = height;
		frameSize.minimumWidth = width;
		frameSize.minimumHeight = height;

		scrolledComposite.setMinSize(width + 6, height + 6);
		scrolledComposite.layout(true, true);
	}

	private static void setCurrentFrame(AnimationFrame frame) {
		currentFrame = frame;

		int[] backgroundData = frame.getBackgroundData();

		setFrameSize(backgroundData[0], backgroundData[1]);

		frameView.redraw();
	}

	private static AnimationContextSWT animationContext;

	private static java.util.List<AnimationFrame> frames;
	private static AnimationFrame currentFrame;

	private static int selectedFrameIndex = 0;

	private static void initializeFrames() {
		frameList.removeAll();

		if (!frames.isEmpty()) {
			if (selectedFrameIndex >= frames.size())
				selectedFrameIndex = 0;

			for (AnimationFrame frame : frames)
				frameList.add(frame.getName());

			frameList.setEnabled(true);
			frameList.setSelection(selectedFrameIndex);

			setCurrentFrame(frames.get(selectedFrameIndex));
		} else {
			frameList.add("No frames");
			frameList.setEnabled(false);

			setFrameSize(0, 0);
		}
	}

	public static void initialize(java.util.List<AnimationFrame> frames) {
		isInitialized = true;
		selectedFrameIndex = 0;

		animationContext = new AnimationContextSWT(PlatformUI.getWorkbench()
				.getDisplay());

		AnimationView.frames = new ArrayList<AnimationFrame>(frames);

		if (isInitialized())
			initializeFrames();

		ExecutionMode currentMode = SimulationModeDispatcher.getMode();
		setAnimationEnabled(currentMode != ExecutionMode.NO_ANIMATION);
	}

	public static void deinitialize() {
		if (frames != null)
			for (AnimationFrame frame : frames)
				animationContext.prepareFrame(frame);

		isInitialized = false;

		if (isInitialized())
			frameView.redraw();
	}

	private static volatile boolean animationEnabled = true;

	public static void setAnimationEnabled(boolean state) {
		animationEnabled = state;
	}

	public static final Runnable realTimeUpdateRunnable = new Runnable() {
		@Override
		public void run() {
			if (isInitialized() && animationEnabled) {
				animationContext.prepareFrame(currentFrame);
				frameView.redraw();
			}
		}
	};

	private static PaintListener painter = new PaintListener() {
		@Override
		public void paintControl(PaintEvent e) {
			if (canDraw()) {
				if (animationEnabled || !isInitialized)
					animationContext.drawFrame(e.gc, currentFrame);
			}
		}
	};

	private static SelectionListener frameListListener = new SelectionListener() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			int index = ((List) e.widget).getSelectionIndex();
			setCurrentFrame(frames.get(index));
			selectedFrameIndex = index;
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
		}
	};

	private static int lastListWidth = InstanceScope.INSTANCE.getNode(
			"ru.bmstu.rk9.rao.ui").getInt("AnimationViewFrameListSize", 120);

	private static Listener sashListener = new Listener() {
		@Override
		public void handleEvent(Event event) {
			Rectangle listRectangle = frameList.getBounds();
			int newListHint = event.x + listSize.widthHint
					- listRectangle.width - listRectangle.x;

			if (newListHint > 20) {
				listSize.widthHint = newListHint;
				lastListWidth = newListHint;
				parent.layout();
			} else
				event.doit = false;
		}
	};

	@Override
	public void createPartControl(Composite parent) {
		ICommandService service = (ICommandService) PlatformUI.getWorkbench()
				.getService(ICommandService.class);

		Command command = service
				.getCommand("ru.bmstu.rk9.rao.ui.runtime.setExecutionMode");
		State state = command.getState("org.eclipse.ui.commands.radioState");

		animationEnabled = !state.getValue().equals("NA");

		AnimationView.parent = parent;

		GridLayoutFactory.fillDefaults().numColumns(3).spacing(0, 0)
				.extendedMargins(1, 1, 2, 1).applyTo(parent);

		frameList = new List(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);

		listSize = GridDataFactory.fillDefaults().grab(false, true)
				.hint(lastListWidth, SWT.DEFAULT).create();
		frameList.setLayoutData(listSize);

		frameList.add("No frames");
		frameList.setEnabled(false);

		frameList.addSelectionListener(frameListListener);

		Sash sash = new Sash(parent, SWT.VERTICAL);

		GridDataFactory.fillDefaults().grab(false, true).applyTo(sash);

		sash.addListener(SWT.Selection, sashListener);

		GridDataFactory.fillDefaults().grab(false, true).hint(4, SWT.DEFAULT)
				.applyTo(sash);

		scrolledComposite = new ScrolledComposite(parent, SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.BORDER);

		GridDataFactory.fillDefaults().grab(true, true)
				.applyTo(scrolledComposite);

		Composite scrolledCompositeInner = new Composite(scrolledComposite,
				SWT.NONE);
		GridLayoutFactory.fillDefaults().applyTo(scrolledCompositeInner);

		Composite frameViewComposite = new Composite(scrolledCompositeInner,
				SWT.NONE);

		frameSize = GridDataFactory.fillDefaults()
				.align(SWT.CENTER, SWT.CENTER).grab(true, true).create();
		frameViewComposite.setLayoutData(frameSize);
		frameViewComposite.setLayout(new FillLayout());
		frameViewComposite.setBackgroundMode(SWT.INHERIT_NONE);

		frameView = new Canvas(frameViewComposite, SWT.BORDER
				| SWT.NO_BACKGROUND);
		frameView.addPaintListener(painter);

		scrolledComposite.setContent(scrolledCompositeInner);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		setFrameSize(0, 0);

		if (frames != null)
			initializeFrames();

		realTimeSubscriberManager.initialize(Arrays
				.asList(realTimeUpdateRunnable));
	}

	@Override
	public void setFocus() {
	}

	private static boolean isInitialized = false;

	private static boolean isInitialized() {
		return frameList != null && !frameList.isDisposed()
				&& frameView != null && !frameView.isDisposed();
	}

	private static boolean canDraw() {
		return isInitialized() && animationContext != null
				&& currentFrame != null;
	}

	private final RealTimeSubscriberManager realTimeSubscriberManager = new RealTimeSubscriberManager();

	@Override
	public void dispose() {
		realTimeSubscriberManager.deinitialize();
		super.dispose();
	}
}
