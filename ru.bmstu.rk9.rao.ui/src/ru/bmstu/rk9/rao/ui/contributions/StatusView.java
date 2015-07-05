package ru.bmstu.rk9.rao.ui.contributions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.RowLayoutFactory;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.themes.ITheme;
import org.eclipse.ui.themes.IThemeManager;

public class StatusView extends ViewPart {
	public static final String ID = "ru.bmstu.rk9.rao.ui.StatusView"; //$NON-NLS-1$

	private static StatusView INSTANCE;

	private ScrolledComposite scrolledComposite;
	private RowLayout scrolledCompositeLayout;

	private Composite composite;

	public static class Element extends Composite {
		private Label label;
		private Text text;

		private Element(Composite parent, String name) {
			super(parent, SWT.NONE);

			setBackground(parent.getBackground());

			GridLayoutFactory.fillDefaults().numColumns(2).applyTo(this);

			label = new Label(this, SWT.NONE);
			label.setText(name + ":");

			GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER)
					.applyTo(label);

			text = new Text(this, SWT.SINGLE | SWT.READ_ONLY | SWT.FLAT);

			GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER)
					.grab(true, false).applyTo(text);
		}
	}

	public static void setValue(String name, int priority, String value) {
		if (value == null) {
			values.remove(name);
			order.remove(name);
		} else {
			values.put(name, value);
			order.put(name, priority);
		}

		if (isInitialized())
			INSTANCE.updateElement(name);
	}

	private void updateElement(String name) {
		Element control = controls.get(name);

		if (control == null) {
			control = createElement(name);
			reorderElements();
		}

		String text = values.get(name);

		if (text == null)
			controls.remove(name).dispose();
		else
			control.text.setText(text);

		updateScrolledCompositeSize();
	}

	private Element createElement(String name) {
		Element control = new Element(composite, name);
		control.label.setFont(editorFont);
		control.text.setFont(editorFont);

		Color background = composite.getBackground();
		control.setBackground(background);
		control.label.setBackground(background);
		control.text.setBackground(background);

		controls.put(name, control);

		return control;
	}

	private void reorderElements() {
		ArrayList<String> list = new ArrayList<String>();
		list.addAll(controls.keySet());
		list.sort((a, b) -> order.get(a).compareTo(order.get(b)));

		Iterator<String> it = list.iterator();
		String element;

		if (it.hasNext())
			element = it.next();
		else
			return;

		while (it.hasNext()) {
			String next = it.next();
			controls.get(next).moveAbove(controls.get(element));
			element = next;
		}

		scrolledComposite.layout(true, true);
	}

	private static Map<String, String> values = new HashMap<String, String>();
	private static Map<String, Integer> order = new HashMap<String, Integer>();
	private Map<String, Element> controls = new HashMap<String, Element>();

	private IThemeManager themeManager;
	private IPropertyChangeListener fontListener;
	private Font editorFont;

	@Override
	public void createPartControl(Composite parent) {
		scrolledComposite = new ScrolledComposite(parent, SWT.H_SCROLL
				| SWT.V_SCROLL);

		themeManager = PlatformUI.getWorkbench().getThemeManager();
		ITheme currentTheme = themeManager.getCurrentTheme();
		FontRegistry fontRegistry = currentTheme.getFontRegistry();
		editorFont = fontRegistry.get(PreferenceConstants.EDITOR_TEXT_FONT);

		composite = new Composite(scrolledComposite, SWT.NONE);
		scrolledCompositeLayout = RowLayoutFactory.fillDefaults()
				.type(SWT.VERTICAL).wrap(false).margins(5, 5).spacing(2)
				.create();
		composite.setLayout(scrolledCompositeLayout);

		fontListener = new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty().equals(
						PreferenceConstants.EDITOR_TEXT_FONT)) {
					Font editorFont = fontRegistry
							.get(PreferenceConstants.EDITOR_TEXT_FONT);

					for (Element e : controls.values()) {
						e.label.setFont(editorFont);
						e.text.setFont(editorFont);
					}
				}
			}
		};
		themeManager.addPropertyChangeListener(fontListener);

		scrolledComposite.setContent(composite);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		INSTANCE = this;

		// late initialization
		parent.getDisplay().asyncExec(() -> {
			scrolledComposite.setBackground(parent.getBackground());
			composite.setBackground(parent.getBackground());

			for (String name : values.keySet()) {
				Element element = createElement(name);
				element.text.setText(values.get(name));
			}
			reorderElements();
			updateScrolledCompositeSize();
		});
	}

	private void updateScrolledCompositeSize() {
		int h = 0, v = 0;
		for (Element e : controls.values()) {
			Point sizeL = e.label.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			Point sizeT = e.text.computeSize(SWT.DEFAULT, SWT.DEFAULT);

			int x = sizeL.x + sizeT.x;
			h = x > h ? x : h;

			v += scrolledCompositeLayout.spacing + sizeL.y > sizeT.y ? sizeL.y
					: sizeT.y;
		}
		h += scrolledCompositeLayout.marginWidth * 2;
		v += scrolledCompositeLayout.marginHeight * 2;

		scrolledComposite.setMinSize(h, v);
		scrolledComposite.layout(true, true);
	}

	@Override
	public void dispose() {
		themeManager.removePropertyChangeListener(fontListener);
		super.dispose();
	}

	private static boolean isInitialized() {
		return INSTANCE != null && !INSTANCE.composite.isDisposed();
	}

	@Override
	public void setFocus() {
	}
}
