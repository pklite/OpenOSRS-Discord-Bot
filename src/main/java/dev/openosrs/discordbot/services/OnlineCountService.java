package dev.openosrs.discordbot.services;


import dev.openosrs.discordbot.OpenOSRS;
import java.io.IOException;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import net.dv8tion.jda.api.entities.Activity;

public class OnlineCountService
{

	private final Scanner s;
	URL url = new URL("https://session.openosrs.com/count");
	private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

	public OnlineCountService() throws IOException
	{
		s = new Scanner(url.openStream());

		scheduledExecutorService.scheduleAtFixedRate(this::updateActivity, 0, 30, TimeUnit.SECONDS);
	}

	private void updateActivity()
	{
		OpenOSRS.setOnlinePlayers(String.valueOf(getPlayerCount()));
		OpenOSRS.getJda().getPresence().setActivity(Activity.playing(OpenOSRS.getOnlinePlayers() + " players online"));
	}

	private int getPlayerCount()
	{
		return Integer.parseInt(s.next());
	}

}
