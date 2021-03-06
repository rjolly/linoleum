package linoleum.html;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import javax.swing.text.*;
import javax.swing.text.html.CSS;

public class StyleSheet extends javax.swing.text.html.StyleSheet {
	private Class<?> cssValueClass;
	private Class<?> fontSizeClass;
	private Class<?> colorValueClass;
	private Class<?> viewAttributeSetClass;
	private Method getValueMethod;
	private Method toStyleConstantsMethod;
	private Method styleConstantsKeyToCSSKeyMethod;
	private Method getInternalCSSValueMethod;
	private Method getAttributesMethod;
	private Method getSizeMapMethod;
	private Field defaultFontSizeField;
	private Field cssField;

	public StyleSheet() {
		try {
			cssValueClass = Class.forName("javax.swing.text.html.CSS$CssValue");
			fontSizeClass = Class.forName("javax.swing.text.html.CSS$FontSize");
			colorValueClass = Class.forName("javax.swing.text.html.CSS$ColorValue");
			viewAttributeSetClass = Class.forName("javax.swing.text.html.StyleSheet$ViewAttributeSet");

			getValueMethod = fontSizeClass.getDeclaredMethod("getValue", AttributeSet.class, javax.swing.text.html.StyleSheet.class);
			toStyleConstantsMethod = cssValueClass.getDeclaredMethod("toStyleConstants", StyleConstants.class, View.class);
			styleConstantsKeyToCSSKeyMethod = CSS.class.getDeclaredMethod("styleConstantsKeyToCSSKey", StyleConstants.class);
			getInternalCSSValueMethod = CSS.class.getDeclaredMethod("getInternalCSSValue", CSS.Attribute.class, String.class);
			getAttributesMethod = viewAttributeSetClass.getSuperclass().getDeclaredMethod("getAttributes");
			getSizeMapMethod = javax.swing.text.html.StyleSheet.class.getDeclaredMethod("getSizeMap");

			defaultFontSizeField = javax.swing.text.html.StyleSheet.class.getDeclaredField("DEFAULT_FONT_SIZE");
			cssField = javax.swing.text.html.StyleSheet.class.getDeclaredField("css");

			getValueMethod.setAccessible(true);
			toStyleConstantsMethod.setAccessible(true);
			styleConstantsKeyToCSSKeyMethod.setAccessible(true);
			getInternalCSSValueMethod.setAccessible(true);
			getAttributesMethod.setAccessible(true);
			getSizeMapMethod.setAccessible(true);

			sizeMap = (int[]) getSizeMapMethod.invoke(this);

			defaultFontSizeField.setAccessible(true);
			cssField.setAccessible(true);

			defaultFontSize = defaultFontSizeField.getInt(null);
			css = (CSS) cssField.get(this);
		} catch (final ReflectiveOperationException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public AttributeSet getViewAttributes(View v) {
		return new ViewAttributeSet(super.getViewAttributes(v), v);
	}

	class ViewAttributeSet implements AttributeSet {
		private final AttributeSet content;
		private final View host;

		ViewAttributeSet(final AttributeSet content, final View host) {
			this.content = content;
			this.host = host;
		}

		@Override
		public int getAttributeCount() {
			return content.getAttributeCount();
		}

		@Override
		public boolean isDefined(final Object attrName) {
			return content.isDefined(attrName);
		}

		@Override
		public boolean isEqual(final AttributeSet attr) {
			return content.isEqual(attr);
		}

		@Override
		public AttributeSet copyAttributes() {
			return content.copyAttributes();
		}

		@Override
		public Object getAttribute(final Object key) {
			if (key instanceof StyleConstants) {
				final Object cssKey = styleConstantsKeyToCSSKey((StyleConstants)key);
				if (cssKey != null) {
					final Object value = doGetAttribute(cssKey);
					if (isInstanceOf(cssValueClass, value)) {
						return toStyleConstants(value, (StyleConstants)key, host);
					}
				}
			}
			return doGetAttribute(key);
		}

		private Object superGetAttribute(final Object key) {
			AttributeSet[] as = null;
			try {
				as = (AttributeSet[]) getAttributesMethod.invoke(content);
			} catch (final ReflectiveOperationException ex) {
				ex.printStackTrace();
			}
			if (as != null) for (int i = 0; i < as.length; i++) {
				final Object o = as[i].getAttribute(key);
				if (o != null) {
					return o;
				}
			}
			return null;
		}

		private Object doGetAttribute(final Object key) {
			final Object value = superGetAttribute(key);
			if (key instanceof CSS.Attribute) {
				if (isInstanceOf(colorValueClass, value)) {
					String str = value.toString();
					if (str.startsWith("#") && str.length() == 4) {
						final String r = str.substring(1, 2);
						final String v = str.substring(2, 3);
						final String b = str.substring(3, 4);
						str = String.format("#%s%s%s%s%s%s", r, r, v, v, b, b);
						return getInternalCSSValue((CSS.Attribute) key, str);
					}
				} else if (isInstanceOf(fontSizeClass, value)) {
					String str = value.toString();
					if (str.endsWith("px")) {
						str = str.substring(0, str.length() - 2) + "pt";
						return getInternalCSSValue((CSS.Attribute) key, str);
					}
				} else if (value == null) {
					if (((CSS.Attribute) key).isInherited()) {
						final AttributeSet parent = getResolveParent();
						if (parent != null) {
							return getAttribute(parent, key);
						}
					}
				}
			} else if (key == StyleConstants.FontSize) {
				if (value == null) {
					return sizeMap[defaultFontSize];
				}
			}
			return value;
		}

		private Object getAttribute(final AttributeSet parent, final Object key) {
			final Object value = parent.getAttribute(key);
			if (isInstanceOf(fontSizeClass, value)) {
				final String str = getValue(value, parent, StyleSheet.this).toString();
				return getInternalCSSValue(CSS.Attribute.FONT_SIZE, str);
			}
			return value;
		}

		@Override
		public Enumeration<?> getAttributeNames() {
			return content.getAttributeNames();
		}

		@Override
		public boolean containsAttribute(final Object name, final Object value) {
			return content.containsAttribute(name, value);
		}

		@Override
		public boolean containsAttributes(final AttributeSet attributes) {
			return content.containsAttributes(attributes);
		}

		@Override
		public AttributeSet getResolveParent() {
			return content.getResolveParent();
		}	
	}

	private boolean isInstanceOf(final Class<?> clazz, final Object value) {
		return value == null?false:clazz.isAssignableFrom(value.getClass());
	}

	private Integer getValue(final Object value, final AttributeSet attrs, final javax.swing.text.html.StyleSheet ss) {
		Integer res = null;
		try {
			res = (Integer) getValueMethod.invoke(value, attrs, ss);
		} catch (final ReflectiveOperationException ex) {
			ex.printStackTrace();
		}
		return res;
	}

	private Object toStyleConstants(final Object value, final StyleConstants key, final View v) {
		Object res = null;
		try {
			res = toStyleConstantsMethod.invoke(value, key, v);
		} catch (final ReflectiveOperationException ex) {
			ex.printStackTrace();
		}
		return res;
	}

	private CSS.Attribute styleConstantsKeyToCSSKey(final StyleConstants sc) {
		CSS.Attribute res = null;
		try {
			res = (CSS.Attribute) styleConstantsKeyToCSSKeyMethod.invoke(css, sc);
		} catch (final ReflectiveOperationException ex) {
			ex.printStackTrace();
		}
		return res;
	}

	private Object getInternalCSSValue(final CSS.Attribute key, final String value) {
		Object res = null;
		try {
			res = getInternalCSSValueMethod.invoke(css, key, value);
		} catch (final ReflectiveOperationException ex) {
			ex.printStackTrace();
		}
		return res;
	}

	private int sizeMap[];
	private int defaultFontSize;
	private CSS css;
}
