package net.anweisen.utilities.commons.config;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author anweisen | https://github.com/anweisen
 * @since 1.0
 */
public interface Propertyable {

	@Nullable
	Object getObject(@Nonnull String path);

	@Nonnull
	Object getObject(@Nonnull String path, @Nonnull Object def);

	@Nonnull
	@SuppressWarnings("unchecked")
	default <T, O> Optional<T> getOptional(@Nonnull String key, @Nonnull BiFunction<O, ? super String, ? extends T> extractor) {
		return Optional.ofNullable(extractor.apply((O) this, key));
	}

	@Nullable
	String getString(@Nonnull String path);

	@Nonnull
	String getString(@Nonnull String path, @Nonnull String def);

	char getChar(@Nonnull String path);

	char getChar(@Nonnull String path, char def);

	long getLong(@Nonnull String path);

	long getLong(@Nonnull String path, long def);

	int getInt(@Nonnull String path);

	int getInt(@Nonnull String path, int def);

	short getShort(@Nonnull String path);

	short getShort(@Nonnull String path, short def);

	byte getByte(@Nonnull String path);

	byte getByte(@Nonnull String path, byte def);

	float getFloat(@Nonnull String path);

	float getFloat(@Nonnull String path, float def);

	double getDouble(@Nonnull String path);

	double getDouble(@Nonnull String path, double def);

	boolean getBoolean(@Nonnull String path);

	boolean getBoolean(@Nonnull String path, boolean def);

	@Nonnull
	List<String> getStringList(@Nonnull String path);

	@Nonnull
	String[] getStringArray(@Nonnull String path);

	@Nonnull
	<E extends Enum<E>> List<E> getEnumList(@Nonnull String path, @Nonnull Class<E> classOfEnum);

	@Nonnull
	List<UUID> getUUIDList(@Nonnull String path);

	@Nonnull
	List<Character> getCharacterList(@Nonnull String path);

	@Nonnull
	List<Byte> getByteList(@Nonnull String path);

	@Nonnull
	List<Short> getShortList(@Nonnull String path);

	@Nonnull
	List<Integer> getIntegerList(@Nonnull String path);

	@Nonnull
	List<Long> getLongList(@Nonnull String path);

	@Nonnull
	List<Float> getFloatList(@Nonnull String path);

	@Nonnull
	List<Double> getDoubleList(@Nonnull String path);

	@Nullable
	UUID getUUID(@Nonnull String path);

	@Nonnull
	UUID getUUID(@Nonnull String path, @Nonnull UUID def);

	@Nullable
	<E extends Enum<E>> E getEnum(@Nonnull String path, @Nonnull Class<E> classOfEnum);

	@Nonnull
	<E extends Enum<E>> E getEnum(@Nonnull String path, @Nonnull E def);

	@Nullable
	Class<?> getClass(@Nonnull String path);

	@Nonnull
	Class<?> getClass(@Nonnull String path, @Nonnull Class<?> def);

	boolean isList(@Nonnull String path);

	boolean isObject(@Nonnull String path);

	boolean contains(@Nonnull String path);

	boolean isEmpty();

	@Nonnegative
	int size();

	@Nonnull
	Map<String, Object> values();

	@Nonnull
	Map<String, String> valuesAsStrings();

	@Nonnull
	<K, V> Map<K, V> mapValues(@Nonnull Function<? super String, ? extends K> keyMapper, @Nonnull Function<? super String, ? extends V> valueMapper);

	@Nonnull
	<T> List<T> mapList(@Nonnull String path, @Nonnull Function<String, ? extends T> mapper);

	@Nonnull
	Collection<String> keys();

	void forEach(@Nonnull BiConsumer<? super String, ? super Object> action);

}
