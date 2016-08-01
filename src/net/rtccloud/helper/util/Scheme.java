package net.rtccloud.helper.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

/**
 * Helper class used to wrap URL Scheme data.
 * 
 * @author Simon Marquis <simon.marquis@sightcall.com>
 */
public class Scheme {

	/** The raw uri String value */
	private String uri;
	/** Parameters needed by the app */
	private Map<Parameter, String> parameters = new HashMap<Parameter, String>();
	/** Parameters already used */
	private Set<Parameter> parametersUsed = new HashSet<Parameter>();
	/** Ignored parameters */
	private Map<String, String> parametersUnknown = new HashMap<String, String>();

	/**
	 * Parameters that can be send through url scheme.
	 * 
	 * @author Simon Marquis <simon.marquis@sightcall.com>
	 */
	public enum Parameter {
		/** APP_ID */
		APP_ID("appid"),
		/** USER_ID */
		USER_ID("userid"),
		/** WILL_USE_AUTH */
		WILL_USE_AUTH("willuseauth"),
		/** AUTH_URL */
		AUTH_URL("authurl"),
		/** USER_TYPE */
		USER_TYPE("usertype"),
		/** DISPLAY_NAME */
		DISPLAY_NAME("displayname"),
		/** CALLEE_ID */
		CALLEE_ID("calleeid");

		/** Associated key */
		public final String key;

		/**
		 * Default constructor
		 * 
		 * @param key
		 */
		private Parameter(String key) {
			this.key = key;
		}

		/**
		 * @param str
		 *            The String value of the parameter
		 * @return The Parameter that matches, <code>null</code> otherwise.
		 */
		public static Parameter fromString(String str) {
			Parameter[] values = Parameter.values();
			for (int i = 0; i < values.length; i++) {
				if (values[i].key.equals(str)) {
					return values[i];
				}
			}
			return null;
		}
	}

	/**
	 * Initialize the Scheme from the provided {@link Intent}
	 * 
	 * @param intent
	 */
	public void init(Intent intent) {
		Uri data = (intent == null ? null : intent.getData());
		if (data == null) {
			return;
		}

		this.uri = data.toString();
		Set<String> params = data.getQueryParameterNames();
		if (params != null && !params.isEmpty()) {
			for (String param : params) {
				Parameter parameter = Parameter.fromString(param);
				if (parameter != null) {
					this.parameters.put(parameter, data.getQueryParameter(param));
				} else {
					this.parametersUnknown.put(param, data.getQueryParameter(param));
				}
			}
		}
	}

	/**
	 * Returns the value of the specified {@link Parameter}.
	 * 
	 * @param p
	 * @return the value of the specified {@link Parameter} or <code>null</code> if it's not found.
	 */
	public String get(Parameter p) {
		return get(p, null);
	}

	/**
	 * Returns the value of the specified {@link Parameter}.
	 * 
	 * @param p
	 * @param defaultValue
	 * @return the value of the specified {@link Parameter} or the provided default value if it's not found.
	 */
	public String get(Parameter p, String defaultValue) {
		return this.parameters.containsKey(p) ? this.parameters.get(p) : defaultValue;
	}

	/**
	 * @param p
	 * @return <code>true</code> if the provided {@link Parameter} exists, <code>false</code> otherwise.
	 */
	public boolean contains(Parameter p) {
		return this.parameters.containsKey(p);
	}

	/**
	 * @param p
	 * @return <code>true</code> if the {@link Parameter} can be used, in other words, if it has not already been used, <code>false</code> otherwise.
	 */
	public boolean canUse(Parameter p) {
		return !this.parametersUsed.contains(p);
	}

	/**
	 * Flags the {@link Parameter} as used.
	 * 
	 * @param p
	 */
	public void use(Parameter p) {
		this.parametersUsed.add(p);
	}

	/**
	 * Reset all data.
	 */
	public void reset() {
		this.uri = null;
		this.parameters.clear();
		this.parametersUsed.clear();
		this.parametersUnknown.clear();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Scheme");
		if (TextUtils.isEmpty(this.uri)) {
			sb.append(this.uri);
		}

		Set<Entry<Parameter, String>> entrySet = this.parameters == null ? null : this.parameters.entrySet();
		if (entrySet == null || entrySet.isEmpty()) {
			//sb.append("\n� {empty}");
		} else {
			for (Entry<Parameter, String> entry : entrySet) {
				//sb.append("\n� ").append(entry.getKey().key).append("=").append(entry.getValue());
			}
		}

		Set<Entry<String, String>> entrySet2 = this.parametersUnknown == null ? null : this.parametersUnknown.entrySet();
		if (entrySet2 == null || entrySet2.isEmpty()) {
			sb.append("\n~ {empty}");
		} else {
			for (Entry<String, String> entry2 : entrySet2) {
				sb.append("\n~ ").append(entry2.getKey()).append("=").append(entry2.getValue());
			}
		}

		return sb.toString();
	}

}
