package dev.openosrs.discordbot.commands;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserCommandTest
{

	private final Random rand = new Random();
	private Logger log = Logger.getGlobal();
	private ConcurrentHashMap<String, UserCommand> commands = new ConcurrentHashMap<>();
	private ObjectOutputStream out;
	private StopWatch timer = new StopWatch();

	UserCommandTest() throws IOException
	{

	}

	private ConcurrentHashMap<String, UserCommand> generateCommandMap(int num)
	{
		ConcurrentHashMap<String, UserCommand> c = new ConcurrentHashMap<String, UserCommand>();
		for (int i = 0; i < num; i++)
		{
			UserCommand userCommand = new UserCommand(RandomStringUtils.random(rand.nextInt(100)),
				RandomStringUtils.random(rand.nextInt(10000)));
			c.put(userCommand.getName(), userCommand);
		}
		return c;
	}

	@BeforeEach
	void setUp() throws IOException
	{
	}

	@Test
	void testSaving() throws IOException
	{
		timer.start();
		commands = generateCommandMap(5000);
		log.info("generated 5000 commands in " + timer.toString());
		log.info("generated 5000 commands using " + Runtime.getRuntime().totalMemory());
		out =
			new ObjectOutputStream(Files.newOutputStream(File.createTempFile("commands",
				".command").toPath()));

		out.writeObject(commands);
		out.flush();
		out.close();

		log.info("Finished in " + timer.getTime() / 1000 + " seconds " + timer.toString());

		try
		{
			timer.reset();
			timer.start();
			XStream xstream = new XStream(new DomDriver());
			Files.writeString(File.createTempFile("commands", "temp").toPath(), xstream.toXML(commands));
			log.info("Finished xstream write in " + timer.getTime() / 1000 + " seconds " + timer.toString());
		}
		catch (IOException e)
		{
			// TODO: implement logging
		}
	}
}