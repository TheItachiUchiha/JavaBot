package com.gmail.inverseconduit.commands;

import groovy.lang.GroovyCodeSource;

import java.lang.reflect.InvocationTargetException;
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
import com.google.common.collect.ImmutableSet;

public class RunScriptCommand implements MessageListener {
	private static final Logger logger = Logger.getLogger(RunScriptCommand.class.getName());

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

		JavaBot jBot = (JavaBot) bot;
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
			try {
				compileAndExecuteMain(jBot, msg, commandText);
			} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				throw new RuntimeException(e);
			}
			break;
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
}
