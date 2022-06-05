package net.anweisen.utility.database.internal.sql.abstraction.upsert;

import net.anweisen.utility.database.action.DatabaseUpsert;
import net.anweisen.utility.database.exception.DatabaseException;
import net.anweisen.utility.database.internal.sql.abstraction.AbstractSQLDatabase;
import net.anweisen.utility.database.internal.sql.abstraction.update.SQLUpdate;
import net.anweisen.utility.database.internal.sql.abstraction.where.SQLWhere;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author anweisen | https://github.com/anweisen
 * @since 2.0
 */
public class SQLInsertionOrUpdate extends SQLUpdate implements DatabaseUpsert {

	public SQLInsertionOrUpdate(@Nonnull AbstractSQLDatabase database, @Nonnull String table) {
		super(database, table);
	}

	@Nonnull
	@Override
	public DatabaseUpsert where(@Nonnull String column, @Nullable Object value) {
		super.where(column, value);
		return this;
	}

	@Nonnull
	@Override
	public DatabaseUpsert where(@Nonnull String column, @Nullable Number value) {
		super.where(column, value);
		return this;
	}

	@Nonnull
	@Override
	public DatabaseUpsert where(@Nonnull String column, @Nullable String value, boolean ignoreCase) {
		super.where(column, value);
		return this;
	}

	@Nonnull
	@Override
	public DatabaseUpsert where(@Nonnull String column, @Nullable String value) {
		super.where(column, value);
		return this;
	}

	@Nonnull
	@Override
	public DatabaseUpsert whereNot(@Nonnull String column, @Nullable Object value) {
		super.whereNot(column, value);
		return this;
	}

	@Nonnull
	@Override
	public DatabaseUpsert set(@Nonnull String column, @Nullable Object value) {
		super.set(column, value);
		return this;
	}

	@Override
	public Void execute() throws DatabaseException {
		if (database.query(table, where).execute().isSet()) {
			return super.execute();
		} else {
			Map<String, Object> insert = new HashMap<>(values);
			for (Entry<String, SQLWhere> entry : where.entrySet()) {
				Object[] args = entry.getValue().getArgs();
				if (args.length == 0) continue;
				insert.put(entry.getKey(), args[0]);
			}

			database.insert(table, insert).execute();
			return null;
		}
	}

	@Override
	public boolean equals(Object o) {
		return super.equals(o);
	}

}
