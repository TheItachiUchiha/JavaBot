package com.gmail.inverseconduit.commands;

import groovy.lang.GroovyCodeSource;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.gmail.inverseconduit.BotConfig;
import com.gmail.inverseconduit.bot.AbstractBot;
import com.gmail.inverseconduit.bot.JavaBot;
import com.gmail.inverseconduit.chat.MessageListener;
import com.gmail.inverseconduit.datatype.ChatMessage;
import com.gmail.inverseconduit.javadoc.ClassInfo;
import com.gmail.inverseconduit.javadoc.PageLoader;
import com.gmail.inverseconduit.javadoc.Java8PageParser;
import com.gmail.inverseconduit.javadoc.JavadocDao;
import com.gmail.inverseconduit.javadoc.MultipleClassesFoundException;
import com.gmail.inverseconduit.javadoc.PageParser;
import com.gmail.inverseconduit.javadoc.ZipPageLoader;
import com.gmail.inverseconduit.utils.PrintUtils;
import com.google.common.collect.ImmutableSet;

public class RunScriptCommand implements MessageListener {
	private static final Logger logger = Logger.getLogger(RunScriptCommand.class.getName());
	private static final JavadocDao javadocDao = new JavadocDao();
	static {
		Path java8Api = BotConfig.JAVADOCS_DIR.resolve("java8.zip");
		if (Files.exists(java8Api)) {
			try {
				PageLoader loader = new ZipPageLoader(java8Api);
				PageParser parser = new Java8PageParser(loader);
				javadocDao.addJavadocApi(parser);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private final Set<Integer> userIds;
	{
		ImmutableSet.Builder<Integer> builder = ImmutableSet.builder();
		builder.add(3622940);
		builder.add(2272617);
		builder.add(1803692);

		userIds = builder.build();
	}
	private final Set<Integer> blacklist = new HashSet<>();

	private final Pattern messageRegex = Pattern.compile("^" + Pattern.quote(BotConfig.TRIGGER) + "(.*?):(.*)");

	@Override
	public void onMessage(AbstractBot bot, ChatMessage msg) {
		//FIXME: Decouple the implementation from JavaBot class!
		JavaBot jBot = (JavaBot) bot;
		try {
			logger.finest("Entered onMessage for RunScriptCommand");

			if (!userIds.contains(msg.getUserId()) || blacklist.contains(msg.getUserId())) {
				logger.finest("Ignoring message");
				return;
			}

			String message = msg.getMessage();
			Matcher messageMatcher = messageRegex.matcher(message);
			if (!messageMatcher.find()) {
				logger.finest("Message is not a bot command.");
				return;
			}

			String command = messageMatcher.group(1);
			String commandText = messageMatcher.group(2);
			switch (command) {
			case "load":
				compileAndCache(jBot, msg, commandText);
				break;
			case "eval":
				evaluateGroovy(jBot, msg, commandText);
				break;
			case "java":
				compileAndExecuteMain(jBot, msg, commandText);
				break;
			case "javadoc":
				javadoc(jBot, msg, commandText);
				break;
			default:
				jBot.sendMessage(msg.getSite(), msg.getRoomId(), "Sorry, I don't know that command. >.<");
				break;
			}
		} catch (Exception ex) {
			jBot.sendMessage(msg.getSite(), msg.getRoomId(), PrintUtils.FixedFont(ex.getMessage()));
		}
	}

	private void evaluateGroovy(JavaBot bot, ChatMessage msg, String commandText) {
		logger.finest("Evaluating Groovy Script");
		Object result = bot.getGroovyShell().evaluate(new GroovyCodeSource(commandText, "UserScript", "/sandboxScript"));
		bot.sendMessage(msg.getSite(), msg.getRoomId(), result.toString());
	}

	private void compileAndCache(JavaBot bot, ChatMessage msg, String commandText) {
		logger.finest("Compiling class to cache it");
		Object gClass = bot.getGroovyLoader().parseClass(new GroovyCodeSource(commandText, "UserScript", "/sandboxScript"), true);
		bot.sendMessage(msg.getSite(), msg.getRoomId(), "Thanks, I'll remember that.");
	}

	private void compileAndExecuteMain(JavaBot bot, ChatMessage msg, String commandText) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		logger.finest("Compiling class for execution");
		Object gClass = bot.getGroovyLoader().parseClass(new GroovyCodeSource(commandText, "UserScript", "/sandboxScript"), false);
		String result = ((Class) gClass).getMethod("main", String[].class).invoke(null, ((Object) new String[] { "" })).toString();
		bot.sendMessage(msg.getSite(), msg.getRoomId(), result);
	}

	private void javadoc(JavaBot bot, ChatMessage msg, String commandText) throws IOException {
		String message;
		try {
			ClassInfo info = javadocDao.getClassInfo(commandText);
			if (info == null) {
				message = "Sorry, I never heard of that class. :(";
			} else {
				message = info.getDescription();
				int pos = message.indexOf("\n\n");
				if (pos >= 0) {
					//just display the first paragraph
					message = message.substring(0, pos);
				}
			}
		} catch (MultipleClassesFoundException e) {
			StringBuilder sb = new StringBuilder("Which one do you mean?");
			for (String name : e.getClasses()) {
				sb.append("\n    ").append(name);
			}
			message = toString();
		}

		bot.sendMessage(msg.getSite(), msg.getRoomId(), message);
	}
}
