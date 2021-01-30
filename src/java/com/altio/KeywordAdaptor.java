package com.altio;

import clojure.lang.Keyword;

import org.stringtemplate.v4.Interpreter;
import org.stringtemplate.v4.ModelAdaptor;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.misc.STNoSuchPropertyException;

import java.io.PrintWriter;
import java.util.Map;

/**
 * Tweaked version of {@link org.stringtemplate.v4.misc.MapModelAdaptor} to add support for Clojure's keyword.
 */
public class KeywordAdaptor implements ModelAdaptor<Map<?, ?>> {

	private final PrintWriter out;

	public KeywordAdaptor(PrintWriter out) {
		this.out = out;
	}

	@Override
	public Object getProperty(Interpreter interp, ST self, Map<?, ?> model, Object property, String propertyName)
			throws STNoSuchPropertyException {
		Object value;

		Keyword kw = Keyword.intern(null, propertyName);
		if (containsKey(model, kw)) {
			return model.get(kw);
		}

		if (property == null) value = getDefaultValue(model);
		else if (containsKey(model, property)) value = model.get(property);
		else if (containsKey(model, propertyName)) { // if can't find the key, try toString version
			value = model.get(propertyName);
		} else if (property.equals("keys")) value = model.keySet();
		else if (property.equals("values")) value = model.values();
		else value = getDefaultValue(model); // not found, use default
		if (value == STGroup.DICT_KEY) {
			value = property;
		}
		return value;
	}

	private static Boolean containsKey(Map<?, ?> map, Object key) {
		try {
			return map.containsKey(key);
		} catch (ClassCastException ex) {
			// Map.containsKey is allowed to throw ClassCastException if the key
			// cannot be compared to keys already in the map.
			return false;
		}
	}

	private static Object getDefaultValue(Map<?, ?> map) {
		try {
			return map.get(STGroup.DEFAULT_KEY);
		} catch (ClassCastException ex) {
			// Map.containsKey is allowed to throw ClassCastException if the key
			// cannot be compared to keys already in the map.
			return false;
		}
	}
}
