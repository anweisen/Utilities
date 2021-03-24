package net.anweisen.utilities.commons.config.document;

import com.google.gson.JsonArray;
import net.anweisen.utilities.commons.config.Document;
import net.anweisen.utilities.commons.misc.GsonUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.function.BiConsumer;

/**
 * @author anweisen | https://github.com/anweisen
 * @since 1.0
 */
public class MapDocument extends AbstractDocument {

	private final Map<String, Object> values;

	public MapDocument(Map<String, Object> values) {
		this.values = values;
	}

	public MapDocument() {
		this(new HashMap<>());
	}

	@Override
	public boolean isReadonly() {
		return false;
	}

	@Nonnull
	@Override
	public Document getDocument0(@Nonnull String path) {
		Object value = this.values.computeIfAbsent(path, key -> new HashMap<>());
		if (value instanceof Map) return new MapDocument((Map<String, Object>) value);
		if (value instanceof Document) return (Document) value;
		if (value instanceof String) return new GsonDocument((String) value);
		throw new IllegalStateException("Expected java.util.Map, found " + values.getClass().getName());
	}

	@Override
	public void set0(@Nonnull String path, @Nullable Object value) {
		values.put(path, value);
	}

	@Override
	public void clear0() {
		values.clear();
	}

	@Override
	public void remove0(@Nonnull String path) {
		values.remove(path);
	}

	@Override
	public void write(@Nonnull Writer writer) throws IOException {
		new GsonDocument(values).write(writer);
	}

	@Nonnull
	@Override
	public String toJson() {
		return new GsonDocument(values).toJson();
	}

	@Nullable
	@Override
	public Object getObject(@Nonnull String path) {
		return values.get(path);
	}

	@Nullable
	@Override
	public String getString(@Nonnull String path) {
		return String.valueOf(values.get(path));
	}

	@Override
	public long getLong(@Nonnull String path, long def) {
		try {
			return Long.parseLong(getString(path));
		} catch (Exception ex) {
			return def;
		}
	}

	@Override
	public int getInt(@Nonnull String path, int def) {
		try {
			return Integer.parseInt(getString(path));
		} catch (Exception ex) {
			return def;
		}
	}

	@Override
	public short getShort(@Nonnull String path, short def) {
		try {
			return Short.parseShort(getString(path));
		} catch (Exception ex) {
			return def;
		}
	}

	@Override
	public byte getByte(@Nonnull String path, byte def) {
		try {
			return Byte.parseByte(getString(path));
		} catch (Exception ex) {
			return def;
		}
	}

	@Override
	public float getFloat(@Nonnull String path, float def) {
		try {
			return Float.parseFloat(getString(path));
		} catch (Exception ex) {
			return def;
		}
	}

	@Override
	public double getDouble(@Nonnull String path, double def) {
		try {
			return Double.parseDouble(getString(path));
		} catch (Exception ex) {
			return def;
		}
	}

	@Override
	public boolean getBoolean(@Nonnull String path, boolean def) {
		try {
			if (!contains(path)) return def;
			return Boolean.parseBoolean(getString(path));
		} catch (Exception ex) {
			return def;
		}
	}

	@Nonnull
	@Override
	public List<String> getStringList(@Nonnull String path) {
		Object object = getObject(path);
		if (object == null) return Collections.emptyList();
		if (object instanceof List) return (List<String>) object;
		if (object instanceof Collection) return new ArrayList<>((Collection<String>) object);
		if (object instanceof String) return GsonUtils.convertJsonArrayToStringList(GsonDocument.GSON.fromJson((String) object, JsonArray.class));
		throw new IllegalStateException("Cannot parse " + object.getClass() + " to ");
	}

	@Nullable
	@Override
	public UUID getUUID(@Nonnull String path) {
		try {
			Object object = getObject(path);
			if (object instanceof UUID) return (UUID) object;
			return UUID.fromString(String.valueOf(object));
		} catch (Exception ex) {
			return null;
		}
	}

	@Override
	public boolean contains(@Nonnull String path) {
		return values.containsKey(path);
	}

	@Override
	public int size() {
		return values.size();
	}

	@Nonnull
	@Override
	public Map<String, Object> values() {
		return Collections.unmodifiableMap(values);
	}

	@Nonnull
	@Override
	public Collection<String> keys() {
		return values.keySet();
	}

	@Override
	public void forEach(@Nonnull BiConsumer<? super String, ? super Object> action) {
		values.forEach(action);
	}

}