package net.anweisen.utilities.database.internal.mongodb.deletion;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Collation;
import com.mongodb.client.model.DeleteOptions;
import com.mongodb.client.model.Filters;
import net.anweisen.utilities.commons.misc.BsonUtils;
import net.anweisen.utilities.database.action.DatabaseDeletion;
import net.anweisen.utilities.database.exceptions.DatabaseException;
import net.anweisen.utilities.database.internal.mongodb.MongoDBDatabase;
import net.anweisen.utilities.database.internal.mongodb.where.MongoDBWhere;
import net.anweisen.utilities.database.internal.mongodb.where.ObjectWhere;
import net.anweisen.utilities.database.internal.mongodb.where.StringIgnoreCaseWhere;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author anweisen | https://github.com/anweisen
 * @since 1.0
 */
public class MongoDBDeletion implements DatabaseDeletion {

	protected final MongoDBDatabase database;
	protected final String collection;
	protected final Map<String, MongoDBWhere> where = new HashMap<>();

	public MongoDBDeletion(@Nonnull MongoDBDatabase database, @Nonnull String collection) {
		this.database = database;
		this.collection = collection;
	}

	@Nonnull
	@Override
	public DatabaseDeletion where(@Nonnull String column, @Nullable Object object) {
		where.put(column, new ObjectWhere(column, object, Filters::eq));
		return this;
	}

	@Nonnull
	@Override
	public DatabaseDeletion where(@Nonnull String column, @Nullable Number value) {
		return where(column, (Object) value);
	}

	@Nonnull
	@Override
	public DatabaseDeletion where(@Nonnull String column, @Nullable String value) {
		return where(column, (Object) value);
	}

	@Nonnull
	@Override
	public DatabaseDeletion where(@Nonnull String column, @Nullable String value, boolean ignoreCase) {
		if (!ignoreCase) return where(column, value);
		if (value == null) throw new NullPointerException("Cannot use where ignore case with null value");
		where.put(column, new StringIgnoreCaseWhere(column, value));
		return this;
	}

	@Nonnull
	@Override
	public DatabaseDeletion whereNot(@Nonnull String column, @Nullable Object object) {
		where.put(column, new ObjectWhere(column, object, Filters::ne));
		return this;
	}

	@Override
	public void execute() throws DatabaseException {
		try {
			MongoCollection<Document> collection = database.getCollection(this.collection);

			Document filter = new Document();
			DeleteOptions options = new DeleteOptions();

			for (MongoDBWhere where : where.values()) {
				Bson whereBson = where.toBson();
				BsonDocument asBsonDocument = BsonUtils.convertBsonToBsonDocument(whereBson);
				filter.putAll(asBsonDocument);

				Collation collation = where.getCollation();
				if (collation != null)
					options.collation(collation);
			}

			collection.deleteMany(filter, options);
		} catch (Exception ex) {
			throw new DatabaseException(ex);
		}
	}

	@Override
	public boolean equals(@Nonnull DatabaseDeletion other) {
		return equals((Object) other);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		MongoDBDeletion that = (MongoDBDeletion) o;
		return database.equals(that.database) && collection.equals(that.collection) && where.equals(that.where);
	}

	@Override
	public int hashCode() {
		return Objects.hash(database, collection, where);
	}

}
