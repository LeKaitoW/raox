package ru.bmstu.rk9.rao.ui.console;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.themes.ITheme;
import org.eclipse.ui.themes.IThemeManager;

import ru.bmstu.rk9.rao.lib.logger.Logger;
import ru.bmstu.rk9.rao.lib.logger.LoggerSubscriberManager.LoggerSubscriberInfo;
import ru.bmstu.rk9.rao.lib.logger.LoggerSubscriberManager;
import ru.bmstu.rk9.rao.lib.notification.Subscriber;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator;

public class ConsoleView extends ViewPart {
	public static final String ID = "ru.bmstu.rk9.rao.ui.ConsoleView"; //$NON-NLS-1$

	private static StyledText styledText;

	private static String text = "";

	@Override
	public void createPartControl(Composite parent) {
		styledText = new StyledText(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		styledText.setAlwaysShowScrollBars(false);
		redrawText();
		styledText.setEditable(false);
		styledText.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, true));
		styledText.setLeftMargin(2);
		styledText.setTopMargin(5);

		Menu popupMenu = new Menu(styledText);
		MenuItem copy = new MenuItem(popupMenu, SWT.CASCADE);
		copy.setText("Copy\tCtrl+C");
		copy.setAccelerator(SWT.CTRL + 'C');
		copy.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				styledText.invokeAction(ST.COPY);
			}
		});
		styledText.setMenu(popupMenu);

		registerTextFontUpdateListener();
		updateTextFont();

		initializeSubscribers();
	}

	@Override
	public void dispose() {
		deinitializeSubscribers();
		themeManager.removePropertyChangeListener(fontListener);
		super.dispose();
	}

	private final void initializeSubscribers() {
		loggerSubscriberManager.initialize(
				Arrays.asList(new LoggerSubscriberInfo(loggingSubscriber, Logger.NotificationCategory.NEW_LOG_ENTRY)));
	}

	private final void deinitializeSubscribers() {
		loggerSubscriberManager.deinitialize();
	}

	private LoggerSubscriberManager loggerSubscriberManager = new LoggerSubscriberManager();

	private final Subscriber loggingSubscriber = new Subscriber() {
		@Override
		public void fireChange() {
			Logger logger = CurrentSimulator.getLogger();
			String line;

			while ((line = logger.poll()) != null) {
				addLine(line);
			}
		}
	};

	private void updateTextFont() {
		IThemeManager themeManager = PlatformUI.getWorkbench().getThemeManager();
		ITheme currentTheme = themeManager.getCurrentTheme();
		FontRegistry fontRegistry = currentTheme.getFontRegistry();
		styledText.setFont(fontRegistry.get(PreferenceConstants.EDITOR_TEXT_FONT));
	}

	public static void clearConsoleText() {
		text = "";
		redrawText();
	}

	public static void addLine(String line) {
		text = text + line + "\n";
		redrawText();
	}

	public static void appendText(String add) {
		text = text + add;
		redrawText();
	}

	public static void printStackTrace(Throwable e) {
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		e.printStackTrace(printWriter);
		appendText(stringWriter.toString());
	}

	// При частом вызове log в модели
	// Окно лога не более не скроллится
	public static void redrawText() {
		if (styledText != null && !styledText.isDisposed())
			styledText.getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					styledText.setText(text);
				}
			});
	}

	private static IThemeManager themeManager;
	private static IPropertyChangeListener fontListener;

	private void registerTextFontUpdateListener() {
		themeManager = PlatformUI.getWorkbench().getThemeManager();
		fontListener = new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				switch (event.getProperty()) {
				case PreferenceConstants.EDITOR_TEXT_FONT:
					updateTextFont();
					break;
				}
			}
		};
		themeManager.addPropertyChangeListener(fontListener);
	}

	@Override
	public void setFocus() {
	}
}
