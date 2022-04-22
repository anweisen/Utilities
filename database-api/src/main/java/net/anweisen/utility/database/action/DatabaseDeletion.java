package net.anweisen.utility.database.action;

import net.anweisen.utility.database.Database;
import net.anweisen.utility.database.SpecificDatabase;
import net.anweisen.utility.database.action.hierarchy.TableAction;
import net.anweisen.utility.database.action.hierarchy.WhereAction;
import net.anweisen.utility.database.exception.DatabaseException;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author anweisen | https://github.com/anweisen
 * @see Database#delete(String)
 * @see SpecificDatabase#delete()
 * @since 1.0
 */
public interface DatabaseDeletion extends DatabaseAction<Void>, WhereAction, TableAction {

	@Nonnull
	@CheckReturnValue
	DatabaseDeletion where(@Nonnull String field, @Nullable Object value);

	@Nonnull
	@CheckReturnValue
	DatabaseDeletion where(@Nonnull String field, @Nullable Number value);

	@Nonnull
	@CheckReturnValue
	DatabaseDeletion where(@Nonnull String field, @Nullable String value, boolean ignoreCase);

	@Nonnull
	@CheckReturnValue
	DatabaseDeletion where(@Nonnull String field, @Nullable String value);

	@Nonnull
	@CheckReturnValue
	DatabaseDeletion whereNot(@Nonnull String field, @Nullable Object value);

	@Nullable
	@Override
	Void execute() throws DatabaseException;

}
