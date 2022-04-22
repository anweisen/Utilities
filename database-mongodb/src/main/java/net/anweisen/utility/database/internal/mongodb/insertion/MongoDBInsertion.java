package net.anweisen.utility.database.internal.mongodb.insertion;

import net.anweisen.utility.database.action.DatabaseInsertion;
import net.anweisen.utility.database.exception.DatabaseException;
import net.anweisen.utility.database.internal.mongodb.MongoDBDatabase;
import net.anweisen.utility.database.internal.mongodb.MongoUtils;
import org.bson.Document;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * @author anweisen | https://github.com/anweisen
 * @since 1.0
 */
public class MongoDBInsertion implements DatabaseInsertion {

	protected final MongoDBDatabase database;
	protected final String collection;
	protected final Document values;

	public MongoDBInsertion(@Nonnull MongoDBDatabase database, @Nonnull String collection) {
		this.database = database;
		this.collection = collection;
		this.values = new Document();
	}

	public MongoDBInsertion(@Nonnull MongoDBDatabase database, @Nonnull String collection, @Nonnull Document values) {
		this.database = database;
		this.collection = collection;
		this.values = values;
	}

	@Nonnull
	@Override
	public String getTable() {
		return collection;
	}

	@Nonnull
	@Override
	public DatabaseInsertion set(@Nonnull String field, @Nullable Object value) {
		values.put(field, MongoUtils.packObject(value));
		return this;
	}

	@Override
	public Void execute() throws DatabaseException {
		try {
			database.getCollection(collection).insertOne(values);
			return null;
		} catch (Exception ex) {
			throw new DatabaseException(ex);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		MongoDBInsertion that = (MongoDBInsertion) o;
		return database.equals(that.database) && collection.equals(that.collection) && values.equals(that.values);
	}

	@Override
	public int hashCode() {
		return Objects.hash(database, collection, values);
	}

}
