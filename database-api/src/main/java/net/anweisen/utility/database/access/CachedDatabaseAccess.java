package net.anweisen.utility.database.access;

import net.anweisen.utility.database.Database;
import net.anweisen.utility.database.exception.DatabaseException;
import net.anweisen.utility.document.Document;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

/**
 * @author anweisen | https://github.com/anweisen
 * @since 1.1
 */
public class CachedDatabaseAccess<V> extends DirectDatabaseAccess<V> {

	protected final Map<String, V> cache = new ConcurrentHashMap<>();

	public CachedDatabaseAccess(@Nonnull Database database, @Nonnull DatabaseAccessConfig config, @Nonnull BiFunction<? super Document, ? super String, ? extends V> mapper) {
		super(database, config, mapper);
	}

	@Nonnull
	public static CachedDatabaseAccess<String> forString(@Nonnull Database database, @Nonnull DatabaseAccessConfig config) {
		return new CachedDatabaseAccess<>(database, config, Document::getString);
	}

	@Nonnull
	public static CachedDatabaseAccess<Integer> forInt(@Nonnull Database database, @Nonnull DatabaseAccessConfig config) {
		return new CachedDatabaseAccess<>(database, config, Document::getInt);
	}

	@Nonnull
	public static CachedDatabaseAccess<Long> forLong(@Nonnull Database database, @Nonnull DatabaseAccessConfig config) {
		return new CachedDatabaseAccess<>(database, config, Document::getLong);
	}

	@Nonnull
	public static CachedDatabaseAccess<Double> forDouble(@Nonnull Database database, @Nonnull DatabaseAccessConfig config) {
		return new CachedDatabaseAccess<>(database, config, Document::getDouble);
	}

	@Nonnull
	public static CachedDatabaseAccess<Document> forDocument(@Nonnull Database database, @Nonnull DatabaseAccessConfig config) {
		return new CachedDatabaseAccess<>(database, config, Document::getDocument);
	}

	@Nullable
	@Override
	public V getValue(@Nonnull String key) throws DatabaseException {
		V value = cache.get(key);
		if (value != null) return value;

		value = super.getValue(key);
		cache.put(key, value);
		return value;
	}

	@Nonnull
	@Override
	public V getValue(@Nonnull String key, @Nonnull V def) throws DatabaseException {
		V value = cache.get(key);
		if (value != null) return value;

		value = super.getValue(key, def);
		cache.put(key, value);
		return value;
	}

	@Nonnull
	@Override
	public Optional<V> getValueOptional(@Nonnull String key) throws DatabaseException {
		V cached = cache.get(key);
		if (cached != null) return Optional.of(cached);

		return super.getValueOptional(key);
	}

	@Override
	public void setValue(@Nonnull String key, @Nullable V value) throws DatabaseException {
		cache.put(key, value);
		super.setValue(key, value);
	}

}
