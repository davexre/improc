package com.slavi.util;

import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PropertyUtil {
	static final int MAX_VARIABLE_SUBSTITUTIONS = 1000;
	
	/**
	 * Performs variable substitution in <code>str</code> with the values
	 * of keys found in the <code>properties</code>.
	 * <p>
	 * The variable are specified as <b>${VARIABLE_NAME}</b>. Nesting of 
	 * variables is SUPPORTED.
	 * <pre>
	 * Properties p = new Properties();
	 * p.setProperty("VAL0", "Value 0");
	 * p.setProperty("VAL1", "Value 1");
	 * p.setProperty("VAL2", "Value 2");
	 * p.setProperty("UseVal", "1");
	 * p.setProperty("Z", "${ VAL${UseVal} }");
	 * System.out.println(Util.substituteVars("${Z}", p));
	 * The output is: "Value 1"
	 * </pre>
	 * <p>
	 * If no value could be found for a specified key in the properties then
	 * the token <b>${VARIABLE_NAME}</b> is removed and evaluation continues.
	 * <p>
	 * In order to prevent endles loop using cyclic evaluation a maximum of 
	 * {@link #MAX_VARIABLE_SUBSTITUTIONS} will be made. If the number maximum 
	 * of substitutions is exceeded the method returns the result of the last 
	 * evaluation. 
	 *  
	 * @see #mergeProperties(Properties, Map)
	 * @see #makeProperties()
	 */
	public static String substituteVars(String str, Properties properties) {
		if (str == null)
			return "";
		final Pattern vars = Pattern.compile("\\$\\{(((?!\\$\\{)[^}])+)\\}");
		Matcher m = vars.matcher(str);
		int substituionsCount = 0;
		
		while (m.find() && (substituionsCount < MAX_VARIABLE_SUBSTITUTIONS)) {
			substituionsCount++;
			StringBuilder sb = new StringBuilder();
			sb.append(str.substring(0, m.start()));
			String innerToken = m.group(1);
			int modifiersIndex = innerToken.indexOf(":");
			String modifiers = Util.trimNZ(modifiersIndex < 0 ? "" : innerToken.substring(modifiersIndex + 1));
			String varName = Util.trimNZ(modifiersIndex < 0 ? innerToken : innerToken.substring(0, modifiersIndex));
			boolean modified = false;
			if (!"".equals(varName)) {
				String envVal = properties.getProperty(varName); // NO trimNZ() should be used here
				if (envVal != null) {
					String formatStr = "%s";
					int modifiersAsInt;
					try {
						modifiersAsInt = Integer.parseInt(modifiers);
					} catch (Exception e) {
						modifiersAsInt = Integer.MIN_VALUE;
					}
					
					if (modifiersAsInt != Integer.MIN_VALUE) {
						formatStr = "%" + Integer.toString(modifiersAsInt) + "s";
					} else if ("R".equalsIgnoreCase(modifiers)) {
						formatStr = "%" + Integer.toString(innerToken.length() + 3) + "s";
					} else if ("L".equalsIgnoreCase(modifiers)) {
						formatStr = "%-" + Integer.toString(innerToken.length() + 3) + "s";
					}
					
					sb.append(String.format(formatStr, envVal));
					modified = true;
				}
			}
			if (modified) {
				sb.append(str.substring(m.end()));
				str = sb.toString();
				m = vars.matcher(str);
			}
		}
		return str;
	}

	/**
	 * Evaluates the values in the evaluateProperties using 
	 * the {@link #substituteVars(String, Properties)} method and merges
	 * the result into the mergeIntoProperties.
	 * @see #substituteVars(String, Properties)
	 * @see #makeProperties()
	 */
	public static void mergeProperties(Properties mergeIntoProperties, Map<?, ?> evaluateProperties) {
		mergeIntoProperties.putAll(evaluateProperties);
		for (Map.Entry<?, ?> entry : evaluateProperties.entrySet()) {
			String propertyName = (String) entry.getKey();
			String value = (String) entry.getValue();
			value = substituteVars(value, mergeIntoProperties);
			mergeIntoProperties.setProperty(propertyName, value);
		}
	}
	
	/**
	 * Returns a Properties map containitng the merge result 
	 * of System.getProperties() and System.getenv() using 
	 * the {@link #mergeProperties(Properties, Map)}.
	 * @see #substituteVars(String, Properties)
	 * @see #mergeProperties(Properties, Map)
	 */
	public static Properties makeProperties() {
		Properties res = new Properties();
		mergeProperties(res, System.getProperties());
		mergeProperties(res, System.getenv());
		return res;
	}
}
