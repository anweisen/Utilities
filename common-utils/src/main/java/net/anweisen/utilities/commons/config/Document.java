package net.anweisen.utilities.commons.config;

import net.anweisen.utilities.commons.config.document.EmptyDocument;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 * @author anweisen | https://github.com/anweisen
 * @since 1.0
 */
public interface Document extends Config, Json {

	/**
	 * Gets the document located at the given path.
	 * If there is no document assigned to this path,
	 * a new document is being created and assigned to this path.
	 *
	 * @return the document assigned to this path
	 */
	@Nonnull
	Document getDocument(@Nonnull String path);

	/**
	 * Returns the list of documents located at the given path.
	 * The returned list will not contain any null elements.
	 * If there are no documents, the list will be empty.
	 * Elements which are no documents in the original list will be skipped.
	 * If a element is {@code null} an empty document will be added.
	 *
	 * @return the list of documents assigned to this path
	 */
	@Nonnull
	List<Document> getDocumentList(@Nonnull String path);

	@Nonnull
	<T> List<T> getSerializableList(@Nonnull String path, @Nonnull Class<T> classOfT);

	@Nullable
	<T> T getSerializable(@Nonnull String path, @Nonnull Class<T> classOfT);

	@Nonnull
	<T> T getSerializable(@Nonnull String path, @Nonnull T def);

	/**
	 * Returns the parent document of this document.
	 * If {@code this} is the {@link #getRoot() root} the this method will return {@code null}.
	 *
	 * @return the parent document of this document
	 */
	@Nullable
	Document getParent();

	/**
	 * Returns the root document of this document.
	 * If the {@link #getParent() parent} is {@code null} this will return {@code this}.
	 *
	 * @return the root document of this document
	 */
	@Nonnull
	Document getRoot();

	@Nonnull
	@Override
	Document set(@Nonnull String path, @Nullable Object value);

	@Nonnull
	@Override
	Document clear();

	@Nonnull
	@Override
	Document remove(@Nonnull String path);

	@Nonnull
	@Override
	@CheckReturnValue
	Document readonly();

	boolean hasChildren(@Nonnull String path);

	void write(@Nonnull Writer writer) throws IOException;

	void save(@Nonnull File file) throws IOException;

	/**
	 * @return an empty, immutable document
	 */
	@Nonnull
	@CheckReturnValue
	static Document empty() {
		return new EmptyDocument();
	}

}
