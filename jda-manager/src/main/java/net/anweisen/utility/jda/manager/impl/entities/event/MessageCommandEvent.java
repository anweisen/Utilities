package net.anweisen.utility.jda.manager.impl.entities.event;

import net.anweisen.utility.common.misc.StringUtils;
import net.anweisen.utility.jda.manager.CommandManager;
import net.anweisen.utility.jda.manager.hooks.event.ReplyMessageAction;
import net.anweisen.utility.jda.manager.hooks.option.CommandOptions;
import net.anweisen.utility.jda.manager.utils.Embeds;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.utils.AttachmentOption;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;

/**
 * @author anweisen | https://github.com/anweisen
 * @since 1.0
 */
public class MessageCommandEvent extends AbstractCommandEvent {

	protected final Message message;

	public MessageCommandEvent(@Nonnull CommandManager manager, @Nonnull CommandOptions commandOptions, @Nonnull Message message, @Nullable Member member, boolean disableMentions, boolean embeds) {
		super(manager, commandOptions, message.getChannel(), member, disableMentions, embeds);
		this.message = message;
	}

	@Override
	public boolean isMessage() {
		return true;
	}

	@Override
	public boolean isInteraction() {
		return false;
	}

	@Nonnull
	@Override
	public ReplyMessageAction reply(@Nonnull Message message) {
		return wrap(applySettings(message.reply(message)));
	}

	@Nonnull
	@Override
	public ReplyMessageAction reply(@Nonnull CharSequence content, @Nonnull Object... args) {
		return embeds
			? reply(Embeds.construct(this).setDescription(StringUtils.format(content.toString(), args)))
			: wrap(applySettings(message.reply(StringUtils.format(content.toString(), args))));
	}

	@Nonnull
	@Override
	public ReplyMessageAction reply(@Nonnull MessageEmbed content) {
		return wrap(applySettings(message.replyEmbeds(content)));
	}

	@Nonnull
	@Override
	public ReplyMessageAction replyFile(@Nonnull File file, @Nonnull String filename, @Nonnull AttachmentOption... options) {
		return wrap(applySettings(message.reply(file, filename, options)));
	}

	@Nonnull
	@Override
	public ReplyMessageAction send(@Nonnull Message message) {
		return wrap(applySettings(channel.sendMessage(message)));
	}

	@Nonnull
	@Override
	public ReplyMessageAction send(@Nonnull CharSequence content) {
		return embeds
			? reply(Embeds.construct(this).setDescription(content))
			: wrap(applySettings(channel.sendMessage(content)));
	}

	@Nonnull
	@Override
	public ReplyMessageAction send(@Nonnull MessageEmbed content) {
		return wrap(applySettings(channel.sendMessageEmbeds(content)));
	}

	@Nonnull
	@Override
	public ReplyMessageAction sendFile(@Nonnull File file, @Nonnull String filename, @Nonnull AttachmentOption... options) {
		return wrap(applySettings(channel.sendFile(file, filename, options)));
	}

	@Nonnull
	@Override
	public User getUser() {
		return message.getAuthor();
	}

	@Nonnull
	@Override
	public Message getMessage() {
		return message;
	}

	@Nonnull
	@Override
	public Interaction getInteraction() {
		requireInteraction();
		return null;  // unreachable
	}

}
