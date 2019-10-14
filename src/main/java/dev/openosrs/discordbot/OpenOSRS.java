package dev.openosrs.discordbot;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import dev.openosrs.discordbot.commands.CommandHandler;
import dev.openosrs.discordbot.commands.UserCommand;
import dev.openosrs.discordbot.services.OnlineCountService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import javax.security.auth.login.LoginException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;

@Log
public class OpenOSRS
{
	private static final String DISCORD_BOT_TOKEN = "";
	private static CommandHandler commandHandler = new CommandHandler();
	static OpenOSRS openOSRS;
	@Getter
	@Setter
	private static String onlinePlayers = "";
	@Getter
	private static JDA jda;

	static
	{
		try
		{
			jda = new JDABuilder().setActivity(Activity.playing(onlinePlayers + " players online"))
				.setToken(DISCORD_BOT_TOKEN).setAutoReconnect(true).setRequestTimeoutRetry(true)
				.addEventListeners(commandHandler).build();
		}
		catch (LoginException e)
		{
			e.printStackTrace();
		}
	}

	public OpenOSRS() throws InterruptedException
	{
		Runtime.getRuntime().addShutdownHook(new Thread(() ->
		{
			OpenOSRS.getJda().shutdown();
		}));
		try
		{
			new OnlineCountService();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		loadCommands();

		jda.awaitReady();
	}

	public static void main(String[] args) throws InterruptedException
	{
		openOSRS = new OpenOSRS();
	}

	@SuppressWarnings("unchecked")
	private synchronized void loadCommands()
	{
		try
		{
			XStream xstream = new XStream(new DomDriver());
			commandHandler.customCommands.addAll((Collection<? extends UserCommand>)
				xstream.fromXML(Files.readString(Paths.get(CommandHandler.COMMANDS_FILENAME))));
			log.info("Loaded " + commandHandler.customCommands.size() + " commands.");
		}
		catch (IOException e)
		{
			log.warning("No command file to load");
		}
	}
}
