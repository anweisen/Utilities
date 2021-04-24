package net.anweisen.utilities.jda.commandmanager;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * @author anweisen | https://github.com/anweisen
 * @since 1.0
 */
public interface CommandArguments {

	@Nonnull
	Object[] getAll();

	@Nonnull
	<T> T get(int index);

	long getLong(int index);

	int getInt(int index);

	short getShort(int index);

	byte getByte(int index);

	float getFloat(int index);

	double getDouble(int index);

	char getChar(int index);

	@Nonnull
	Class<?> getType(int index);

	/**
	 * Returns an array containing the classes of the types of the arguments which is the same length as {@link #size()}.
	 * The classes dont have to be the classes of the arguments, but the argument has to be an instance of the class or a subclass of the class.
	 *
	 * @return an array containing the classes of the types
	 */
	@Nonnull
	Class<?>[] getTypes();

	@Nonnegative
	int size();

}
