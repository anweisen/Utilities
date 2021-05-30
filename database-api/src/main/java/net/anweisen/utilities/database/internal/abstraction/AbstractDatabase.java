package net.anweisen.utilities.database.internal.abstraction;

import net.anweisen.utilities.database.Database;
import net.anweisen.utilities.database.DatabaseConfig;
import net.anweisen.utilities.database.SQLColumn;
import net.anweisen.utilities.database.exceptions.DatabaseAlreadyConnectedException;
import net.anweisen.utilities.database.exceptions.DatabaseConnectionClosedException;
import net.anweisen.utilities.database.exceptions.DatabaseException;

import javax.annotation.Nonnull;

/**
 * @author anweisen | https://github.com/anweisen
 * @since 1.0
 */
public abstract class AbstractDatabase implements Database {

	protected final DatabaseConfig config;

	public AbstractDatabase(@Nonnull DatabaseConfig config) {
		this.config = config;
	}

	@Override
	public boolean disconnectSafely() {
		try {
			disconnect();
			LOGGER.info("Successfully closed connection to database of type " + this.getClass().getSimpleName());
			return true;
		} catch (DatabaseException ex) {
			LOGGER.error("Could not disconnect from database (" + this.getClass().getSimpleName() + ")", ex);
			return false;
		}
	}

	@Override
	public void disconnect() throws DatabaseException {
		checkConnection();
		try  {
			disconnect0();
		} catch (Exception ex) {
			throw new DatabaseException(ex);
		}
	}

	protected abstract void disconnect0() throws Exception;

	@Override
	public boolean connectSafely() {
		try {
			connect();
			return true;
		} catch (DatabaseException ex) {
			LOGGER.error("Could not connect to database (" + this.getClass().getSimpleName() + ")", ex);
			return false;
		}
	}

	@Override
	public void connect() throws DatabaseException {
		if (isConnected()) throw new DatabaseAlreadyConnectedException();
		try {
			connect0();
			LOGGER.status("Successfully created connection to database of type " + this.getClass().getSimpleName());
		} catch (Exception ex) {
			if (ex instanceof DatabaseException) throw (DatabaseException) ex;
			throw new DatabaseException(ex);
		}
	}

	protected abstract void connect0() throws Exception;

	@Override
	public void createTableIfNotExistsSafely(@Nonnull String name, @Nonnull SQLColumn... columns) {
		try {
			createTableIfNotExists(name, columns);
		} catch (DatabaseException ex) {
			LOGGER.error("Could not create table (" + this.getClass().getSimpleName() + ")", ex);
		}
	}

	@Nonnull
	@Override
	public DatabaseConfig getConfig() {
		return config;
	}

	protected final void checkConnection() throws DatabaseConnectionClosedException {
		if (!isConnected())
			throw new DatabaseConnectionClosedException();
	}

}
