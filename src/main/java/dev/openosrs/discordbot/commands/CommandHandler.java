package dev.openosrs.discordbot.commands;

import com.google.common.base.Splitter;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import java.awt.Color;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.hooks.SubscribeEvent;

public class CommandHandler extends ListenerAdapter
{
	public static final String COMMANDS_FILENAME = "commands.dat";
	public static final Pattern ALLOWED_CHARS_REGEX = Pattern.compile("^[a-zA-Z0-9]+$");
	public static final int MAX_NAME_LENGTH = 100;
	public static final String EMBED_FOOTER_IMAGE_URL = "https://cdn.discordapp.com/avatars/560644885250572289/8cf594592ad4bed20670512264c6a173.png";
	private final String COMMAND_IDENTIFIER = "!";
	public CopyOnWriteArrayList<UserCommand> customCommands = new CopyOnWriteArrayList<>();


	@SubscribeEvent
	public void onMessageReceived(@Nonnull MessageReceivedEvent event)
	{
		User author = event.getAuthor();
		final Message message = event.getMessage();
		final MessageChannel channel = event.getChannel();
		final Member member = event.getMember();
		assert member != null;
		final boolean admin = member.getPermissions().contains(Permission.MANAGE_CHANNEL);

		if (!(message.getContentRaw().startsWith(COMMAND_IDENTIFIER)))
		{
			return;
		}
		String commandName = message.getContentDisplay().substring(1).split(" ")[0].toLowerCase();

		if (customCommands.contains(new UserCommand(commandName)))
		{
			channel.sendMessage(customCommands.get(customCommands.indexOf(new UserCommand(commandName)))
				.getResponse()).queue();
			return;
		}

		if (commandName.equals("add") && admin)
		{
			addUserCommand(message);
			return;
		}
		if (commandName.equals("delete") && admin)
		{
			deleteCommand(message, channel);
			return;
		}
		if (commandName.equals("shutdown") && admin)
		{
			System.exit(0);
			return;
		}
		if (commandName.equals("help"))
		{
			sendHelpMessage(message);
		}
	}

	private void deleteCommand(Message message, MessageChannel channel)
	{
		final String target = message.getContentDisplay().split(" ", 3)[1];
		if (target == null)
		{
			message.getChannel().sendMessage(getOpenOSRSEmbedBuilder().setColor(Color.orange)
				.setDescription("Invalid command syntax").build()).queue();
			return;
		}
		final UserCommand o = new UserCommand(target);
		if (customCommands.remove(o))
		{
			channel.sendMessage(getOpenOSRSEmbedBuilder().appendDescription("Command " + o.getName()
				+ " was deleted!").setColor(Color.GREEN).build()).queue();
			saveCommands();
		}
		else
		{
			channel.sendMessage(getOpenOSRSEmbedBuilder().appendDescription("Error deleting command." +
				" Are you sure it exists? type " + COMMAND_IDENTIFIER + "help").setColor(Color.RED).build()).queue();
		}
	}

	private void sendHelpMessage(Message message)
	{
		final EmbedBuilder embedBuilder = getOpenOSRSEmbedBuilder();
		embedBuilder.addField(COMMAND_IDENTIFIER + "help", "Displays this list of commands", false);
		embedBuilder.addField(COMMAND_IDENTIFIER + "add", "Add a new command", false);
		embedBuilder.addField(COMMAND_IDENTIFIER + "delete", "Delte a command", false);
		embedBuilder.addField(COMMAND_IDENTIFIER + "shutdown", "make bot go sleep", false);

		customCommands.forEach(userCommand ->
			embedBuilder.addField(COMMAND_IDENTIFIER + userCommand.getName(),
				Splitter.fixedLength(100).split(userCommand.response).iterator().next(),
				false));

		message.getChannel().sendMessage(embedBuilder.build()).queue();
	}

	private EmbedBuilder getOpenOSRSEmbedBuilder()
	{
		final EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setTitle("List of commands");
		embedBuilder.setFooter("OpenOSRS Bot",
			EMBED_FOOTER_IMAGE_URL);
		return embedBuilder;
	}

	private void addUserCommand(Message message)
	{
		final String name = message.getContentDisplay().split(" ", 3)[1]
			.replace(COMMAND_IDENTIFIER, "");
		final String content = message.getContentDisplay().split(" ", 3)[2];
		if (content == null || name.isEmpty() || content.isEmpty())
		{
			message.getChannel().sendMessage(getOpenOSRSEmbedBuilder().setColor(Color.orange)
				.setDescription("Invalid command syntax").build()).queue();
			return;
		}
		final Member member = message.getMember();

		assert member != null;
		final List<Role> roles = member.getRoles();

		UserCommand command = new UserCommand(name, content);
		if (!customCommands.contains(command))
		{
			customCommands.add(command);
			message.getChannel().sendMessage(getOpenOSRSEmbedBuilder().appendDescription("Command " + command.getName()
				+ " was added!").setColor(Color.GREEN).build()).queue();
		}
		saveCommands();
	}

	private synchronized void saveCommands()
	{
		try
		{
			XStream xstream = new XStream(new DomDriver());
			Files.writeString(Paths.get(COMMANDS_FILENAME), xstream.toXML(customCommands));
		}
		catch (IOException e)
		{
			// TODO: implement logging
		}
	}
}
