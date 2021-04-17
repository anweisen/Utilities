package net.anweisen.utilities.jda.commandmanager.impl.prefix;

import net.anweisen.utilities.database.access.DatabaseAccess;
import net.anweisen.utilities.database.exceptions.DatabaseException;
import net.anweisen.utilities.jda.commandmanager.PrefixProvider;
import net.dv8tion.jda.api.entities.Guild;

import javax.annotation.Nonnull;

/**
 * @author anweisen | https://github.com/anweisen
 * @since 1.0
 */
public class DatabasePrefixProvider implements PrefixProvider {

	private final DatabaseAccess<String> access;
	private final String defaultPrefix;

	public DatabasePrefixProvider(@Nonnull String defaultPrefix, @Nonnull DatabaseAccess<String> access) {
		this.access = access;
		this.defaultPrefix = defaultPrefix;
	}

	@Nonnull
	@Override
	public String getGuildPrefix(@Nonnull Guild guild) {
		try {
			return getGuildPrefix0(guild);
		} catch (DatabaseException ex) {
			LOGGER.error("Unable to get prefix for guild {}", guild, ex);
			return defaultPrefix;
		}
	}

	@Nonnull
	public String getGuildPrefix0(@Nonnull Guild guild) throws DatabaseException {
		return access.getValue(guild.getId(), defaultPrefix);
	}

	@Nonnull
	@Override
	public String getPrivatePrefix() {
		return defaultPrefix;
	}

	@Override
	public void setGuildPrefix(@Nonnull Guild guild, @Nonnull String prefix) throws DatabaseException {
		access.setValue(guild.getId(), prefix);
	}

}
