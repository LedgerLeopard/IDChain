package com.ledgerleopard.sorvin.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import ru.bscmsk.fingerly.utils.IPrefsStore;

import java.util.Set;

/**
 * Created by sergeybrazhnik on 29.01.18.
 */

public class SharedPreferenceStorage implements IPrefsStore {

	public final String SP_NAME = SharedPreferenceStorage.class.getCanonicalName();
	private final SharedPreferences prefs;
	private final SharedPreferences.Editor editor;

	private SharedPreferenceStorage(Context context ) {
		prefs = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
		editor = prefs.edit();
	}

	private static SharedPreferenceStorage instance;
	public static SharedPreferenceStorage getInstanse( Context context) {
		if ( instance == null )
			instance = new SharedPreferenceStorage(context);

		return instance;
	}

	@Override
	public void add(String key, Object value) {
		if (TextUtils.isEmpty(key) || value == null)
			return;

		if (value instanceof String)
			editor.putString(key, (String) value).apply();

		if (value instanceof Integer)
			editor.putInt(key, (Integer) value).apply();

		if (value instanceof Long)
			editor.putLong(key, (Long) value).apply();

		if (value instanceof Boolean)
			editor.putBoolean(key, (Boolean) value).apply();

		if (value instanceof Float)
			editor.putFloat(key, (Float) value).apply();

		if (value instanceof Set ){
			try {
				Set<String> value1 = (Set<String>) value;
				editor.putStringSet(key, value1);
			} catch (Exception e) {
				throw new RuntimeException("Set generic param should be String class");
			}
		}

		editor.commit();
	}

	@Override
	public <T> T get(String key, Class<T> tClass, T def) {
		Class<T> newClass = null;
		if(TextUtils.isEmpty(key) || tClass == null)
			return def;

		if (tClass.equals(String.class))
			return (T) prefs.getString(key, (String) def);

		if (tClass.equals(Integer.class))
			return (T) Integer.valueOf(prefs.getInt(key, (Integer) def));

		if (tClass.equals(Long.class))
			return (T) Long.valueOf(prefs.getLong(key, (Long) def));

		if (tClass.equals(Boolean.class))
			return (T) Boolean.valueOf(prefs.getBoolean(key, (Boolean) def));

		if (tClass.equals(Boolean.class))
			return (T) Float.valueOf(prefs.getFloat(key, (Float) def));

		if (tClass.equals(Set.class))
			return (T)prefs.getStringSet(key, (Set<String>) def);

		return def;
	}

	@Override
	public boolean contains(String key) {
		return prefs.contains(key);
	}

	@Override
	public void remove(String key) {
		prefs.edit().remove(key).apply();
	}
}
