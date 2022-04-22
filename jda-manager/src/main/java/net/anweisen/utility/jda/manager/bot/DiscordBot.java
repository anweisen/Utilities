package net.anweisen.utility.jda.manager.bot;

import com.google.common.base.Preconditions;
import net.anweisen.utility.common.collection.NamedThreadFactory;
import net.anweisen.utility.common.function.ExceptionallyBiConsumer;
import net.anweisen.utility.common.logging.ILogger;
import net.anweisen.utility.database.Database;
import net.anweisen.utility.database.SqlColumn;
import net.anweisen.utility.document.wrapped.StorableDocument;
import net.anweisen.utility.jda.manager.CommandManager;
import net.anweisen.utility.jda.manager.bot.config.BotConfigCreator;
import net.anweisen.utility.jda.manager.bot.config.ConfigProvider;
import net.anweisen.utility.jda.manager.impl.DatabaseTeamRoleManager;
import net.anweisen.utility.jda.manager.impl.DefaultCommandManager;
import net.anweisen.utility.jda.manager.impl.language.ConstantLanguageManager;
import net.anweisen.utility.jda.manager.impl.language.DatabaseLanguageManager;
import net.anweisen.utility.jda.manager.impl.prefix.ConstantPrefixProvider;
import net.anweisen.utility.jda.manager.impl.prefix.DatabasePrefixProvider;
import net.anweisen.utility.jda.manager.language.LanguageManager;
import net.anweisen.utility.jda.manager.listener.CommandListener;
import net.anweisen.utility.jda.manager.listener.manager.ActionEventListener;
import net.anweisen.utility.jda.manager.listener.manager.CombinedEventManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDA.Status;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.ApplicationInfo;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.IEventManager;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.sharding.ThreadPoolProvider;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author anweisen | https://github.com/anweisen
 * @since 1.0
 */
public abstract class DiscordBot implements IDiscordBot {

	private static DiscordBot instance;

	@Nonnull
	public static DiscordBot getInstance() {
		return instance;
	}

	private final ILogger logger = ILogger.forClassOf(this);
	private final IEventManager eventManager = new CombinedEventManager();

	// Built in constructor
	private final ConfigProvider config;

	// Built on first use
	private ScheduledExecutorService executor;

	// Built on init
	private Database database;
	private CommandManager commandManager;
	private ShardManager shardManager;
	private ApplicationInfo applicationInfo;
	private DiscordBotBuilder builder;

	private boolean initialized = false;

	public DiscordBot() throws Exception {
		this(new BotConfigCreator());
	}

	public DiscordBot(@Nonnull ConfigProvider configProvider) throws Exception {
		Preconditions.checkNotNull(configProvider, "ConfigProvider cannot be null");

		instance = this;

		config = configProvider;
	}

	protected final void init() throws Exception {
		if (initialized) throw new IllegalStateException("init() was already called");
		initialized = true;

		builder = builder().validate();
		DiscordBotBuilder.logger.debug("Building bot with following configuration:" +
			"\n\t" + config +
			"\n\t" + builder +
			"\n\t" + (builder.databaseConfig == null ? "BotDatabaseConfig{null}" : builder.databaseConfig));

		boolean requireDatabase = builder.databaseConfig != null || !builder.tables.isEmpty() || builder.requireDatabase;
		if (builder.databaseConfig == null) {
			builder.databaseConfig = new BotDatabaseConfig("guilds", "guildId", null, null, null);
		}
		if (requireDatabase) {
			database = config.createDatabase();
			database.connect();
			builder.tables.forEach((ExceptionallyBiConsumer<String, SqlColumn[]>) database::createTable);
		} else {
			database = Database.unsupported();
		}

		builder.fileLanguages.addAll(config.getLanguageFiles());
		commandManager = new DefaultCommandManager(builder.databaseConfig.getPrefixColumn() == null
			? new ConstantPrefixProvider(config.getDefaultPrefix())
			: new DatabasePrefixProvider(config.getDefaultPrefix(), database, builder.databaseConfig.getGuildTable(), builder.databaseConfig.getGuildKeyColumn(), builder.databaseConfig.getPrefixColumn())
		).setUseEmbeds(builder.useEmbeds);

		if (builder.databaseConfig.getTeamRoleColumn() != null) {
			commandManager.setTeamRoleManager(new DatabaseTeamRoleManager(database, builder.databaseConfig.getGuildTable(), builder.databaseConfig.getGuildKeyColumn(), builder.databaseConfig.getTeamRoleColumn()));
		}

		if (builder.databaseConfig.getLanguageColumn() != null) {
			LanguageManager languageManager = new DatabaseLanguageManager(database, builder.databaseConfig.getGuildTable(), builder.databaseConfig.getGuildKeyColumn(), builder.databaseConfig.getLanguageColumn());
			commandManager.setLanguageManager(languageManager);

			for (String filename : builder.fileLanguages) languageManager.readFile(filename);
			for (String filename : builder.resourceLanguages) languageManager.readResource(filename);

			languageManager.setDefaultLanguage(config.getDefaultLanguage());
		} else if (!builder.fileLanguages.isEmpty() || !builder.resourceLanguages.isEmpty()) {
			DiscordBotBuilder.logger.warn("Languages were registered but no database for language management is setup!");
			LanguageManager languageManager = new ConstantLanguageManager();
			commandManager.setLanguageManager(languageManager);

			for (String filename : builder.fileLanguages) languageManager.readFile(filename);
			for (String filename : builder.resourceLanguages) languageManager.readResource(filename);

			languageManager.setDefaultLanguage(config.getDefaultLanguage());
		}

		builder.parsers.forEach((key, parser) -> commandManager.getParserContext().registerParser(key, parser.getFirst(), parser.getSecond()));

		commandManager.register(builder.commands);
		builder.taskCommands.forEach(pair -> commandManager.register(pair.getFirst(), pair.getSecond()));

		List<GatewayIntent> intents = Arrays.asList(builder.intents);
		if (!intents.contains(GatewayIntent.DIRECT_MESSAGES))
			DiscordBotBuilder.logger.warn("Missing GatewayIntent.DIRECT_MESSAGES, no commands will be available in private chats");
		if (!intents.contains(GatewayIntent.GUILD_MESSAGES))
			DiscordBotBuilder.logger.warn("Missing GatewayIntent.GUILD_MESSAGES, no commands will be available in guild chats");

		DefaultShardManagerBuilder shardManagerBuilder = DefaultShardManagerBuilder.create(config.getToken(), intents)
			.setCallbackPoolProvider(newThreadPoolProvider("Callback"))
			.setEventPoolProvider(newThreadPoolProvider("Events"))
			.setShardsTotal(config.getShards())
			.setMemberCachePolicy(builder.memberCachePolicy)
			.setEventManagerProvider(shardId -> eventManager)
			.setStatusProvider(shardId -> config.getOnlineStatus())
			.addEventListeners(new CommandListener(commandManager), this);

		if (builder.chunkingFilter != null)
			shardManagerBuilder.setChunkingFilter(builder.chunkingFilter);

		builder.builderSettings.forEach(action -> action.accept(shardManagerBuilder));
		builder.listener.forEach(shardManagerBuilder::addEventListeners);

		for (CacheFlag cache : CacheFlag.values()) {
			if (builder.cacheFlags.contains(cache)) {
				shardManagerBuilder.enableCache(cache);
			} else {
				shardManagerBuilder.disableCache(cache);
			}
		}

		shardManager = shardManagerBuilder.build();

		builder.shardManagerSettings.forEach(action -> action.accept(shardManager));

		if (!builder.customSlashCommands.isEmpty())
			getJDA().updateCommands().addCommands(builder.customSlashCommands).queue();
		else if (!builder.disableAutoSlashCommands)
			commandManager.setupSlashCommands(getJDA());

		getJDA().retrieveApplicationInfo().queue(applicationInfo -> this.applicationInfo = applicationInfo);

		onStart();

		if (!builder.activities.isEmpty()) {
			new Timer("BotActivityChanger").schedule(new TimerTask() {

				private int index = 0;

				@Override
				public void run() {
					if (index >= builder.activities.size()) {
						index = 0;
					}

					Supplier<? extends Activity> activitySupplier = builder.activities.get(index);
					Activity activity = activitySupplier.get();
					shardManager.setActivity(activity);

					index++;
				}
			}, 3000, builder.activityUpdateRate * 1000L);
		}

	}

	protected void onStart() throws Exception {
	}

	protected void onReady() throws Exception {
	}

	@SubscribeEvent
	private void onReady(@Nonnull ReadyEvent event) throws Exception {
		if (isReady())
			onReady();

		builder.shardsSettings.forEach(action -> action.accept(event.getJDA()));
	}

	@Nonnull
	@CheckReturnValue
	protected abstract DiscordBotBuilder builder() throws Exception;

	@Nonnull
	@CheckReturnValue
	protected static DiscordBotBuilder newBuilder() {
		return new DiscordBotBuilder();
	}

	public <E extends GenericEvent> void on(@Nonnull Class<E> classOfE, @Nonnull Consumer<? super E> action) {
		// Using the event manager directly allows us to register listeners before the shardmanager is event built
		eventManager.register(new ActionEventListener<>(classOfE, action));
	}

	public void registerListener(@Nonnull Object... listeners) {
		for (Object listener : listeners) {
			eventManager.register(listener);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Nonnull
	@Override
	public ShardManager getShardManager() {
		if (shardManager == null) throw new IllegalStateException("Bot is not built yet! Call init() first");
		return shardManager;
	}

	/**
	 * {@inheritDoc}
	 */
	@Nonnull
	@Override
	public JDA getJDA() {
		if (shardManager == null) throw new IllegalStateException("Bot is not built yet! Call init() first");
		return shardManager.getShardCache().stream().findFirst().orElseThrow(() -> new IllegalStateException("No JDA is built yet"));
	}

	/**
	 * {@inheritDoc}
	 */
	@Nonnull
	@Override
	public Database getDatabase() {
		if (!initialized) throw new IllegalStateException("Bot is not built yet! Call init() first");
		return database;
	}

	/**
	 * {@inheritDoc}
	 */
	@Nonnull
	@Override
	public CommandManager getCommandManager() {
		if (commandManager == null) throw new IllegalStateException("Bot is not built yet! Call init() first");
		return commandManager;
	}

	/**
	 * {@inheritDoc}
	 */
	@Nonnull
	@Override
	public ConfigProvider getConfig() {
		return config;
	}

	@Nonnull
	public StorableDocument getConfigDocument() {
		return config.getDocument();
	}

	/**
	 * {@inheritDoc}
	 */
	@Nonnull
	@Override
	public ApplicationInfo getApplicationInfo() {
		if (!initialized) throw new IllegalStateException("Bot is not built yet! Call init() first");
		if (applicationInfo == null) throw new IllegalStateException("ApplicationInfo not received yet");
		return applicationInfo;
	}

	@Nonnull
	@Override
	public ILogger getLogger() {
		return logger;
	}

	@Nonnull
	@Override
	public ScheduledExecutorService getExecutor() {
		return executor != null ? executor : (executor = Executors.newScheduledThreadPool(4, new NamedThreadFactory(threadId -> String.format("AsyncTask-%s", threadId))));
	}

	@Override
	public boolean isInitialized() {
		return initialized;
	}

	@Override
	public boolean isReady() {
		return shardManager != null && shardManager.getShardCache().stream().allMatch(jda -> jda.getStatus() == Status.CONNECTED);
	}

	@Override
	public int getReadyShardCount() {
		return shardManager == null ? 0 : (int) shardManager.getShardCache().stream().filter(jda -> jda.getStatus() == Status.CONNECTED).count();
	}

	@Nonnull
	@Override
	public List<JDA> getReadyShards() {
		return shardManager == null ? Collections.emptyList() : shardManager.getShardCache().stream().filter(jda -> jda.getStatus() == Status.CONNECTED).collect(Collectors.toList());
	}

	@Nonnull
	protected ThreadPoolProvider<?> newThreadPoolProvider(@Nonnull String scope) {
		return new ThreadPoolProvider<ExecutorService>() {

			@Nonnull
			@Override
			public ExecutorService provide(int shardId) {
				return Executors.newCachedThreadPool(new NamedThreadFactory(String.format("Shard-%s-%s", shardId + 1, scope)));
			}

			@Override
			public boolean shouldShutdownAutomatically(int shardId) {
				return true;
			}

		};
	}

}
